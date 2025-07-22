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

import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.form.FieldValue;
import io.openk9.datasource.model.form.FormField;
import io.openk9.datasource.model.form.FormFieldType;
import io.openk9.datasource.model.form.FormTemplate;

import java.util.EnumMap;
import java.util.Map;

/**
 * Configuration registry for query parser types and their associated DTOs and form templates.
 *
 * <p>This utility class provides centralized access to configuration objects for different
 * query parser types used in the search system. It maintains two primary mappings:
 * <ul>
 *   <li>Query parser type to configuration DTO mappings, useful for Tenant initialization</li>
 *   <li>Query parser type to form template mappings, useful for UI</li>
 * </ul>
 *
 * <p>The class supports the following query parser types:
 * <ul>
 *   <li><strong>DATE</strong> - Handles date-based queries with configurable field matching</li>
 *   <li><strong>DATE_ORDER</strong> - Provides date-based result ordering with scale and boost parameters</li>
 *   <li><strong>ENTITY</strong> - Manages entity-based searches with boost and query condition settings</li>
 *   <li><strong>FILTER</strong> - Enables filtered searches with configurable query types and fuzziness</li>
 *   <li><strong>HYBRID</strong> - Combines multiple search approaches with k-nearest neighbors support</li>
 *   <li><strong>KNN</strong> - Implements k-nearest neighbors search functionality</li>
 *   <li><strong>TEXT</strong> - Handles text-based searches with boost, query types, and fuzziness options</li>
 * </ul>
 *
 * <p>Note: The following query parser types (ACL, AUTOCOMPLETE, DATASOURCE_ID, DOC_TYPE)
 * are recognized but do not have associated configuration DTOs or form templates.
 *
 * @see QueryParserConfig
 * @see QueryParserType
 * @see QueryParserConfigDTO
 * @see FormTemplate
 */
public class QueryParserConfigs {

	public static Map<QueryParserType, QueryParserConfigDTO> DTOs =
		new EnumMap<>(QueryParserType.class);

	public static Map<QueryParserType, FormTemplate> FORM_TEMPLATES =
		new EnumMap<>(QueryParserType.class);

	static {
		for (var type : QueryParserType.values()) {
			switch (type) {
				case ACL, AUTOCOMPLETE, DATASOURCE, DOCTYPE -> {
				}
				case DATE_ORDER -> DTOs.put(type, DateOrder.DTO);
				case DATE -> DTOs.put(type, Date.DTO);
				case ENTITY -> DTOs.put(type, Entity.DTO);
				case FILTER -> DTOs.put(type, Filter.DTO);
				case HYBRID -> DTOs.put(type, Hybrid.DTO);
				case KNN -> DTOs.put(type, Knn.DTO);
				case TEXT -> DTOs.put(type, Text.DTO);
			}

			switch (type) {
				case ACL, AUTOCOMPLETE, DATASOURCE, DOCTYPE -> {
				}
				case DATE_ORDER -> FORM_TEMPLATES.put(type, DateOrder.FORM_TEMPLATE);
				case DATE -> FORM_TEMPLATES.put(type, Date.FORM_TEMPLATE);
				case ENTITY -> FORM_TEMPLATES.put(type, Entity.FORM_TEMPLATE);
				case FILTER -> FORM_TEMPLATES.put(type, Filter.FORM_TEMPLATE);
				case HYBRID -> FORM_TEMPLATES.put(type, Hybrid.FORM_TEMPLATE);
				case KNN -> FORM_TEMPLATES.put(type, Knn.FORM_TEMPLATE);
				case TEXT -> FORM_TEMPLATES.put(type, Text.FORM_TEMPLATE);
			}
		}
	}

	private QueryParserConfigs() {}

	private static class Date {
		private static final String NAME = "Date Query Parser";
		private static final String DESCRIPTION = "Configuration for Date query parser";
		private static final String TYPE = "DATE";
		private static final String JSON_CONFIG = """
			{
				"allFieldsWhenKeywordIsEmpty": true
			}
			""";

		private static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("allFieldsWhenKeywordIsEmpty")
				.label("All Fields When Keyword Is Empty")
				.value(FieldValue.builder()
					.value("true")
					.build()
				)
				.type(FormFieldType.CHECKBOX)
				.build()
			)
			.build();

	}

	private static class DateOrder {
		private static final String NAME = "Date Order Query Parser";
		private static final String DESCRIPTION = "Configuration for Date Order query parser";
		private static final String TYPE = "DATE_ORDER";
		private static final String JSON_CONFIG = """
			{
				"scale": "3650d",
				"boost": 0.1
			}
			""";

		private static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("scale")
				.label("Scale")
				.value(FieldValue.builder()
					.value("3650d")
					.build()
				)
				.type(FormFieldType.TEXT)
				.build()
			)
			.field(FormField.builder()
				.name("boost")
				.label("Boost")
				.value(FieldValue.builder()
					.value(0.1)
					.build())
				.type(FormFieldType.NUMBER)
				.build())
			.build();

	}

	private static class Entity {
		private static final String NAME = "Entity Query Parser";
		private static final String DESCRIPTION = "Configuration for Entity query parser";
		private static final String TYPE = "ENTITY";
		private static final String JSON_CONFIG = """
			{
				"boost": 50.0,
				"queryCondition": "SHOULD",
				"manageEntityName": "true"
			}
			""";

		private static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("boost")
				.label("Boost")
				.value(FieldValue.builder().value(50f).build())
				.type(FormFieldType.NUMBER)
				.build()
			)
			.field(FormField.builder()
				.name("queryCondition")
				.label("Query Condition")
				.value(FieldValue.builder()
					.value("MUST")
					.isDefault(true)
					.build())
				.value(FieldValue.builder().value("SHOULD").build())
				.type(FormFieldType.SELECT)
				.build()
			)
			.field(FormField.builder()
				.name("manageEntityName")
				.label("Manage Entity Name")
				.value(FieldValue.builder().value(true).build())
				.type(FormFieldType.CHECKBOX)
				.build()
			)
			.build();

	}

	private static class Filter {
		private static final String NAME = "Filter Query Parser";
		private static final String DESCRIPTION = "Configuration for Filter query parser";
		private static final String TYPE = "FILTER";
		private static final String JSON_CONFIG = """
			{
				"boost": 1.0,
				"valuesQueryType": "SHOULD",
				"globalQueryType": "MUST",
				"fuzziness": "ZERO",
				"multiMatchType": "MOST_FIELDS",
				"tieBreaker": 0.0
			}
			""";

		private static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("boost")
				.label("Boost")
				.value(FieldValue.builder()
					.value(1.0f)
					.build()
				)
				.type(FormFieldType.NUMBER)
				.build()
			)
			.field(FormField.builder()
				.name("valuesQueryType")
				.label("Values Query Type")
				.value(FieldValue.builder()
					.value("SHOULD")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("MUST")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.field(FormField.builder()
				.name("globalQueryType")
				.label("Global Query Type")
				.value(FieldValue.builder()
					.value("MUST")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("SHOULD")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.field(FormField.builder()
				.name("fuzziness")
				.label("Fuzziness")
				.value(FieldValue.builder()
					.value("ZERO")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("ONE")
					.build()
				)
				.value(FieldValue.builder()
					.value("TWO")
					.build()
				)
				.value(FieldValue.builder()
					.value("AUTO")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.build();

	}

	private static class Hybrid {
		private static final String NAME = "Hybrid Query Parser";
		private static final String DESCRIPTION = "Configuration for Hybrid query parser";
		private static final String TYPE = QueryParserType.HYBRID.name();
		private static final String JSON_CONFIG = """
			{
				"kNeighbors": 2,
				"boost": 1.0,
				"fuzziness": "ZERO"
			}
			""";

		private static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("kNeighbors")
				.label("K Neighbors")
				.value(FieldValue.builder()
					.value(2)
					.build())
				.type(FormFieldType.NUMBER)
				.build()
			)
			.field(FormField.builder()
				.name("boost")
				.label("Boost")
				.value(FieldValue.builder()
					.value(1.0f)
					.build()
				)
				.type(FormFieldType.NUMBER)
				.build()
			)
			.field(FormField.builder()
				.name("fuzziness")
				.label("Fuzziness")
				.value(FieldValue.builder()
					.value("ZERO")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("ONE")
					.build()
				)
				.value(FieldValue.builder()
					.value("TWO")
					.build()
				)
				.value(FieldValue.builder()
					.value("AUTO")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.build();

	}

	private static class Knn {
		private static final String NAME = "KNN Query Parser";
		private static final String DESCRIPTION = "Configuration for Knn query parser";
		private static final String TYPE = QueryParserType.KNN.name();
		private static final String JSON_CONFIG = """
			{
				"kNeighbors": 2
			}
			""";

		private static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("kNeighbors")
				.label("K Neighbors")
				.value(FieldValue.builder().value(2).build())
				.type(FormFieldType.NUMBER)
				.build()
			)
			.build();
	}

	private static class Text {
		private static final String NAME = "Text Query Parser";
		private static final String DESCRIPTION = "Configuration for Text query parser";
		private static final String TYPE = "TEXT";
		private static final String JSON_CONFIG = """
			{
				"boost": 1.0,
				"valuesQueryType": "SHOULD",
				"globalQueryType": "MUST",
				"fuzziness": "ZERO",
				"multiMatchType": "MOST_FIELDS",
				"tieBreaker": 0.0
			}
			""";

		static final QueryParserConfigDTO DTO = QueryParserConfigDTO.builder()
			.name(NAME)
			.description(DESCRIPTION)
			.type(TYPE)
			.jsonConfig(JSON_CONFIG)
			.build();

		private static final FormTemplate FORM_TEMPLATE = FormTemplate.builder()
			.field(FormField.builder()
				.name("boost")
				.label("Boost")
				.value(FieldValue.builder()
					.value(1.0f)
					.build()
				)
				.type(FormFieldType.NUMBER)
				.build()
			)
			.field(FormField.builder()
				.name("valuesQueryType")
				.label("Values Query Type")
				.value(FieldValue.builder()
					.value("SHOULD")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("MUST")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.field(FormField.builder()
				.name("globalQueryType")
				.label("Global Query Type")
				.value(FieldValue.builder()
					.value("MUST")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("SHOULD")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.field(FormField.builder()
				.name("fuzziness")
				.label("Fuzziness")
				.value(FieldValue.builder()
					.value("ZERO")
					.isDefault(true)
					.build()
				)
				.value(FieldValue.builder()
					.value("ONE")
					.build()
				)
				.value(FieldValue.builder()
					.value("TWO")
					.build()
				)
				.value(FieldValue.builder()
					.value("AUTO")
					.build()
				)
				.type(FormFieldType.SELECT)
				.build()
			)
			.build();
	}

}
