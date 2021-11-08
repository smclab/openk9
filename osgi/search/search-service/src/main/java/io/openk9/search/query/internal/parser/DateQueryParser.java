package io.openk9.search.query.internal.parser;

import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DateQueryParser implements QueryParser {

	@interface Config {
		String sortableDateField() default "parsingDate";
	}

	@Activate
	void activate(Config config) {
		_sortableDateField = config.sortableDateField();
	}

	@Modified
	void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _rangeQuery(searchTokens, bool);

		});
	}

	private void _rangeQuery(
		List<SearchToken> searchTokens, BoolQueryBuilder bool) {

		if (searchTokens.isEmpty()) {
			return;
		}

		BoolQueryBuilder internalBool = QueryBuilders.boolQuery();

		for (SearchToken searchToken : searchTokens) {

			String[] values = searchToken.getValues();

			String gte;
			String lte;

			if (values.length == 0) {
				continue;
			}
			else {

				RangeQueryBuilder rangeQueryBuilder =
					QueryBuilders.rangeQuery(_sortableDateField);

				if (values.length == 1) {
					gte = values[0];
					rangeQueryBuilder.gte(gte);
				}
				else if (values.length == 2) {
					gte = values[0];
					lte = values[1];
					if (gte == null || gte.isBlank()) {
						rangeQueryBuilder.lte(lte);
					}
					else {
						rangeQueryBuilder
							.gte(gte)
							.lte(lte);
					}
				}

				internalBool.should(rangeQueryBuilder);

			}

		}

		bool.must(internalBool);


	}

	private String _sortableDateField;

	private static final String TYPE = "DATE";

}
