package io.openk9.search.query.internal.query.parser.annotator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.api.query.parser.Tuple;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.time.Duration;
import java.util.List;

public abstract class BaseAnnotator implements Annotator {

	protected BaseAnnotator() {
		this(false, Duration.ZERO);
	}

	protected BaseAnnotator(boolean cacheEnabled, Duration cacheDuration) {

		this.cacheEnabled = cacheEnabled;

		if (cacheEnabled) {
			_cache = Caffeine
				.newBuilder()
				.expireAfterWrite(cacheDuration)
				.build();
		}
	}

	public abstract List<CategorySemantics> annotate_(
		long tenantId, String...tokens);

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.matchQuery(field, token);
	}

	@Override
	public final List<CategorySemantics> annotate(
		long tenantId, String...tokens) {

		if (cacheEnabled) {
			List<CategorySemantics> cacheSemantics =
				_cache.getIfPresent(Tuple.of(tokens));

			if (cacheSemantics == null) {
				List<CategorySemantics> categorySemantics =
					annotate_(tenantId, tokens);

				_cache.put(tokens, categorySemantics);

				return categorySemantics;

			}
			else {
				return cacheSemantics;
			}
		}

		return annotate_(tenantId, tokens);

	}

	@Override
	public int compareTo(Annotator o) {
		return Integer.compare(this.weight(), o.weight());
	}

	@Override
	public int weight() {
		return 1;
	}

	protected void setAnnotatorConfig(AnnotatorConfig annotatorConfig) {
		_annotatorConfig = annotatorConfig;
		stopWords = List.of(annotatorConfig.stopWords());
	}

	protected AnnotatorConfig _annotatorConfig;
	protected List<String> stopWords;
	protected Cache<Object, List<CategorySemantics>> _cache;
	protected final boolean cacheEnabled;


}
