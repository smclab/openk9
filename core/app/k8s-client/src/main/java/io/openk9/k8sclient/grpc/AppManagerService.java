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

import java.util.List;
import jakarta.inject.Inject;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.app.manager.grpc.ApplyResponse;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.common.util.StringUtils;
import io.openk9.k8s.crd.Manifest;
import io.openk9.k8sclient.service.IngressDef;
import io.openk9.k8sclient.service.IngressService;

import com.google.protobuf.Empty;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.grpc.GrpcService;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@GrpcService
public class AppManagerService implements AppManager {

	private static final Logger log =
		Logger.getLogger(AppManagerService.class);

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

		return VertxContextSupport.executeBlocking(() -> k8sClient
				.resource(manifest.asResource())
				.createOr(ignore -> k8sClient
					.resource(manifest.asResource())
					.update())
			)
			.map(hasMetadata -> ApplyResponse.newBuilder()
				.setStatus(hasMetadata
					.getMetadata()
					.getName())
				.build()
			)
			.onFailure()
			.invoke(throwable ->
				log.warn("Resource apply got an error", throwable));
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

		return VertxContextSupport.executeBlocking(() -> k8sClient
				.resource(manifest.asResource()).delete())
			.map(ignore -> Empty.newBuilder().build())
			.onFailure()
			.invoke(throwable ->
				log.warn("Resource delete got an error", throwable));
	}

	@Override
	public Uni<CreateIngressResponse> createIngress(CreateIngressRequest request) {
		var ingressDef = getIngressDef(request.getSchemaName(), request.getVirtualHost());

		return ingressService.create(ingressDef)
			.map(hasMetadata -> CreateIngressResponse.newBuilder()
				.setStatus("SUCCESS")
				.setResourceName(hasMetadata.getMetadata().getName())
				.build())
			.onFailure()
			.invoke(throwable ->
				log.warn("Ingress create got an error", throwable));
	}

	@Override
	public Uni<DeleteIngressResponse> deleteIngress(DeleteIngressRequest request) {
		var ingressDef = getIngressDef(request.getSchemaName(), request.getVirtualHost());

		return ingressService.delete(ingressDef)
			.map(statusDetails -> DeleteIngressResponse.newBuilder()
				.setStatus("SUCCESS")
				.setResourceName(ingressDef.ingressName())
				.build())
			.onFailure()
			.invoke(throwable ->
				log.warn("Ingress delete got an error", throwable));
	}

	private static IngressDef getIngressDef(String schemaName, String virtualHost) {
		return IngressDef.of(
			String.format(
				"%s-default-ingress",
				schemaName
			),
			virtualHost,
			List.of(
				IngressDef.Route.of("/search", "openk9-search-frontend", 8080),
				IngressDef.Route.of("/admin", "openk9-admin-ui", 8080),
				IngressDef.Route.of("/api/datasource", "openk9-datasource", 8080),
				IngressDef.Route.of("/api/searcher", "openk9-searcher", 8080),
				IngressDef.Route.of("/api/rag", "openk9-rag-module", 5000),
				IngressDef.Route.of("/chat", "openk9-talk-to", 8080)
			)
		);
	}


}
