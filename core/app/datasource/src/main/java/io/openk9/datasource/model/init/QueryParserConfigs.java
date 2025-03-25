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

package io.openk9.datasource.model.init;


import java.util.Set;

import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;

public class QueryParserConfigs {

	public static Set<QueryParserConfigDTO> INSTANCE = Set.of(
		DateOrder.INSTANCE,
		Entity.INSTANCE,
		Text.INSTANCE,
		Filter.INSTANCE,
		Date.INSTANCE,
		Doctype.INSTANCE
	);

	private QueryParserConfigs() {}

	public static class DateOrder {
		private static final String NAME = "Date Order Query Parser";
		private static final String DESCRIPTION = "Configuration for Date Order query parser";
		private static final String TYPE = "DATE_ORDER";
		private static final String JSON_CONFIG = """
			{
				"scale": "3650d",
				"boost": 50
			}
			""";

		static final QueryParserConfigDTO INSTANCE = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Entity {
		private static final String NAME = "Entity Query Parser";
		private static final String DESCRIPTION = "Configuration for Entity query parser";
		private static final String TYPE = "ENTITY";
		private static final String JSON_CONFIG = """
			{
				"boost": 50,
				"queryCondition": "SHOULD",
				"manageEntityName": "true"
			}
			""";

		static final QueryParserConfigDTO INSTANCE = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Text {
		private static final String NAME = "Text Query Parser";
		private static final String DESCRIPTION = "Configuration for Text query parser";
		private static final String TYPE = "TEXT";
		private static final String JSON_CONFIG = """
			{
				"boost": 50,
				"valuesQueryType": "MUST",
				"globalQueryType": "MUST",
				"fuzziness":"ZERO"
			}
			""";

		static final QueryParserConfigDTO INSTANCE = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Filter {
		private static final String NAME = "Filter Query Parser";
		private static final String DESCRIPTION = "Configuration for Filter query parser";
		private static final String TYPE = "FILTER";
		private static final String JSON_CONFIG = """
			{
				"boost": 50,
				"valuesQueryType": "MUST",
				"globalQueryType": "MUST",
				"fuzziness":"ZERO"
			}
			""";

		static final QueryParserConfigDTO INSTANCE = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Date {
		private static final String NAME = "Date Query Parser";
		private static final String DESCRIPTION = "Configuration for Date query parser";
		private static final String TYPE = "DATE";
		private static final String JSON_CONFIG = "{}";

		static final QueryParserConfigDTO INSTANCE = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

	public static class Doctype {
		private static final String NAME = "Doctype Query Parser";
		private static final String DESCRIPTION = "Configuration for Doctype query parser";
		private static final String TYPE = "DOCTYPE";
		private static final String JSON_CONFIG = "{}";

		static final QueryParserConfigDTO INSTANCE = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

	}

}
