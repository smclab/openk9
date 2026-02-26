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

package io.openk9.datasource.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.dto.base.AnnotatorDTO;
import io.openk9.datasource.model.dto.base.LanguageDTO;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.base.RuleDTO;
import io.openk9.datasource.model.init.Annotators;
import io.openk9.datasource.model.init.Languages;
import io.openk9.datasource.model.init.QueryParserConfigs;
import io.openk9.datasource.model.init.Rules;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class TenantInitializerService {

	@Inject
	AnnotatorService annotatorService;
	@Inject
	BucketService bucketService;
	@Inject
	LanguageService languageService;
	@Inject
	QueryAnalysisService queryAnalysisService;
	@Inject
	QueryParserConfigService queryParserConfigService;
	@Inject
	RuleService ruleService;
	@Inject
	SearchConfigService searchConfigService;
	@Inject
	Mutiny.SessionFactory sessionFactory;


	public Uni<Long> createDefault(String tenantId) {

		return sessionFactory
			.withTransaction(tenantId, (s, t) -> defaultBucket(s))
			.map(bucket -> 1L);
	}

	private Uni<Bucket> defaultBucket(Mutiny.Session s) {
		var bucketDTO = io.openk9.datasource.model.init.Bucket.INSTANCE;

		return defaultLanguages(s)
			.map(this::onlyBucketAvailableLanguages)
			.flatMap(availableLanguages -> defaultQueryAnalysis(s)
				.flatMap(queryAnalysis -> defaultSearchConfig(s)
					.flatMap(searchConfig -> bucketService
						.upsert(s, bucketDTO)
						.flatMap(bucket -> {
							bucket.setAvailableLanguages(availableLanguages);
							bucket.setQueryAnalysis(queryAnalysis);
							bucket.setSearchConfig(searchConfig);
							return bucketService.merge(s, bucket);
						})
						.flatMap(bucket -> bucketService.enableTenant(s, bucket.getId()))
					)
				)
			);
	}

	private Uni<io.openk9.datasource.model.QueryAnalysis> defaultQueryAnalysis(Mutiny.Session s) {

		var queryAnalysisDTO = io.openk9.datasource.model.init.QueryAnalysis.INSTANCE;

		return defaultRules(s)
			.flatMap(rules -> defaultAnnotators(s)
				.flatMap(annotators -> queryAnalysisService
					.upsert(s, queryAnalysisDTO)
					.flatMap(queryAnalysis -> {
						queryAnalysis.setAnnotators(new HashSet<>(annotators));
						queryAnalysis.setRules(new HashSet<>(rules));
						return queryAnalysisService.merge(s, queryAnalysis);
					})
				)
			);
	}

	private Uni<SearchConfig> defaultSearchConfig(Mutiny.Session s) {
		var searchConfigDTO = io.openk9.datasource.model.init.SearchConfig.INSTANCE;

		return defaultQueryParserConfigs(s)
			.flatMap(queryParserConfigs -> searchConfigService
				.upsert(s, searchConfigDTO)
				.flatMap(searchConfig -> {

					for (QueryParserConfig queryParserConfig : queryParserConfigs) {
						queryParserConfig.setSearchConfig(searchConfig);
					}

					searchConfig.setQueryParserConfigs(new HashSet<>(queryParserConfigs));
					return searchConfigService.merge(s, searchConfig);
				})
			);
	}

	private Uni<List<Annotator>> defaultAnnotators(Mutiny.Session s) {
		var upserts = new ArrayList<Uni<Annotator>>();

		for (AnnotatorDTO annotatorDTO : Annotators.INSTANCE) {
			upserts.add(annotatorService.upsert(s, annotatorDTO));
		}

		return Uni.join().all(upserts)
			.usingConcurrencyOf(1)
			.andCollectFailures();
	}

	private Uni<List<Language>> defaultLanguages(Mutiny.Session s) {
		var upserts = new ArrayList<Uni<Language>>();

		for (LanguageDTO languageDTO : Languages.INSTANCE) {
			upserts.add(languageService.upsert(s, languageDTO));
		}

		return Uni.join().all(upserts)
			.usingConcurrencyOf(1)
			.andCollectFailures();
	}

	private Uni<List<Rule>> defaultRules(Mutiny.Session s) {
		var upserts = new ArrayList<Uni<Rule>>();

		for (RuleDTO ruleDTO : Rules.INSTANCE) {
			upserts.add(ruleService.upsert(s, ruleDTO));
		}

		return Uni.join().all(upserts)
			.usingConcurrencyOf(1)
			.andCollectFailures();
	}

	private Uni<List<QueryParserConfig>> defaultQueryParserConfigs(Mutiny.Session s) {
		var upserts = new ArrayList<Uni<QueryParserConfig>>();

		for (QueryParserConfigDTO queryParserConfigDTO : QueryParserConfigs.DTOs.values()) {
			upserts.add(queryParserConfigService.upsert(s, queryParserConfigDTO));
		}

		return Uni.join().all(upserts)
			.usingConcurrencyOf(1)
			.andCollectFailures();
	}

	private Set<Language> onlyBucketAvailableLanguages(Collection<Language> languages) {
		return languages
			.stream()
			.filter(language -> io.openk9.datasource.model.init.Bucket.LANGUAGE_NAMES.contains(
				language.getName()))
			.collect(Collectors.toSet());
	}

}
