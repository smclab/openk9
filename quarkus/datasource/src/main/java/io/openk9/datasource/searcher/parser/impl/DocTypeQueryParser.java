package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.dto.ParserSearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DocTypeQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "DOCTYPE";
	}

	@Override
	public void accept(ParserContext parserContext) {

		List<ParserSearchToken> tokenTypeGroup =
			parserContext.getTokenTypeGroup();

		if (tokenTypeGroup.isEmpty()) {
			return;
		}

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		List<List<String>> typeList =
			tokenTypeGroup
				.stream()
				.map(ParserSearchToken::getValues)
				.toList();

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (List<String> types : typeList) {
			BoolQueryBuilder shouldBool = QueryBuilders.boolQuery();

			for (String type : types) {
				shouldBool
					.should(
						QueryBuilders
							.matchQuery("documentTypes", type)
							.operator(Operator.AND)
					);
			}

			boolQueryBuilder.must(shouldBool);
		}

		mutableQuery.filter(boolQueryBuilder);

	}


}
