package io.openk9.datasource.searcher.parser;

import io.openk9.datasource.model.QueryParserConfig;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class QueryParserConfigListener {

	@PreUpdate
	public void beforeUpdate(QueryParserConfig queryParserConfig) {
		for (QueryParser queryParser : _queryParserInstance) {
			if (queryParser.getType().equals(queryParserConfig.getType())) {

				String jsonConfig = queryParserConfig.getJsonConfig();

				if (jsonConfig == null) {
					logger.warn("jsonConfig is null for type: " + queryParserConfig.getType());
					continue;
				}

				try {
					queryParser.configure(new JsonObject(jsonConfig));
				}
				catch (DecodeException e) {
					logger.error("Error configuring queryParser: " + queryParserConfig.getType(), e);
				}
			}
		}
	}

	@Inject
	Instance<QueryParser> _queryParserInstance;

	@Inject
	Logger logger;

}
