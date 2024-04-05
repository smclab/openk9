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
import io.cattle.helm.v1.HelmChartSpec;
import io.cattle.helm.v1.helmchartspec.AuthSecret;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.openk9.common.util.StringUtils;

import java.util.HashMap;

class HelmManifest {

	private static final String CONTROLLER_NAMESPACE = "helm-controller";

	private HelmManifest() {}

	static HelmChart createHelmChart(Manifest manifest) {

		var metadata = new ObjectMeta();
		metadata.setName(StringUtils.withSuffix(manifest.chart(), manifest.tenant()));
		metadata.setNamespace(CONTROLLER_NAMESPACE);

		var spec = new HelmChartSpec();
		spec.setRepo(manifest.repoURL());
		spec.setChart(manifest.chart());
		spec.setVersion(manifest.version());
		spec.setTargetNamespace(manifest.targetNamespace());

		if (manifest.authSecretName() != null) {
			var authSecret = new AuthSecret();
			authSecret.setName(manifest.authSecretName());
			spec.setAuthSecret(authSecret);
		}

		if (manifest.values() != null) {
			var set = new HashMap<String, IntOrString>();
			manifest.values().forEach((s, o) -> set.put(s, new IntOrString(o)));
			spec.setSet(set);
		}

		var helmChart = new HelmChart();
		helmChart.setMetadata(metadata);
		helmChart.setSpec(spec);

		return helmChart;
	}

}
