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

package io.openk9.datasource.event.sender;

import io.openk9.datasource.event.dto.EventDto;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EventSenderImpl implements EventSender {

	@PostConstruct
	void init() {

		logger.info("Initializing EventSender");

		_many = Sinks.unsafe().many().unicast().onBackpressureBuffer();

		_disposable = _many
			.asFlux()
			.map(this::_toTuple)
			.bufferTimeout(100, Duration.ofSeconds(5))
			.flatMap(this::_insertEvents)
			.subscribe();

	}

	@PreDestroy
	void destroy() {
		logger.info("Destroying EventSender");
		_disposable.dispose();
		_many.tryEmitComplete();
	}

	@Override
	public void sendEvent(EventDto event) {
		sendEventAsJson(
			event.getType(), event.getGroupKey(), event.getClassName(),
			event.getData());
	}

	@Override
	public void sendEventAsJson(String type, Object data) {
		sendEventAsJson(type, null, null, data);
	}

	@Override
	public void sendEventAsJson(String type, String groupKey, Object data) {
		sendEventAsJson(type, groupKey, null, data);
	}

	@Override
	public void sendEventAsJson(
		String type, String groupKey, String className, Object data) {

		sendEventAsJson(type, groupKey, className, null, null, data);

	}

	@Override
	public void sendEventAsJson(
		String type, String groupKey, String className, String classPK,
		Object data) {

		sendEventAsJson(type, groupKey, className, classPK, null, data);

	}

	@Override
	public void sendEventAsJson(
		String type, String groupKey, String className, String classPK,
		LocalDateTime parsingDate, Object data) {

		_many.tryEmitNext(
			EventMessage
				.builder()
				.type(type)
				.groupKey(groupKey)
				.className(className)
				.classPK(classPK)
				.parsingDate(parsingDate)
				.data(data)
				.build()
		);

	}

	private Publisher<Void> _insertEvents(List<Tuple> tupleList) {

		logger.info("Inserting events size: " + tupleList.size());

		return client.preparedQuery(INSERT_QUERY)
			.executeBatch(tupleList)
			.replaceWithVoid()
			.toMulti();

	}

	private Tuple _toTuple(EventMessage eventMessage) {

		Object objData = eventMessage.getData();

		String data = _getData(objData);

		return Tuple.from(
			List.of(
				UUID.randomUUID(),
				eventMessage.getType(),
				eventMessage.getGroupKey(),
				eventMessage.getClassName(),
				eventMessage.getClassPK(),
				eventMessage.getParsingDate(),
				"{}"/*data*/,
				LocalDateTime.now(),
				data.length()
			)
		);

	}

	private String _getData(Object objData) {
		String data;
		if (objData == null) {
			data = "";
		}
		else if (objData instanceof String) {
			data = (String) objData;
		}
		else if (objData instanceof JsonObject) {
			data = objData.toString();
		}
		else {
			data = Json.encode(objData);
		}
		return data;
	}

	@Inject
	PgPool client;

	@Inject
	Logger logger;

	private Sinks.Many<EventMessage> _many;

	private Disposable _disposable;

	public static final String INSERT_QUERY =
		"INSERT INTO event (id,type,groupKey,className,classPK,parsingDate,data,created,size) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9) RETURNING id";

}
