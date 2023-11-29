package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.web.BucketResource;
import io.openk9.datasource.web.dto.DocTypeFieldResponseDTO;
import io.openk9.datasource.web.dto.TabResponseDTO;
import io.openk9.datasource.web.dto.TemplateResponseDTO;
import io.openk9.datasource.web.dto.TokenTabResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(
	componentModel = "cdi"
)
public interface BucketResourceMapper {

	List<TemplateResponseDTO> toTemplateResponseDtoList(
		List<DocTypeTemplate> docTypeTemplateList);

	TemplateResponseDTO toTemplateResponseDto(
		DocTypeTemplate docTypeTemplate);

	BucketResource.CurrentBucket toCurrentBucket(Bucket bucket);

	default List<TabResponseDTO> toTabResponseDtoList(List<Tab> tabList) {
		return toTabResponseDtoList(tabList, null);
	}

	default List<TabResponseDTO> toTabResponseDtoList(List<Tab> tabList, Map<Long, Map<String, String>> translations) {
		if (translations != null) {
			return tabList
				.stream()
				.map(tab -> new TabResponseDTO(
					tab.getName(),
					tab.getTokenTabs()
						.stream()
						.map(this::toTokenTabResponseDto)
						.toList(),
					translations.get(tab.getId()))
				)
				.toList();
		}
		else {
			return tabList
				.stream()
				.map(this::toTabResponseDto)
				.toList();
		}
	}

	default List<DocTypeFieldResponseDTO> toDocTypeFieldResponseDtoList(List<DocTypeField> docTypeFieldList) {
		return toDocTypeFieldResponseDtoList(docTypeFieldList, null);
	}

	default List<DocTypeFieldResponseDTO> toDocTypeFieldResponseDtoList(List<DocTypeField> docTypeFieldList, Map<Long, Map<String, String>> translations) {
		if (translations != null) {
			return docTypeFieldList
				.stream()
				.map(docTypeField -> new DocTypeFieldResponseDTO(
					docTypeField.getPath(),
					docTypeField.getId(),
					docTypeField.getName(),
					translations.get(docTypeField.getId()))
				)
				.toList();
		}
		else {
			return docTypeFieldList
				.stream()
				.map((docTypeField -> new DocTypeFieldResponseDTO(
					docTypeField.getPath(),
					docTypeField.getId(),
					docTypeField.getName(),
					null)))
				.toList();
		}
	}

	@Mapping(
		target = "label", source = "name"
	)
	@Mapping(
		target = "tokens", source = "tokenTabs"
	)
	TabResponseDTO toTabResponseDto(Tab tab);

	@Mapping(
		target = "keywordKey", source = "docTypeField.fieldName"
	)
	@Mapping(
		target = "values", source = "value"
	)
	@Mapping(
		target = "extra", source = "extraParams"
	)
	TokenTabResponseDTO toTokenTabResponseDto(TokenTab tokenTab);

	static List<String> toValues(String value) {
		return value == null ? List.of() : List.of(value);
	}

}
