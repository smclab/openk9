package io.openk9.datasource.model.dto.request;

import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.base.SearchConfigDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class SearchConfigWithQueryParsersDTO extends SearchConfigDTO {

	private List<QueryParserConfigDTO> queryParsers;
}
