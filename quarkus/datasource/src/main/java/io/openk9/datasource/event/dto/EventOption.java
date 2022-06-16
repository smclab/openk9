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

import io.openk9.datasource.event.storage.Event;
import io.openk9.datasource.event.util.Sortable;
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
	private String classPK;

	public enum EventOptionSortable implements Sortable {
		TYPE(Event.TYPE),
		GROUP_KEY(Event.GROUP_KEY),
		CLASS_NAME(Event.CLASS_NAME),
		CLASS_PK(Event.CLASS_PK);

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
