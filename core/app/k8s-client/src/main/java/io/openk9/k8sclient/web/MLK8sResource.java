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

package io.openk9.k8sclient.web;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.openk9.k8sclient.dto.MlDto;
import io.openk9.k8sclient.dto.MlPodResponse;
import io.openk9.k8sclient.dto.ModelActionesponse;
import io.openk9.k8sclient.dto.PodResponse;
import io.openk9.tenantmanager.grpc.TenantManagerGrpc;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.openk9.tenantmanager.grpc.TenantResponse;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.quarkus.grpc.GrpcClient;
import io.vertx.core.http.HttpServerRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
@ActivateRequestContext
@Path("/k8s")
@RolesAllowed("k9-admin")
public class MLK8sResource {

	@Inject
	HttpServerRequest _request;

	@Inject
	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	String namespace;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.docker-registry")
	String dockerRegistry;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.base-transformers-tensorflow-image")
	String baseMlTransformersTensorflowImage;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.base-transformers-pytorch-image")
	String baseMlTransformersPytorchImage;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.response-url")
	String pipelineResponseUrl;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.ml-prediction-timeout")
	String predictionTimeout;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.ml-prediction-max-length")
	String textMaxLength;

	@Inject
	@ConfigProperty(name = "openk9.pipeline.download-endpoint")
	String downloadEndpoint;

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.ingestion-url")
	String ingestionUrl;

	private String getName(Pod e) {

		String name = e.getMetadata().getLabels().get("statefulset.kubernetes.io/pod-name");

		if (name == null) {
			name = e.getMetadata().getLabels().get("app.kubernetes.io/name");
		}

		if (name == null) {
			name = e.getMetadata().getLabels().get("app");
		}

		return name;
	}

	private String getTenant() {

		TenantResponse tenantResponse = tenantmanager
			.findTenant(TenantRequest.newBuilder().setVirtualHost(_request.host()).build());

		return tenantResponse.getSchemaName();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get/pods/ml")
	public List<MlPodResponse> getMlPods() {

		return _kubernetesClient.pods().inNamespace(namespace)
			.list().getItems().stream().map(pod -> {

				if (Objects.equals(
					pod.getMetadata().getLabels().get("app-type"), "ml") &&
					Objects.equals(
						pod.getMetadata().getLabels().get("tenant"), getTenant())) {

					MlPodResponse mlPodResponse = new MlPodResponse();

					String name = getName(pod).replace( "-" + getTenant(), "");

					mlPodResponse.setName(name);
					mlPodResponse.setTask(pod.getMetadata().getLabels().get("task"));
					mlPodResponse.setLibrary(pod.getMetadata().getLabels().get("library"));

					boolean isTerminating = pod.getMetadata().getDeletionTimestamp() != null;
					if (isTerminating) {
						mlPodResponse.setStatus("Terminating");
					}
					else {
						mlPodResponse.setStatus(pod.getStatus().getPhase());
					}

					return mlPodResponse;
				}

				return null;

			}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/deploy-ml-model")
	public ModelActionesponse deployMlModel(MlDto mlDto) throws KubernetesClientException{

		String tokenizerName = mlDto.getModelName();
		String modelName = mlDto.getModelName();
		String deploymentName = modelName
			.replace("/", "").replace("_", "")
			.toLowerCase()  + "-" + getTenant();
		String pipelineName = mlDto.getPipelineName();
		String library = mlDto.getLibrary();

		String configMapName = deploymentName + "-configmap";

		String baseMlImage = switch (library) {
			case "transformers-tensorflow" -> baseMlTransformersTensorflowImage;
			case "transformers-pytorch" -> baseMlTransformersPytorchImage;
			default -> baseMlTransformersPytorchImage;
		};

		baseMlImage = dockerRegistry + "/" + baseMlImage;

		try {

			ConfigMap configMap = new ConfigMapBuilder()
				.withNewMetadata().withName(configMapName).endMetadata()
				.addToData("TOKENIZER", tokenizerName)
				.addToData("MODEL", modelName)
				.addToData("PIPELINE_NAME", pipelineName)
				.addToData("PREDICTION_TIMEOUT", predictionTimeout)
				.addToData("MAX_LENGTH", textMaxLength)
				.addToData("PIPELINE_RESPONSE_URL", pipelineResponseUrl)
				.addToData("DOWNLOAD_ENDPOINT", downloadEndpoint)
				.build();

			_kubernetesClient.configMaps()
				.inNamespace(namespace).resource(configMap).create();

			logger.info("Created configmap: " + configMap.toString());


			EnvFromSource envFrom =
				new EnvFromSourceBuilder()
					.withNewConfigMapRef()
					.withName(configMapName)
					.endConfigMapRef()
					.build();

			Deployment deployment = new DeploymentBuilder()
				.withNewMetadata()
				.withName(deploymentName)
				.endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withNewTemplate()
				.withNewMetadata()
				.addToLabels("app", deploymentName)
				.addToLabels("library", library)
				.addToLabels("app-type", "ml")
				.addToLabels("task", pipelineName)
				.addToLabels("tenant", getTenant())
				.endMetadata()
				.withNewSpec()
				.addNewContainer()
				.withName(deploymentName)
				.withImage(baseMlImage)
				.withImagePullPolicy("Always")
				.withEnvFrom(envFrom)
				.addNewPort()
				.withContainerPort(5000)
				.endPort()
				.endContainer()
				.endSpec()
				.endTemplate()
				.withNewSelector()
				.addToMatchLabels("app", deploymentName)
				.endSelector()
				.endSpec()
				.build();

			deployment =
				_kubernetesClient.apps().deployments().inNamespace(namespace)
					.resource(deployment).create();

			logger.info("Created deployment: " + deployment.toString());


			Service service = new ServiceBuilder()
				.withNewMetadata()
				.withName(deploymentName)
				.endMetadata()
				.withNewSpec()
				.withSelector(Collections.singletonMap("app", deploymentName))
				.addNewPort()
				.withName("prediction-port")
				.withProtocol("TCP")
				.withPort(5000)
				.withTargetPort(new IntOrString(5000))
				.endPort()
				.withType("ClusterIP")
				.endSpec()
				.build();

			service = _kubernetesClient.services().inNamespace(namespace)
					.resource(service).create();

			logger.info("Created service with name " + service.getMetadata().getName());

			return new ModelActionesponse("Model deploy started", "SUCCESS");

		}
		catch (KubernetesClientException e) {

			if (e.getStatus().getCode() == 409) {
				return new ModelActionesponse("Model already exist", "DANGER");
			}
			else {
				deleteMlModel(deploymentName);
				throw e;
			}

		} catch (Exception e) {
			deleteMlModel(deploymentName);
			throw e;
		}

	}

	@DELETE
	@Path("/delete-ml-model/{name}")
	public ModelActionesponse deleteMlModel(@PathParam("name") String name) throws KubernetesClientException {

		String configMapName = name  + "-" + getTenant() + "-configmap";

		_kubernetesClient.apps()
			.deployments()
			.inNamespace(namespace).withName(name  + "-" + getTenant())
			.delete();

		_kubernetesClient.configMaps()
			.inNamespace(namespace).withName(configMapName).delete();

		_kubernetesClient.services()
			.inNamespace(namespace).withName(name  + "-" + getTenant()).delete();

		return new ModelActionesponse("Model deleted", "SUCCESS");

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/deploy-parser/{parserName}")
	public ModelActionesponse deployParserModel(@PathParam("parserName")String parserName) throws
		KubernetesClientException {

		String serviceName = parserName.toLowerCase() + "-" + getTenant();

		try {

			Deployment deployment = new DeploymentBuilder()
				.withNewMetadata()
				.withName(serviceName)
				.endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withNewTemplate()
				.withNewMetadata()
				.addToLabels(
					"app.kubernetes.io/name", serviceName.toLowerCase())
				.addToLabels("app-type", "parser")
				.addToLabels("tenant", getTenant())
				.endMetadata()
				.withNewSpec()
				.addNewContainer()
				.withName(serviceName)
				.withImage(dockerRegistry + "/" + serviceName + ":latest")
				.withImagePullPolicy("Always")
				.withEnv(
					new EnvVar(
						"INGESTION_URL",
						ingestionUrl,
						null)
				)
				.addNewPort()
				.withContainerPort(5000)
				.endPort()
				.endContainer()
				.endSpec()
				.endTemplate()
				.withNewSelector()
				.addToMatchLabels("app.kubernetes.io/name", serviceName)
				.endSelector()
				.endSpec()
				.build();

			deployment =
				_kubernetesClient.apps().deployments().inNamespace(namespace)
					.resource(deployment).create();

			logger.info("Created deployment: " + deployment.toString());

			Service service = new ServiceBuilder()
				.withNewMetadata()
				.withName(serviceName)
				.endMetadata()
				.withNewSpec()
				.withSelector(Collections.singletonMap("app.kubernetes.io/name",
					serviceName))
				.addNewPort()
				.withName("trigger-port")
				.withProtocol("TCP")
				.withPort(5000)
				.withTargetPort(new IntOrString(5000))
				.endPort()
				.withType("ClusterIP")
				.endSpec()
				.build();

			service =
				_kubernetesClient.services().inNamespace(namespace)
						.resource(service).create();

			logger.info(
				"Created service with name " + service.getMetadata().getName());

		}
		catch (KubernetesClientException e) {
			if (e.getStatus().getCode() == 409) {
				return new ModelActionesponse("Parser already exist", "DANGER");
			}
			else {
				deleteParserModel(serviceName);
				throw e;
			}
		} catch (Exception e) {
			deleteParserModel(parserName);
			throw e;
		}

		return new ModelActionesponse("Model deploy started", "SUCCESS");

	}

	@DELETE
	@Path("/delete-parser-model/{parserName}")
	public ModelActionesponse deleteParserModel(@PathParam("parserName") String parserName) throws KubernetesClientException {

		String serviceName = parserName.toLowerCase() + "-"  + getTenant();

		_kubernetesClient.apps()
			.deployments()
			.inNamespace(namespace).withName(serviceName)
			.delete();

		logger.info(
			"Deleted deployment with name " + serviceName);

		_kubernetesClient.services()
			.inNamespace(namespace).withName(serviceName).delete();

		logger.info(
			"Deleted service with name " + serviceName);

		return new ModelActionesponse("Model deleted", "SUCCESS");

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get/pod/{parserName}")
	public List<PodResponse> getParserStatus(@PathParam("parserName") String parserName) {

		return _kubernetesClient.pods().inNamespace(namespace)
			.list().getItems().stream().map(pod -> {

				PodResponse podResponse = new PodResponse();

				String name = getName(pod).replace( "-" + getTenant(), "");;

				if (name.equals(parserName) && Objects.equals(
					pod.getMetadata().getLabels().get("tenant"), getTenant())) {

					podResponse.setServiceName(name);
					podResponse.setPodName(pod.getMetadata().getName());
					podResponse.setStatus(pod.getStatus().getPhase());

					return podResponse;

				}

				return null;

			}).filter(Objects::nonNull).collect(Collectors.toList());
	}


	@Inject
	KubernetesClient _kubernetesClient;

	@Inject
	Logger logger;

	// TODO: passing through tenantRegistry
	@GrpcClient("tenantmanager")
	TenantManagerGrpc.TenantManagerBlockingStub tenantmanager;

}
