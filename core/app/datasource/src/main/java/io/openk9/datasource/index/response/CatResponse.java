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

package io.openk9.datasource.index.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CatResponse {
	private String health;
	private String status;
	private String index;
	private String uuid;
	private String pri;
	private String rep;
	private String docsCount;
	private String docsDeleted;
	private long storeSize;
	private long priStoreSize;

	@JsonProperty(value = "docs.count")
	public void setDocsCount(String docsCount) {
		this.docsCount = docsCount;
	}

	@JsonProperty(value = "docs.deleted")
	public void setDocsDeleted(String docsDeleted) {
		this.docsDeleted = docsDeleted;
	}

	@JsonProperty(value = "store.size")
	public void setStoreSize(long storeSize) {
		this.storeSize = storeSize;
	}

	@JsonProperty(value = "pri.store.size")
	public void setPriStoreSize(long priStoreSize) {
		this.priStoreSize = priStoreSize;
	}

}
