package io.openk9.plugin.driver.manager.web;

import io.openk9.http.exception.HttpException;
import io.openk9.http.util.BaseEndpointRegister;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeProvider;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriverRegistry;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.FieldBoostDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component(immediate = true, service = PluginDriverEndPoints.class)
public class PluginDriverEndPoints extends BaseEndpointRegister {

	@Activate
	public void activate(BundleContext bundleContext) {

		setBundleContext(bundleContext);

		this.registerEndpoint(
			HttpHandler.get("/{service-driver-name}", this::_findPluginDriverByName),
			HttpHandler.post("/", this::_findPluginDriverByNames),
			HttpHandler.get("/", this::_findAll)
		);

	}

	private Publisher<Void> _findPluginDriverByNames(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() -> {

			Mono<PluginDriverDTOList> response =
				Mono
					.from(httpRequest.aggregateBodyToString())
					.map(json -> _jsonFactory.fromJsonList(json, String.class))
					.map(_pluginDriverRegistry::getPluginDriverList)
					.map(list -> list
						.stream()
						.map(this::_findDocumentType)
						.collect(
							Collectors.collectingAndThen(
								Collectors.toList(),
								PluginDriverDTOList::of
							)
						)
					);

			return Mono.from(_httpResponseWriter.write(httpResponse, response));

		});
	}

	private Publisher<Void> _findPluginDriverByName(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() -> {

			String serviceDriverName =
				httpRequest.pathParam("service-driver-name");

			PluginDriverDTO response =
				_pluginDriverRegistry
					.getPluginDriver(serviceDriverName)
					.map(this::_findDocumentType)
					.orElseThrow(() -> new HttpException(
						204,
						"No Content. PluginDriver not found for serviceDriverName: " +
						serviceDriverName));

			return Mono.from(
				_httpResponseWriter.write(httpResponse, response));

		});
	}

	private Publisher<Void> _findAll(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() ->
			Mono.from(
				_httpResponseWriter.write(
					httpResponse,
					_pluginDriverRegistry
						.getPluginDriverList()
						.stream()
						.map(this::_findDocumentType)
						.collect(
							Collectors.collectingAndThen(
								Collectors.toList(),
								PluginDriverDTOList::of
							)
						)
				)
			)
		);
	}

	private PluginDriverDTO _findDocumentType(PluginDriver pluginDriver) {

		String name = pluginDriver.getName();

		List<DocumentType> documentTypeList =
			_documentTypeProvider.getDocumentTypeList(name);

		if (documentTypeList.isEmpty()) {
			documentTypeList = List.of(
				_documentTypeProvider.getDefaultDocumentType(name));
		}

		List<DocumentTypeDTO> documentTypeDTOS =
			documentTypeList
				.stream()
				.map(DocumentType::getSearchKeywords)
				.map(searchKeywords ->
					DocumentTypeDTO.of(
						searchKeywords
							.stream()
							.map(searchKeyword ->
								SearchKeywordDTO.of(
									searchKeyword.getKeyword(),
									searchKeyword.isText(),
									FieldBoostDTO.of(
										searchKeyword.getFieldBoost().getKey(),
										searchKeyword.getFieldBoost().getValue()
									)
								)
							)
							.collect(Collectors.toList())
					)
				)
			.collect(Collectors.toList());

		return PluginDriverDTO.of(
			pluginDriver.getName(),
			documentTypeDTOS
		);
	}

	@Deactivate
	public void deactivate() {
		this.close();
	}

	@Override
	public String getBasePath() {
		return "/v1/plugin-driver";
	}

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference
	private DocumentTypeProvider _documentTypeProvider;

	@Reference
	private JsonFactory _jsonFactory;

}
