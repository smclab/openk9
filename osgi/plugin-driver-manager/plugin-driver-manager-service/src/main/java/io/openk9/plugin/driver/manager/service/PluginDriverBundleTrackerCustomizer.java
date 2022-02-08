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

package io.openk9.plugin.driver.manager.service;

import io.openk9.entity.manager.client.api.EntityManagerClient;
import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.driver.manager.api.BasePluginDriver;
import io.openk9.plugin.driver.manager.api.Constants;
import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactory;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactoryRegistry;
import io.openk9.plugin.driver.manager.api.Field;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.SearchKeyword;
import io.openk9.plugin.driver.manager.api.config.DocumentTypeConfig;
import io.openk9.plugin.driver.manager.api.config.EnrichProcessorConfig;
import io.openk9.plugin.driver.manager.api.config.PluginDriverConfig;
import io.openk9.plugin.driver.manager.api.config.SearchKeywordConfig;
import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.BaseNerEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(
	immediate = true,
	service = PluginDriverBundleTrackerCustomizer.class
)
public class PluginDriverBundleTrackerCustomizer
	implements BundleTrackerCustomizer<Void> {

	@Activate
	void activate(BundleContext bundleContext) {
		_bundleTracker = new BundleTracker<>(
			bundleContext, Bundle.ACTIVE | Bundle.STOPPING,
			this);
		_bundleTracker.open();
	}
	@Deactivate
	void deactivate() {
		_bundleTracker.close();
	}

	@Override
	public Void addingBundle(Bundle bundle, BundleEvent event) {

		if (bundle.getState() != Bundle.ACTIVE) {
			removedBundle(bundle, event, null);
			return null;
		}

		Dictionary<String, String> headers = bundle.getHeaders(null);

		String pluginDriverConfiguration =
			headers.get(Constants.PLUGIN_DRIVER_CONFIGURATION);

		if (pluginDriverConfiguration == null) {
			pluginDriverConfiguration = "plugin-driver-config.json";
		}

		URL entry = bundle.getEntry(pluginDriverConfiguration);

		if (entry == null) {
			return null;
		}

		PluginDriverConfig pluginDriverConfig;

		try (InputStream is = entry.openStream()) {

			byte[] bytes = is.readAllBytes();

			pluginDriverConfig =
				_jsonFactory.fromJson(bytes, PluginDriverConfig.class);

		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
			return null;
		}

		String pluginDriverName = pluginDriverConfig.getName();

		String driverServiceName =
			pluginDriverConfig.getDriverServiceName() != null
				? pluginDriverConfig.getDriverServiceName()
				: pluginDriverName;

		boolean schedulerEnabled = pluginDriverConfig.isSchedulerEnabled();

		PluginDriverConfig.Type type = pluginDriverConfig.getType();

		PluginDriver pluginDriver;

		switch (type) {
			case HTTP: {

				Map<String, Object> options = pluginDriverConfig.getOptions();

				if (options == null) {
					options = Map.of();
				}

				String path =(String)options.getOrDefault("path", "");
				String url =(String)options.getOrDefault("url", "");

				Map<String, Object> headersObject =
					(Map<String, Object>)options.getOrDefault(
						"headers", Map.of());

				String method =
					(String)options.getOrDefault("method", "GET");

				List<String> jsonKeys =
					(List<String>)options.getOrDefault("jsonKeys", new String[0]);

				int methodN = _findHttpMethod(method);

				HttpClient httpClient = _httpClientFactory.getHttpClient(url);

				pluginDriver = new BasePluginDriver() {

					@Override
					protected Map<String, Object> headersObject() {
						return headersObject;
					}

					@Override
					protected String[] headers() {
						return new String[0];
					}

					@Override
					protected String path() {
						return path;
					}

					@Override
					protected int method() {
						return methodN;
					}

					@Override
					protected String[] jsonKeys() {
						return jsonKeys.toArray(new String[0]);
					}

					@Override
					protected JsonFactory getJsonFactory() {
						return _jsonFactory;
					}

					@Override
					protected HttpClient getHttpClient() {
						return httpClient;
					}

					@Override
					public String getName() {
						return pluginDriverName;
					}

					@Override
					public boolean schedulerEnabled() {
						return schedulerEnabled;
					}

					@Override
					public String getDriverServiceName() {
						return driverServiceName;
					}
				};
				break;
			}
			default:
				throw new IllegalStateException("type: " + type + " not supported");
		}

		List<AutoCloseables.AutoCloseableSafe> autoCloseableList = new ArrayList<>();

		ServiceRegistration<PluginDriver> pluginDriverServiceRegistration =
			bundle
				.getBundleContext()
				.registerService(
					PluginDriver.class, pluginDriver, null
				);

		autoCloseableList.add(
			AutoCloseables.mergeAutoCloseableToSafe(
				pluginDriverServiceRegistration::unregister)
		);

		for (DocumentTypeConfig documentType : pluginDriverConfig.getDocumentTypes()) {

			DocumentTypeFactory.DefaultDocumentTypeFactory documentTypeFactory =
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriver.getName(),
					documentType.isDefaultDocumentType(),
					DocumentType
						.builder()
						.icon(documentType.getIcon())
						.name(documentType.getName())
						.searchKeywords(
							_searchKeywordConfigToSearchKeyword(
								documentType.getSearchKeywords())
						)
						.sourceFields(
							_mappingsToSourceFields(documentType.getMappings())
						)
						.build()
				);

			autoCloseableList.add(
				_documentTypeFactoryRegistry.register(documentTypeFactory));

		}

		for (EnrichProcessorConfig enrichProcessor :
			pluginDriverConfig.getEnrichProcessors()) {

			String name = enrichProcessor.getName();

			if (name == null || name.isBlank()) {
				_log.warn("name must be specified");
				continue;
			}

			EnrichProcessorConfig.Type typeEP = enrichProcessor.getType();

			if (typeEP == null) {
				_log.warn("name enrichProcessor.type be specified");
				continue;
			}

			Map<String, Object> options = enrichProcessor.getOptions();

			if (options == null) {
				options = Map.of();
			}

			EnrichProcessor enrichProcessorService = null;

			switch (typeEP) {

				case SYNC:

					Map<String, Object> headersEP =
						(Map<String, Object>)options.getOrDefault("headers", Map.of());

					String pathEP =(String)options.getOrDefault("path", "");

					String urlEP =(String)options.getOrDefault("url", "");

					String methodStringEP =(String)options.getOrDefault("method", "GET");

					int methodEP = _findHttpMethod(methodStringEP);

					BaseNerEnrichProcessor baseNerEnrichProcessor =
						new BaseNerEnrichProcessor() {
							@Override
							public String name() {
								return name;
							}

							@Override
							protected Map<String, Object> getHeaders() {
								return headersEP;
							}

							@Override
							protected int getMethod() {
								return methodEP;
							}

							@Override
							protected String getPath() {
								return pathEP;
							}
						};

					baseNerEnrichProcessor.setHttpClient(
						_httpClientFactory.getHttpClient(urlEP));

					baseNerEnrichProcessor.setJsonFactory(_jsonFactory);

					baseNerEnrichProcessor.setEntityManagerClient(
						_entityManagerClient);

					enrichProcessorService = baseNerEnrichProcessor;

					break;
				case ASYNC:

					String destinationName =(String)options.get("destinationName");

					if (destinationName == null || destinationName.isBlank()) {
						_log.warn("destinationName must be specified");
						break;
					}

					enrichProcessorService = new AsyncEnrichProcessor() {
						@Override
						public String destinationName() {
							return destinationName;
						}

						@Override
						public String name() {
							return name;
						}
					};
					break;

				default:
					throw new IllegalStateException("type: " + typeEP + " not supported");
			}

			if (enrichProcessorService != null) {
				ServiceRegistration<EnrichProcessor>
					enrichProcessorServiceRegistration = bundle
					.getBundleContext()
					.registerService(
						EnrichProcessor.class, enrichProcessorService, null
					);

				autoCloseableList.add(
					AutoCloseables
						.mergeAutoCloseableToSafe(
							enrichProcessorServiceRegistration::unregister)
				);
			}

		}

		_registrationMap.put(
			bundle, AutoCloseables
				.mergeAutoCloseableToSafe(autoCloseableList));

		return null;
	}

	private List<Field> _mappingsToSourceFields(Map<String, Object> mappings) {
		return List.of(Field.ofMapping(mappings));
	}

	private List<SearchKeyword> _searchKeywordConfigToSearchKeyword(
		List<SearchKeywordConfig> searchKeywords) {

		List<SearchKeyword> searchKeywordList =
			new ArrayList<>(searchKeywords.size());

		for (SearchKeywordConfig searchKeyword : searchKeywords) {

			Map<String, Object> options = searchKeyword.getOptions();

			if (options == null) {
				options = Map.of();
			}

			Double boost =(Double)options.get("boost");

			String referenceKeyword =(String)options.get("referenceKeyword");

			boolean isBoostable = boost != null;

			switch (searchKeyword.getType()) {
				case DATE:
					searchKeywordList.add(
						isBoostable
							? SearchKeyword.boostDate(
								searchKeyword.getKeyword(),
							"", boost.floatValue())
							: SearchKeyword.date(searchKeyword.getKeyword())
					);
					break;
				case AUTOCOMPLETE:
					searchKeywordList.add(
						SearchKeyword.autocomplete(
							searchKeyword.getKeyword(), referenceKeyword)
					);
					break;
				case NUMBER:
					searchKeywordList.add(
						isBoostable
							? SearchKeyword.boostNumber(
								searchKeyword.getKeyword(), boost.floatValue())
							: SearchKeyword.number(
								searchKeyword.getKeyword())
					);
					break;
				default:
					searchKeywordList.add(
						isBoostable
							? SearchKeyword.boostText(
								searchKeyword.getKeyword(), boost.floatValue())
							: SearchKeyword.text(
								searchKeyword.getKeyword())
					);
					break;
			}

		}

		return searchKeywordList;
	}

	@Override
	public void modifiedBundle(
		Bundle bundle, BundleEvent event, Void nothing) {

		removedBundle(bundle, event, null);

		addingBundle(bundle, event);

	}

	@Override
	public void removedBundle(
		Bundle bundle, BundleEvent event, Void nothing) {

		AutoCloseables.AutoCloseableSafe autoCloseableSafe =
			_registrationMap.remove(bundle);

		if (autoCloseableSafe != null) {
			autoCloseableSafe.close();
		}

	}

	private int _findHttpMethod(String method) {

		switch (method) {
			case "POST":
				return HttpHandler.POST;
			case "PATCH":
				return HttpHandler.PATCH;
			case "PUT":
				return HttpHandler.PUT;
			case "DELETE":
				return HttpHandler.DELETE;
			case "OPTIONS":
				return HttpHandler.OPTIONS;
			default:
				return HttpHandler.GET;

		}

	}

	private BundleTracker<Void> _bundleTracker;

	private final Map<Bundle, AutoCloseables.AutoCloseableSafe> _registrationMap =
		new ConcurrentHashMap<>();

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpClientFactory _httpClientFactory;

	@Reference
	private EntityManagerClient _entityManagerClient;

	@Reference
	private DocumentTypeFactoryRegistry _documentTypeFactoryRegistry;

	private static final Logger _log = LoggerFactory.getLogger(
		PluginDriverBundleTrackerCustomizer.class);

}
