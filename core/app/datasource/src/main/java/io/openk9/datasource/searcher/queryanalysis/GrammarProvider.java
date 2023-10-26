package io.openk9.datasource.searcher.queryanalysis;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.searcher.queryanalysis.annotator.AnnotatorFactory;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String virtualHost) {

		Uni<Tuple2<String, Bucket>> getTenantUni = _getBucket(virtualHost);

		return getTenantUni
			.map(t2 -> {

				String schemaName = t2.getItem1();
				Bucket b = t2.getItem2();

				QueryAnalysis queryAnalysis = b.getQueryAnalysis();

				Set<Rule> rules = queryAnalysis.getRules();

				List<io.openk9.datasource.searcher.queryanalysis.Rule> mappedRules =
					_toGrammarRule(rules);

				List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> mappedAnnotators =
					_toAnnotator(schemaName, b, queryAnalysis.getStopWordsList());

				GrammarMixin grammarMixin = GrammarMixin.of(
					mappedRules, mappedAnnotators);

				return new Grammar(schemaName, List.of(grammarMixin));

			});
	}

	private List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> _toAnnotator(
		String schemaName, Bucket bucket, List<String> stopWords) {
		return bucket.getQueryAnalysis().getAnnotators()
			.stream()
			.map(a -> annotatorFactory.getAnnotator(schemaName, bucket, a, stopWords))
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

	private Uni<Tuple2<String, Bucket>> _getBucket(String virtualHost) {
		return tenantManager
				.findTenant(TenantRequest.newBuilder().setVirtualHost(virtualHost).build())
				.flatMap(tenantResponse -> sessionFactory
					.withTransaction(tenantResponse.getSchemaName(), (s, t) -> s
						.createNamedQuery("Bucket.all", Bucket.class)
						.setParameter("virtualHost", virtualHost)
						.getSingleResult()
						.map(b -> Tuple2.of(tenantResponse.getSchemaName(), b))));
	}

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	AnnotatorFactory annotatorFactory;

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

}
