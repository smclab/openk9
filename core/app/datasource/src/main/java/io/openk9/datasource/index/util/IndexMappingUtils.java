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

package io.openk9.datasource.index.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.openk9.datasource.index.model.MappingsKey;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.searcher.util.Utils;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

public final class IndexMappingUtils {

	public static final String ANALYSIS = "analysis";

	private static final String ANALYSIS_PREFIX = ANALYSIS + ".";
	private static final String INDEX_PREFIX = "index.";

	private IndexMappingUtils() {
	}

	public static Map<MappingsKey, Object> docTypesToMappings(Collection<DocType> docTypes) {
		return docTypes
			.stream()
			.map(DocType::getDocTypeFields)
			.flatMap(Collection::stream)
			.collect(
				Collectors.collectingAndThen(
					Collectors.toCollection(LinkedList::new),
					IndexMappingUtils::docTypeFieldsToMappings
				)
			);
	}

	public static Map<String, Object> docTypesToSettings(Collection<DocType> docTypes) {

		List<Analyzer> analyzers =
			docTypes
				.stream()
				.flatMap(Utils::getDocTypeFieldsFrom)
				.flatMap(dtf -> Stream.of(dtf.getAnalyzer(), dtf.getSearchAnalyzer()))
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

		Map<String, Object> settingsMap = new LinkedHashMap<>();

		settingsMap.put(ANALYSIS, analysis);

		settingsMap.put("index", index);

		return settingsMap;

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
				if (!docTypeName.equals(DocType.DEFAULT_NAME)) {
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

				switch (docTypeField.getOffsetSource()) {
					case TERM_VECTOR ->
						current.put(MappingsKey.of("term_vector"), "with_positions_offsets");
					case INDEX_OPTIONS ->
						current.put(MappingsKey.of("index_options"), "offsets");
					case NONE -> {
					}
				}

				Analyzer analyzer = docTypeField.getAnalyzer();

				if (analyzer != null) {
					current.put(MappingsKey.of("analyzer"), analyzer.getName());
				}

				Analyzer searchAnalyzer = docTypeField.getSearchAnalyzer();

				if (searchAnalyzer != null) {
					current.put(MappingsKey.of("search_analyzer"), searchAnalyzer.getName());
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

	private static Map<MappingsKey, Object> docTypeFieldToMappings(DocTypeField docTypeField) {
		return docTypeFieldsToMappings(Set.of(docTypeField));
	}

	private static Map<MappingsKey, Object> docTypeFieldsToMappings(
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

	private static Map<MappingsKey, Object> visit(MappingsKey nextKey, Map<MappingsKey, Object> current) {
		return (Map<MappingsKey, Object>) current.computeIfAbsent(
			nextKey, k -> new LinkedHashMap<>());
	}

	/**
	 * Tells whether a live index already carries every analysis definition the
	 * docTypes derive, which is what decides if it has to be closed: the
	 * analysis block is a static setting and cannot be updated while the index
	 * is open.
	 * <p>
	 * The comparison is directional on purpose. A definition the index has and
	 * the docTypes do not know is left alone, because it harms nothing and
	 * could only be removed by closing the index; what the docTypes declare,
	 * instead, has to be there, since the mappings refer to it by name. Both
	 * sides are flattened to dotted keys and compared as text, because
	 * OpenSearch answers every setting as a string while the derived document
	 * keeps the types it was built with.
	 *
	 * @param derivedSettings the settings derived from the docTypes, as
	 *                        {@link #docTypesToSettings} builds them
	 * @param liveSettings the settings the index is running with
	 * @return {@code true} when the index needs no settings update
	 */
	public static boolean isAnalysisApplied(
		Map<String, Object> derivedSettings, JsonObject liveSettings) {

		var derived = flatten(
			new JsonObject(Json.encode(derivedSettings)).getJsonObject(ANALYSIS));

		var live = flatten(liveSettings
			.getJsonObject("index", new JsonObject())
			.getJsonObject(ANALYSIS));

		return live.entrySet().containsAll(derived.entrySet());
	}

	/**
	 * Finds the analysis definitions a settings document touches that the
	 * docTypes already derive.
	 * <p>
	 * The mappings name analyzers, tokenizers and filters by name, and both
	 * halves of an index template are built from the same docTypes: a
	 * definition the docTypes own cannot be set by hand without the two halves
	 * drifting apart. A definition under a name the docTypes do not know is
	 * nobody else's, and is not reported here.
	 * <p>
	 * A definition is matched whatever shape the document uses to name it,
	 * nested or dotted, under {@code analysis} or under {@code index.analysis}.
	 *
	 * @param derivedSettings the settings derived from the docTypes
	 * @param requestedSettings the settings a caller wants to apply
	 * @return the definitions of the docTypes the document touches, as
	 * {@code <category>.<name>}, empty when it touches none
	 */
	public static Set<String> derivedAnalysisNamesIn(
		Map<String, Object> derivedSettings, JsonObject requestedSettings) {

		var derived = flatten(
			new JsonObject(Json.encode(derivedSettings)).getJsonObject(ANALYSIS))
			.keySet()
			.stream()
			.map(IndexMappingUtils::definitionOf)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		return flatten(requestedSettings)
			.keySet()
			.stream()
			.map(key -> key.startsWith(INDEX_PREFIX)
				? key.substring(INDEX_PREFIX.length())
				: key)
			.filter(key -> key.startsWith(ANALYSIS_PREFIX))
			.map(key -> definitionOf(key.substring(ANALYSIS_PREFIX.length())))
			.filter(Objects::nonNull)
			.filter(derived::contains)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Names the definition a flattened analysis key belongs to, which is its
	 * first two segments: the category and the name, as in
	 * {@code analyzer.my_analyzer}.
	 */
	private static String definitionOf(String analysisKey) {

		var segments = analysisKey.split("\\.");

		return segments.length >= 2 ? segments[0] + "." + segments[1] : null;
	}

	/**
	 * Merges the requested settings into the recorded ones the way OpenSearch
	 * merges them into an index, so that what is recorded keeps telling what
	 * the index has: a key the document does not name is left alone, and a key
	 * set to {@code null} is dropped, because that is how OpenSearch is told to
	 * put a setting back to its default.
	 *
	 * @param recorded the settings recorded so far
	 * @param requested the settings a caller wants to apply
	 * @return the settings to record
	 */
	public static JsonObject mergeSettings(
		JsonObject recorded, JsonObject requested) {

		var merged = recorded.copy().mergeIn(requested, true);

		removeNulls(merged);

		return merged;
	}

	private static void removeNulls(JsonObject object) {

		var removed = new LinkedList<String>();

		for (var entry : object) {
			if (entry.getValue() == null) {
				removed.add(entry.getKey());
			}
			else if (entry.getValue() instanceof JsonObject nested) {
				removeNulls(nested);

				// a branch a removal emptied holds no setting, and would
				// otherwise stay in what is recorded for good
				if (nested.isEmpty()) {
					removed.add(entry.getKey());
				}
			}
		}

		removed.forEach(object::remove);
	}

	/**
	 * Strips the analysis block off a settings document, leaving what OpenSearch
	 * accepts while the index is open: a static setting is refused on an open
	 * index even when its value does not change.
	 *
	 * @param settings the settings derived from the docTypes
	 * @return the same settings without their analysis block
	 */
	public static Map<String, Object> withoutAnalysis(Map<String, Object> settings) {

		var remainder = new LinkedHashMap<>(settings);

		remainder.remove(ANALYSIS);

		return remainder;
	}

	private static Map<String, String> flatten(JsonObject object) {

		var flat = new LinkedHashMap<String, String>();

		if (object != null) {
			flatten("", object, flat);
		}

		return flat;
	}

	private static void flatten(
		String prefix, Object value, Map<String, String> flat) {

		if (value instanceof JsonObject object) {
			object.forEach(entry -> flatten(
				prefix.isEmpty()
					? entry.getKey()
					: prefix + "." + entry.getKey(),
				entry.getValue(),
				flat
			));
		}
		else if (value instanceof JsonArray array) {
			for (int i = 0; i < array.size(); i++) {
				flatten(prefix + "." + i, array.getValue(i), flat);
			}
		}
		else {
			flat.put(prefix, String.valueOf(value));
		}
	}

}
