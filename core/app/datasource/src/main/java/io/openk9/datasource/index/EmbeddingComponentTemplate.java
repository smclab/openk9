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

package io.openk9.datasource.index;

import io.openk9.datasource.util.OpenSearchUtils;

public record EmbeddingComponentTemplate(
	String tenantId, String embeddingModelName, int vectorSize
) {

	public EmbeddingComponentTemplate {

		assert embeddingModelName != null && !embeddingModelName.isEmpty();
		assert tenantId != null && !tenantId.isEmpty();
		assert vectorSize > 0;
	}

	@Override
	public String embeddingModelName() {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return OpenSearchUtils.indexNameSanitizer(
			String.format("%s-%s", tenantId, embeddingModelName));
	}

	@Override
	public String tenantId() {
		throw new UnsupportedOperationException();
	}

}
