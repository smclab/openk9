package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import io.openk9.datasource.tenant.TenantResolver;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

abstract class BaseAnnotator implements Annotator {

	public BaseAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopwords, TenantResolver tenantResolver) {
		this.bucket = bucket;
		this.annotator = annotator;
		this.stopWords = stopwords;
		this.tenantResolver = tenantResolver;
	}

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.fuzzyQuery(field, token);
	}

	protected boolean _containsStopword(String[] tokens) {

		if (Arrays.stream(tokens).allMatch(stopWords::contains)) {
			return true;
		}

		int length = tokens.length;

		if (length > 1) {

			return stopWords.contains(tokens[0]) ||
				   stopWords.contains(tokens[length - 1]);

		}

		return false;

	}


	@Override
	public List<CategorySemantics> annotate(
		Set<String> context, String...tokens) {

		List<CategorySemantics> result = annotate(tokens);

		if (tokens.length == 1 && !result.isEmpty()) {

			String key = tokens[0];

			context.add(key);

		}

		return result;

	}

	@Override
	public int compareTo(Annotator o) {
		return Integer.compare(this.weight(), o.weight());
	}

	protected final Bucket bucket;

	protected final io.openk9.datasource.model.Annotator annotator;

	protected final List<String> stopWords;

	protected final TenantResolver tenantResolver;

}