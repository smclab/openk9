package io.openk9.datasource.searcher.parser;

import io.openk9.datasource.model.Tenant;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class ParserContext {
	private List<ParserSearchToken> tokenTypeGroup;
	private BoolQueryBuilder mutableQuery;
	private Tenant currentTenant;
	private JsonObject queryParserConfig;
}
