/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.searcher.queryanalysis;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.queryanalysis.annotator.AnnotatorFactory;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String virtualHost, JWT jwt) {

		Uni<Tuple2<String, Bucket>> getTenantUni = _getBucket(virtualHost);

		return getTenantUni
			.map(t2 -> {

				String schemaName = t2.getItem1();
				Bucket b = t2.getItem2();

				if (b != null) {
					QueryAnalysis queryAnalysis = b.getQueryAnalysis();

					Set<Rule> rules = queryAnalysis.getRules();

					List<io.openk9.datasource.searcher.queryanalysis.Rule> mappedRules =
						_toGrammarRule(rules);

					List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> mappedAnnotators =
						_toAnnotator(schemaName, b, queryAnalysis.getStopWordsList(), jwt);

					GrammarMixin grammarMixin = GrammarMixin.of(
						mappedRules, mappedAnnotators);

					return new Grammar(schemaName, List.of(grammarMixin));
				}
				else {
					return new Grammar(schemaName, List.of());
				}
			});
	}

	private List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> _toAnnotator(
		String schemaName, Bucket bucket, List<String> stopWords, JWT jwt) {
		return bucket.getQueryAnalysis().getAnnotators()
			.stream()
			.map(a -> annotatorFactory.getAnnotator(schemaName, bucket, a, stopWords, jwt))
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
		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(virtualHost, "grammarProvider", "_getBucket"),
			tenantManager
				.findTenant(TenantRequest.newBuilder().setVirtualHost(virtualHost).build())
				.flatMap(tenantResponse -> sessionFactory
					.withTransaction(tenantResponse.getSchemaName(), (s, t) -> s
						.createNamedQuery(Bucket.FETCH_ANNOTATORS_NAMED_QUERY, Bucket.class)
						.setParameter(TenantBinding_.VIRTUAL_HOST, virtualHost)
						.getSingleResult()
						.onItemOrFailure()
						.transform((bucket, throwable) -> {
							if (throwable != null) {
								return Tuple2.of(tenantResponse.getSchemaName(), null);
							}
							else {
								return Tuple2.of(tenantResponse.getSchemaName(), bucket);
							}
						})
					)
				)
		);
	}

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	AnnotatorFactory annotatorFactory;

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@CacheName("bucket-resource")
	Cache cache;

}
