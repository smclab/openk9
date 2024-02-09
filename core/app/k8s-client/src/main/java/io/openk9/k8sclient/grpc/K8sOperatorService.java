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

package io.openk9.k8sclient.grpc;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.openk9.k8s.crd.Manifest;
import io.openk9.k8s.operator.grpc.DeployEnrichItemRequest;
import io.openk9.k8s.operator.grpc.DeployEnrichItemResponse;
import io.openk9.k8s.operator.grpc.DeployMLEnrichItemRequest;
import io.openk9.k8s.operator.grpc.DeployMLEnrichItemResponse;
import io.openk9.k8s.operator.grpc.DeployPluginDriverRequest;
import io.openk9.k8s.operator.grpc.DeployPluginDriverResponse;
import io.openk9.k8s.operator.grpc.K8sOperator;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.function.Function;
import javax.inject.Inject;

@GrpcService
public class K8sOperatorService implements K8sOperator {
	@Inject
	KubernetesClient k8sClient;

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.namespace")
	String namespace;

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.manifest-type")
	Manifest.Type manifestType;

	@Override
	public Uni<DeployEnrichItemResponse> deployEnrichItem(DeployEnrichItemRequest request) {

		var chart = request.getChart();
		var version = request.getVersion();
		var schemaName = request.getSchemaName();

		return createResource(
			Manifest.builder()
				.targetNamespace(namespace)
				.chart(chart)
				.version(version)
				.tenant(schemaName)
				.type(manifestType)
				.build(),
			hasMetadata -> DeployEnrichItemResponse
				.newBuilder()
				.setStatus(hasMetadata.getMetadata().getName())
				.build()
		);

	}

	@Override
	public Uni<DeployMLEnrichItemResponse> deployMLEnrichItem(DeployMLEnrichItemRequest request) {

		var chart = request.getChart();
		var version = request.getVersion();
		var schemaName = request.getSchemaName();

		return createResource(
			Manifest.builder()
				.targetNamespace(namespace)
				.chart(chart)
				.version(version)
				.tenant(schemaName)
				.type(manifestType)
				.build(),
			hasMetadata -> DeployMLEnrichItemResponse
				.newBuilder()
				.setStatus(hasMetadata.getMetadata().getName())
				.build()
		);

	}

	@Override
	public Uni<DeployPluginDriverResponse> deployPluginDriver(DeployPluginDriverRequest request) {

		var chart = request.getChart();
		var version = request.getVersion();
		var schemaName = request.getSchemaName();

		return createResource(
			Manifest.builder()
				.targetNamespace(namespace)
				.chart(chart)
				.version(version)
				.tenant(schemaName)
				.type(manifestType)
				.build(),
			hasMetadata -> DeployPluginDriverResponse
				.newBuilder()
				.setStatus(hasMetadata.getMetadata().getName())
				.build()
		);

	}

	private <T> Uni<T> createResource(Manifest manifest, Function<HasMetadata, T> responseMapper) {
		return Uni
			.createFrom()
			.emitter(emitter -> {
				try {
					var resource = k8sClient.resource(manifest.asResource()).createOrReplace();

					emitter.complete(responseMapper.apply(resource));
				}
				catch (Exception e) {
					emitter.fail(e);
				}
			});
	}

}
