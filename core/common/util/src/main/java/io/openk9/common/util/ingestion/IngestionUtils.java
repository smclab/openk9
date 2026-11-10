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

package io.openk9.common.util.ingestion;

import java.util.Map;

public class IngestionUtils {
	/**
	 * Returns the document types of an ingested content: the top-level keys
	 * of its datasource payload.
	 *
	 * @param datasourcePayload the payload sent by the connector, may be null
	 * @return the document types, empty when the payload is null or empty
	 */
	public static String[] getDocumentTypes(Map<String, Object> datasourcePayload) {
		if (datasourcePayload == null) {
			return new String[0];
		}

		return datasourcePayload.keySet().toArray(new String[0]);
	}

	private IngestionUtils() {}

}
