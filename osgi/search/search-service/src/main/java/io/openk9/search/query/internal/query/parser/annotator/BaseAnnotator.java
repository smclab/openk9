package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Set;

public abstract class BaseAnnotator implements Annotator {

	public abstract List<CategorySemantics> annotate_(
		long tenantId, String...tokens);

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.fuzzyQuery(field, token);
	}

	@Override
	public List<CategorySemantics> annotate(
		long tenantId, Set<String> context, String...tokens) {

		List<CategorySemantics> result = annotate_(tenantId, tokens);

		if (tokens.length == 1) {

			String key = tokens[0];

			context.add(key);

		}

		return result;



	}

	@Override
	public List<CategorySemantics> annotate(
		long tenantId, String...tokens) {
		return annotate_(tenantId, tokens);
	}

	@Override
	public int compareTo(Annotator o) {
		return Integer.compare(this.weight(), o.weight());
	}

	@Override
	public int weight() {
		return 1;
	}

	protected void setAnnotatorConfig(AnnotatorConfig annotatorConfig) {
		_annotatorConfig = annotatorConfig;
		stopWords = List.of(annotatorConfig.stopWords());
	}

	protected AnnotatorConfig _annotatorConfig;
	protected List<String> stopWords;

}
