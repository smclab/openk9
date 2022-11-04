package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.tenant.TenantResolver;
import org.elasticsearch.client.RestHighLevelClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class AnnotatorFactory {

	public Annotator getAnnotator(
		Tenant tenant,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords) {

		return switch (annotator.getType()) {
			case TOKEN -> new TokenAnnotator(tenant, annotator, stopWords);
			case STOPWORD -> new StopWordsAnnotator(tenant, annotator, stopWords);
			case NER -> new BaseNerAnnotator(
				tenant, annotator, stopWords,
				annotator.getDocTypeField().getName(), client, tenantResolver);
			case DOCTYPE -> new DocTypeAnnotator(
				tenant, annotator, stopWords, client);
			case AGGREGATOR -> new AggregatorAnnotator(
				annotator.getDocTypeField().getName(),
				tenant, annotator, stopWords, client);
			case AUTOCOMPLETE -> new BaseAutoCompleteAnnotator(
				tenant, annotator, stopWords, client,
				annotator.getDocTypeField().getName());
			case AUTOCORRECT -> Annotator.DUMMY_ANNOTATOR;
		};
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	TenantResolver tenantResolver;

}
