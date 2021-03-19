package io.openk9.search.query.internal.parser;

import io.openk9.datasource.model.Datasource;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DatasourceIdQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		return Mono.fromSupplier(() -> {

			List<Datasource> datasources = context.getDatasourceList();

			long[] ids = new long[datasources.size()];

			for (int i = 0; i < datasources.size(); i++) {

				Datasource datasource = datasources.get(i);

				ids[i] = datasource.getDatasourceId();

			}

			return (bool) -> bool.filter(
				QueryBuilders
					.termsQuery("datasourceId", ids)
			);

		});
	}
}
