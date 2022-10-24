package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;
import java.util.Set;

@ApplicationScoped
public class DatasourceIdQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "DATASOURCE_ID_QUERY_PARSER";
	}

	@Override
	public void accept(ParserContext parserContext) {

		Tenant currentTenant = parserContext.getCurrentTenant();

		Set<Datasource> datasources = currentTenant.getDatasources();

		Iterator<Datasource> iterator = datasources.iterator();

		long[] ids = new long[datasources.size()];

		for (int i = 0; iterator.hasNext(); i++) {

			Datasource datasource = iterator.next();

			ids[i] = datasource.getId();

		}

		parserContext.getMutableQuery().filter(
			QueryBuilders
				.termsQuery("datasourceId", ids)
		);

	}

}