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

package io.openk9.datasource.event.dto;

import io.openk9.datasource.event.model.Event;
import io.openk9.datasource.event.util.Sortable;
import io.vertx.mutiny.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventOption {

	@Builder.Default
	private final String __typename = EventOption.class.getName();
	private String type;
	private String groupKey;
	private String className;

	public static EventOption from(Row row) {

		EventOptionBuilder builder = EventOption.builder();

		for (int i = 0; i < row.size(); i++) {
			String columnName = row.getColumnName(i);
			if (columnName.equalsIgnoreCase(Event.TYPE)) {
				builder.type(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(Event.GROUP_KEY)) {
				builder.groupKey(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(Event.CLASS_NAME)) {
				builder.className(row.getString(i));
			}
		}

		return builder.build();

	}

	public enum EventOptionSortable implements Sortable {
		TYPE(Event.TYPE),
		GROUP_KEY(Event.GROUP_KEY),
		CLASS_NAME(Event.CLASS_NAME);

		EventOptionSortable(String column) {
			this.column = column;
		}

		@Override
		public String getColumn() {
			return column;
		}

		public static EventOptionSortable fromColumn(String column) {
			for (EventOptionSortable sortable : values()) {
				if (sortable.getColumn().equals(column)) {
					return sortable;
				}
			}
			return null;
		}

		private final String column;
	}

}
