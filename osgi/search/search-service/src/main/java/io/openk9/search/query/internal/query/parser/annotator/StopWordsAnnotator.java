package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = Annotator.class,
	configurationPid = AnnotatorConfig.PID
)
public class StopWordsAnnotator extends BaseAnnotator {

	@Activate
	@Modified
	void activate(AnnotatorConfig annotatorConfig) {
		setAnnotatorConfig(annotatorConfig);
	}

	@Override
	public List<CategorySemantics> annotate_(
		long tenantId, String... tokens) {

		if (Arrays.stream(tokens).allMatch(stopWords::contains)) {
			return _RESULT;
		}

		return List.of();
	}

	@Override
	public int weight() {
		return 1;
	}

	private static final List<CategorySemantics> _RESULT = List.of(
		CategorySemantics.of(
			"$StopWord",
			Map.of()
		)
	);

}
