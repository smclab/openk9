package io.openk9.monitoring.web;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.PrettyLoggable;
import io.openk9.monitoring.PodEventListener;
import io.openk9.monitoring.PodMetricResponse;
import io.openk9.monitoring.dto.PodResponse;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.SseElementType;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/k8s")
public class K8sResource {

	@Inject
	@ConfigProperty(name = "openk9.kubernetes-client.namespace")
	String namespace;

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

	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@SseElementType(MediaType.APPLICATION_JSON)
	@Path("/pods/sse")
	public Multi<PodResponse> getPodsSSE() {

		return podEventListener.getPods().group()
			.by(this::getName)
			.flatMap(nameStream -> nameStream.select().first())
			.map(pod -> {
			PodResponse podResponse = new PodResponse();

			String name = getName(pod);

			podResponse.setServiceName(name);
			podResponse.setPodName(pod.getMetadata().getName());
			podResponse.setStatus(pod.getStatus().getPhase());

			return podResponse;

		});

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get/pods")
	public List<PodResponse> getPods() {

		return _kubernetesClient.pods().inNamespace(namespace)
			.list().getItems().stream().map(pod -> {

				PodResponse podResponse = new PodResponse();

				String name = getName(pod);

				podResponse.setServiceName(name);
				podResponse.setPodName(pod.getMetadata().getName());
				podResponse.setStatus(pod.getStatus().getPhase());

				return podResponse;

			}).collect(Collectors.toList());
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get/metrics/{namespace}/{podName}")
	public PodMetricResponse getPodsMetrics(
		@PathParam("namespace")String namespace,
		@PathParam("podName")String podName) {

		Map<String, Quantity> usage =
			_kubernetesClient.top().pods().inNamespace(namespace)
			.withName(podName).metric()
			.getContainers().get(0).getUsage();

		PodMetricResponse podMetricResponse = new PodMetricResponse();
		podMetricResponse.setCpuUsage(usage.get("cpu").toString());
		podMetricResponse.setRamUsage(usage.get("memory").toString());
		podMetricResponse.setPodName(podName);

		return podMetricResponse;
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/log/{namespace}/{podName}")
	public Response getLog(
		@PathParam("namespace")String namespace,
		@PathParam("podName")String podName,
		@QueryParam("tail") @DefaultValue("-1") int tail) {

		PodResource podResource = _kubernetesClient
			.pods()
			.inNamespace(namespace)
			.withName(podName);

		PrettyLoggable prettyLoggable = podResource;

		if (tail != -1) {
			prettyLoggable = podResource.tailingLines(tail);
		}

		LocalDateTime now = LocalDateTime.now();

		Reader reader = prettyLoggable.getLogReader();

		Response.ResponseBuilder response = Response.ok(reader);

		response.header(
			"Content-Disposition",
			"attachment;filename=" +
			namespace + "-" + podName + "." + now + ".log");

		return response.build();

	}

	@Blocking
	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@Path("/log/sse/{podName}")
	public void getLogSSE(
		@Context SseEventSink sseEventSink, @Context Sse sse,
		@PathParam("podName")String podName,
		@QueryParam("tail") @DefaultValue("1000") int tail) {

		logger.info(namespace);

		LogWatch logWatch = _kubernetesClient
			.pods()
			.inNamespace(namespace)
			.withName(podName)
			.tailingLines(tail)
			.watchLog();

		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(logWatch.getOutput()))) {

			int lastEventId = 0;
			while (!sseEventSink.isClosed()) {
				String line = reader.readLine();
				if (line != null) {
					sseEventSink.send(
						sse.newEventBuilder()
							.data(line)
							.id(Long.toString(lastEventId++))
							.build()
					);
				}
			}

		}
		catch (Exception ioe) {
			logger.warn(ioe.getMessage());
		}

	}


	@Inject
	KubernetesClient _kubernetesClient;

	@Inject
	Logger logger;

	@Inject
	PodEventListener podEventListener;

}