package io.openk9.datasource.mapper;

import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.web.BucketResource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
	componentModel = "cdi"
)
public interface BucketResourceMapper {

	List<BucketResource.TemplateResponseDto> toTemplateResponseDtoList(
		List<DocTypeTemplate> docTypeTemplateList);

	BucketResource.TemplateResponseDto toTemplateResponseDto(
		DocTypeTemplate docTypeTemplate);

	List<BucketResource.TabResponseDto> toTabResponseDtoList(List<Tab> tabList);

	@Mapping(
		target = "label", source = "name"
	)
	BucketResource.TabResponseDto toTabResponseDto(Tab tab);

	@Mapping(
		target = "keywordKey", source = "docTypeField.fieldName"
	)
	@Mapping(
		target = "values", source = "value"
	)
	BucketResource.TokenTabResponseDto toTokenTabResponseDto(TokenTab tokenTab);

	static List<String> toValues(String value) {
		return value == null ? List.of() : List.of(value);
	}

}
