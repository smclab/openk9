package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	property = {
		Constants.SERVICE_RANKING + ":Integer=1000"
	},
	service = Annotator.class
)
public class TokenAnnotator extends BaseAnnotator {

	public TokenAnnotator() {
		super(true, Duration.ofSeconds(30));
	}

	@Override
	public List<CategorySemantics> annotate_(long tenantId, String...tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];
			return List.of(
				CategorySemantics.of(
					"$TOKEN",
					Map.of(
						"tokenType", "TOKEN",
						"value", token
					)
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
