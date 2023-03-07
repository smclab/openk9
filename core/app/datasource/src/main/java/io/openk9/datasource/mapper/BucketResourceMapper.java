package io.openk9.datasource.mapper;

import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.web.dto.TabResponseDTO;
import io.openk9.datasource.web.dto.TemplateResponseDTO;
import io.openk9.datasource.web.dto.TokenTabResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
	componentModel = "cdi"
)
public interface BucketResourceMapper {

	List<TemplateResponseDTO> toTemplateResponseDtoList(
		List<DocTypeTemplate> docTypeTemplateList);

	TemplateResponseDTO toTemplateResponseDto(
		DocTypeTemplate docTypeTemplate);

	List<TabResponseDTO> toTabResponseDtoList(List<Tab> tabList);

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
