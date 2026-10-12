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

package io.openk9.k8sclient.service;

import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.KubernetesServer;
import io.quarkus.test.kubernetes.client.KubernetesTestServer;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@WithKubernetesTestServer
@QuarkusTest
@TestProfile(IngressServiceEmptyConfigTest.EmptyIngressConfig.class)
class IngressServiceEmptyConfigTest {

	private static final String NAMESPACE = "k9-unit-test";
	private static final String INGRESS_NAME = "tenant-ingress";

	@KubernetesTestServer
	KubernetesServer mockServer;
	@Inject
	IngressService ingressService;

	@Test
	@RunOnVertxContext
	void emptyProperties_createIngressWithoutTlsAndClass(UniAsserter asserter) {
		var ingressDef = IngressDef.of(
			INGRESS_NAME,
			"tenant.openk9.local",
			List.of(IngressDef.Route.of("/", "openk9-search-frontend", 8080))
		);

		asserter.execute(() -> ingressService.create(ingressDef));

		asserter.assertThat(
			this::readIngress,
			ingress -> {
				Assertions.assertNotNull(ingress);
				Assertions.assertTrue(ingress.getSpec().getTls().isEmpty());
				Assertions.assertNull(ingress.getSpec().getIngressClassName());
			}
		);
	}

	private Uni<Ingress> readIngress() {
		return VertxContextSupport.executeBlocking(() -> mockServer.getClient()
			.network()
			.v1()
			.ingresses()
			.inNamespace(NAMESPACE)
			.withName(INGRESS_NAME)
			.get()
		);
	}

	public static class EmptyIngressConfig implements QuarkusTestProfile {

		@Override
		public Map<String, String> getConfigOverrides() {
			return Map.of(
				"quarkus.kubernetes.ingress.secretName", "",
				"quarkus.kubernetes.ingress.ingressClassName", ""
			);
		}

	}

}
