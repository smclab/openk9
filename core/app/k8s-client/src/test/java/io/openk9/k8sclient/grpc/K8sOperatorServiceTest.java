/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.k8sclient.grpc;

import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.openk9.k8s.operator.grpc.DeployEnrichItemRequest;
import io.openk9.k8s.operator.grpc.K8sOperator;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@WithKubernetesTestServer
@QuarkusTest
class K8sOperatorServiceTest {

	@KubernetesTestServer
	KubernetesServer mockServer;
	@GrpcClient
	K8sOperator client;

	private static final DeployEnrichItemRequest goodRequest = DeployEnrichItemRequest
		.newBuilder()
		.setSchemaName("mew")
		.setChart("openk9-foo-enrich")
		.setVersion("1.0.0")
		.build();

	private static final DeployEnrichItemRequest badRequest = DeployEnrichItemRequest
		.newBuilder()
		.setChart("openk9-foo-enrich")
		.build();

	@Test
	@RunOnVertxContext
	void deployEnrichItemSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> client.deployEnrichItem(goodRequest),
			response -> Assertions.assertEquals(response.getStatus(), "openk9-foo-enrich-mew")
		);

	}

	@Test
	@RunOnVertxContext
	void deployEnrichItemFailure(UniAsserter asserter) {

		mockServer.expect()
			.post()
			.withPath("/apis/argoproj.io/v1alpha1/namespaces/argocd/applications")
			.andReturn(500, List.of())
			.once();

		mockServer.expect()
			.put()
			.withPath(
				"/apis/argoproj.io/v1alpha1/namespaces/argocd/applications/openk9-foo-enrich-mew")
			.andReturn(500, List.of())
			.once();

		asserter.assertFailedWith(
			() -> client.deployEnrichItem(goodRequest),
			throwable -> {}
		);

	}

	@Test
	@RunOnVertxContext
	void deployEnrichItemBadRequest(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> client.deployEnrichItem(badRequest),
			throwable -> {}
		);

	}


}