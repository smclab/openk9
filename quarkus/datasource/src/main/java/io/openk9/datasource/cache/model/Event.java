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

package io.openk9.datasource.cache.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.openk9.datasource.cache.config.EventDataSerializableFactory;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.mutiny.sqlclient.Row;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Entity
@Table(name = "event", indexes = {
	@Index(name = "idx_event_type", columnList = "type"),
	@Index(name = "idx_event_groupKey", columnList = "groupKey"),
	@Index(name = "idx_event_className", columnList = "className"),
	@Index(name = "idx_event_classPk", columnList = "classPk")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@Builder
@RegisterForReflection
public class Event
	extends PanacheEntityBase
	implements IdentifiedDataSerializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = ID, nullable = false)
	private UUID id;

	@Column(name = TYPE)
	private String type;

	@Column(name = SIZE)
	private Integer size;

	@Setter(AccessLevel.NONE)
	@Version
	@Column(name = VERSION)
	private Integer version;

	@Column(name = CREATED, nullable = false)
	private LocalDateTime created = LocalDateTime.now();

	@Column(name = PARSING_DATE)
	private LocalDateTime parsingDate;

	@Column(name = GROUP_KEY)
	private String groupKey;

	@Column(name = CLASS_PK)
	private String classPK;

	@Column(name = CLASS_NAME)
	private String className;


	@Override
	public int getFactoryId() {
		return EventDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EventDataSerializableFactory.EVENT_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput objectDataOutput)
		throws IOException {
		objectDataOutput.writeObject(id);
		objectDataOutput.writeObject(created);;
		objectDataOutput.writeString(type);
		objectDataOutput.writeString(groupKey);
		objectDataOutput.writeString(className);
		objectDataOutput.writeString(classPK);
		objectDataOutput.writeObject(parsingDate);
		objectDataOutput.writeObject(size);
		objectDataOutput.writeObject(version);
	}

	@Override
	public void readData(ObjectDataInput objectDataInput) throws IOException {
		id = objectDataInput.readObject();
		created = objectDataInput.readObject();
		type = objectDataInput.readString();
		groupKey = objectDataInput.readString();
		className = objectDataInput.readString();
		classPK = objectDataInput.readString();
		parsingDate = objectDataInput.readObject();
		size = objectDataInput.readObject();
		version = objectDataInput.readObject();
	}

	public static Event from(Row row) {

		EventBuilder builder = Event.builder();

		for (int i = 0; i < row.size(); i++) {
			String columnName = row.getColumnName(i);
			if (columnName.equalsIgnoreCase(ID)) {
				builder.id(row.getUUID(i));
			}
			else if (columnName.equalsIgnoreCase(TYPE)) {
				builder.type(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(SIZE)) {
				builder.size(row.getInteger(i));
			}
			else if (columnName.equalsIgnoreCase(VERSION)) {
				builder.version(row.getInteger(i));
			}
			else if (columnName.equalsIgnoreCase(CREATED)) {
				builder.created(row.getLocalDateTime(i));
			}
			else if (columnName.equalsIgnoreCase(GROUP_KEY)) {
				builder.groupKey(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(CLASS_NAME)) {
				builder.className(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(CLASS_PK)) {
				builder.classPK(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(PARSING_DATE)) {
				builder.parsingDate(row.getLocalDateTime(i));
			}
		}

		return builder.build();

	}

	public static final String TABLE_NAME = "event";
	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String SIZE = "size";
	public static final String VERSION = "version";
	public static final String CREATED = "created";
	public static final String GROUP_KEY = "groupKey";
	public static final String CLASS_NAME = "className";
	public static final String PARSING_DATE = "parsingDate";
	public static final String CLASS_PK = "classPK";

	public static final String DELETE_BY_ID =
		"DELETE FROM " + TABLE_NAME + " WHERE " + ID + " = $1";

	public static final String INSERT_QUERY =
		"INSERT INTO event (id,type,groupKey,className,classPK,parsingDate,created,size) " +
		"VALUES ($1,$2,$3,$4,$5,$6,$7,$8) " +
		"RETURNING id";

	public static final String SELECT_BY_ID =
		"SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = $1";

	public static final String SELECT_ID = "SELECT id FROM " + TABLE_NAME;

	public enum EventSortable {
		TYPE {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getType);
			}
		},
		SIZE {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getSize);
			}
		},
		VERSION {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getVersion);
			}
		},
		CREATED {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getCreated);
			}
		},
		GROUP_KEY {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getGroupKey);
			}
		},
		CLASS_NAME {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getClassName);
			}
		},
		PARSING_DATE {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getParsingDate);
			}
		},
		CLASS_PK {
			@Override
			public Comparator<Event> getComparator() {
				return Comparator.comparing(Event::getClassPK);
			}
		};

		public abstract Comparator<Event> getComparator();

	}
}
