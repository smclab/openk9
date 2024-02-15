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
import io.cattle.helm.v1.HelmChart;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.Builder;
import lombok.Singular;

import java.util.Map;
import java.util.Objects;

@Builder(toBuilder = true)
public record Manifest(
	String targetNamespace,
	String chart,
	String version,
	String repoURL,
	String authSecretName,
	@Singular("set") Map<String, Object> values,
	String tenant,
	Type type
) {

	public Manifest {
		Objects.requireNonNull(targetNamespace);
		Objects.requireNonNull(chart);
		Objects.requireNonNull(version);
		if (targetNamespace.isBlank()) {
			throw new IllegalArgumentException("targetNamespace cannot be blank");
		}
		if (chart.isBlank()) {
			throw new IllegalArgumentException("chart cannot be blank");
		}
		if (version.isBlank()) {
			throw new IllegalArgumentException("version cannot be blank");
		}
	}

	public static Application asApplication(Manifest manifest) {
		return (Application) manifest.asResource();
	}

	public static HelmChart asHelmChart(Manifest manifest) {
		return (HelmChart) manifest.asResource();
	}

	public HasMetadata asResource() {
		if (this.type == null) {
			throw new IllegalStateException("Resource type is not defined");
		}

		return switch (this.type) {
			case ARGOCD -> ArgoCDManifest.createApplication(this);
			case HELM -> HelmManifest.createHelmChart(this);
		};

	}

	public enum Type {
		ARGOCD,
		HELM
	}

}
