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

package io.openk9.datasource.index.mappings;

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.searcher.util.Utils;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class MappingsUtil {

	private MappingsUtil() {}

	public static Map<String, Object> docTypesToSettings(Collection<DocType> docTypes) {

		List<Analyzer> analyzers =
			docTypes
				.stream()
				.flatMap(Utils::getDocTypeFieldsFrom)
				.map(DocTypeField::getAnalyzer)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		Map<String, Object> analyzerMap = _createAnalyzer(analyzers);

		Map<String, Object> tokenizerMap = _createTokenizer(analyzers);

		Map<String, Object> filterMap = _createFilter(analyzers);

		Map<String, Object> charFilterMap = _createCharFilter(analyzers);

		Map<String, Object> analysis = new LinkedHashMap<>();

		Map<String, Object> index = new LinkedHashMap<>();

		Map<String, Object> highlight = new LinkedHashMap<>();

		highlight.put("max_analyzed_offset", "10000000");

		index.put("highlight", highlight);

		if (!analyzerMap.isEmpty()) {
			analysis.put("analyzer", analyzerMap);
		}

		if (!tokenizerMap.isEmpty()) {
			analysis.put("tokenizer", tokenizerMap);
		}

		if (!filterMap.isEmpty()) {
			analysis.put("filter", filterMap);
		}

		if (!charFilterMap.isEmpty()) {
			analysis.put("char_filter", charFilterMap);
		}

		if (!analysis.isEmpty() || !index.isEmpty() ) {
			Map<String, Object> settingsMap = new LinkedHashMap<>();

			settingsMap.put("analysis", analysis);

			settingsMap.put("index", index);

			return settingsMap;

		}

		return Map.of();

	}

	private static Map<String, Object> _createCharFilter(List<Analyzer> analyzers) {
		return analyzers
			.stream()
			.map(Analyzer::getCharFilters)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.filter(tokenFilter -> StringUtils.isNotBlank(tokenFilter.getJsonConfig()))
			.distinct()
			.collect(
				Collectors.toMap(
					CharFilter::getName,
					charFilter -> new JsonObject(charFilter.getJsonConfig()).getMap())
			);
	}

	private static Map<String, Object> _createFilter(List<Analyzer> analyzers) {
		return analyzers
			.stream()
			.map(Analyzer::getTokenFilters)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.filter(tokenFilter -> StringUtils.isNotBlank(tokenFilter.getJsonConfig()))
			.distinct()
			.collect(
				Collectors.toMap(
					TokenFilter::getName,
					tokenFilter -> new JsonObject(tokenFilter.getJsonConfig()).getMap())
			);
	}

	private static Map<String, Object> _createTokenizer(List<Analyzer> analyzers) {

		return analyzers
			.stream()
			.map(Analyzer::getTokenizer)
			.filter(tokenizer -> tokenizer != null && StringUtils.isNotBlank(tokenizer.getJsonConfig()))
			.distinct()
			.collect(
				Collectors.toMap(
					Tokenizer::getName,
					tokenizer -> new JsonObject(tokenizer.getJsonConfig()).getMap())
			);

	}

	private static Map<String, Object> _createAnalyzer(
		List<Analyzer> analyzers) {

		Map<String, Object> analyzerMap = new LinkedHashMap<>();

		for (Analyzer analyzer : analyzers) {

			Map<String, Object> internalSettings = new LinkedHashMap<>();

			Tokenizer tokenizer = analyzer.getTokenizer();

			if (tokenizer != null) {
				internalSettings.put("tokenizer", tokenizer.getName());
			}

			Set<TokenFilter> tokenFilters = analyzer.getTokenFilters();

			if (tokenFilters != null && !tokenFilters.isEmpty()) {
				internalSettings.put(
					"filter", tokenFilters
						.stream()
						.map(TokenFilter::getName)
						.toList()
				);
			}

			Set<CharFilter> charFilters = analyzer.getCharFilters();

			if (charFilters != null && !charFilters.isEmpty()) {
				internalSettings.put(
					"char_filter", charFilters
						.stream()
						.map(CharFilter::getName)
						.toList()
				);
			}

			String jsonConfig = analyzer.getJsonConfig();

			if (jsonConfig != null) {

				JsonObject jsonObject = new JsonObject(jsonConfig);

				Map<String, Object> map = jsonObject.getMap();

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					if (!internalSettings.containsKey(entry.getKey())) {
						internalSettings.put(entry.getKey(), entry.getValue());
					}
				}

			}

			analyzerMap.put(analyzer.getName(), internalSettings);

		}

		return analyzerMap;
	}

	public static Map<MappingsKey, Object> docTypesToMappings(Collection<DocType> docTypes) {
		return docTypes
			.stream()
			.map(DocType::getDocTypeFields)
			.flatMap(Collection::stream)
			.collect(
				Collectors.collectingAndThen(
					Collectors.toCollection(LinkedList::new),
					MappingsUtil::docTypeFieldsToMappings)
			);
	}

	public static Map<MappingsKey, Object> docTypeFieldToMappings(DocTypeField docTypeField) {
		return docTypeFieldsToMappings(Set.of(docTypeField));
	}

	public static Map<MappingsKey, Object> docTypeFieldsToMappings(
		Collection<DocTypeField> docTypeFields) {

		return createMappings_(
			docTypeFields
				.stream()
				.filter(docTypeField -> docTypeField.getParentDocTypeField() == null)
				.collect(Collectors.toList()),
			new LinkedHashMap<>(),
			MappingsKey.of("properties")
		);
	}

	private static Map<MappingsKey, Object> createMappings_(
		Collection<DocTypeField> docTypeFields,
		Map<MappingsKey, Object> acc,
		MappingsKey nextKey) {

		for (DocTypeField docTypeField : docTypeFields) {

			Map<MappingsKey, Object> current = acc;

			current = visit(nextKey, current);

			if (docTypeField.getParentDocTypeField() == null) {
				DocType docType = docTypeField.getDocType();
				String docTypeName = docType.getName();
				if (!docTypeName.equals("default")) {
					current = visit(MappingsKey.of(docTypeName), current);

					current = visit(MappingsKey.of("properties"), current);
				}
			}

			String fieldName = docTypeField.getFieldName();

			FieldType fieldType = docTypeField.getFieldType();

			boolean isObject = fieldType == FieldType.OBJECT || fieldType == FieldType.I18N;

			if (!isObject) {
				current = visit(MappingsKey.of(fieldName), current);

				current.put(MappingsKey.of("type"), fieldType.getType());

				Analyzer analyzer = docTypeField.getAnalyzer();

				if (analyzer != null) {
					current.put(MappingsKey.of("analyzer"), analyzer.getName());
				}

				String fieldConfig = docTypeField.getJsonConfig();

				if (fieldConfig != null) {
					JsonObject fieldConfigJson = new JsonObject(fieldConfig);
					for (Map.Entry<String, Object> entry : fieldConfigJson) {
						current.putIfAbsent(new MappingsKey(entry.getKey()), entry.getValue());
					}
				}
			}

			Set<DocTypeField> subDocTypeFields = docTypeField.getSubDocTypeFields();

			if (subDocTypeFields != null && !subDocTypeFields.isEmpty()) {
				if (isObject) {
					current = visit(MappingsKey.of(fieldName), current);
				}

				createMappings_(
					subDocTypeFields,
					current,
					isObject
						? MappingsKey.of("properties")
						: MappingsKey.of("fields"));
			}


		}

		return acc;

	}

	private static Map<MappingsKey, Object> visit(MappingsKey nextKey, Map<MappingsKey, Object> current) {
		return (Map<MappingsKey, Object>) current.computeIfAbsent(
			nextKey, k -> new LinkedHashMap<>());
	}

}
