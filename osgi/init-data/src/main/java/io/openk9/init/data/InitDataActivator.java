package io.openk9.init.data;

import io.openk9.datasource.repository.DatasourceRepository;
import io.openk9.datasource.repository.EnrichItemRepository;
import io.openk9.datasource.repository.EnrichPipelineRepository;
import io.openk9.datasource.repository.TenantRepository;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.EnrichItem;
import io.openk9.model.EnrichPipeline;
import io.openk9.model.Tenant;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.net.URL;

@Component(
	immediate = true,
	service = InitDataActivator.class
)
public class InitDataActivator {

	@Activate
	void activate(BundleContext bundleContext) throws Exception {

		Bundle bundle = bundleContext.getBundle();

		Tenant tenant =
			_jsonFileToObj(bundle, "tenant.json", Tenant.class);

		Datasource datasource =
			_jsonFileToObj(bundle, "datasource.json", Datasource.class);

		EnrichItem enrichItem =
			_jsonFileToObj(bundle, "enrich-item.json", EnrichItem.class);

		EnrichPipeline enrichPipeline =
			_jsonFileToObj(
				bundle, "enrich-pipeline.json", EnrichPipeline.class);

		_disposable =
			_tenantRepository
				.findByVirtualHost(tenant.getVirtualHost())
				.thenEmpty(
					_tenantRepository.insert(tenant)
						.then(
							_datasourceRepository.insert(datasource)
						)
						.then(
							_enrichItemRepository.insert(enrichItem)
						)
						.then(
							_enrichPipelineRepository.insert(enrichPipeline)
						)
						.then(
							Mono.empty()
						)
				)
		.subscribe();

	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private <T> T _jsonFileToObj(
		Bundle bundle, String path, Class<T> clazz) throws Exception {

		URL resource = bundle.getResource(path);

		try (InputStream inputStream = resource.openStream()) {
			byte[] bytes = inputStream.readAllBytes();
			return _jsonFactory.fromJson(new String(bytes), clazz);
		}

	}

	private Disposable _disposable;

	@Reference
	private TenantRepository _tenantRepository;

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private EnrichItemRepository _enrichItemRepository;

	@Reference
	private EnrichPipelineRepository _enrichPipelineRepository;

	@Reference
	private JsonFactory _jsonFactory;

}
