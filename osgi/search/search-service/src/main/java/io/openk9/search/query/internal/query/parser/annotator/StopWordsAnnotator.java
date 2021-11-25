package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = Annotator.class,
	enabled = false
)
public class StopWordsAnnotator extends BaseAnnotator {

	@Override
	public List<CategorySemantics> annotate_(
		long tenantId, String... tokens) {

		if (tokens.length == 1) {

			String token = tokens[0];

			if (stopWords.contains(token)) {
				return List.of(
					CategorySemantics.of(
						"$Optional",
						Map.of()
					)
				);
			}
		}

		return List.of();
	}

	@Override
	public int weight() {
		return 1;
	}

	@Override
	@Reference
	public void setAnnotatorConfig(
		AnnotatorConfig annotatorConfig) {
		super.setAnnotatorConfig(annotatorConfig);
	}

}
