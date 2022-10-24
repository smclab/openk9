package io.openk9.datasource.searcher.parser;

import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.QueryParserConfig_;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class QueryParserActivator {

	void onStart(@Observes StartupEvent ev) {
		eventBus.send("initialize_query_parser", "initialize_scheduler");
	}

	@ConsumeEvent(value = "initialize_query_parser")
	@ActivateRequestContext
	public Uni<Void> initQueryParser(String txt) {

		return sessionFactory.withTransaction(s -> {
			List<String> queryTypes =
				queryParserInstance
					.stream()
					.map(QueryParser::getType)
					.distinct()
					.toList();

			Uni<List<QueryParserConfig>> queryParserConfigByTypes =
				getQueryParserConfigByTypes(s, queryTypes);

			return queryParserConfigByTypes
				.onItem()
				.transform(
					queryParserConfigs -> {

						List<QueryParserConfig> newQueryParserConfigs =
							new ArrayList<>();

						for (QueryParser queryParser : queryParserInstance) {

							Optional<QueryParserConfig> queryParserOptional =
								queryParserConfigs
									.stream()
									.filter(qp -> qp.getType().equals(
										queryParser.getType()))
									.findFirst();

							if (queryParserOptional.isEmpty()) {

								QueryParserConfig newQueryParserConfig =
									new QueryParserConfig();

								newQueryParserConfig.setType(
									queryParser.getType());

								newQueryParserConfig.setDescription(
									"Default Query Parser"
								);

								newQueryParserConfig.setJsonConfig(
									queryParser.getConfiguration().encodePrettily());

								newQueryParserConfigs.add(
									newQueryParserConfig
								);
							}
							else {
								QueryParserConfig queryParserConfig =
									queryParserOptional.get();

								newQueryParserConfigs.add(
									queryParserConfig
								);
							}

						}

						return newQueryParserConfigs;

					}
				)
				.flatMap(list -> {

					List<Uni<QueryParserConfig>> unis = new ArrayList<>();

					for (QueryParserConfig queryParserConfig : list) {
						if (queryParserConfig.getId() == null) {
							unis.add(
								s
									.persist(queryParserConfig)
									.map(__ -> queryParserConfig)
							);
						}
						else {
							unis.add(Uni.createFrom().item(queryParserConfig));
						}
					}

					return Uni
						.join()
						.all(unis)
						.andCollectFailures();

				})
				.invoke(list -> {
					for (QueryParserConfig queryParserConfig : list) {

						String jsonConfig = queryParserConfig.getJsonConfig();

						if (jsonConfig != null) {
							try {

								JsonObject jsonObject =
									new JsonObject(jsonConfig);

								for (QueryParser queryParser : queryParserInstance) {

									if (queryParser
										.getType()
										.equals(queryParserConfig.getType())) {

										queryParser.configure(jsonObject);

										logger.info(
											"Query Parser Activated: " +
											queryParser.getType()
										);

										break;
									}

								}

							}
							catch (Exception e) {
								logger.warn("Error parsing json config", e);
							}
						}
					}
				})
				.replaceWithVoid();
		});

	}


	private Uni<List<QueryParserConfig>> getQueryParserConfigByTypes(
		Mutiny.Session s, List<String> queryParserTypes) {

		CriteriaBuilder criteriaBuilder =
			sessionFactory.getCriteriaBuilder();

		CriteriaQuery<QueryParserConfig> query =
			criteriaBuilder.createQuery(QueryParserConfig.class);

		Root<QueryParserConfig> root = query.from(QueryParserConfig.class);

		query.where(root.get(QueryParserConfig_.type).in(queryParserTypes));

		return s.createQuery(query).getResultList();

	}

	@Inject
	Logger logger;

	@Inject
	Instance<QueryParser> queryParserInstance;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	EventBus eventBus;

}
