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

package io.openk9.datasource.event.storage;

import io.openk9.datasource.event.util.SortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
	private UUID id;
	private LocalDateTime created;
	private String type;
	private String groupKey;
	private String className;
	private String classPK;
	private LocalDateTime parsingDate;
	private int dataSize;
	private String dataPath;

	public static final String CLASS_NAME = "className";
	public static final String CLASS_PK = "classPK";
	public static final String GROUP_KEY = "groupKey";
	public static final String TYPE = "type";
	public static final String CREATED = "created";
	public static final String ID = "id";


	public enum EventSortable {
		CLASS_NAME {
			@Override
			public Comparator<Event> getComparator(SortType sortType) {
				Comparator<Event> comparing =
					Comparator.comparing(Event::getClassName);

				if (sortType == SortType.DESC) {
					return comparing.reversed();
				}

				return comparing;
			}
		},
		CLASS_PK {
			@Override
			public Comparator<Event> getComparator(SortType sortType) {
				Comparator<Event> comparing =
					Comparator.comparing(Event::getClassPK);

				if (sortType == SortType.DESC) {
					return comparing.reversed();
				}

				return comparing;
			}
		},
		GROUP_KEY {
			@Override
			public Comparator<Event> getComparator(SortType sortType) {
				Comparator<Event> comparing =
					Comparator.comparing(Event::getGroupKey);

				if (sortType == SortType.DESC) {
					return comparing.reversed();
				}

				return comparing;
			}
		},
		TYPE {
			@Override
			public Comparator<Event> getComparator(SortType sortType) {
				Comparator<Event> comparing =
					Comparator.comparing(Event::getType);

				if (sortType == SortType.DESC) {
					return comparing.reversed();
				}

				return comparing;
			}
		},
		CREATED {
			@Override
			public Comparator<Event> getComparator(SortType sortType) {
				Comparator<Event> comparing =
					Comparator.comparing(Event::getCreated);

				if (sortType == SortType.DESC) {
					return comparing.reversed();
				}

				return comparing;
			}
		},
		ID {
			@Override
			public Comparator<Event> getComparator(SortType sortType) {
				Comparator<Event> comparing =
					Comparator.comparing(Event::getId);

				if (sortType == SortType.DESC) {
					return comparing.reversed();
				}

				return comparing;
			}
		};

		public abstract Comparator<Event> getComparator(SortType sortType);

	}
}
