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

package io.openk9.datasource.event.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
public class Event {

	private UUID id;
	private String type;
	private Integer size;
	private Integer version;
	@Builder.Default
	private LocalDateTime created = LocalDateTime.now();
	private LocalDateTime parsingDate;
	private String groupKey;
	private String classPK;
	private String className;
	private byte[] data;

	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String SIZE = "size";
	public static final String VERSION = "version";
	public static final String CREATED = "created";
	public static final String GROUP_KEY = "groupKey";
	public static final String CLASS_NAME = "className";
	public static final String PARSING_DATE = "parsingDate";
	public static final String CLASS_PK = "classPK";

	public enum EventSortable {

		TYPE(Event.TYPE),
		SIZE(Event.SIZE),
		VERSION(Event.VERSION),
		CREATED(Event.CREATED),
		GROUP_KEY(Event.GROUP_KEY),
		CLASS_NAME(Event.CLASS_NAME),
		PARSING_DATE(Event.PARSING_DATE),
		CLASS_PK(Event.CLASS_PK);

		EventSortable(String column) {
			this.column = column;
		}

		private final String column;

		public String getColumn() {
			return column;
		}
	}
}
