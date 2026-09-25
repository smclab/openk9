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

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@WithKubernetesTestServer
@QuarkusTest
@TestProfile(IngressServiceEmptyConfigTest.EmptyIngressConfig.class)
class IngressServiceEmptyConfigTest {

	@Inject
	IngressService ingressService;

	@Test
	void emptyProperties_resolveAsAbsent() {
		var ingressDef = IngressDef.of(
			"tenant-ingress",
			"tenant.openk9.local",
			List.of(IngressDef.Route.of("/", "openk9-search-frontend", 8080))
		);

		var spec = ingressService.getIngress(ingressDef).getSpec();

		Assertions.assertTrue(spec.getTls().isEmpty());
		Assertions.assertNull(spec.getIngressClassName());
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
