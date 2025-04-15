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

package io.openk9.datasource.index.model;

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.util.OpenSearchUtils;

public record IndexName(String value) {

	public static IndexName from(TenantManager.Tenant tenant, String dataIndexName) {
		return from(tenant.schemaName(), dataIndexName);
	}

	public static IndexName from(TenantManager.Tenant tenant, DataIndex dataIndex) {
		return from(tenant.schemaName(), dataIndex.getName());
	}

	public static IndexName from(String tenantId, DataIndex dataIndex) {
		return from(tenantId, dataIndex.getName());
	}

	public static IndexName from(String tenantId, String dataIndexName) {
		return new IndexName(OpenSearchUtils.indexNameSanitizer(
			String.format("%s-%s", tenantId, dataIndexName)));
	}

	@Override
	public String toString() {
		return value();
	}

}
