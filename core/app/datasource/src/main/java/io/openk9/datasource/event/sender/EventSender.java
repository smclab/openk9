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

import io.openk9.datasource.event.dto.EventDTO;

import java.time.LocalDateTime;

public interface EventSender {

	void sendEvent(EventDTO event);
	void sendEventAsJson(String type, Object data);

	void sendEventAsJson(String type, String groupKey, Object data);

	void sendEventAsJson(
		String type, String groupKey, String className, Object data);

	void sendEventAsJson(
		String type, String groupKey, String className, String classPK,
		Object data);

	void sendEventAsJson(
		String type, String groupKey, String className, String classPK,
		LocalDateTime parsingDate, Object data);

}
