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

package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity(name = EnrichItem.ENTITY_NAME)
@Table(name = EnrichItem.TABLE_NAME)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EnrichItem extends K9Entity {

	public static final String TABLE_NAME = "enrich_item";
	public static final String ENTITY_NAME = "EnrichItem";

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private EnrichItemType type;

	@Column(name = "service_name", nullable = false)
	private String serviceName;

	@Embedded
	private ResourceUri resourceUri;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "script")
	private String script;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;

	@Column(name = "json_path", nullable = false)
	private String jsonPath;

	@Enumerated(EnumType.STRING)
	@Column(name = "behavior_merge_type", nullable = false)
	private BehaviorMergeType behaviorMergeType;

	@Column(name = "request_timeout", nullable = false)
	private Long requestTimeout;

	@Enumerated(EnumType.STRING)
	@Column(name = "behavior_on_error", nullable = false)
	private BehaviorOnError behaviorOnError;


	public enum EnrichItemType {
		HTTP_ASYNC, HTTP_SYNC, GROOVY_SCRIPT
	}

	public enum BehaviorOnError {
		SKIP, FAIL, REJECT
	}

	public enum BehaviorMergeType {
		MERGE, REPLACE
	}

}