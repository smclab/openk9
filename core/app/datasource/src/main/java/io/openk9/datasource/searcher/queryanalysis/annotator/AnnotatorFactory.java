package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Bucket;
import org.elasticsearch.client.RestHighLevelClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class AnnotatorFactory {

	public Annotator getAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords) {

		return switch (annotator.getType()) {
			case TOKEN -> new TokenAnnotator(bucket, annotator, stopWords);
			case KEYWORD -> new KeywordAnnotator(bucket, annotator, stopWords);
			case STOPWORD -> new StopWordsAnnotator(bucket, annotator, stopWords);
			case NER -> new BaseNerAnnotator(
				bucket, annotator, stopWords, annotator.getFieldName(), client, tenantResolver);
			case DOCTYPE -> new DocTypeAnnotator(
				bucket, annotator, stopWords, client);
			case AGGREGATOR -> new AggregatorAnnotator(
				annotator.getDocTypeField().getPath(),
				bucket, annotator, stopWords, client);
			case AUTOCOMPLETE -> new BaseAutoCompleteAnnotator(
				bucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath());
			case KEYWORD_AUTOCOMPLETE -> new BaseKeywordAutoCompleteAnnotator(
				bucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath());
			case NER_AUTOCOMPLETE -> new BaseAutoCompleteNerAnnotator(
				bucket, annotator, stopWords, annotator.getFieldName(), client, tenantResolver);
			case AUTOCORRECT -> new BaseAutoCorrectAnnotator(
				bucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath());
		};
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	TenantResolver tenantResolver;

}
