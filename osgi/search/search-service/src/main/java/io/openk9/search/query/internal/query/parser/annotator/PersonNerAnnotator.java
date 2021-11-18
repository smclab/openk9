package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Duration;

@Component(
	immediate = true, service = Annotator.class
)
public class PersonNerAnnotator extends BaseNerAnnotator {

	public PersonNerAnnotator() {
		super("person", true, Duration.ofSeconds(30));
	}

	@Override
	@Reference
	public void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

	@Override
	@Reference
	protected void setAnnotatorConfig(
		AnnotatorConfig annotatorConfig) {
		super.setAnnotatorConfig(annotatorConfig);
	}

}
