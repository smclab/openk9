package io.openk9.datasource.searcher.queryanalysis;

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
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String virtualHost) {

		Uni<Bucket> getTenantUni = _getTenant(virtualHost);

		return getTenantUni
			.emitOn(Infrastructure.getDefaultWorkerPool())
			.map(t -> {

				QueryAnalysis queryAnalysis = t.getQueryAnalysis();

				Set<Rule> rules = queryAnalysis.getRules();

				List<io.openk9.datasource.searcher.queryanalysis.Rule> mappedRules =
					_toGrammarRule(rules);

				List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> mappedAnnotators =
					_toAnnotator(t, queryAnalysis.getStopWordsList());

				GrammarMixin grammarMixin = GrammarMixin.of(
					mappedRules, mappedAnnotators);

				return new Grammar(List.of(grammarMixin));

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

	private Uni<Bucket> _getTenant(String virtualHost) {
		return sf.withStatelessTransaction(
			s -> {

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

				queryAnalysisFetch
					.fetch(QueryAnalysis_.annotators, JoinType.LEFT)
					.fetch(Annotator_.docTypeField, JoinType.LEFT);

				query.where(
					cb.equal(
						tenantBindingJoin.get(TenantBinding_.virtualHost),
						virtualHost
					)
				);

				query.distinct(true);

				return s.createQuery(query).getSingleResultOrNull();

			}
		);
	}

	@Inject
	TransactionInvoker sf;

	@Inject
	AnnotatorFactory annotatorFactory;

}
