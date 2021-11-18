package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;

@Component(
	immediate = true, service = Annotator.class
)
public class AggragatorAnnotator extends BaseAggregatorAnnotator {

	public AggragatorAnnotator() {
		super(
			"istat.category", "istat.topic",
			"pubblicazioni.category", "pubblicazioni.topic",
			"notize.category", "notizie.topic", "documentTypes"
		);
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey) {

		if ("documentTypes".equals(aggregatorName)) {
			return CategorySemantics.of(
				"$DOCTYPE",
				Map.of(
					"tokenType", "DOCTYPE",
					"value", aggregatorKey
				)
			);
		}
		return CategorySemantics.of(
			"$AGGREGATE",
			Map.of(
				"tokenType", "TEXT",
				"keywordKey", aggregatorName,
				"value", aggregatorKey
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
	public void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

}
