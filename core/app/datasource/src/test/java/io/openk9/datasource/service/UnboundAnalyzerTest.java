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

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.dto.AnalyzerDTO;
import io.openk9.datasource.model.dto.CharFilterDTO;
import io.openk9.datasource.model.dto.TokenFilterDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnboundAnalyzerTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundAnalyzerTest - ";

	private static final String ANALYZER_ONE_NAME = ENTITY_NAME_PREFIX + "Analyzer 1";
	private static final String ANALYZER_TWO_NAME = ENTITY_NAME_PREFIX + "Analyzer 2";
	private static final String ANALYZER_UNBOUND_NAME = ENTITY_NAME_PREFIX + "Unbound analyzer";
	private static final String CHAR_FILTER_ONE_NAME = ENTITY_NAME_PREFIX + "Char filter 1";
	private static final String CHAR_FILTER_TWO_NAME = ENTITY_NAME_PREFIX + "Char filter 2";
	private static final String CHAR_FILTER_THREE_NAME = ENTITY_NAME_PREFIX + "Char filter 3";
	private static final String CUSTOM = "custom";
	private static final String STRING_BLANK = "";
	private static final String TOKEN_FILTER_ONE_NAME = ENTITY_NAME_PREFIX + "Token filter 1";
	private static final String TOKEN_FILTER_TWO_NAME = ENTITY_NAME_PREFIX + "Token filter 2";
	private static final String TOKEN_FILTER_THREE_NAME = ENTITY_NAME_PREFIX + "Token filter 3";

	@Inject
	AnalyzerService analyzerService;

	@Inject
	CharFilterService charFilterService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	TokenFilterService tokenFilterService;

	@Test
	@Order(1)
	void should_init_test_environment() {

		createAnalyzerOne();
		createAnalyzerTwo();
		createAnalyzerUnbound();

		createTokenFilterOne();
		createTokenFilterTwo();
		createTokenFilterThree();

		createCharFilterOne();
		createCharFilterTwo();
		createCharFilterThree();

		//Bind analyzer 1 with each entity 1
		bindAnalyzerOneToTokenFilterOne();
		bindAnalyzerOneToCharFilterOne();

		//Bind analyzer 2 with each entity 2 and 3
		bindAnalyzerTwoToTokenFilterTwo();
		bindAnalyzerTwoToTokenFilterThree();
		bindAnalyzerTwoToCharFilterTwo();
		bindAnalyzerTwoToCharFilterThree();

	}

	@Test
	@Order(2)
	void should_retrieve_unbound_analyzer_from_token_filter_two() {
		var unboundAnalyzers = getUnboundAnalyzerByTokenFilterTwo();

		assertFalse(unboundAnalyzers.isEmpty());

		//Must not have other analyzer except analyzer 2
		assertTrue(unboundAnalyzers.stream()
			.anyMatch(analyzer ->
				ANALYZER_ONE_NAME.equalsIgnoreCase(analyzer.getName())));

		assertTrue(unboundAnalyzers.stream()
			.anyMatch(analyzer ->
				ANALYZER_UNBOUND_NAME.equalsIgnoreCase(analyzer.getName())));

		//Must not have analyzer 2
		assertFalse(unboundAnalyzers.stream()
			.anyMatch(analyzer ->
				ANALYZER_TWO_NAME.equalsIgnoreCase(analyzer.getName())));

		assertEquals(allAnalyzerCount() - 1, unboundAnalyzers.size());

	}

	@Test
	@Order(3)
	void should_retrieve_unbound_analyzer_from_missing_token_filter() {
		var unboundAnalyzer = getUnboundAnalyzerByMissingTokenFilter();

		assertFalse(unboundAnalyzer.isEmpty());

		assertTrue(unboundAnalyzer.stream()
			.anyMatch(analyzer ->
				ANALYZER_ONE_NAME.equalsIgnoreCase(analyzer.getName())));
		assertTrue(unboundAnalyzer.stream()
			.anyMatch(analyzer ->
				ANALYZER_TWO_NAME.equalsIgnoreCase(analyzer.getName())));
		assertTrue(unboundAnalyzer.stream()
			.anyMatch(analyzer ->
				ANALYZER_UNBOUND_NAME.equalsIgnoreCase(analyzer.getName())));

		assertEquals(allAnalyzerCount(), unboundAnalyzer.size());
	}

	@Test
	@Order(4)
	void should_retrieve_unbound_analyzer_from_char_filter_two() {
		var unboundAnalyzers = getUnboundAnalyzerByCharFilterTwo();

		assertFalse(unboundAnalyzers.isEmpty());

		//Must not have other analyzer except analyzer 2
		assertTrue(unboundAnalyzers.stream()
			.anyMatch(analyzer ->
				ANALYZER_ONE_NAME.equalsIgnoreCase(analyzer.getName())));

		assertTrue(unboundAnalyzers.stream()
			.anyMatch(analyzer ->
				ANALYZER_UNBOUND_NAME.equalsIgnoreCase(analyzer.getName())));

		//Must not have analyzer 2
		assertFalse(unboundAnalyzers.stream()
			.anyMatch(analyzer ->
				ANALYZER_TWO_NAME.equalsIgnoreCase(analyzer.getName())));

		assertEquals(allAnalyzerCount() - 1, unboundAnalyzers.size());

	}

	@Test
	@Order(5)
	void should_retrieve_unbound_analyzer_from_missing_char_filter() {
		var unboundAnalyzer = getUnboundAnalyzerByMissingCharFilter();

		assertFalse(unboundAnalyzer.isEmpty());

		assertTrue(unboundAnalyzer.stream()
			.anyMatch(analyzer ->
				ANALYZER_ONE_NAME.equalsIgnoreCase(analyzer.getName())));
		assertTrue(unboundAnalyzer.stream()
			.anyMatch(analyzer ->
				ANALYZER_TWO_NAME.equalsIgnoreCase(analyzer.getName())));
		assertTrue(unboundAnalyzer.stream()
			.anyMatch(analyzer ->
				ANALYZER_UNBOUND_NAME.equalsIgnoreCase(analyzer.getName())));

		assertEquals(allAnalyzerCount(), unboundAnalyzer.size());
	}

	@Test
	@Order(6)
	void should_remove_all_entities_used() {
		removeAnalyzerOne();
		removeAnalyzerTwo();
		removeAnalyzerUnbound();

		removeTokenFilterOne();
		removeTokenFilterTwo();
		removeTokenFilterThree();

		removeCharFilterOne();
		removeCharFilterTwo();
		removeCharFilterThree();
	}

	private Long allAnalyzerCount() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				analyzerService.count()
		)
		.await()
		.indefinitely();
	}

	private void bindAnalyzerOneToTokenFilterOne() {
		var analyzerOneId = getAnalyzerOne().getId();
		var tokenFilterOneId = getTokenFilterOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.addTokenFilterToAnalyzer(
						analyzerOneId, tokenFilterOneId)
			)
			.await()
			.indefinitely();
	}

	private void bindAnalyzerTwoToTokenFilterTwo() {
		var analyzerTwoId = getAnalyzerTwo().getId();
		var tokenFilterTwoId = getTokenFilterTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.addTokenFilterToAnalyzer(
						analyzerTwoId, tokenFilterTwoId)
			)
			.await()
			.indefinitely();
	}

	private void bindAnalyzerTwoToTokenFilterThree() {
		var analyzerTwoId = getAnalyzerTwo().getId();
		var tokenFilterThreeId = getTokenFilterThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.addTokenFilterToAnalyzer(
						analyzerTwoId, tokenFilterThreeId)
			)
			.await()
			.indefinitely();
	}

	private void bindAnalyzerOneToCharFilterOne() {
		var analyzerOneId = getAnalyzerOne().getId();
		var charFilterOneId = getCharFilterOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.addCharFilterToAnalyzer(
						analyzerOneId, charFilterOneId)
			)
			.await()
			.indefinitely();
	}

	private void bindAnalyzerTwoToCharFilterTwo() {
		var analyzerTwoId = getAnalyzerTwo().getId();
		var charFilterTwoId = getCharFilterTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.addCharFilterToAnalyzer(
						analyzerTwoId, charFilterTwoId)
			)
			.await()
			.indefinitely();
	}

	private void bindAnalyzerTwoToCharFilterThree() {
		var analyzerTwoId = getAnalyzerTwo().getId();
		var charFilterThreeId = getCharFilterThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.addCharFilterToAnalyzer(
						analyzerTwoId, charFilterThreeId)
			)
			.await()
			.indefinitely();
	}

	private void createAnalyzerOne() {
		AnalyzerDTO dto = AnalyzerDTO.builder()
			.name(ANALYZER_ONE_NAME)
			.type(CUSTOM)
			.build();

		sessionFactory.withTransaction(
			(s, transaction) ->
				analyzerService.create(s, dto)
		)
		.await()
		.indefinitely();
	}

	private void createAnalyzerTwo() {
		AnalyzerDTO dto = AnalyzerDTO.builder()
			.name(ANALYZER_TWO_NAME)
			.type(CUSTOM)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createAnalyzerUnbound() {
		AnalyzerDTO dto = AnalyzerDTO.builder()
			.name(ANALYZER_UNBOUND_NAME)
			.type(CUSTOM)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createCharFilterOne() {
		CharFilterDTO dto = CharFilterDTO.builder()
			.name(CHAR_FILTER_ONE_NAME)
			.type(STRING_BLANK)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createCharFilterTwo() {
		CharFilterDTO dto = CharFilterDTO.builder()
			.name(CHAR_FILTER_TWO_NAME)
			.type(STRING_BLANK)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createCharFilterThree() {
		CharFilterDTO dto = CharFilterDTO.builder()
			.name(CHAR_FILTER_THREE_NAME)
			.type(STRING_BLANK)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createTokenFilterOne() {
		TokenFilterDTO dto = TokenFilterDTO.builder()
			.name(TOKEN_FILTER_ONE_NAME)
			.type(STRING_BLANK)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createTokenFilterTwo() {
		TokenFilterDTO dto = TokenFilterDTO.builder()
			.name(TOKEN_FILTER_TWO_NAME)
			.type(STRING_BLANK)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private void createTokenFilterThree() {
		TokenFilterDTO dto = TokenFilterDTO.builder()
			.name(TOKEN_FILTER_THREE_NAME)
			.type(STRING_BLANK)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.create(s, dto)
			)
			.await()
			.indefinitely();
	}

	private List<Analyzer> getAnalyzerAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.findAll()
			)
			.await()
			.indefinitely();
	}

	private Analyzer getAnalyzerOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.findByName(s, ANALYZER_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private Analyzer getAnalyzerTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.findByName(s, ANALYZER_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private Analyzer getAnalyzerUnbound() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.findByName(s, ANALYZER_UNBOUND_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<CharFilter> getCharFilterAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.findAll()
			)
			.await()
			.indefinitely();
	}

	private CharFilter getCharFilterOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.findByName(s, CHAR_FILTER_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private CharFilter getCharFilterTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.findByName(s, CHAR_FILTER_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private CharFilter getCharFilterThree() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.findByName(s, CHAR_FILTER_THREE_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<TokenFilter> getTokenFilterAll() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.findAll()
			)
			.await()
			.indefinitely();
	}

	private TokenFilter getTokenFilterOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.findByName(s, TOKEN_FILTER_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private TokenFilter getTokenFilterTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.findByName(s, TOKEN_FILTER_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private TokenFilter getTokenFilterThree() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.findByName(s, TOKEN_FILTER_THREE_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<Analyzer> getUnboundAnalyzerByCharFilterTwo() {
		var charFilterId = getCharFilterTwo().getId();

		return analyzerService.findUnboundAnalyzersByCharFilter(charFilterId)
			.await()
			.indefinitely();
	}

	private List<Analyzer> getUnboundAnalyzerByMissingCharFilter() {
		return analyzerService.findUnboundAnalyzersByCharFilter(0L)
			.await()
			.indefinitely();
	}

	private List<Analyzer> getUnboundAnalyzerByTokenFilterTwo() {
		var tokenFilterId = getTokenFilterTwo().getId();

		return analyzerService.findUnboundAnalyzersByTokenFilter(tokenFilterId)
			.await()
			.indefinitely();
	}

	private List<Analyzer> getUnboundAnalyzerByMissingTokenFilter() {
		return analyzerService.findUnboundAnalyzersByTokenFilter(0L)
			.await()
			.indefinitely();
	}

	private void removeAnalyzerOne() {
		var analyzerId = getAnalyzerOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.deleteById(analyzerId)
			)
			.await()
			.indefinitely();
	}

	private void removeAnalyzerTwo() {
		var analyzerId = getAnalyzerTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.deleteById(analyzerId)
			)
			.await()
			.indefinitely();
	}

	private void removeAnalyzerUnbound() {
		var analyzerId = getAnalyzerUnbound().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					analyzerService.deleteById(analyzerId)
			)
			.await()
			.indefinitely();
	}

	private void removeCharFilterOne() {
		var charFilterId = getCharFilterOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.deleteById(charFilterId)
			)
			.await()
			.indefinitely();
	}

	private void removeCharFilterTwo() {
		var charFilterId = getCharFilterTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.deleteById(charFilterId)
			)
			.await()
			.indefinitely();
	}

	private void removeCharFilterThree() {
		var charFilterId = getCharFilterThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					charFilterService.deleteById(charFilterId)
			)
			.await()
			.indefinitely();
	}

	private void removeTokenFilterOne() {
		var tokenFilterId = getTokenFilterOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.deleteById(tokenFilterId)
			)
			.await()
			.indefinitely();
	}

	private void removeTokenFilterTwo() {
		var tokenFilterId = getTokenFilterTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.deleteById(tokenFilterId)
			)
			.await()
			.indefinitely();
	}

	private void removeTokenFilterThree() {
		var tokenFilterId = getTokenFilterThree().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenFilterService.deleteById(tokenFilterId)
			)
			.await()
			.indefinitely();
	}

}
