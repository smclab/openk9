package io.openk9.datasource.service;

import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.dto.QueryAnalysisDTO;
import io.openk9.datasource.model.dto.QueryAnalysisWithListsDTO;
import io.openk9.datasource.model.dto.RuleDTO;
import io.openk9.datasource.model.init.Rules;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class QueryAnalysisWithListsTest {

	private static final QueryAnalysisDTO queryAnalysisDTO =
		io.openk9.datasource.model.init.QueryAnalysis.INSTANCE;
	public static final String QUERY_ANALYSIS_TEST_NAME_1 = "Query Analysis test 1";
	public static final String QUERY_ANALYSIS_TEST_NAME_2 = "Query Analysis test 2";
	public static final String ROOT_ROLE_NAME = "$ROOT_$Query";

	@Inject
	RuleService ruleService;

	@Inject
	QueryAnalysisService queryAnalysisService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_create_query_analysis() {

		QueryAnalysisWithListsDTO withListsDTO =
			QueryAnalysisWithListsDTO.builder()
				.name(QUERY_ANALYSIS_TEST_NAME_1)
				.build();

		createQueryAnalysis(withListsDTO);
		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_TEST_NAME_1);

		assertEquals(QUERY_ANALYSIS_TEST_NAME_1, queryAnalysis.getName());
	}

	@Test
	void should_create_query_analysis_with_lists() {

		var rootRule = getRule(ROOT_ROLE_NAME);

		QueryAnalysisWithListsDTO withListsDTO =
			QueryAnalysisWithListsDTO.builder()
				.name(QUERY_ANALYSIS_TEST_NAME_2)
				//.annotatorsIds(Set.of(0L))
				.rulesIds(Set.of(rootRule.getId()))
				.build();

		createQueryAnalysis(withListsDTO);

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_TEST_NAME_2);

		assertEquals(QUERY_ANALYSIS_TEST_NAME_2, queryAnalysis.getName());
		assertEquals(1, queryAnalysis.getRules().size());
	}

	@Test
	@Disabled
	void should_patch_query_analysis() {

		QueryAnalysisWithListsDTO withListsDTO =
			QueryAnalysisWithListsDTO.builder()
				.name(QUERY_ANALYSIS_TEST_NAME_2)
				.build();

		createQueryAnalysis(withListsDTO);
		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_TEST_NAME_2);
	}

	@Test
	@Disabled
	void should_patch_query_analysis_with_lists() {

	}

	private QueryAnalysis getQueryAnalysis(String name) {

		return sessionFactory.withTransaction(
				(s, transactions) ->
					queryAnalysisService.findByName(s, name)
			)
			.call(queryAnalysis ->
				Mutiny.fetch(queryAnalysis.getRules()))
			.await()
			.indefinitely();
	}

	private Rule getRule(String name) {
		return sessionFactory.withTransaction(
				(s, transactions) ->
					ruleService.findByName(s, name)
			)
			.await()
			.indefinitely();
	}

	private QueryAnalysis createQueryAnalysis(QueryAnalysisWithListsDTO withListsDTO) {

		return sessionFactory.withTransaction(
				(s, transactions) ->
					queryAnalysisService.create(withListsDTO)
			)
			.await()
			.indefinitely();
	}

}
