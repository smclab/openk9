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

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.inject.Inject;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.app.manager.grpc.AppManifestList;
import io.openk9.app.manager.grpc.ApplyResponse;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteAllResourcesResponse;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.app.manager.grpc.DeleteResourceStatus;
import io.openk9.app.manager.grpc.IngressScope;
import io.openk9.app.manager.grpc.Status;
import io.openk9.common.util.Strings;
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

/**
 * Quarkus gRPC implementation of the {@link AppManager} service contract.
 * <p>
 * Bridges gRPC requests to the Kubernetes cluster: applies and deletes
 * {@link Manifest} resources via {@link KubernetesClient}, and
 * manages tenant Ingresses via {@link IngressService}.
 */
@GrpcService
public class AppManagerService implements AppManager {

	private static final Logger log =
		Logger.getLogger(AppManagerService.class);
	@Inject
	IngressService ingressService;
	@Inject
	KubernetesClient k8sClient;
	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.manifest-type")
	Manifest.Type manifestType;
	@Inject
	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	String namespace;
	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.repository-url")
	String repositoryUrl;

	private static final Map<IngressScope, List<IngressDef.Route>> SCOPE_ROUTES =
		Map.of(
			IngressScope.SEARCH, List.of(
				IngressDef.Route.of("/", "openk9-search-frontend", 8080),
				IngressDef.Route.of("/api/searcher", "openk9-api-gateway", 8080),
				IngressDef.Route.of("/api/datasource", "openk9-api-gateway", 8080)
			),
			IngressScope.ADMINISTRATION, List.of(
				IngressDef.Route.of("/admin", "openk9-admin-ui", 8080),
				IngressDef.Route.of("/api/datasource", "openk9-api-gateway", 8080)
			),
			IngressScope.RAG, List.of(
				IngressDef.Route.of("/chat", "openk9-talk-to", 8080),
				IngressDef.Route.of("/api/rag", "openk9-api-gateway", 8080)
			),
			IngressScope.INGESTION, List.of(
				IngressDef.Route.of("/api/ingestion", "openk9-api-gateway", 8080),
				IngressDef.Route.of("/api/datasource", "openk9-api-gateway", 8080)
			)
		);

	private static final Set<IngressScope> DEFAULT_SCOPES = EnumSet.of(
		IngressScope.SEARCH, IngressScope.ADMINISTRATION, IngressScope.RAG);

	private static String ingressName(String tenantId) {
		return String.format("%s-default-ingress", tenantId);
	}

	/**
	 * Builds the {@link IngressDef} describing the default Ingress for a tenant,
	 * aggregating the HTTP routes required by the requested {@link IngressScope}s.
	 * <p>
	 * When {@code scopes} is {@code null} or empty, {@link #DEFAULT_SCOPES}
	 * ({@code SEARCH}, {@code ADMINISTRATION}, {@code RAG}) is used. Unknown
	 * enum values ({@link IngressScope#UNRECOGNIZED}, produced by protobuf when
	 * the client sends a scope the server does not know) are silently filtered
	 * out.
	 * <p>
	 * Routes are collected into a {@link LinkedHashSet} so that scope iteration
	 * order is preserved and routes shared across scopes (e.g.
	 * {@code /api/datasource}) are de-duplicated.
	 *
	 * @param tenantId    tenant identifier, used to derive the ingress name
	 * @param virtualHost host the ingress answers on
	 * @param scopes      requested scopes; {@code null}/empty lead to defaults
	 * @return the ingress definition to be created
	 */
	private static IngressDef getIngressDef(
		String tenantId, String virtualHost, List<IngressScope> scopes) {

		Set<IngressScope> effectiveScopes;
		if (scopes == null || scopes.isEmpty()) {
			effectiveScopes = DEFAULT_SCOPES;
		}
		else {
			effectiveScopes = EnumSet.noneOf(IngressScope.class);
			for (IngressScope scope : scopes) {
				if (scope != IngressScope.UNRECOGNIZED) {
					effectiveScopes.add(scope);
				}
			}
		}

		LinkedHashSet<IngressDef.Route> routes = new LinkedHashSet<>();
		for (IngressScope scope : effectiveScopes) {
			List<IngressDef.Route> scopeRoutes = SCOPE_ROUTES.get(scope);
			if (scopeRoutes != null) {
				routes.addAll(scopeRoutes);
			}
		}

		return IngressDef.of(
			ingressName(tenantId),
			virtualHost,
			List.copyOf(routes)
		);
	}

	/**
	 * Applies a Kubernetes resource described by the given manifest.
	 * Creates the resource if absent, otherwise updates it in place.
	 *
	 * @param request the manifest to materialize (chart, version, tenant)
	 * @return a {@link Uni} emitting an {@link ApplyResponse} whose status
	 *         carries the applied resource name
	 */
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
				Strings.withSuffix(request.getChart(), request.getSchemaName())
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

	/**
	 * Creates the default Ingress for a tenant. The routes exposed by the
	 * Ingress are determined by the requested scopes; when no scopes are
	 * provided the defaults ({@code SEARCH}, {@code ADMINISTRATION},
	 * {@code RAG}) are applied. See {@link #getIngressDef} for the aggregation
	 * rules.
	 *
	 * @param request tenantId, virtual host and requested scopes
	 * @return a {@link Uni} emitting a {@link CreateIngressResponse} with the
	 *         created resource name
	 */
	@Override
	public Uni<CreateIngressResponse> createIngress(CreateIngressRequest request) {
		var ingressDef = getIngressDef(
			request.getTenantId(),
			request.getVirtualHost(),
			request.getScopesList());

		return ingressService.create(ingressDef)
			.map(hasMetadata -> CreateIngressResponse.newBuilder()
				.setStatus("SUCCESS")
				.setResourceName(hasMetadata.getMetadata().getName())
				.build())
			.onFailure()
			.invoke(throwable ->
				log.warn("Ingress create got an error", throwable));
	}

	/**
	 * Deletes every resource in the supplied list in parallel.
	 * <p>
	 * Individual failures do not fail the whole call: per-item outcomes are
	 * collected into the response as {@link DeleteResourceStatus} entries with
	 * either {@code SUCCESS} or {@code ERROR} status.
	 *
	 * @param request the manifests to delete
	 * @return a {@link Uni} emitting the aggregated
	 *         {@link DeleteAllResourcesResponse}
	 */
	@Override
	public Uni<DeleteAllResourcesResponse> deleteAllResources(AppManifestList request) {

		var unis = request.getAppManifestsList().stream()
			.map(appManifest -> executeDeleteResource(appManifest)
				.map(ignored ->
					DeleteResourceStatus.newBuilder()
						.setStatus(Status.SUCCESS)
						.setResourceName(appManifest.getChart())
						.build()
				)
				.onFailure()
				.recoverWithItem(failure -> {
					log.warnf("%s resource delete got an error", appManifest.getChart(), failure);

					return DeleteResourceStatus.newBuilder()
						.setStatus(Status.ERROR)
						.setResourceName(appManifest.getChart())
						.build();
				})
			)
			.toList();

		return Uni.join()
			.all(unis)
			.andCollectFailures()
			.map(deleteResourcesStatusList ->
					DeleteAllResourcesResponse.newBuilder()
					.addAllDeleteResourceStatus(deleteResourcesStatusList)
					.build()
				);
	}

	/**
	 * Deletes the default Ingress of a tenant, whose name follows the
	 * {@code <tenant>-default-ingress} convention.
	 *
	 * @param request identifies the tenant whose Ingress is being removed
	 * @return a {@link Uni} emitting a {@link DeleteIngressResponse} with the
	 *         removed resource name
	 */
	@Override
	public Uni<DeleteIngressResponse> deleteIngress(DeleteIngressRequest request) {
		var ingressName = ingressName(request.getSchemaName());

		return ingressService.delete(ingressName)
			.map(statusDetails -> DeleteIngressResponse.newBuilder()
				.setStatus("SUCCESS")
				.setResourceName(ingressName)
				.build())
			.onFailure()
			.invoke(throwable ->
				log.warn("Ingress delete got an error", throwable));
	}

	/**
	 * Deletes a single Kubernetes resource described by the given manifest.
	 *
	 * @param appManifest the manifest identifying the resource to delete
	 * @return a {@link Uni} emitting {@link Empty} on success
	 */
	@Override
	public Uni<Empty> deleteResource(AppManifest appManifest) {
		return executeDeleteResource(appManifest)
			.onFailure()
			.invoke(throwable ->
				log.warn("Resource delete got an error", throwable));
	}

	private Uni<Empty> executeDeleteResource(AppManifest appManifest) {
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
			.map(ignore -> Empty.newBuilder().build());
	}


}
