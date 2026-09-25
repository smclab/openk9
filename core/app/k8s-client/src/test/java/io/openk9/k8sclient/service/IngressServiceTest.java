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
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IngressServiceTest {

	private static final String VIRTUAL_HOST = "tenant.openk9.local";

	private static final IngressDef INGRESS_DEF = IngressDef.of(
		"tenant-ingress",
		VIRTUAL_HOST,
		List.of(IngressDef.Route.of("/", "openk9-search-frontend", 8080))
	);

	@Test
	void getIngress_withSecretAndClass_setsTlsAndIngressClass() {
		var service = service(Optional.of("openk9-tls-star-secret"), Optional.of("nginx"));

		var spec = service.getIngress(INGRESS_DEF).getSpec();

		Assertions.assertEquals(1, spec.getTls().size());
		Assertions.assertEquals(
			"openk9-tls-star-secret", spec.getTls().getFirst().getSecretName());
		Assertions.assertEquals(
			List.of(VIRTUAL_HOST), spec.getTls().getFirst().getHosts());
		Assertions.assertEquals("nginx", spec.getIngressClassName());
	}

	@Test
	void getIngress_withoutSecret_omitsTls() {
		var service = service(Optional.empty(), Optional.of("nginx"));

		var spec = service.getIngress(INGRESS_DEF).getSpec();

		Assertions.assertTrue(spec.getTls().isEmpty());
		Assertions.assertEquals("nginx", spec.getIngressClassName());
	}

	@Test
	void getIngress_withoutIngressClass_omitsIngressClassName() {
		var service = service(Optional.of("openk9-tls-star-secret"), Optional.empty());

		var spec = service.getIngress(INGRESS_DEF).getSpec();

		Assertions.assertNull(spec.getIngressClassName());
		Assertions.assertEquals(1, spec.getTls().size());
	}

	@Test
	void getIngress_withBlankValues_omitsTlsAndIngressClassName() {
		var service = service(Optional.of(" "), Optional.of(""));

		var spec = service.getIngress(INGRESS_DEF).getSpec();

		Assertions.assertTrue(spec.getTls().isEmpty());
		Assertions.assertNull(spec.getIngressClassName());
		Assertions.assertEquals(VIRTUAL_HOST, spec.getRules().getFirst().getHost());
	}

	private static IngressService service(
		Optional<String> secretName, Optional<String> ingressClassName) {

		var service = new IngressService();
		service.namespace = "openk9";
		service.secretName = secretName;
		service.ingressClassName = ingressClassName;
		return service;
	}

}
