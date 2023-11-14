package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.index.query.BoolQueryBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named("FilterQueryParser")
public class FilterQueryParser extends TextQueryParser implements QueryParser {
	@Override
	public String getType() {
		return "FILTER";
	}

	@Override
	protected void doAddTokenClause(
		ParserSearchToken token, JsonObject jsonConfig, BoolQueryBuilder mutableQuery,
		BoolQueryBuilder tokenClauseBuilder) {

		QueryType.FILTER.useConfiguredQueryType(mutableQuery, tokenClauseBuilder);
	}

}
