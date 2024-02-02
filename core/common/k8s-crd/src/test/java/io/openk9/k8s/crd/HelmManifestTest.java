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

package io.openk9.k8s.crd;

import io.cattle.helm.v1.HelmChart;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelmManifestTest {

	@Test
	void shouldMatchManifest() {

		var expected = Serialization.unmarshal(Objects.requireNonNull(getClass()
			.getClassLoader()
			.getResourceAsStream("expected/full-helmchart.yaml")), HelmChart.class);

		var actual = HelmManifest.builder()
			.targetNamespace("default")
			.repoURL("https://registry.acme.com/repository/helm/")
			.chart("openk9-foo-parser")
			.version("1.0.0")
			.set("KEY1", "val1")
			.set("KEY2", 2)
			.set("KEY3", "VAL3")
			.authSecretName("bar-repo-auth")
			.build()
			.asResource();

		var expectedSpec = expected.getSpec();
		var actualSpec = actual.getSpec();
		var expectedAuthSecret = expectedSpec.getAuthSecret();
		var actualAuthSecret = actualSpec.getAuthSecret();

		assertEquals(expected.getApiVersion(), actual.getApiVersion());
		assertEquals(expected.getKind(), actual.getKind());
		assertEquals(expected.getMetadata(), actual.getMetadata());
		assertEquals(expectedSpec.getTargetNamespace(), actualSpec.getTargetNamespace());
		assertEquals(expectedSpec.getRepo(), actualSpec.getRepo());
		assertEquals(expectedSpec.getChart(), actualSpec.getChart());
		assertEquals(expectedSpec.getVersion(), actualSpec.getVersion());
		assertEquals(expectedAuthSecret.getName(), actualAuthSecret.getName());
		assertEquals(expectedSpec.getSet(), actualSpec.getSet());

	}

}