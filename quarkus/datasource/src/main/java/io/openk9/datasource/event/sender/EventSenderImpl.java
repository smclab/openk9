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

import com.github.luben.zstd.Zstd;
import io.openk9.datasource.event.dto.EventDTO;
import io.openk9.datasource.event.model.Event;
import io.openk9.datasource.event.repo.EventRepository;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class EventSenderImpl implements EventSender {

	@Override
	public void sendEvent(EventDTO event) {
		sendEventAsJson(
			event.getType(), event.getGroupKey(), event.getClassName(),
			null);
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

		try {

			UUID id = UUID.randomUUID();

			Tuple2<byte[], Integer> t2 = _compressData(data);

			Event event =
				Event.builder()
					.id(id)
					.type(type)
					.groupKey(groupKey)
					.className(className)
					.classPK(classPK)
					.parsingDate(parsingDate)
					.size(t2.getItem2())
					.created(LocalDateTime.now())
					.data(Base64.getEncoder().encodeToString(t2.getItem1()))
					.build();

			eventRepository.batchSave(event);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private Tuple2<byte[], Integer> _compressData(Object data) {

		if (data == null) {
			return Tuple2.of(new byte[0], 0);
		}

		byte[] bytes;

		if (data instanceof String) {
			bytes =((String)data).getBytes();
		}
		else if (data instanceof byte[]) {
			bytes = (byte[])data;
		}
		else if (data instanceof JsonObject) {
			bytes = data.toString().getBytes();
		}
		else {
			bytes = Json.encode(data).getBytes();
		}

		return Tuple2.of(Zstd.compress(bytes), bytes.length);

	}

	@Inject
	Logger logger;

	@Inject
	EventRepository eventRepository;

}
