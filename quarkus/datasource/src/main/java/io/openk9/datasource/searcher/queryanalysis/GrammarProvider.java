package io.openk9.datasource.searcher.queryanalysis;

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.AnnotatorType;
import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.searcher.queryanalysis.annotator.AnnotatorFactory;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.tuples.Tuple2;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String virtualHost) {

		Uni<Tuple2<String, Bucket>> getTenantUni = _getTenant(virtualHost);

		return getTenantUni
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.map(t2 -> {

				String schemaName = t2.getItem1();
				Bucket t = t2.getItem2();

				QueryAnalysis queryAnalysis = t.getQueryAnalysis();

				Set<Rule> rules = queryAnalysis.getRules();

				List<io.openk9.datasource.searcher.queryanalysis.Rule> mappedRules =
					_toGrammarRule(rules);

				List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> mappedAnnotators =
					_toAnnotator(t, queryAnalysis.getStopWordsList());

				GrammarMixin grammarMixin = GrammarMixin.of(
					mappedRules, mappedAnnotators);

				return new Grammar(
					() -> tenantResolver.setTenant(schemaName), List.of(grammarMixin));

			});
	}

	private List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> _toAnnotator(
		Bucket bucket, List<String> stopWords) {
		return bucket.getQueryAnalysis().getAnnotators()
			.stream()
			.map(a -> annotatorFactory.getAnnotator(bucket, a, stopWords))
			.toList();
	}

	private List<io.openk9.datasource.searcher.queryanalysis.Rule> _toGrammarRule(
		Collection<Rule> rules) {

		return rules
			.stream()
			.map(r -> io.openk9.datasource.searcher.queryanalysis.Rule.of(
				r.getLhs(), r.getRhs(), Semantic.identity()))
			.toList();
	}

	private Uni<Tuple2<String, Bucket>> _getTenant(String virtualHost) {
		return
			tenantManager
				.findTenant(TenantRequest.newBuilder().setVirtualHost(virtualHost).build())
				.flatMap(tenantResponse -> sf.withStatelessTransaction(tenantResponse.getSchemaName(), s -> {

					CriteriaBuilder cb = sf.getCriteriaBuilder();

					CriteriaQuery<Bucket> query = cb.createQuery(Bucket.class);

					Root<Bucket> tenantRoot = query.from(Bucket.class);

					Join<Bucket, TenantBinding> tenantBindingJoin =
						tenantRoot.join(Bucket_.tenantBinding);

					tenantRoot
						.fetch(Bucket_.datasources)
						.fetch(Datasource_.dataIndex);

					Join<Bucket, QueryAnalysis> queryAnalysisFetch =
						(Join<Bucket, QueryAnalysis>)
							tenantRoot.fetch(Bucket_.queryAnalysis);

					queryAnalysisFetch.fetch(QueryAnalysis_.rules);

					Join<QueryAnalysis, Annotator> annotatorInnerJoin =
						(Join<QueryAnalysis, Annotator>)queryAnalysisFetch.fetch(
							QueryAnalysis_.annotators, JoinType.INNER);

					Predicate annotatorOn =
						annotatorInnerJoin
							.get(Annotator_.type)
							.in(
								AnnotatorType.AGGREGATOR,
								AnnotatorType.AUTOCOMPLETE,
								AnnotatorType.AUTOCORRECT
							);

					annotatorInnerJoin.on(annotatorOn);

					annotatorInnerJoin.fetch(Annotator_.docTypeField);

					SetJoin<QueryAnalysis, Annotator> annotatorLeftJoin =
						(SetJoin<QueryAnalysis, Annotator>)queryAnalysisFetch.fetch(
							QueryAnalysis_.annotators, JoinType.INNER);

					annotatorLeftJoin.on(annotatorOn.not());

					annotatorLeftJoin.fetch(Annotator_.docTypeField, JoinType.LEFT);

					query.where(
						cb.equal(
							tenantBindingJoin.get(TenantBinding_.virtualHost),
							virtualHost
						)
					);

					query.distinct(true);

					return s
						.createQuery(query)
						.setCacheable(true)
						.getSingleResultOrNull()
						.map(b -> Tuple2.of(tenantResponse.getSchemaName(), b));

				}));
	}

	@Inject
	TransactionInvoker sf;

	@Inject
	AnnotatorFactory annotatorFactory;

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@Inject
	TenantResolver tenantResolver;

}
