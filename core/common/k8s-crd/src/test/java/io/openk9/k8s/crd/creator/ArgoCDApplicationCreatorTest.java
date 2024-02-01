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

package io.openk9.k8s.crd.creator;

import io.argoproj.v1alpha1.Application;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgoCDApplicationCreatorTest {

	@Test
	void createApplication() {

		var expected = Serialization.unmarshal(Objects.requireNonNull(
			getClass()
				.getClassLoader()
				.getResourceAsStream("expected/full-application.yaml")
		), Application.class);

		var mayActual = ArgoCDApplicationCreator.INSTANCE.create(
			"default",
			"https://registry.acme.com/repository/helm/",
			"openk9-foo-parser",
			"1.0.0",
			Map.of(
				"K1", "V1",
				"K2", 2,
				"K3", "v3"
			)
		);

		assertTrue(mayActual.isPresent());
		var actual = mayActual.get();

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

}