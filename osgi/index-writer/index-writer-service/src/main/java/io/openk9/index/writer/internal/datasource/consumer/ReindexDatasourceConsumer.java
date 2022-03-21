package io.openk9.index.writer.internal.datasource.consumer;

import io.openk9.datasource.event.consumer.api.DatasourceEventConsumer;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.search.client.api.ReactorActionListener;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Instant;

@Component(
	immediate = true,
	service = ReindexDatasourceConsumer.class
)
public class ReindexDatasourceConsumer {

	@Activate
	void activate() {

		RestHighLevelClient restHighLevelClient =
			_restHighLevelClientProvider.get();

		_disposable =
			_datasourceEventConsumer
				.datasourceUpdateEvents()
				.concatMap(datasource ->
					_pluginDriverManagerClient
						.getPluginDriver(
							datasource.getDriverServiceName())
						.onErrorResume(throwable -> {
							if (_log.isErrorEnabled()) {
								_log.error(throwable.getMessage());
							}
							return Mono.empty();
						})
						.map(pluginDriverDTO -> Tuples.of(
							datasource,
							datasource.getTenantId() +
							"-" +
							pluginDriverDTO.getName() +
							"-data"
						))
				)
				.filterWhen(t2 ->
					Mono.create(sink ->
						restHighLevelClient
							.indices()
							.existsAsync(
								new GetIndexRequest(t2.getT2()),
								RequestOptions.DEFAULT,
								new ReactorActionListener<>(sink)
							))
				)
				.concatMap(t2 ->
					Mono.<GetSettingsResponse>create(sink ->
						restHighLevelClient
							.indices()
							.getSettingsAsync(
								new GetSettingsRequest()
									.indices(t2.getT2())
									.names("index.creation_date"),
								RequestOptions.DEFAULT,
								new ReactorActionListener<>(sink)
							))
					.map(response -> Tuples.of(
						t2.getT1(),
						t2.getT2(),
						Instant.ofEpochMilli(
							Long.parseLong(
								response.getSetting(
									t2.getT2(), "index.creation_date")))
						)
					)
				)
				.log()
				.filter(t3 -> t3.getT1().getLastIngestionDate().isBefore(t3.getT3()))
				.concatMap(t3 -> Mono.<AcknowledgedResponse>create(sink ->
					restHighLevelClient
						.indices()
						.deleteAsync(
							new DeleteIndexRequest(t3.getT2()),
							RequestOptions.DEFAULT,
							new ReactorActionListener<>(sink)
						))
				)
				.onErrorContinue((throwable, ignore) -> {
					if (_log.isErrorEnabled()) {
						_log.error(throwable.getMessage());
					}
				})
				.subscribe();
	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverManagerClient _pluginDriverManagerClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DatasourceEventConsumer _datasourceEventConsumer;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private static final Logger _log =
		LoggerFactory.getLogger(ReindexDatasourceConsumer.class);

}
