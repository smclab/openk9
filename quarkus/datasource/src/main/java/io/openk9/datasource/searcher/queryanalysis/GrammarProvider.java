package io.openk9.datasource.searcher.queryanalysis;

import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.Tenant_;
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

		Uni<Tenant> getTenantUni = _getTenant(virtualHost);

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
		Tenant tenant, List<String> stopWords) {
		return tenant.getQueryAnalysis().getAnnotators()
			.stream()
			.map(a -> annotatorFactory.getAnnotator(tenant, a, stopWords))
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

	private Uni<Tenant> _getTenant(String virtualHost) {
		return sf.withStatelessTransaction(
			s -> {

				CriteriaBuilder cb = sf.getCriteriaBuilder();

				CriteriaQuery<Tenant> query = cb.createQuery(Tenant.class);

				Root<Tenant> tenantRoot = query.from(Tenant.class);

				Join<Tenant, TenantBinding> tenantBindingJoin =
					tenantRoot.join(Tenant_.tenantBinding);

				tenantRoot
					.fetch(Tenant_.datasources)
					.fetch(Datasource_.dataIndex);

				Fetch<Tenant, QueryAnalysis> queryAnalysisFetch =
					tenantRoot.fetch(Tenant_.queryAnalysis);

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
