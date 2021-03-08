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

package io.openk9.search.client.internal;

import io.openk9.json.api.JsonFactory;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.search.client.api.BulkReactorActionListener;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.client.internal.configuration.ElasticSearchConfiguration;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component(immediate = true, service = ElasticSearchIndexer.class)
public class ElasticSearchIndexer {

	@Activate
	public void activate() {

		_dataMany = Sinks.many().unicast().onBackpressureBuffer();

		AutoCloseable dataManyAutoClosable =
			_registerIndexer(
				_dataMany, _elasticSearchConfiguration.bufferMaxSize(),
				_elasticSearchConfiguration.bufferMaxTime());

		_registrationList.add(
			AutoCloseables.mergeAutoCloseableToSafe(
				_closeMany(_dataMany), dataManyAutoClosable));

	}

	@Modified
	public void modified() {
		deactivate();
		activate();
	}

	@Deactivate
	public void deactivate() {
		Iterator<AutoCloseables.AutoCloseableSafe> iterator =
			_registrationList.iterator();

		while (iterator.hasNext()) {
			iterator.next().close();
			iterator.remove();
		}

		_dataMany = null;

	}

	public void sendDocWriteRequest(DocWriteRequest<?> request) {
		_dataMany.emitNext(request, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	private AutoCloseable _registerIndexer(
		Sinks.Many<DocWriteRequest<?>> many, int maxSize, long maxTimeMs) {

		Disposable disposable = many
			.asFlux()
			.groupBy(DocWriteRequest::index)
			.flatMap(group -> group.bufferTimeout(
				maxSize, Duration.ofMillis(maxTimeMs)))
			.flatMap(docWriteRequestList ->
				Flux.<BulkItemResponse>create(
					sink -> _restHighLevelClientProvider
						.get()
						.bulkAsync(
							new BulkRequest().add(docWriteRequestList),
							RequestOptions.DEFAULT,
							new BulkReactorActionListener(sink))
				)
			)
			.doOnNext(bulkItemResponse -> {
				if (_log.isDebugEnabled()) {
					try {
						_log.debug(
							"BulkResponse: " +
							bulkItemResponse.toXContent(
								XContentBuilder.builder(
									XContentType.JSON.xContent()), null));
					}
					catch (IOException e) {
						_log.error(e.getMessage(), e);
					}
				}
			})
			.onErrorContinue(this::_manageExceptions)
			.subscribe();

		return disposable::dispose;
	}

	private void _manageExceptions(Throwable throwable, Object object) {

		if (_log.isErrorEnabled()) {
			if (object == null) {
				_log.error(throwable.getMessage(), throwable);
			}
			else {
				_log.error(
					"error on object: { " + object.toString() + " }",
					throwable);
			}
		}

	}

	private AutoCloseable _closeMany(Sinks.Many many) {
		return () -> many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
	}

	private Sinks.Many<DocWriteRequest<?>> _dataMany;

	private final List<AutoCloseables.AutoCloseableSafe> _registrationList =
		new ArrayList<>();

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference
	private ElasticSearchConfiguration _elasticSearchConfiguration;

	private static final Logger _log = LoggerFactory
		.getLogger(ElasticSearchIndexer.class);

}
