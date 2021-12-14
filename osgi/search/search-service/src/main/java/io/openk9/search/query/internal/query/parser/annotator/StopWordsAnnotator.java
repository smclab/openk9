package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = Annotator.class,
	enabled = false
)
@Designate(ocd = AnnotatorConfig.class)
public class StopWordsAnnotator extends BaseAnnotator {

	@Activate
	@Modified
	void activate(AnnotatorConfig annotatorConfig) {
		setAnnotatorConfig(annotatorConfig);
	}

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

}
