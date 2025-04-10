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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.auth.tenant.TenantRegistry;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.util.JWT;
import io.openk9.datasource.searcher.TenantWithBucket;
import io.openk9.datasource.searcher.queryanalysis.annotator.AnnotatorFactory;
import io.openk9.datasource.util.QuarkusCacheUtil;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String virtualHost, JWT jwt) {

		return getTenantWithBucket(virtualHost)
			.map(tenantWithBucket -> {

				var bucket = tenantWithBucket.getBucket();
				var tenantId = tenantWithBucket.getTenant().schemaName();

				QueryAnalysis queryAnalysis = bucket.getQueryAnalysis();

				Set<Rule> rules = queryAnalysis.getRules();

				List<io.openk9.datasource.searcher.queryanalysis.Rule> mappedRules =
					_toGrammarRule(rules);

				List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator>
					mappedAnnotators =
					_toAnnotator(tenantWithBucket, queryAnalysis, jwt);

				GrammarMixin grammarMixin = GrammarMixin.of(
					mappedRules, mappedAnnotators);

				return new Grammar(tenantId, List.of(grammarMixin));
			});
	}

	private List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> _toAnnotator(
		TenantWithBucket tenantWithBucket, QueryAnalysis queryAnalysis, JWT jwt) {

		var stopWords = queryAnalysis.getStopWordsList();

		return queryAnalysis.getAnnotators()
			.stream()
			.map(a -> annotatorFactory.getAnnotator(tenantWithBucket, a, stopWords, jwt))
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

	@Inject
	TenantRegistry tenantRegistry;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	AnnotatorFactory annotatorFactory;

	private Uni<TenantWithBucket> getTenantWithBucket(String virtualHost) {
		return QuarkusCacheUtil.getAsync(
			cache,
			new CompositeCacheKey(virtualHost, "grammarProvider", "getTenantWithBucket"),
			tenantRegistry
				.getTenantByVirtualHost(virtualHost)
				.flatMap(tenant -> sessionFactory
					.withTransaction(
						tenant.schemaName(), (s, t) -> s
							.createNamedQuery(Bucket.FETCH_ANNOTATORS_NAMED_QUERY, Bucket.class)
							.setParameter(TenantBinding_.VIRTUAL_HOST, virtualHost)
							.getSingleResult()
							.map(bucket -> new TenantWithBucket(tenant, bucket))
					)
				)
		);
	}

	@CacheName("bucket-resource")
	Cache cache;

}
