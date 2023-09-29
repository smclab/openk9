package io.openk9.datasource.searcher.queryanalysis;

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.AnnotatorType;
import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
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
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String virtualHost) {

		Uni<Tuple2<String, Bucket>> getTenantUni = _getTenant(virtualHost);

		return getTenantUni
			.invoke(t2 -> tenantResolver.setTenant(t2.getItem1()))
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
				.flatMap(tenantResponse -> sf.withTransaction(tenantResponse.getSchemaName(), s -> {

					CriteriaBuilder cb = sf.getCriteriaBuilder();

					CriteriaQuery<Bucket> query = cb.createQuery(Bucket.class);

					Root<Bucket> tenantRoot = query.from(Bucket.class);

					Join<Bucket, TenantBinding> tenantBindingJoin =
						tenantRoot.join(Bucket_.tenantBinding);

					tenantRoot
						.fetch(Bucket_.datasources)
						.fetch(Datasource_.dataIndex);

					Fetch<Bucket, QueryAnalysis> queryAnalysisFetch =
						tenantRoot.fetch(Bucket_.queryAnalysis);

					queryAnalysisFetch.fetch(QueryAnalysis_.rules);

					Join<QueryAnalysis, Annotator> annotatorJoin1 =
						(Join<QueryAnalysis, Annotator>)queryAnalysisFetch.fetch(
							QueryAnalysis_.annotators, JoinType.INNER);

					Fetch<Annotator, DocTypeField> docTypeFieldFetch =
						annotatorJoin1.fetch(Annotator_.docTypeField);

					docTypeFieldFetch
						.fetch(DocTypeField_.parentDocTypeField, JoinType.LEFT);

					docTypeFieldFetch
						.fetch(DocTypeField_.subDocTypeFields, JoinType.LEFT);

					Join<QueryAnalysis, Annotator> annotatorJoin2 =
						(Join<QueryAnalysis, Annotator>)queryAnalysisFetch.fetch(
							QueryAnalysis_.annotators, JoinType.INNER);

					query.where(
						cb.and(
							cb.equal(
								tenantBindingJoin.get(TenantBinding_.virtualHost),
								virtualHost
							),
							cb.or(
								createAnnotatorTypePredicate(annotatorJoin1),
								createAnnotatorTypePredicate(annotatorJoin2).not()
							)
						)
					);

					return s
						.createQuery(query)
						.setCacheable(true)
						.getSingleResult()
						.map(b -> Tuple2.of(tenantResponse.getSchemaName(), b));

				}));
	}

	private static Predicate createAnnotatorTypePredicate(Path<Annotator> path) {
		return path
			.get(Annotator_.type)
			.in(
				AnnotatorType.AGGREGATOR,
				AnnotatorType.AUTOCOMPLETE,
				AnnotatorType.AUTOCORRECT,
				AnnotatorType.KEYWORD_AUTOCOMPLETE
			);
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
