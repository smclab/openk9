package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.api.query.parser.Tuple;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class BaseAnnotator implements Annotator {

	public abstract List<CategorySemantics> annotate_(
		long tenantId, String...tokens);

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.matchQuery(field, token);
	}

	@Override
	public final List<CategorySemantics> annotate(
		long tenantId, Map<Tuple, List<CategorySemantics>> context, String...tokens) {

		String[] strings = Arrays
			.stream(tokens)
			.filter(token -> !Arrays.asList(_annotatorConfig.stopWords()).contains(token))
			.toArray(String[]::new);

		if (strings.length == 0) {
			return List.of();
		}
		else {

			Tuple<String> key = Tuple.of(tokens);

			List<CategorySemantics> categorySemantics = context.get(key);

			if (categorySemantics == null) {
				List<CategorySemantics> response =
					annotate_(tenantId, strings);

				if (!response.isEmpty()) {
					context.put(key, response);
				}

				return response;

			}

			return categorySemantics;
		}

	}

	protected void setAnnotatorConfig(AnnotatorConfig annotatorConfig) {
		_annotatorConfig = annotatorConfig;
	}

	protected AnnotatorConfig _annotatorConfig;

}
