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

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.queryanalysis.annotator.AnnotatorFactory;
import io.openk9.datasource.service.TenantIdResolver;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class GrammarProvider {

	public Uni<Grammar> getOrCreateGrammar(String tenantId, JsonWebToken jwt) {

		return getTenantWithBucket(tenantId)
			.onItem().ifNull().fail()
			.onItem().ifNotNull().transform(tenantWithBucket -> {

				var bucket = tenantWithBucket.getBucket();
				var resolvedTenantId = tenantWithBucket.getTenantId();

				QueryAnalysis queryAnalysis = bucket.getQueryAnalysis();

				Set<Rule> rules = queryAnalysis.getRules();

				List<io.openk9.datasource.searcher.queryanalysis.Rule> mappedRules =
					_toGrammarRule(rules);

				List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator>
					mappedAnnotators =
					_toAnnotator(tenantWithBucket, queryAnalysis, jwt);

				GrammarMixin grammarMixin = GrammarMixin.of(
					mappedRules, mappedAnnotators);

				return new Grammar(resolvedTenantId, List.of(grammarMixin));
			});
	}

	private List<io.openk9.datasource.searcher.queryanalysis.annotator.Annotator> _toAnnotator(
		TenantWithBucket tenantWithBucket, QueryAnalysis queryAnalysis, JsonWebToken jwt) {

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
	Mutiny.SessionFactory sessionFactory;

	@Inject
	AnnotatorFactory annotatorFactory;

	private Uni<TenantWithBucket> getTenantWithBucket(String tenantId) {
		return cache.getAsync(
			new CompositeCacheKey(tenantId, "grammarProvider", "getTenantWithBucket"),
				key -> sessionFactory.withTransaction(
					tenantId, (s, t) -> s
						.createNamedQuery(Bucket.FETCH_ANNOTATORS_NAMED_QUERY, Bucket.class)
						.getSingleResult()
						.map(bucket -> new TenantWithBucket(tenantId, bucket))
						.onFailure()
						.recoverWithNull()
				)
			);
	}

	@CacheName("bucket-resource")
	Cache cache;

}
