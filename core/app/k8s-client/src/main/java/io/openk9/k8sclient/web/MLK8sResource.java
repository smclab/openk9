package io.openk9.k8sclient.web;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.openk9.k8sclient.dto.MlDto;
import io.openk9.k8sclient.dto.MlPodResponse;
import io.openk9.k8sclient.dto.ModelActionesponse;
import io.openk9.k8sclient.dto.PodResponse;
import io.openk9.tenantmanager.grpc.TenantManagerGrpc;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.vertx.core.http.HttpServerRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import java.util.Collections;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import io.openk9.auth.tenant.TenantResolver;

@ApplicationScoped
@ActivateRequestContext
@Path("/k8s")
@RolesAllowed("k9-admin")
public class MLK8sResource {

	@Inject
	HttpServerRequest _request;

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.namespace")
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

	@Inject
	TenantResolver _tenantResolver;

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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get/pods/ml")
	public List<MlPodResponse> getMlPods() {

		TenantResponse tenantResponse = tenantmanager.findTenant(TenantRequest.newBuilder().setVirtualHost(_request.host()).build());

		logger.info(tenantResponse.getSchemaName());

		return _kubernetesClient.pods().inNamespace(namespace)
			.list().getItems().stream().map(pod -> {

				if (Objects.equals(
					pod.getMetadata().getLabels().get("app-type"), "ml")) {

					MlPodResponse mlPodResponse = new MlPodResponse();

					String name = getName(pod);

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
			.replace("/", "")
			.toLowerCase();
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
				.inNamespace(namespace).create(configMap);

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
					.create(deployment);

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

			service = _kubernetesClient.services().inNamespace(namespace).create(service);

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

		String configMapName = name + "-configmap";

		_kubernetesClient.apps()
			.deployments()
			.inNamespace(namespace).withName(name)
			.delete();

		_kubernetesClient.configMaps()
			.inNamespace(namespace).withName(configMapName).delete();

		_kubernetesClient.services()
			.inNamespace(namespace).withName(name).delete();

		return new ModelActionesponse("Model deleted", "SUCCESS");

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/deploy-parser/{parserName}")
	public ModelActionesponse deployParserModel(@PathParam("parserName")String parserName) throws
		KubernetesClientException {

		String serviceName = parserName.toLowerCase();

		try {

			Deployment deployment = new DeploymentBuilder()
				.withNewMetadata()
				.withName(serviceName.toLowerCase())
				.endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withNewTemplate()
				.withNewMetadata()
				.addToLabels(
					"app.kubernetes.io/name", serviceName.toLowerCase())
				.addToLabels("app-type", "parser")
				.endMetadata()
				.withNewSpec()
				.addNewContainer()
				.withName(serviceName.toLowerCase())
				.withImage(dockerRegistry + serviceName + ":latest")
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
				.addToMatchLabels("app.kubernetes.io/name", serviceName.toLowerCase())
				.endSelector()
				.endSpec()
				.build();

			deployment =
				_kubernetesClient.apps().deployments().inNamespace(namespace)
					.create(deployment);

			logger.info("Created deployment: " + deployment.toString());

		}
		catch (KubernetesClientException e) {
			if (e.getStatus().getCode() == 409) {
				return new ModelActionesponse("Deployment already exist", "DANGER");
			}
			else {
				_kubernetesClient.apps()
					.deployments()
					.inNamespace(namespace).withName(serviceName.toLowerCase())
					.delete();
				throw e;
			}
		}

		try {
			Service service = new ServiceBuilder()
				.withNewMetadata()
				.withName(serviceName.toLowerCase())
				.endMetadata()
				.withNewSpec()
				.withSelector(Collections.singletonMap("app.kubernetes.io/name",
					serviceName.toLowerCase()))
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
				_kubernetesClient.services().inNamespace(namespace).create(
					service);

			logger.info(
				"Created service with name " + service.getMetadata().getName());

		}
		catch (KubernetesClientException e) {
			if (e.getStatus().getCode() == 409) {
				return new ModelActionesponse("Service already exist", "DANGER");
			}
			else {
				deleteMlModel(parserName);
				throw e;
			}
		} catch (Exception e) {
			deleteMlModel(parserName);
			throw e;
		}

		return new ModelActionesponse("Model deploy started", "SUCCESS");

	}

	@DELETE
	@Path("/delete-parser-model/{parserName}")
	public ModelActionesponse deleteParserModel(@PathParam("parserName") String parserName) throws KubernetesClientException {

		String serviceName = parserName.toLowerCase();

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

				String name = getName(pod);

				if (name.equals(parserName)) {

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

	@GrpcClient("tenantmanager")
	TenantManagerGrpc.TenantManagerBlockingStub tenantmanager;

}
