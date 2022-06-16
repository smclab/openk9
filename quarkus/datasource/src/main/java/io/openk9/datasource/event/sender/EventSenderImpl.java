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
import io.openk9.datasource.event.dto.EventDto;
import io.openk9.datasource.event.storage.Event;
import io.openk9.datasource.event.storage.EventStorageRepository;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class EventSenderImpl implements EventSender {

	@ConfigProperty(
		name = "openk9.events.data.dir.path",
		defaultValue = "./events/data"
	)
	String storageDir;

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

		try {

			Path path = Paths.get(storageDir);

			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

			UUID eventId = UUID.randomUUID();

			byte[] bytes = _compressData(data);

			Path write = Files.write(
				path.resolve(eventId.toString()), bytes);

			eventRepository.storeEvent(
				Event.builder()
					.id(eventId)
					.type(type)
					.groupKey(groupKey)
					.className(className)
					.classPK(classPK)
					.parsingDate(parsingDate)
					.size(bytes.length)
					.dataPath(write.toString())
					.created(LocalDateTime.now())
					.build()
			);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}


	}

	private byte[] _compressData(Object data) {

		if (data == null) {
			return new byte[0];
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

		return Zstd.compress(bytes);

	}

	@Inject
	Logger logger;

	@Inject
	EventStorageRepository eventRepository;



}
