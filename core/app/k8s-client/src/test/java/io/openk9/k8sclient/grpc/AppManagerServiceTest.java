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
import io.grpc.StatusRuntimeException;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@WithKubernetesTestServer
@QuarkusTest
class AppManagerServiceTest {

	@ConfigProperty(
		name = "quarkus.kubernetes-client.request-retry-backoff-limit",
		defaultValue = "0"
	)
	int maxRetry;

	private static final String NAMESPACE = "k9-unit-test";

	private static final AppManifest goodRequest = AppManifest
		.newBuilder()
		.setSchemaName("mew")
		.setChart("openk9-foo-enrich")
		.setVersion("1.0.0")
		.build();
	private static final AppManifest badRequest = AppManifest
		.newBuilder()
		.setChart("openk9-foo-enrich")
		.build();
	private static final String APPLICATION_INSTANCE_PATH =
		"/apis/argoproj.io/v1alpha1/namespaces/k9-unit-test/applications/openk9-foo-enrich-mew";
	private static final String APPLICATIONS_PATH =
		"/apis/argoproj.io/v1alpha1/namespaces/k9-unit-test/applications";
	private static final int INTERNAL_SERVER_ERROR = 500;
	private static final List<Object> EMPTY_ARRAY = List.of();

	@KubernetesTestServer
	KubernetesServer mockServer;
	@GrpcClient
	AppManager appManager;

	@Test
	@RunOnVertxContext
	void deploySuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> appManager.applyResource(goodRequest),
			response -> Assertions.assertEquals(response.getStatus(), "openk9-foo-enrich-mew")
		);

	}

	@Test
	@RunOnVertxContext
	void deployFailure(UniAsserter asserter) {

		mockServer.expect()
			.post()
			.withPath(APPLICATIONS_PATH)
			.andReturn(INTERNAL_SERVER_ERROR, EMPTY_ARRAY)
			.times(maxRetry + 1);

		asserter.assertFailedWith(
			() -> appManager.applyResource(goodRequest),
			StatusRuntimeException.class
		);

	}

	@Test
	@RunOnVertxContext
	void deployBadRequest(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> appManager.applyResource(badRequest),
			StatusRuntimeException.class
		);

	}

	@Test
	@RunOnVertxContext
	void deleteWithResourceSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> appManager
				.applyResource(goodRequest)
				.call(() -> appManager.deleteResource(goodRequest)),
			empty -> {}
		);

	}

	@Test
	@RunOnVertxContext
	void deleteWithoutResourceSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> appManager.deleteResource(goodRequest),
			empty -> {}
		);

	}

	@Test
	@RunOnVertxContext
	void deleteFailure(UniAsserter asserter) {

		mockServer.expect()
			.delete()
			.withPath(APPLICATION_INSTANCE_PATH)
			.andReturn(INTERNAL_SERVER_ERROR, EMPTY_ARRAY)
			.times(maxRetry + 1);

		asserter.assertFailedWith(
			() -> appManager.deleteResource(goodRequest),
			StatusRuntimeException.class
		);

	}

	@Test
	@RunOnVertxContext
	void deleteBadRequest(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> appManager.deleteResource(badRequest),
			StatusRuntimeException.class
		);

	}

	@Test
	@RunOnVertxContext
	void createIngressSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> appManager.createIngress(
				CreateIngressRequest
					.newBuilder()
					.setSchemaName("mew")
					.setVirtualHost("mew.openk9.io")
					.build()
				),
			response -> Assertions.assertEquals(
				"mew-default-ingress",
				response.getResourceName()
			)
		);
	}

	@Test
	@RunOnVertxContext
	void deleteIngressSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> appManager.deleteIngress(
				DeleteIngressRequest
					.newBuilder()
					.setSchemaName("mew")
					.setVirtualHost("mew.openk9.io")
					.build()
			),
			response -> Assertions.assertEquals(
				"mew-default-ingress",
				response.getResourceName()
			)
		);
	}

}
