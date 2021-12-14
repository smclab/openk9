package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

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
@Designate(ocd = AnnotatorConfig.class)
public class TokenAnnotator extends BaseAnnotator {

	@Activate
	@Modified
	void activate(AnnotatorConfig annotatorConfig) {
		setAnnotatorConfig(annotatorConfig);
	}

	@Override
	public List<CategorySemantics> annotate(
		long tenantId, Set<String> context, String... tokens) {

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
						)
					)
				);
			}

		}

		return List.of();
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
						"value", token,
						"score", 1.0f
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

}
