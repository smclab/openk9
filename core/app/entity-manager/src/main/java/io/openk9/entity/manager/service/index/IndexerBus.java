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

package io.openk9.entity.manager.service.index;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.jboss.logging.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;

@ApplicationScoped
public class IndexerBus {

	public void emit(DocWriteRequest<?> request) {
		_many.emitNext(request, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	@PostConstruct
	public void init() {
		_many =
			Sinks
				.unsafe()
				.many()
				.unicast()
				.onBackpressureBuffer();

		_disposable = _many
			.asFlux()
			.groupBy(DocWriteRequest::index)
			.flatMap(group -> group.bufferTimeout(
				100, Duration.ofMillis(5_000)))
			.flatMap(docWriteRequestList ->
				Flux.<BulkItemResponse>create(
					sink -> _restHighLevelClient
						.bulkAsync(
							new BulkRequest().add(docWriteRequestList),
							RequestOptions.DEFAULT,
							new ActionListener<>() {
								@Override
								public void onResponse(
									BulkResponse bulkItemResponses) {
									for (BulkItemResponse item : bulkItemResponses.getItems()) {
										if (item.isFailed()) {
											onFailure(
												item.getFailure().getCause());
										}
										else {
											sink.next(item);
										}
									}

									sink.complete();
								}

								@Override
								public void onFailure(Exception e) {
									sink.error(e);
								}
							})
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

	}

	@PreDestroy
	public void destroy() {
		_disposable.dispose();
		_many.tryEmitComplete();
	}

	private void _manageExceptions(Throwable throwable, Object object) {

		if (_log.isEnabled(Logger.Level.ERROR)) {
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

	private Sinks.Many<DocWriteRequest<?>> _many;
	private Disposable _disposable;

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	Logger _log;

}
