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

import io.argoproj.v1alpha1.Application;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArgoCDManifestTest {

	@Test
	void shouldMatchFullApplication() {

		var expected = deserialize("expected/full-application.yaml");

		var actual = Manifest.asApplication(Manifest.builder()
			.targetNamespace("k9-baz")
			.repoURL("https://registry.acme.com/repository/helm/")
			.chart("openk9-foo-parser")
			.version("1.0.0")
			.set("K1", "V1")
			.set("K2", 2)
			.set("K3", "v3")
			.type(Manifest.Type.ARGOCD)
			.build()
		);

		var expectedMetadata = expected.getMetadata();
		var actualMetadata = actual.getMetadata();
		var expectedSpec = expected.getSpec();
		var actualSpec = actual.getSpec();
		var expectedSource = expectedSpec.getSource();
		var actualSource = actualSpec.getSource();
		var expectedHelm = expectedSource.getHelm();
		var actualHelm = actualSource.getHelm();
		var expectedValues = expectedHelm.getValuesObject();
		var actualValues = actualHelm.getValuesObject();
		var expectedDestination = expectedSpec.getDestination();
		var actualDestination = actualSpec.getDestination();
		var expectedSyncPolicy = expectedSpec.getSyncPolicy();
		var actualSyncPolicy = actualSpec.getSyncPolicy();
		var expectedAutomated = expectedSyncPolicy.getAutomated();
		var actualAutomated = actualSyncPolicy.getAutomated();

		assertEquals(expected.getApiVersion(), actual.getApiVersion());
		assertEquals(expected.getKind(), actual.getKind());
		assertEquals(expectedMetadata.getName(), actualMetadata.getName());
		assertEquals(expectedMetadata.getNamespace(), actualMetadata.getNamespace());
		assertEquals(expectedMetadata.getFinalizers(), actualMetadata.getFinalizers());
		assertEquals(expectedSpec.getProject(), actualSpec.getProject());
		assertEquals(expectedSource.getRepoURL(), actualSpec.getSource().getRepoURL());
		assertEquals(expectedSource.getChart(), actualSource.getChart());
		assertEquals(
			expectedValues.getAdditionalProperties(),
			actualValues.getAdditionalProperties()
		);
		assertEquals(expectedSource.getTargetRevision(), actualSource.getTargetRevision());
		assertEquals(expectedDestination.getServer(), actualDestination.getServer());
		assertEquals(expectedDestination.getNamespace(), actualDestination.getNamespace());
		assertEquals(expectedAutomated.getPrune(), actualAutomated.getPrune());
		assertEquals(expectedSyncPolicy.getSyncOptions(), actualSyncPolicy.getSyncOptions());

	}


	@Test
	void shouldMatchAsYaml() {

		var expected = deserialize("expected/minimal-application.yaml");

		var serialized = Serialization.asYaml(Manifest.builder()
			.targetNamespace("fooBar")
			.repoURL("https://registry.acme.com/repository/helm/")
			.chart("openk9-foo-parser")
			.version("1.0.0")
			.type(Manifest.Type.ARGOCD)
			.build()
			.asResource()
		);


		var actual = Serialization.unmarshal(serialized, Application.class);

		assertEquals(
			expected.getSpec().getSource().getChart(),
			actual.getSpec().getSource().getChart()
		);

		assertEquals(
			expected.getSpec().getSource().getRepoURL(),
			actual.getSpec().getSource().getRepoURL()
		);

		assertEquals(
			expected.getSpec().getSource().getHelm(),
			actual.getSpec().getSource().getHelm()
		);

		assertEquals(
			expected.getMetadata().getNamespace(),
			actual.getMetadata().getNamespace()
		);

		assertEquals(
			expected.getSpec().getProject(),
			actual.getSpec().getProject()
		);
	}

	private Application deserialize(String name) {
		return Serialization.unmarshal(Objects.requireNonNull(getClass()
			.getClassLoader()
			.getResourceAsStream(name)), Application.class);
	}

}