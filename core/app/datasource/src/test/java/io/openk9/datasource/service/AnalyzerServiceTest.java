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

import java.util.Set;

import jakarta.inject.Inject;

import io.openk9.datasource.model.dto.AnalyzerWithListsDTO;
import io.openk9.datasource.model.dto.CharFilterDTO;
import io.openk9.datasource.model.dto.TokenFilterDTO;
import io.openk9.datasource.model.dto.TokenizerDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnalyzerServiceTest {

	private static final String ANALYZER_SERVICE_TOKEN_FILTER_1 = "AnalyserService.tokenFilter1";
	private static final String ANALYZER_SERVICE_TOKEN_FILTER_2 = "AnalyserService.tokenFilter2";
	private static final String ANALYZER_SERVICE_TOKENIZER_1 = "AnalyzerService.tokenizer1";
	private static final String ANALYZER_SERVICE_CREATE_WITH_LIST = "AnalyzerService.createWithList";
	private static final String ANALYZER_SERVICE_TEST_CHAR_FILTER_1 = "AnalyzerServiceTest.charFilter1";
	private static final String ANALYZER_SERVICE_TEST_CHAR_FILTER_2 = "AnalyzerServiceTest.charFilter2";
	private static final String ATYPE = "atype";
	private static final String JSON_CONFIG = "{}";
	private static final String PUBLIC = "public";

	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	AnalyzerService analyzerService;
	@Inject
	CharFilterService charFilterService;
	@Inject
	TokenFilterService tokenFilterService;
	@Inject
	TokenizerService tokenizerService;

	@Test
	@Order(1)
	void setup() {
		charFilterService.create(CharFilterDTO.builder()
				.name(ANALYZER_SERVICE_TEST_CHAR_FILTER_1)
				.type(ATYPE)
				.jsonConfig(JSON_CONFIG)
				.build()
		).await().indefinitely();

		charFilterService.create(CharFilterDTO.builder()
				.name(ANALYZER_SERVICE_TEST_CHAR_FILTER_2)
				.type(ATYPE)
				.jsonConfig(JSON_CONFIG)
				.build()
		).await().indefinitely();

		tokenFilterService.create(TokenFilterDTO.builder()
				.name(ANALYZER_SERVICE_TOKEN_FILTER_1)
				.type(ATYPE)
				.jsonConfig(JSON_CONFIG)
				.build()
		).await().indefinitely();

		tokenFilterService.create(TokenFilterDTO.builder()
				.name(ANALYZER_SERVICE_TOKEN_FILTER_2)
				.type(ATYPE)
				.jsonConfig(JSON_CONFIG)
				.build()
		).await().indefinitely();

		tokenizerService.create(TokenizerDTO.builder()
				.name(ANALYZER_SERVICE_TOKENIZER_1)
				.type(ATYPE)
				.jsonConfig(JSON_CONFIG)
				.build()
		).await().indefinitely();
	}

	@Test
	@Order(2)
	void should_create_with_list() {

		var charFilter1 = charFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TEST_CHAR_FILTER_1)
				.await().indefinitely();
		var charFilter2 = charFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TEST_CHAR_FILTER_2)
				.await().indefinitely();
		var tokenFilter1 = tokenFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKEN_FILTER_1)
				.await().indefinitely();
		var tokenFilter2 = tokenFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKEN_FILTER_2)
				.await().indefinitely();
		var tokenizer1 = tokenizerService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKENIZER_1)
				.await().indefinitely();

		var analyzer = analyzerService.create(AnalyzerWithListsDTO.builder()
				.charFilterIds(Set.of(charFilter1.getId(), charFilter2.getId()))
				.tokenFilterIds(Set.of(tokenFilter1.getId(), tokenFilter2.getId()))
				.tokenizerId(tokenizer1.getId())
				.name(ANALYZER_SERVICE_CREATE_WITH_LIST)
				.type(ATYPE)
				.build()
		).await().indefinitely();

		final var analyzerId = analyzer.getId();

		analyzer = sessionFactory.withTransaction(PUBLIC, (s, t) ->
				analyzerService.findById(s, analyzerId)
						.call(i -> s.fetch(i.getCharFilters()))
						.call(i -> s.fetch(i.getTokenFilters()))
		).await().indefinitely();

		Assertions.assertEquals(2, analyzer.getCharFilters().size());
		Assertions.assertEquals(2, analyzer.getTokenFilters().size());
		Assertions.assertEquals(tokenizer1.getId(), analyzer.getTokenizer().getId());
	}

	@Test
	@Order(3)
	void should_patch_with_list() {

		var analyzer = analyzerService.findByName(PUBLIC, ANALYZER_SERVICE_CREATE_WITH_LIST)
				.await().indefinitely();
		var tokenFilter1 = tokenFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKEN_FILTER_1)
				.await().indefinitely();
		var tokenizer1 = tokenizerService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKENIZER_1)
				.await().indefinitely();

		analyzer = analyzerService.patch(analyzer.getId(), AnalyzerWithListsDTO.builder()
				.charFilterIds(Set.of())
				.tokenFilterIds(Set.of(tokenFilter1.getId()))
				.name(ANALYZER_SERVICE_CREATE_WITH_LIST)
				.type(ATYPE)
				.build()
		).await().indefinitely();

		final var analyzerId = analyzer.getId();

		analyzer = sessionFactory.withTransaction(PUBLIC, (s, t) ->
				analyzerService.findById(s, analyzerId)
						.call(i -> s.fetch(i.getCharFilters()))
						.call(i -> s.fetch(i.getTokenFilters()))
		).await().indefinitely();

		Assertions.assertEquals(0, analyzer.getCharFilters().size());
		Assertions.assertEquals(1, analyzer.getTokenFilters().size());
		Assertions.assertEquals(tokenizer1.getId(), analyzer.getTokenizer().getId());

	}

	@Test
	@Order(4)
	void should_update_with_list() {

		var charFilter1 = charFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TEST_CHAR_FILTER_1)
				.await().indefinitely();
		
		var analyzer = analyzerService.findByName(PUBLIC, ANALYZER_SERVICE_CREATE_WITH_LIST)
				.await().indefinitely();

		analyzer = analyzerService.update(analyzer.getId(), AnalyzerWithListsDTO.builder()
				.name(ANALYZER_SERVICE_CREATE_WITH_LIST)
				.charFilterIds(Set.of(charFilter1.getId()))
				.type(ATYPE)
				.build()
		).await().indefinitely();

		final var analyzerId = analyzer.getId();

		analyzer = sessionFactory.withTransaction(PUBLIC, (s, t) ->
				analyzerService.findById(s, analyzerId)
						.call(i -> s.fetch(i.getCharFilters()))
						.call(i -> s.fetch(i.getTokenFilters()))
		).await().indefinitely();

		var charFilters = analyzer.getCharFilters();
		var firstCharFilter = charFilters.iterator().next();

		Assertions.assertEquals(1, charFilters.size());
		Assertions.assertEquals(charFilter1.getId(), firstCharFilter.getId());
		Assertions.assertEquals(0, analyzer.getTokenFilters().size());
		Assertions.assertNull(analyzer.getTokenizer());

		analyzerService.removeCharFilterFromAnalyzer(analyzerId, firstCharFilter.getId())
				.await().indefinitely();
	}

	@Test
	@Order(5)
	void tearDown() {

		charFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TEST_CHAR_FILTER_1)
				.flatMap(charFilter -> charFilterService.deleteById(charFilter.getId()))
				.await().indefinitely();
		charFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TEST_CHAR_FILTER_2)
				.flatMap(charFilter -> charFilterService.deleteById(charFilter.getId()))
				.await().indefinitely();
		tokenFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKEN_FILTER_1)
				.flatMap(tokenFilter -> tokenFilterService.deleteById(tokenFilter.getId()))
				.await().indefinitely();
		tokenFilterService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKEN_FILTER_2)
				.flatMap(tokenFilter -> tokenFilterService.deleteById(tokenFilter.getId()))
				.await().indefinitely();
		tokenizerService.findByName(
						PUBLIC, ANALYZER_SERVICE_TOKENIZER_1)
				.flatMap(tokenizer -> tokenizerService.deleteById(tokenizer.getId()))
				.await().indefinitely();
		analyzerService.findByName(PUBLIC, ANALYZER_SERVICE_CREATE_WITH_LIST)
				.flatMap(analyzer -> analyzerService.deleteById(analyzer.getId()))
				.await().indefinitely();

	}

}
