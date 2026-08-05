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

package io.openk9.datasource.config.model;

import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.Highlight;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.base.AnalyzerDTO;
import io.openk9.datasource.model.dto.base.AnnotatorDTO;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.CharFilterDTO;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.DocTypeTemplateDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.EnrichItemDTO;
import io.openk9.datasource.model.dto.base.EnrichPipelineDTO;
import io.openk9.datasource.model.dto.base.HighlightDTO;
import io.openk9.datasource.model.dto.base.LanguageDTO;
import io.openk9.datasource.model.dto.base.LargeLanguageModelDTO;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.model.dto.base.QueryAnalysisDTO;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.base.RuleDTO;
import io.openk9.datasource.model.dto.base.SearchConfigDTO;
import io.openk9.datasource.model.dto.base.SortingDTO;
import io.openk9.datasource.model.dto.base.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.base.TabDTO;
import io.openk9.datasource.model.dto.base.TokenFilterDTO;
import io.openk9.datasource.model.dto.base.TokenTabDTO;
import io.openk9.datasource.model.dto.base.TokenizerDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;

/**
 * Registry of the configuration entity types that can travel in a
 * {@link ConfigPackage}, paired with the JPA entity class and the typed DTO used
 * as {@code attributes} The existing base DTO is reused where
 * complete; a dedicated {@code *Representation} is used where the entity has no
 * DTO (join entities) or the base DTO is incomplete.
 * <p>
 * This is the single source of "what is exportable": the exporter derives the
 * reference graph from these entity classes, and a guard test asserts every
 * persistent entity is either listed here or annotated {@code @ExportIgnore}.
 * <p>
 * Runtime entities ({@code DataIndex}, {@code Scheduler}, {@code Translation})
 * are intentionally absent: configuration is portable,
 * runtime state is environment-bound.
 */
public enum ConfigEntityType {

	BUCKET(Bucket.class, BucketDTO.class),
	DATASOURCE(Datasource.class, DatasourceDTO.class),
	PLUGIN_DRIVER(PluginDriver.class, PluginDriverDTO.class),
	ENRICH_PIPELINE(EnrichPipeline.class, EnrichPipelineDTO.class),
	ENRICH_PIPELINE_ITEM(
		EnrichPipelineItem.class, EnrichPipelineItemRepresentation.class),
	ENRICH_ITEM(EnrichItem.class, EnrichItemDTO.class),
	DOC_TYPE(DocType.class, DocTypeDTO.class),
	DOC_TYPE_FIELD(DocTypeField.class, DocTypeFieldDTO.class),
	DOC_TYPE_TEMPLATE(DocTypeTemplate.class, DocTypeTemplateDTO.class),
	ANALYZER(Analyzer.class, AnalyzerDTO.class),
	CHAR_FILTER(CharFilter.class, CharFilterDTO.class),
	TOKEN_FILTER(TokenFilter.class, TokenFilterDTO.class),
	TOKENIZER(Tokenizer.class, TokenizerDTO.class),
	ACL_MAPPING(AclMapping.class, AclMappingRepresentation.class),
	QUERY_ANALYSIS(QueryAnalysis.class, QueryAnalysisDTO.class),
	ANNOTATOR(Annotator.class, AnnotatorDTO.class),
	RULE(Rule.class, RuleDTO.class),
	QUERY_PARSER_CONFIG(QueryParserConfig.class, QueryParserConfigDTO.class),
	SEARCH_CONFIG(SearchConfig.class, SearchConfigDTO.class),
	EMBEDDING_MODEL(EmbeddingModel.class, EmbeddingModelDTO.class),
	LARGE_LANGUAGE_MODEL(LargeLanguageModel.class, LargeLanguageModelDTO.class),
	LANGUAGE(Language.class, LanguageDTO.class),
	RAG_CONFIGURATION(RAGConfiguration.class, CreateRAGConfigurationDTO.class),
	SUGGESTION_CATEGORY(SuggestionCategory.class, SuggestionCategoryDTO.class),
	TAB(Tab.class, TabDTO.class),
	SORTING(Sorting.class, SortingDTO.class),
	TOKEN_TAB(TokenTab.class, TokenTabDTO.class),
	AUTOCOMPLETE(Autocomplete.class, AutocompleteDTO.class),
	AUTOCORRECTION(Autocorrection.class, AutocorrectionDTO.class),
	HIGHLIGHT(Highlight.class, HighlightDTO.class);

	private final Class<?> entityType;
	private final Class<?> attributesType;

	ConfigEntityType(Class<?> entityType, Class<?> attributesType) {
		this.entityType = entityType;
		this.attributesType = attributesType;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public Class<?> getAttributesType() {
		return attributesType;
	}

}
