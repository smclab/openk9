package io.openk9.entity.manager.logic;


import io.openk9.entity.manager.model.Entity;
import io.openk9.entity.manager.model.payload.EntityRequest;
import io.openk9.entity.manager.model.payload.RelationRequest;
import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.entity.manager.model.payload.ResponseList;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import io.openk9.relationship.graph.api.client.GraphClient;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Functions;
import org.neo4j.cypherdsl.core.Node;
import org.neo4j.cypherdsl.core.Statement;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.cypherdsl.core.Cypher.literalOf;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class GetOrAddEntitiesHttpHandler
	implements HttpHandler, RouterHandler {

	@interface Config {
		boolean transactional() default false;
	}

	@Activate
	void activate(Config config) {
		_transactional = config.transactional();
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
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/get-or-add-entities", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Mono<Request> requestMono = Mono
			.from(ReactorNettyUtils.aggregateBodyAsString(httpRequest))
			.map(body -> _jsonFactory.fromJson(body, Request.class));

		Flux<RequestContext> requestContextFlux =
			requestMono
				.flatMapIterable(
					request -> request
						.getEntities()
						.stream()
						.map(entityRequest -> RequestContext
							.builder()
							.current(entityRequest)
							.content(request.getContent())
							.contentId(request.getContentId())
							.tenantId(request.getTenantId())
							.ingestionId(request.getIngestionId())
							.datasourceId(request.getDatasourceId())
							.rest(
								request
									.getEntities()
									.stream()
									.filter(er -> er != entityRequest)
									.collect(Collectors.toList())
							)
							.build()
						)
						.collect(Collectors.toList())
				);

		Mono<List<EntityContext>> disambiguateListMono =
			GetOrAddEntities.stopWatch(
				"disambiguate-all-entities",
				requestContextFlux
					.flatMap(
						request ->
							GetOrAddEntities.stopWatch(
								"disambiguate-" + request.getCurrent().getName(),
								Mono.<EntityContext>create(
									fluxSink ->
										_startDisambiguation.disambiguate(
											request, fluxSink)
								)
							)
					)
					.collectList()
			);

		Mono<ResponseList> writeRelations = disambiguateListMono
			.flatMap(entityContexts ->
				GetOrAddEntities.stopWatch(
					"write-relations", writeRelations(entityContexts)));

		return Mono
			.from(_httpResponseWriter.write(
				httpResponse,
				_transactional
					? _graphClient.makeTransactional(writeRelations)
					: writeRelations));
	}

	public Mono<ResponseList> writeRelations(List<EntityContext> entityContext) {

		return Mono.defer(() -> {

			List<Statement> statementList = new ArrayList<>();

			for (EntityContext context : entityContext) {

				EntityRequest entityRequest = context.getEntityRequest();

				List<RelationRequest> relations =
					entityRequest.getRelations();

				if (relations == null || relations.isEmpty()) {
					continue;
				}

				Entity currentEntity = context.getEntity();

				List<Tuple2<String, Entity>> entityRelations =
					entityContext
						.stream()
						.flatMap(entry -> {

							for (RelationRequest relation : relations) {
								if (entry.getEntityRequest().getTmpId() == relation.getTo()) {
									return Stream.of(
										Tuples.of(
											relation.getName(),
											entry.getEntity())
									);
								}
							}

							return Stream.empty();

						})
						.collect(Collectors.toList());

				Node currentEntityNode =
					Cypher
						.node(currentEntity.getType())
						.named("a");

				List<Statement> currentStatementList =
					entityRelations
						.stream()
						.map(t2 -> {

							Entity entityRelation = t2.getT2();

							Node entityRelationNode =
								Cypher
									.node(entityRelation.getType())
									.named("b");

							return Cypher
								.match(currentEntityNode, entityRelationNode)
								.where(
									Functions
										.id(currentEntityNode)
										.eq(literalOf(currentEntity.getId()))
										.and(
											Functions
												.id(entityRelationNode)
												.eq(literalOf(
													entityRelation.getId()))
										)
								)
								.merge(
									currentEntityNode
										.relationshipTo(
											entityRelationNode, t2.getT1())
								)
								.build();
						})
						.collect(Collectors.toList());

				statementList.addAll(currentStatementList);

			}

			List<Response> response =
				entityContext
					.stream()
					.map(context -> Response
						.builder()
						.entity(
							Entity
								.builder()
								.name(context.getEntity().getName())
								.id(context.getEntity().getId())
								.tenantId(context.getEntity().getTenantId())
								.type(context.getEntity().getType())
								.build()
						)
						.tmpId(context.getEntityRequest().getTmpId())
						.build()
					)
					.collect(Collectors.toList());

			if (statementList.size() > 1) {
				return _graphClient
					.write(Cypher.unionAll(statementList.toArray(new Statement[0])))
					.then(Mono.just(ResponseList.of("", response)));
			}
			else if (statementList.size() == 1) {
				return _graphClient
					.write(statementList.get(0))
					.then(Mono.just(ResponseList.of("", response)));
			}
			else {
				return Mono.just(ResponseList.of("", response));
			}

		});

	}

	private boolean _transactional;

	@Reference
	private GraphClient _graphClient;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private StartDisambiguation _startDisambiguation;

}
