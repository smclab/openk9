package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.api.query.parser.Tuple;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(
	immediate = true,
	property = {
		Constants.SERVICE_RANKING + ":Integer=1000"
	},
	service = Annotator.class
)
public class TokenAnnotator extends BaseAnnotator {

	@Override
	public List<CategorySemantics> annotate(
		Tuple<Integer> pos,
		long tenantId, Set<String> context, String...tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];

			if (!context.contains(token)) {
				return List.of(
					CategorySemantics.of(
						"$TOKEN",
						Map.of(
							"tokenType", "TOKEN",
							"value", token,
							"score", 1.0f
						),
						pos
					)
				);
			}

		}

		return List.of();
	}

	@Override
	public List<CategorySemantics> annotate_(
		Tuple<Integer> pos, long tenantId, List<Token> tokenList) {

		if (tokenList.size() == 1) {
			Token token = tokenList.get(0);
			return List.of(
				CategorySemantics.of(
					"$TOKEN",
					Map.of(
						"tokenType", "TOKEN",
						"value", token.getToken(),
						"score", 1.0f
					),
					pos
				)
			);
		}

		return List.of();

	}

	@Override
	public int weight() {
		return 10;
	}

	@Override
	@Reference
	public void setAnnotatorConfig(
		AnnotatorConfig annotatorConfig) {
		super.setAnnotatorConfig(annotatorConfig);
	}

}
