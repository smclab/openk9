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

import io.cattle.helm.v1.HelmChart;
import io.cattle.helm.v1.HelmChartSpec;
import io.cattle.helm.v1.helmchartspec.AuthSecret;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HelmChartCreator
	implements CustomResourceCreator<HelmChart> {

	public static final HelmChartCreator INSTANCE = new HelmChartCreator();
	private static final String CONTROLLER_NAMESPACE = "helm-controller";

	private HelmChartCreator() {}

	@Override
	public Optional<HelmChart> create(
		String targetNamespace,
		String repoURL,
		String chart,
		String version,
		String authSecretName,
		Map<String, Object> values) {

		return createHelmChart(targetNamespace, repoURL, chart, version, authSecretName, values);
	}

	@Override
	public Optional<HelmChart> create(
		String targetNamespace,
		String repoURL,
		String chart,
		String version,
		Map<String, Object> values) {

		return create(targetNamespace, repoURL, chart, version, null, values);
	}

	@Override
	public Optional<HelmChart> create(
		String targetNamespace,
		String chart,
		String version,
		Map<String, Object> values) {

		return create(targetNamespace, null, chart, version, null, values);
	}

	@Override
	public Optional<HelmChart> create(String targetNamespace, String chart, String version) {

		return create(targetNamespace, null, chart, version, null, null);
	}

	protected static Optional<HelmChart> createHelmChart(
		String targetNamespace,
		String repoURL,
		String chart,
		String version,
		String authSecretName,
		Map<String, Object> values) {

		if (Objects.isNull(targetNamespace)
			|| Objects.isNull(chart)
			|| Objects.isNull(version)
			|| targetNamespace.isBlank()
			|| chart.isBlank()
			|| version.isBlank()) {

			return Optional.empty();
		}

		var metadata = new ObjectMeta();
		metadata.setName(chart);
		metadata.setNamespace(CONTROLLER_NAMESPACE);

		var authSecret = new AuthSecret();
		authSecret.setName(authSecretName);

		var spec = new HelmChartSpec();
		spec.setRepo(repoURL);
		spec.setChart(chart);
		spec.setVersion(version);
		spec.setTargetNamespace(targetNamespace);
		spec.setAuthSecret(authSecret);

		var set = new HashMap<String, IntOrString>();
		values.forEach((s, o) -> set.put(s, new IntOrString(o)));
		spec.setSet(set);

		var helmChart = new HelmChart();
		helmChart.setMetadata(metadata);
		helmChart.setSpec(spec);

		return Optional.of(helmChart);
	}

}
