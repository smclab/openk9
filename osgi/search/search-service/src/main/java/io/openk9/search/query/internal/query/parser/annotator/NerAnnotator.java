package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.client.api.RestHighLevelClientProvider;

public class NerAnnotator extends BaseNerAnnotator {

	public NerAnnotator(
		String keyword,
		AnnotatorConfig annotatorConfig,
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super(keyword);
		super.setAnnotatorConfig(annotatorConfig);
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

}
