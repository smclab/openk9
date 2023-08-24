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
				.flatMap(Utils::getDocTypeFieldsAndChildrenFrom)
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

	public static Map<MappingsKey, Object> docTypeFieldsToMappings(Collection<DocTypeField> docTypeFields) {
		return createMappings_(docTypeFields, new LinkedHashMap<>(), "", new MappingsKey("properties"));
	}

	private static Map<MappingsKey, Object> createMappings_(
		Collection<DocTypeField> docTypeFields,
		Map<MappingsKey, Object> acc, String accPath, MappingsKey nextKey) {

		for (DocTypeField docTypeField : docTypeFields) {

			String fieldName = docTypeField
				.getFieldName()
				.replace(accPath.isEmpty() ? "" : accPath + ".", "");

			FieldType fieldType = docTypeField.getFieldType();

			boolean isObject = fieldType == FieldType.OBJECT || fieldType == FieldType.I18N;

			String[] fieldNamesArray = fieldName.split("\\.");

			Map<MappingsKey, Object> current = acc;

			for (int i = 0; i < fieldNamesArray.length; i++) {

				String currentFieldName = fieldNamesArray[i];

				boolean isLast = i == fieldNamesArray.length - 1;

				current = (Map<MappingsKey, Object>) current.computeIfAbsent(
					nextKey, k -> new LinkedHashMap<>());


				current = (Map<MappingsKey, Object>) current.computeIfAbsent(
					new MappingsKey(currentFieldName), k -> new LinkedHashMap<>());

				if (isLast) {

					if (!isObject) {
						current.put(new MappingsKey("type"), fieldType.getType());

						Analyzer analyzer = docTypeField.getAnalyzer();

						if (analyzer != null) {
							current.put(new MappingsKey("analyzer"), analyzer.getName());
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

					if (subDocTypeFields != null) {
						createMappings_(
							subDocTypeFields,
							current,
							accPath.isEmpty()
								? fieldName
								: String.join(".", accPath, fieldName),
							isObject
								? new MappingsKey("properties")
								: new MappingsKey("fields"));
					}
				}

			}

		}

		return acc;

	}

}
