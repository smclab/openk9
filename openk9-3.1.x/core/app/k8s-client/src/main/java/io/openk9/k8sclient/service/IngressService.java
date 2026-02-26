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

package io.openk9.k8sclient.service;

import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressTLSBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.ServiceBackendPort;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class IngressService {

	private static final String PATH_TYPE = "Prefix";
	private static final String EMPTY_PORT_NAME = "";

	@Inject
	KubernetesClient k8sClient;

	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	String namespace;

	@ConfigProperty(name = "quarkus.kubernetes.ingress.secretName")
	String secretName;

	@ConfigProperty(name = "quarkus.kubernetes.ingress.ingressClassName")
	String ingressClassName;

	public Uni<HasMetadata> create(IngressDef ingressDef) {
		return VertxContextSupport.executeBlocking(() -> {
				var ingress = getIngress(ingressDef);

				return k8sClient.resource(ingress).createOrReplace();
			})
			.map(ingress -> ingress);
	}

	public Uni<List<StatusDetails>> delete(IngressDef ingressDef) {
		return VertxContextSupport.executeBlocking(() -> {
				var ingress = getIngress(ingressDef);

				return k8sClient.resource(ingress).delete();
		});
	}

	private Ingress getIngress(IngressDef ingressDef) {
		List<HTTPIngressPath> ingressPaths = new ArrayList<>();
		for (IngressDef.Route route : ingressDef.routes()) {
			var ingressPath = new HTTPIngressPathBuilder()
				.withPath(route.path())
				.withPathType(PATH_TYPE)
				.withNewBackend()
				.withNewService()
				.withName(route.serviceName())
				.withPort(servicePort(route))
				.endService()
				.endBackend()
				.build();
			ingressPaths.add(ingressPath);
		}

		return new IngressBuilder()
			.withNewMetadata()
			.withName(ingressDef.ingressName())
			.withNamespace(namespace)
			.endMetadata()
			.withNewSpec()
			.withTls(new IngressTLSBuilder()
				.withSecretName(secretName)
				.withHosts(ingressDef.virtualHost())
				.build()
			)
			.withIngressClassName(ingressClassName)
			.withRules(new IngressRuleBuilder()
				.withHost(ingressDef.virtualHost())
				.withNewHttp()
				.withPaths(ingressPaths)
				.endHttp()
				.build()
			)
			.endSpec()
			.build();
	}

	private static ServiceBackendPort servicePort(IngressDef.Route route) {
		return new ServiceBackendPort(EMPTY_PORT_NAME, route.servicePort());
	}

}
