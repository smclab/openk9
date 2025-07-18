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

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import io.openk9.common.util.StringUtils;

import io.argoproj.v1alpha1.Application;
import io.argoproj.v1alpha1.ApplicationSpec;
import io.argoproj.v1alpha1.applicationspec.Destination;
import io.argoproj.v1alpha1.applicationspec.Source;
import io.argoproj.v1alpha1.applicationspec.SyncPolicy;
import io.argoproj.v1alpha1.applicationspec.source.Helm;
import io.argoproj.v1alpha1.applicationspec.source.helm.ValuesObject;
import io.argoproj.v1alpha1.applicationspec.syncpolicy.Automated;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ArgoCDManifest {

	private static final String ARGOCD_FINALIZER = "resources-finalizer.argocd.argoproj.io";
	private static final String DEFAULT_SVC = "https://kubernetes.default.svc";
	private static final String VALIDATE_FALSE = "Validate=false";
	public static final String DEFAULT_PROJECT = "default";

	private ArgoCDManifest() {}

	static Application createApplication(Manifest manifest) {

		var application = new Application();

		var metadata = new ObjectMeta();
		metadata.setName(StringUtils.withSuffix(manifest.chart(), manifest.tenant()));
		metadata.setNamespace(manifest.targetNamespace());
		metadata.setFinalizers(List.of(ARGOCD_FINALIZER));

		var source = new Source();

		source.setChart(manifest.chart());
		source.setTargetRevision(manifest.version());

		Objects.requireNonNull(manifest.repoURL(), "repoUrl cannot be null!");
		source.setRepoURL(manifest.repoURL());

		if (manifest.values() != null && !manifest.values().isEmpty()) {
			var valuesObject = new ValuesObject();
			valuesObject.setAdditionalProperties(manifest.values());

			var helm = new Helm();
			helm.setValuesObject(valuesObject);

			source.setHelm(helm);
		}

		var destination = new Destination();
		destination.setServer(DEFAULT_SVC);
		destination.setNamespace(manifest.targetNamespace());

		var automated = new Automated();
		automated.setPrune(true);

		var syncPolicy = new SyncPolicy();
		syncPolicy.setAutomated(automated);
		syncPolicy.setSyncOptions(List.of(VALIDATE_FALSE));

		var spec = new ApplicationSpec();
		spec.setProject(getProject(manifest));
		spec.setSource(source);
		spec.setDestination(destination);
		spec.setSyncPolicy(syncPolicy);

		application.setMetadata(metadata);
		application.setSpec(spec);

		return application;
	}

	private static String getProject(Manifest manifest) {
		var namespace = manifest.targetNamespace();
		var nsRegex = Pattern.compile("^(?:open)?k9-(?<id>[A-Za-z0-9\\-_]+)$");
		var matcher = nsRegex.matcher(namespace);

		if (matcher.find()) {
			var id = matcher.group("id");

			return String.format("openk9-%s", id);
		}

		return DEFAULT_PROJECT;
	}

}
