package io.openk9.datasource.searcher.parser;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.util.JWT;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class ParserContext {
	private List<ParserSearchToken> tokenTypeGroup;
	private BoolQueryBuilder mutableQuery;
	private Bucket currentTenant;
	private JsonObject queryParserConfig;
	private JWT jwt;
	private Map<String, List<String>> extraParams;
}
