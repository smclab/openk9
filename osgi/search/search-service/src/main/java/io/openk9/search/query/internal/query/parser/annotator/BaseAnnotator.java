package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.api.query.parser.Tuple;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseAnnotator implements Annotator {

	public abstract List<CategorySemantics> annotate_(
		Tuple<Integer> chartKey, long tenantId, List<Token> tokenList);

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.fuzzyQuery(field, token);
	}

	@Override
	public List<CategorySemantics> annotate(
		Tuple<Integer> chartKey, long tenantId, Set<String> context, String...tokens) {

		List<Token> tokenList = _getTokenList(tokens);

		List<CategorySemantics> result = annotate_(chartKey, tenantId, tokenList);

		if (tokens.length == 1 && !result.isEmpty()) {

			String key = tokens[0];

			context.add(key);

		}

		return result;

	}

	private List<Token> _getTokenList(String[] tokens) {

		List<Token> tokenList = new ArrayList<>();

		for (String s : tokens) {
			tokenList.add(Token.of(s, stopWords.contains(s)));
		}

		return tokenList;
	}

	@Override
	public List<CategorySemantics> annotate(
		Tuple<Integer> chartKey, long tenantId, String...tokens) {

		List<Token> tokenList = _getTokenList(tokens);

		return annotate_(chartKey, tenantId, tokenList);
	}

	@Override
	public int compareTo(Annotator o) {
		return Integer.compare(this.weight(), o.weight());
	}

	@Override
	public int weight() {
		return 5;
	}

	protected static Tuple<Integer> getPos(
		Tuple<Integer> chartKey, List<Token> tokenList) {

		Integer startPos = chartKey.get(0);

		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			if (!token.isStopword()) {
				startPos += i;
				break;
			}
		}

		Integer endPos = chartKey.get(1);

		for (int i = tokenList.size() - 1; i >= 0; i--) {
			Token token = tokenList.get(i);
			if (!token.isStopword()) {
				endPos = chartKey.get(0) + i;
				break;
			}
		}

		return Tuple.of(startPos, endPos);

	}

	protected void setAnnotatorConfig(AnnotatorConfig annotatorConfig) {
		_annotatorConfig = annotatorConfig;
		stopWords = List.of(annotatorConfig.stopWords());
	}

	protected AnnotatorConfig _annotatorConfig;
	protected List<String> stopWords;

}
