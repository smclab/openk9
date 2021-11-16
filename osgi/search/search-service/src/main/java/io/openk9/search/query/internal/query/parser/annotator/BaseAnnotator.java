package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;

import java.util.Arrays;
import java.util.List;

public abstract class BaseAnnotator implements Annotator {

	public abstract List<CategorySemantics> annotate_(
		long tenantId, String... tokens);


	@Override
	public final List<CategorySemantics> annotate(
		long tenantId, String...tokens) {
		return annotate_(
			tenantId,
			Arrays
				.stream(tokens)
				.filter(token -> Arrays.asList(
					_annotatorConfig.stopWords()).contains(token))
				.toArray(String[]::new));
	}

	protected void setAnnotatorConfig(AnnotatorConfig annotatorConfig) {
		_annotatorConfig = annotatorConfig;
	}

	protected AnnotatorConfig _annotatorConfig;

}
