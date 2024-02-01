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
import io.argoproj.v1alpha1.ApplicationSpec;
import io.argoproj.v1alpha1.applicationspec.Destination;
import io.argoproj.v1alpha1.applicationspec.Source;
import io.argoproj.v1alpha1.applicationspec.SyncPolicy;
import io.argoproj.v1alpha1.applicationspec.source.Helm;
import io.argoproj.v1alpha1.applicationspec.source.helm.ValuesObject;
import io.argoproj.v1alpha1.applicationspec.syncpolicy.Automated;
import io.fabric8.kubernetes.api.model.ObjectMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ArgoCDApplicationCreator implements CustomResourceCreator<Application> {

	public static final ArgoCDApplicationCreator INSTANCE = new ArgoCDApplicationCreator();
	private static final String ARGOCD_NAMESPACE = "argocd";
	private static final String ARGOCD_FINALIZER = "resources-finalizer.argocd.argoproj.io";
	private static final String DEFAULT_SVC = "https://kubernetes.default.svc";
	private static final String VALIDATE_FALSE = "Validate=false";

	private ArgoCDApplicationCreator() {}

	@Override
	public Optional<Application> create(
		String targetNamespace,
		String repoURL,
		String chart,
		String version,
		String authSecretName,
		Map<String, Object> values) {

		return create(targetNamespace, repoURL, chart, version, values);
	}

	@Override
	public Optional<Application> create(
		String targetNamespace,
		String repoURL,
		String chart,
		String version,
		Map<String, Object> values) {

		return createApplication(targetNamespace, repoURL, chart, version, values);
	}

	@Override
	public Optional<Application> create(
		String targetNamespace,
		String chart,
		String version,
		Map<String, Object> values) {

		return create(targetNamespace, null, chart, version, values);
	}

	@Override
	public Optional<Application> create(String targetNamespace, String chart, String version) {
		return create(targetNamespace, null, chart, version, null);
	}

	protected static Optional<Application> createApplication(
		String targetNamespace,
		String repoURL,
		String chart,
		String version,
		Map<String, Object> values) {

		if (Objects.isNull(targetNamespace)
			|| Objects.isNull(chart)
			|| Objects.isNull(version)
			|| targetNamespace.isBlank()
			|| chart.isBlank()
			|| version.isBlank()) {

			return Optional.empty();
		}

		var application = new Application();

		var metadata = new ObjectMeta();
		metadata.setName(chart);
		metadata.setNamespace(ARGOCD_NAMESPACE);
		metadata.setFinalizers(List.of(ARGOCD_FINALIZER));

		var valuesObject = new ValuesObject();
		valuesObject.setAdditionalProperties(values);

		var helm = new Helm();
		helm.setValuesObject(valuesObject);

		var source = new Source();
		source.setRepoURL(repoURL);
		source.setChart(chart);
		source.setTargetRevision(version);

		source.setHelm(helm);

		var destination = new Destination();
		destination.setServer(DEFAULT_SVC);
		destination.setNamespace(targetNamespace);

		var automated = new Automated();
		automated.setPrune(true);

		var syncPolicy = new SyncPolicy();
		syncPolicy.setAutomated(automated);
		syncPolicy.setSyncOptions(List.of(VALIDATE_FALSE));

		var spec = new ApplicationSpec();
		spec.setProject(targetNamespace);
		spec.setSource(source);
		spec.setDestination(destination);
		spec.setSyncPolicy(syncPolicy);

		application.setMetadata(metadata);
		application.setSpec(spec);

		return Optional.of(application);
	}

}
