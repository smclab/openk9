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

import io.openk9.datasource.graphql.util.JsonObjectAdapter;
import io.openk9.datasource.model.util.K9Entity;
import io.smallrye.graphql.api.AdaptWith;
import io.vertx.core.json.JsonObject;
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
@Cacheable
@Table(name = "enrich_item")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EnrichItem extends K9Entity {

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private EnrichItemType type;

	@Column(name = "service_name", nullable = false)
	private String serviceName;

	@Lob
	@Column(name = "validation_script")
	private String validationScript;

	@Column(name = "json_config")
	@AdaptWith(JsonObjectAdapter.class)
	private JsonObject jsonConfig;

	public enum EnrichItemType {
		ASYNC, SYNC
	}

}