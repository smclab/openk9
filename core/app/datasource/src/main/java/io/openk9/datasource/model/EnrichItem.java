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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "enrich_item")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class EnrichItem extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private EnrichItemType type;

	@Column(name = "service_name", nullable = false)
	private String serviceName;

	@Lob
	@Column(name = "validation_script")
	private String validationScript;

	@Lob
	@Column(name = "json_config")
	private String jsonConfig;

	@Column(name = "json_path", nullable = false)
	private String jsonPath;

	@Enumerated(EnumType.STRING)
	@Column(name = "behavior_merge_type", nullable = false)
	private BehaviorMergeType behaviorMergeType;

	public enum EnrichItemType {
		ASYNC, SYNC
	}

	public enum BehaviorMergeType {
		MERGE, REPLACE
	}

}