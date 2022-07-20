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

package io.openk9.datasource.graphql.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.graphql.api.Adapter;
import org.jboss.logging.Logger;

public final class JsonNodeAdapter implements Adapter<JsonNode, String> {
	@Override
	public String to(JsonNode jsonObject) throws Exception {
		return jsonObject == null
			? "{}"
			: jsonObject.toString();
	}

	@Override
	public JsonNode from(String json) throws Exception {
		try {
			return INSTANCE.readTree(json)	;
		}
		catch (Exception e) {
			LOGGER.warn("Invalid JSON: " + json + " error message: " + e.getMessage());
			return INSTANCE.createObjectNode();
		}
	}

	private static final Logger LOGGER = Logger.getLogger(JsonNodeAdapter.class);

	private final static ObjectMapper INSTANCE = new ObjectMapper();

}
