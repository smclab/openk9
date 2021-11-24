package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.api.query.parser.Tuple;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = Annotator.class
)
public class StopWordsAnnotator extends BaseAnnotator {

	@Override
	public List<CategorySemantics> annotate_(
		Tuple<Integer> pos, long tenantId, List<Token> tokenList) {

		if (tokenList.size() == 1) {

			Token token = tokenList.get(0);

			if (token.isStopword()) {
				return List.of(
					CategorySemantics.of(
						"$Optional",
						Map.of(),
						pos
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
