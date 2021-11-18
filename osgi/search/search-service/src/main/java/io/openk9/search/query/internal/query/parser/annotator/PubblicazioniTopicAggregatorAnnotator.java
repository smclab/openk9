package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Duration;
import java.util.Map;

@Component(
	immediate = true, service = Annotator.class
)
public class PubblicazioniTopicAggregatorAnnotator extends BaseAggregatorAnnotator {

	public PubblicazioniTopicAggregatorAnnotator() {
		super(true, Duration.ofSeconds(30), "pubblicazioni.topic");
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey) {

		return CategorySemantics.of(
			"$AGGREGATE",
			Map.of(
				"tokenType", "TEXT",
				"keywordKey", aggregatorName,
				"value", aggregatorKey,
				"score", 1.0f
			)
		);

	}

	@Override
	@Reference
	protected void setAnnotatorConfig(
		AnnotatorConfig annotatorConfig) {
		super.setAnnotatorConfig(annotatorConfig);
	}

	@Override
	@Reference
	public void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

}
