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

import com.google.protobuf.Empty;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.app.manager.grpc.ApplyResponse;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.common.util.StringUtils;
import io.openk9.k8s.crd.Manifest;
import io.openk9.k8sclient.service.IngressDef;
import io.openk9.k8sclient.service.IngressService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import javax.inject.Inject;

@GrpcService
public class AppManagerService implements AppManager {

	@Inject
	KubernetesClient k8sClient;

	@Inject
	IngressService ingressService;

	@Inject
	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	String namespace;

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.manifest-type")
	Manifest.Type manifestType;

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.repository-url")
	String repositoryUrl;

	@Override
	public Uni<ApplyResponse> applyResource(AppManifest request) {
		var manifest = Manifest.builder()
			.repoURL(repositoryUrl)
			.chart(request.getChart())
			.version(request.getVersion())
			.targetNamespace(namespace)
			.type(manifestType)
			.tenant(request.getSchemaName())
			.set(
				"nameOverride",
				StringUtils.withSuffix(request.getChart(), request.getSchemaName())
			)
			.build();

		return Uni.createFrom()
			.emitter(emitter -> {
				try {
					var resource = k8sClient.resource(manifest.asResource()).createOrReplace();

					emitter.complete(ApplyResponse
						.newBuilder()
						.setStatus(resource.getMetadata().getName())
						.build());
				}
				catch (Exception e) {
					emitter.fail(e);
				}
			});
	}

	@Override
	public Uni<Empty> deleteResource(AppManifest appManifest) {
		var manifest = Manifest.builder()
			.repoURL(repositoryUrl)
			.chart(appManifest.getChart())
			.version(appManifest.getVersion())
			.targetNamespace(namespace)
			.type(manifestType)
			.tenant(appManifest.getSchemaName())
			.build();

		return Uni.createFrom()
			.emitter(emitter -> {
				try {
					k8sClient.resource(manifest.asResource()).delete();

					emitter.complete(Empty.newBuilder().build());
				}
				catch (Exception e) {
					emitter.fail(e);
				}
			});

	}

	@Override
	public Uni<CreateIngressResponse> createIngress(CreateIngressRequest createIngressRequest) {
		var ingressDef = IngressDef.of(
			String.format("%s-default-ingress", createIngressRequest.getSchemaName()),
			createIngressRequest.getVirtualHost(),
			List.of(
				IngressDef.Route.of("/", "openk9-search-frontend", 8080),
				IngressDef.Route.of("/admin", "openk9-admin-ui", 8080),
				IngressDef.Route.of("/api/datasource", "openk9-datasource", 8080),
				IngressDef.Route.of("/api/searcher", "openk9-searcher", 8080)
			)
		);

		return ingressService.create(ingressDef)
			.map(hasMetadata -> CreateIngressResponse.newBuilder()
				.setStatus("SUCCESS")
				.setResourceName(hasMetadata.getMetadata().getName())
				.build());
	}


}
