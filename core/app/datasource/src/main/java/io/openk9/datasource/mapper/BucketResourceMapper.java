package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.web.BucketResource;
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
	TokenTabResponseDTO toTokenTabResponseDto(TokenTab tokenTab);

	static List<String> toValues(String value) {
		return value == null ? List.of() : List.of(value);
	}

}
