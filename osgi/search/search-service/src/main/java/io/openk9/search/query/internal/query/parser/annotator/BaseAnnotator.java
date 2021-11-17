package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.List;

public abstract class BaseAnnotator implements Annotator {

	public abstract List<CategorySemantics> annotate_(
		long tenantId, String...tokens);

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.matchQuery(field, token);
	}

	@Override
	public final List<CategorySemantics> annotate(
		long tenantId, String...tokens) {

		String[] strings = Arrays
			.stream(tokens)
			.filter(token -> !Arrays.asList(_annotatorConfig.stopWords()).contains(token))
			.toArray(String[]::new);

		if (strings.length == 0) {
			return List.of();
		}
		else {
			return annotate_(tenantId, strings);
		}

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
	}

	protected AnnotatorConfig _annotatorConfig;

}
