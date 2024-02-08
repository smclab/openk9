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
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@WithKubernetesTestServer
@QuarkusTest
class K8sOperatorServiceTest {

	@KubernetesTestServer
	KubernetesServer mockServer;
	@GrpcClient
	K8sOperator client;

	@Test
	void deployEnrichItem() throws ExecutionException, InterruptedException, TimeoutException {

		var message = new CompletableFuture<>();

		client.deployEnrichItem(DeployEnrichItemRequest
			.newBuilder()
			.setSchemaName("mew")
			.setChart("openk9-foo-enrich")
			.setVersion("1.0.0")
			.build()
		).subscribe().with(message::complete);

	}

}