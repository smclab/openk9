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

package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.web.BucketResource;
import io.openk9.datasource.web.dto.DocTypeFieldResponseDTO;
import io.openk9.datasource.web.dto.SortingResponseDTO;
import io.openk9.datasource.web.dto.TabResponseDTO;
import io.openk9.datasource.web.dto.TemplateResponseDTO;
import io.openk9.datasource.web.dto.TokenTabResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(
	componentModel = "cdi"
)
public interface BucketResourceMapper {

	static List<String> toValues(String value) {
		return value == null ? List.of() : List.of(value);
	}

	List<TemplateResponseDTO> toTemplateResponseDtoList(
		List<DocTypeTemplate> docTypeTemplateList);

	TemplateResponseDTO toTemplateResponseDto(
		DocTypeTemplate docTypeTemplate);

	BucketResource.CurrentBucket toCurrentBucket(Bucket bucket);

	default List<TabResponseDTO> toTabResponseDtoList(List<Tab> tabList) {
		return toTabResponseDtoList(tabList, null, null);
	}

	default List<TabResponseDTO> toTabResponseDtoList(
		List<Tab> tabList,
		Map<Long, Map<String, String>> translations,
		Map<Long, Map<String, String>> sortingsTranslationMaps) {

		if (translations != null) {
			return tabList
				.stream()
				.map(tab -> new TabResponseDTO(
					tab.getName(),
					tab.getTokenTabs()
						.stream()
						.map(this::toTokenTabResponseDto)
						.toList(),
					this.toSortingResponseDtoList(
						new ArrayList<>(tab.getSortings()),
						sortingsTranslationMaps
					),
					translations.get(tab.getId())
				))
				.toList();
		}
		else {
			return tabList
				.stream()
				.map(this::toTabResponseDto)
				.toList();
		}
	}

	default List<SortingResponseDTO> toSortingResponseDtoList(List<Sorting> sortingList) {
		return toSortingResponseDtoList(sortingList, null);
	}

	default List<SortingResponseDTO> toSortingResponseDtoList(
		List<Sorting> sortingList,
		Map<Long, Map<String, String>> translations) {
		if (translations != null) {
			return sortingList
				.stream()
				.map(sorting -> {
					String path = null;
					if (sorting.getDocTypeField() != null) {
						path = sorting.getDocTypeField().getPath();
					}
					return new SortingResponseDTO(
						sorting.getId(),
						sorting.getName(),
						sorting.getType().getValue(),
						path,
						sorting.isDefaultSort(),
						translations.get(sorting.getId())
					);
					}
				)
				.toList();
		}
		else {
			return sortingList
				.stream()
				.map(this::toSortingResponseDTO)
				.toList();
		}
	}

	default List<DocTypeFieldResponseDTO> toDocTypeFieldResponseDtoList(List<DocTypeField> docTypeFieldList) {
		return toDocTypeFieldResponseDtoList(docTypeFieldList, null);
	}

	default List<DocTypeFieldResponseDTO> toDocTypeFieldResponseDtoList(
		List<DocTypeField> docTypeFieldList,
		Map<Long, Map<String, String>> translations) {
		if (translations != null) {
			return docTypeFieldList
				.stream()
				.map(docTypeField -> new DocTypeFieldResponseDTO(
					docTypeField.getPath(),
					docTypeField.getId(),
					docTypeField.getName(),
					translations.get(docTypeField.getId())
				))
				.toList();
		}
		else {
			return docTypeFieldList
				.stream()
				.map((docTypeField -> new DocTypeFieldResponseDTO(
					docTypeField.getPath(),
					docTypeField.getId(),
					docTypeField.getName(),
					null
				)))
				.toList();
		}
	}

	@Mapping(target = "label", source = "name")
	@Mapping(target = "tokens", source = "tokenTabs")
	@Mapping(target = "sortings", source = "sortings")
	TabResponseDTO toTabResponseDto(Tab tab);

	@Mapping(target = "label", source = "name")
	SortingResponseDTO toSortingResponseDTO(Sorting sorting);

	@Mapping(target = "keywordKey", source = "docTypeField.name")
	@Mapping(target = "values", source = "value")
	@Mapping(target = "extra", source = "extraParams")
	TokenTabResponseDTO toTokenTabResponseDto(TokenTab tokenTab);

}
