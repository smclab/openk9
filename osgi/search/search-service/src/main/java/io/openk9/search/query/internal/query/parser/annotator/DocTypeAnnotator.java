package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import java.util.Map;

@Component(
	immediate = true, service = Annotator.class,
	configurationPid = AnnotatorConfig.PID
)
public class DocTypeAnnotator extends BaseAggregatorAnnotator {

	@Activate
	@Modified
	void activate(AnnotatorConfig annotatorConfig) {
		setAnnotatorConfig(annotatorConfig);
	}

	public DocTypeAnnotator() {
		super("documentTypes");
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey) {

		return CategorySemantics.of(
			"$DOCTYPE",
			Map.of(
				"tokenType", "DOCTYPE",
				"value", aggregatorKey,
				"score", 50.0f
			)
		);

	}

	@Override
	@Reference
	public void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

}
