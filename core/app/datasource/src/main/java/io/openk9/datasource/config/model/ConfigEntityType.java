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
 * {@link ConfigPackage}, paired with the typed DTO used as {@code attributes}
 * (ADR-0003 §3c). The existing base DTO is reused where complete; a dedicated
 * {@code *Representation} is used where the entity has no DTO (join entities)
 * or the base DTO is incomplete.
 * <p>
 * Runtime entities ({@code DataIndex}, {@code Scheduler}, {@code FileResource},
 * {@code Translation*}) are intentionally absent: configuration is portable,
 * runtime state is environment-bound.
 */
public enum ConfigEntityType {

	BUCKET(BucketDTO.class),
	DATASOURCE(DatasourceDTO.class),
	PLUGIN_DRIVER(PluginDriverDTO.class),
	ENRICH_PIPELINE(EnrichPipelineDTO.class),
	ENRICH_PIPELINE_ITEM(EnrichPipelineItemRepresentation.class),
	ENRICH_ITEM(EnrichItemDTO.class),
	DOC_TYPE(DocTypeDTO.class),
	DOC_TYPE_FIELD(DocTypeFieldDTO.class),
	DOC_TYPE_TEMPLATE(DocTypeTemplateDTO.class),
	ANALYZER(AnalyzerDTO.class),
	CHAR_FILTER(CharFilterDTO.class),
	TOKEN_FILTER(TokenFilterDTO.class),
	TOKENIZER(TokenizerDTO.class),
	ACL_MAPPING(AclMappingRepresentation.class),
	QUERY_ANALYSIS(QueryAnalysisDTO.class),
	ANNOTATOR(AnnotatorDTO.class),
	RULE(RuleDTO.class),
	QUERY_PARSER_CONFIG(QueryParserConfigDTO.class),
	SEARCH_CONFIG(SearchConfigDTO.class),
	EMBEDDING_MODEL(EmbeddingModelDTO.class),
	LARGE_LANGUAGE_MODEL(LargeLanguageModelDTO.class),
	LANGUAGE(LanguageDTO.class),
	RAG_CONFIGURATION(CreateRAGConfigurationDTO.class),
	SUGGESTION_CATEGORY(SuggestionCategoryDTO.class),
	TAB(TabDTO.class),
	SORTING(SortingDTO.class),
	TOKEN_TAB(TokenTabDTO.class),
	AUTOCOMPLETE(AutocompleteDTO.class),
	AUTOCORRECTION(AutocorrectionDTO.class);

	private final Class<?> attributesType;

	ConfigEntityType(Class<?> attributesType) {
		this.attributesType = attributesType;
	}

	public Class<?> getAttributesType() {
		return attributesType;
	}

}
