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

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.search.sort.SortOrder;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RegisterForReflection
public class EventDTO {
	private UUID id;
	private String type;
	private Integer size;
	private Integer version;
	private LocalDateTime created;
	private LocalDateTime parsingDate;
	private String groupKey;
	private String classPK;
	private String className;

	public enum EventSortable {
		TYPE {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getType);
			}

		},
		SIZE {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getSize);
			}
		},
		VERSION {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getVersion);
			}
		},
		CREATED {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getCreated);
			}
		},
		GROUP_KEY {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getGroupKey);
			}
		},
		CLASS_NAME {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getClassName);
			}
		},
		PARSING_DATE {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getParsingDate);
			}
		},
		CLASS_PK {
			@Override
			public Comparator<EventDTO> getComparator() {
				return Comparator.comparing(EventDTO::getClassPK);
			}
		};

		public abstract Comparator<EventDTO> getComparator();

		public Comparator<EventDTO> getComparator(SortOrder sortOrder) {
			return sortOrder == SortOrder.ASC
				? getComparator()
				: getComparator().reversed();
		}

	}

}
