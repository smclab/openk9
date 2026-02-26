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

package io.openk9.datasource.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
	value = {
		@JsonSubTypes.Type(value = DatasourceMessage.Failure.class, name = "FAILURE"),
		@JsonSubTypes.Type(value = DatasourceMessage.Delete.class, name = "DELETE"),
		@JsonSubTypes.Type(value = DatasourceMessage.New.class, name = "NEW"),
		@JsonSubTypes.Type(value = DatasourceMessage.Update.class, name = "UPDATE")
	}
)
@Data
@SuperBuilder(toBuilder = true)
public abstract class DatasourceMessage {
	protected String ingestionId;
	protected Long datasourceId;
	protected String contentId;
	protected Long parsingDate;
	protected String tenantId;
	protected String indexName;

	@Getter
	@Setter
	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Failure extends DatasourceMessage {
		private String error;
	}

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Delete extends DatasourceMessage { }

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class New extends DatasourceMessage { }

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Update extends DatasourceMessage { }

	@ToString(callSuper = true)
	@SuperBuilder(toBuilder = true)
	@Jacksonized
	public static class Unknown extends DatasourceMessage { }

}
