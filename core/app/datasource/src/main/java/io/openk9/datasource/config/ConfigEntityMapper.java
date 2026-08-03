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

package io.openk9.datasource.config;

import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Analyzer;
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
import io.openk9.datasource.model.ProviderModel;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.Range;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.dto.base.*;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Entity → typed DTO mapping for the export side of the import/export feature.
 * <p>
 * The existing MapStruct mappers only map DTO → entity; this single mapper
 * provides the inverse for every exportable type, populating the {@code attributes}
 * of a {@code ConfigEntity}. The base DTOs are scalar projections, so MapStruct
 * maps only the configuration fields and ignores the entity relationships (carried
 * separately as references). {@code unmappedTargetPolicy = IGNORE} also means the
 * FK-id fields that exist on a few DTOs ({@code AutocompleteDTO.fieldIds},
 * {@code AutocorrectionDTO.autocorrectionDocTypeFieldId}) are intentionally left
 * null here and emitted as references by the collector.
 */
@Mapper(
	componentModel = MappingConstants.ComponentModel.CDI,
	unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ConfigEntityMapper {

	BucketDTO dto(Bucket entity);

	DatasourceDTO dto(Datasource entity);

	PluginDriverDTO dto(PluginDriver entity);

	EnrichPipelineDTO dto(EnrichPipeline entity);

	EnrichPipelineItemRepresentation dto(EnrichPipelineItem entity);

	EnrichItemDTO dto(EnrichItem entity);

	DocTypeDTO dto(DocType entity);

	DocTypeFieldDTO dto(DocTypeField entity);

	DocTypeTemplateDTO dto(DocTypeTemplate entity);

	AnalyzerDTO dto(Analyzer entity);

	CharFilterDTO dto(CharFilter entity);

	TokenFilterDTO dto(TokenFilter entity);

	TokenizerDTO dto(Tokenizer entity);

	AclMappingRepresentation dto(AclMapping entity);

	QueryAnalysisDTO dto(QueryAnalysis entity);

	AnnotatorDTO dto(Annotator entity);

	RuleDTO dto(Rule entity);

	QueryParserConfigDTO dto(QueryParserConfig entity);

	SearchConfigDTO dto(SearchConfig entity);

	EmbeddingModelDTO dto(EmbeddingModel entity);

	LargeLanguageModelDTO dto(LargeLanguageModel entity);

	LanguageDTO dto(Language entity);

	CreateRAGConfigurationDTO dto(RAGConfiguration entity);

	SuggestionCategoryDTO dto(SuggestionCategory entity);

	TabDTO dto(Tab entity);

	SortingDTO dto(Sorting entity);

	TokenTabDTO dto(TokenTab entity);

	AutocompleteDTO dto(Autocomplete entity);

	AutocorrectionDTO dto(Autocorrection entity);

	HighlightDTO dto(Highlight entity);

	// Nested value objects, reused as sub-mappings by the methods above.

	ProviderModelDTO dto(ProviderModel providerModel);

	RangeDTO dto(Range range);

	// Typed DTO → entity mapping for the import side. The importer resolves the
	// entity(<declaredDto>) overload reflectively from the ConfigEntityType, so
	// the mapping copies exactly the DTO the enum declares. This is what makes
	// RAG_CONFIGURATION correct: its declared DTO is CreateRAGConfigurationDTO,
	// which carries the mandatory type; the per-entity K9EntityMapper is typed on
	// the base RAGConfigurationDTO and would silently drop it. Associations are
	// unmapped targets (IGNORE) and (re)wired separately by the importer.

	Bucket entity(BucketDTO dto);

	Datasource entity(DatasourceDTO dto);

	PluginDriver entity(PluginDriverDTO dto);

	EnrichPipeline entity(EnrichPipelineDTO dto);

	EnrichItem entity(EnrichItemDTO dto);

	DocType entity(DocTypeDTO dto);

	DocTypeField entity(DocTypeFieldDTO dto);

	DocTypeTemplate entity(DocTypeTemplateDTO dto);

	Analyzer entity(AnalyzerDTO dto);

	CharFilter entity(CharFilterDTO dto);

	TokenFilter entity(TokenFilterDTO dto);

	Tokenizer entity(TokenizerDTO dto);

	QueryAnalysis entity(QueryAnalysisDTO dto);

	Annotator entity(AnnotatorDTO dto);

	Rule entity(RuleDTO dto);

	QueryParserConfig entity(QueryParserConfigDTO dto);

	SearchConfig entity(SearchConfigDTO dto);

	EmbeddingModel entity(EmbeddingModelDTO dto);

	LargeLanguageModel entity(LargeLanguageModelDTO dto);

	Language entity(LanguageDTO dto);

	RAGConfiguration entity(CreateRAGConfigurationDTO dto);

	SuggestionCategory entity(SuggestionCategoryDTO dto);

	Tab entity(TabDTO dto);

	Sorting entity(SortingDTO dto);

	TokenTab entity(TokenTabDTO dto);

	Autocomplete entity(AutocompleteDTO dto);

	Autocorrection entity(AutocorrectionDTO dto);

	Highlight entity(HighlightDTO dto);

	ProviderModel entity(ProviderModelDTO dto);

	Range entity(RangeDTO dto);

	// Typed DTO → existing-entity update for the overwrite side. Same selection by
	// declared DTO as entity(...), but onto a managed target. The create/update
	// distinction of RAGConfiguration lives in exactly one place: type is immutable,
	// so its update ignores it and an overwrite never rewrites it.

	Bucket update(@MappingTarget Bucket entity, BucketDTO dto);

	Datasource update(@MappingTarget Datasource entity, DatasourceDTO dto);

	PluginDriver update(@MappingTarget PluginDriver entity, PluginDriverDTO dto);

	EnrichPipeline update(@MappingTarget EnrichPipeline entity, EnrichPipelineDTO dto);

	EnrichItem update(@MappingTarget EnrichItem entity, EnrichItemDTO dto);

	DocType update(@MappingTarget DocType entity, DocTypeDTO dto);

	DocTypeField update(@MappingTarget DocTypeField entity, DocTypeFieldDTO dto);

	DocTypeTemplate update(@MappingTarget DocTypeTemplate entity, DocTypeTemplateDTO dto);

	Analyzer update(@MappingTarget Analyzer entity, AnalyzerDTO dto);

	CharFilter update(@MappingTarget CharFilter entity, CharFilterDTO dto);

	TokenFilter update(@MappingTarget TokenFilter entity, TokenFilterDTO dto);

	Tokenizer update(@MappingTarget Tokenizer entity, TokenizerDTO dto);

	QueryAnalysis update(@MappingTarget QueryAnalysis entity, QueryAnalysisDTO dto);

	Annotator update(@MappingTarget Annotator entity, AnnotatorDTO dto);

	Rule update(@MappingTarget Rule entity, RuleDTO dto);

	QueryParserConfig update(@MappingTarget QueryParserConfig entity, QueryParserConfigDTO dto);

	SearchConfig update(@MappingTarget SearchConfig entity, SearchConfigDTO dto);

	EmbeddingModel update(@MappingTarget EmbeddingModel entity, EmbeddingModelDTO dto);

	LargeLanguageModel update(@MappingTarget LargeLanguageModel entity, LargeLanguageModelDTO dto);

	Language update(@MappingTarget Language entity, LanguageDTO dto);

	RAGConfiguration update(@MappingTarget RAGConfiguration entity, RAGConfigurationDTO dto);

	SuggestionCategory update(@MappingTarget SuggestionCategory entity, SuggestionCategoryDTO dto);

	Tab update(@MappingTarget Tab entity, TabDTO dto);

	Sorting update(@MappingTarget Sorting entity, SortingDTO dto);

	TokenTab update(@MappingTarget TokenTab entity, TokenTabDTO dto);

	Autocomplete update(@MappingTarget Autocomplete entity, AutocompleteDTO dto);

	Autocorrection update(@MappingTarget Autocorrection entity, AutocorrectionDTO dto);

	Highlight update(@MappingTarget Highlight entity, HighlightDTO dto);

}
