package io.openk9.datasource.service;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.model.QueryAnalysis;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.dto.QueryAnalysisDTO;
import io.openk9.datasource.model.dto.QueryAnalysisWithListsDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryAnalysisWithListsTest {

	private static final QueryAnalysisDTO queryAnalysisDTO =
		io.openk9.datasource.model.init.QueryAnalysis.INSTANCE;
	public static final List<String> ANNOTATOR_NAME_TWO_ITEM_LIST =
		Arrays.asList(
			"token annotator",
			"keyword annotator");
	public static final List<String> ANNOTATOR_NAME_THREE_ITEM_LIST =
		Arrays.asList(
			"token annotator",
			"keyword annotator",
			"stopword annotator");
	public static final String PATCHED = "_PATCHED";
	public static final String QUERY_ANALYSIS_TEST_NAME_1 = "Query Analysis test 1";
	public static final String QUERY_ANALYSIS_TEST_NAME_2 = "Query Analysis test 2";
	public static final String QUERY_ANALYSIS_WITH_LISTS = "QueryAnalysis with lists";
	public static final String QUERY_ANALYSIS_WITH_ONLY_NAME = "QueryAnalysis with only name";
	public static final List<String> RULE_NAME_TWO_ITEM_LIST =
		Arrays.asList(
			"$ROOT_$Query",
			"$Query_$Collection");
	public static final List<String> RULE_NAME_THREE_ITEM_LIST =
		Arrays.asList(
			"$ROOT_$Query",
			"$Query_$Collection",
			"$Part_$Intent");
	public static final String UPDATED = "_UPDATED";

	@Inject
	AnnotatorService annotatorService;

	@Inject
	QueryAnalysisService queryAnalysisService;

	@Inject
	RuleService ruleService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void initQueryAnalysisEntity(){

		var annotatorIds = ANNOTATOR_NAME_THREE_ITEM_LIST.stream()
			.map(this::getAnnotator)
			.map(Annotator::getId)
			.collect(Collectors.toSet());

		var ruleIds = RULE_NAME_THREE_ITEM_LIST.stream()
			.map(this::getRule)
			.map(Rule::getId)
			.collect(Collectors.toSet());

		QueryAnalysisWithListsDTO onlyNameDTO =
			QueryAnalysisWithListsDTO.builder()
				.name(QUERY_ANALYSIS_WITH_ONLY_NAME)
				.build();

		QueryAnalysisWithListsDTO withListsDTO =
			QueryAnalysisWithListsDTO.builder()
				.name(QUERY_ANALYSIS_WITH_LISTS)
				.annotatorsIds(annotatorIds)
				.rulesIds(ruleIds)
				.build();

		createQueryAnalysis(onlyNameDTO);
		createQueryAnalysis(withListsDTO);
	}

	@AfterEach
	void removeQueryAnalysisEntity() {

		var onlyNameId = getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME).getId();
		var withListsId = getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS).getId();

		removeQueryAnalysis(onlyNameId);
		removeQueryAnalysis(withListsId);
	}

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

		var annotatorIds = ANNOTATOR_NAME_THREE_ITEM_LIST.stream()
			.map(this::getAnnotator)
			.map(Annotator::getId)
			.collect(Collectors.toSet());

		var ruleIds = RULE_NAME_THREE_ITEM_LIST.stream()
			.map(this::getRule)
			.map(Rule::getId)
			.collect(Collectors.toSet());

		QueryAnalysisWithListsDTO withListsDTO =
			QueryAnalysisWithListsDTO.builder()
				.name(QUERY_ANALYSIS_TEST_NAME_2)
				.annotatorsIds(annotatorIds)
				.rulesIds(ruleIds)
				.build();

		createQueryAnalysis(withListsDTO);

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_TEST_NAME_2);

		assertEquals(QUERY_ANALYSIS_TEST_NAME_2, queryAnalysis.getName());
		assertEquals(RULE_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getRules().size());
		assertEquals(ANNOTATOR_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getAnnotators().size());
	}

	@Test
	void should_patch_query_analysis_name() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_ONLY_NAME, queryAnalysis.getName());
		assertEquals(0, queryAnalysis.getRules().size());
		assertEquals(0, queryAnalysis.getAnnotators().size());

		//Check if only the name has been changed
		patchQueryAnalysisNameWithEmptyLists(
			queryAnalysis.getId(), QUERY_ANALYSIS_WITH_ONLY_NAME + PATCHED);

		var queryAnalysisPatched =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME + PATCHED);

		assertNotNull(queryAnalysisPatched);
		assertEquals(
			QUERY_ANALYSIS_WITH_ONLY_NAME + PATCHED, queryAnalysisPatched.getName());
		assertEquals(0, queryAnalysisPatched.getRules().size());
		assertEquals(0, queryAnalysisPatched.getAnnotators().size());

		//restore query analysis with the init name for @AfterAll delete method
		patchQueryAnalysisNameWithEmptyLists(
			queryAnalysis.getId(), QUERY_ANALYSIS_WITH_ONLY_NAME);
	}

	@Test
	void should_not_change_with_empty_lists_patch() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysis.getName());
		assertEquals(RULE_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getAnnotators().size());

		//Check if query analysis has not been changed
		patchQueryAnalysisNameWithEmptyLists(queryAnalysis.getId(), QUERY_ANALYSIS_WITH_LISTS);

		var queryAnalysisPatched =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysisPatched.getName());
		assertEquals(RULE_NAME_THREE_ITEM_LIST.size(), queryAnalysisPatched.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_THREE_ITEM_LIST.size(), queryAnalysisPatched.getAnnotators().size());

	}

	@Test
	void should_patch_empty_lists_query_analysis_with_two_item_lists() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_ONLY_NAME, queryAnalysis.getName());
		assertEquals(0, queryAnalysis.getRules().size());
		assertEquals(0, queryAnalysis.getAnnotators().size());

		patchQueryAnalysisWithTwoItemLists(
			queryAnalysis.getId(), QUERY_ANALYSIS_WITH_ONLY_NAME);

		var queryAnalysisPatched =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME);

		assertEquals(QUERY_ANALYSIS_WITH_ONLY_NAME, queryAnalysisPatched.getName());
		assertEquals(RULE_NAME_TWO_ITEM_LIST.size(), queryAnalysisPatched.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_TWO_ITEM_LIST.size(), queryAnalysisPatched.getAnnotators().size());

	}

	@Test
	void should_patch_three_item_lists_query_analysis_with_two_item_lists() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysis.getName());
		assertEquals(RULE_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getAnnotators().size());

		patchQueryAnalysisWithTwoItemLists(queryAnalysis.getId(), QUERY_ANALYSIS_WITH_LISTS);

		var queryAnalysisPatched =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysisPatched.getName());
		assertEquals(RULE_NAME_TWO_ITEM_LIST.size(), queryAnalysisPatched.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_TWO_ITEM_LIST.size(), queryAnalysisPatched.getAnnotators().size());

	}

	@Test
	void should_update_query_analysis_name() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_ONLY_NAME, queryAnalysis.getName());
		assertEquals(0, queryAnalysis.getRules().size());
		assertEquals(0, queryAnalysis.getAnnotators().size());

		//Check if the name has been changed
		updateQueryAnalysisNameWithEmptyLists(
			queryAnalysis.getId(), QUERY_ANALYSIS_WITH_ONLY_NAME + UPDATED);

		var queryAnalysisUpdated =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME + UPDATED);

		assertNotNull(queryAnalysisUpdated);
		assertEquals(
			QUERY_ANALYSIS_WITH_ONLY_NAME + UPDATED, queryAnalysisUpdated.getName());
		assertEquals(0, queryAnalysisUpdated.getRules().size());
		assertEquals(0, queryAnalysisUpdated.getAnnotators().size());

		//restore query analysis with the init name for @AfterAll delete method
		updateQueryAnalysisNameWithEmptyLists(
			queryAnalysis.getId(), QUERY_ANALYSIS_WITH_ONLY_NAME);
	}

	@Test
	void should_update_empty_lists_query_analysis_with_two_item_lists() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_ONLY_NAME, queryAnalysis.getName());
		assertEquals(0, queryAnalysis.getRules().size());
		assertEquals(0, queryAnalysis.getAnnotators().size());

		updateQueryAnalysisWithTwoItemLists(
			queryAnalysis.getId(), QUERY_ANALYSIS_WITH_ONLY_NAME);

		var queryAnalysisUpdated =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_ONLY_NAME);

		assertEquals(QUERY_ANALYSIS_WITH_ONLY_NAME, queryAnalysisUpdated.getName());
		assertEquals(RULE_NAME_TWO_ITEM_LIST.size(), queryAnalysisUpdated.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_TWO_ITEM_LIST.size(), queryAnalysisUpdated.getAnnotators().size());

	}

	@Test
	void should_update_query_analysis_with_empty_lists() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysis.getName());
		assertEquals(RULE_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getAnnotators().size());

		updateQueryAnalysisNameWithEmptyLists(queryAnalysis.getId(), QUERY_ANALYSIS_WITH_LISTS);

		var queryAnalysisUpdated =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysisUpdated.getName());
		assertEquals(0, queryAnalysisUpdated.getRules().size());
		assertEquals(0, queryAnalysisUpdated.getAnnotators().size());

	}

	@Test
	void should_update_three_item_lists_query_analysis_with_two_item_lists() {

		var queryAnalysis = getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		//check initial state
		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysis.getName());
		assertEquals(RULE_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_THREE_ITEM_LIST.size(), queryAnalysis.getAnnotators().size());

		updateQueryAnalysisWithTwoItemLists(queryAnalysis.getId(), QUERY_ANALYSIS_WITH_LISTS);

		var queryAnalysisPatched =
			getQueryAnalysis(QUERY_ANALYSIS_WITH_LISTS);

		assertEquals(QUERY_ANALYSIS_WITH_LISTS, queryAnalysisPatched.getName());
		assertEquals(RULE_NAME_TWO_ITEM_LIST.size(), queryAnalysisPatched.getRules().size());
		assertEquals(
			ANNOTATOR_NAME_TWO_ITEM_LIST.size(), queryAnalysisPatched.getAnnotators().size());

	}

	private void createQueryAnalysis(QueryAnalysisWithListsDTO withListsDTO) {

		sessionFactory.withTransaction(
				(s, transactions) ->
					queryAnalysisService.create(withListsDTO)
			)
			.await()
			.indefinitely();
	}

	private QueryAnalysis getQueryAnalysis(String name) {

		return sessionFactory.withTransaction(
				(s, transactions) ->
					queryAnalysisService.findByName(s, name)
						.call(queryAnalysis ->
							Mutiny.fetch(queryAnalysis.getRules()))
						.call(queryAnalysis ->
							Mutiny.fetch(queryAnalysis.getAnnotators()))
			)
			.await()
			.indefinitely();
	}

	private Annotator getAnnotator(String name) {
		return sessionFactory.withTransaction(
				(s, transactions) ->
					annotatorService.findByName(s, name)
			)
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

	private void patchQueryAnalysisNameWithEmptyLists(Long id, String name) {

		QueryAnalysisWithListsDTO dto =
			QueryAnalysisWithListsDTO.builder()
				.name(name)
				.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					queryAnalysisService.patch(id, dto)
			)
			.await()
			.indefinitely();
	}

	private void patchQueryAnalysisWithTwoItemLists(Long id, String name) {

		var annotatorIds = ANNOTATOR_NAME_TWO_ITEM_LIST.stream()
			.map(this::getAnnotator)
			.map(Annotator::getId)
			.collect(Collectors.toSet());

		var ruleIds = RULE_NAME_TWO_ITEM_LIST.stream()
			.map(this::getRule)
			.map(Rule::getId)
			.collect(Collectors.toSet());

		QueryAnalysisWithListsDTO dto =
			QueryAnalysisWithListsDTO.builder()
				.name(name)
				.annotatorsIds(annotatorIds)
				.rulesIds(ruleIds)
				.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					queryAnalysisService.patch(id, dto)
			)
			.await()
			.indefinitely();
	}

	private void removeQueryAnalysis(Long id) {
		sessionFactory.withTransaction(
			(s, transaction) ->
				queryAnalysisService.deleteById(id)
			)
			.await()
			.indefinitely();
	}

	private void updateQueryAnalysisNameWithEmptyLists(Long id, String name) {

		QueryAnalysisWithListsDTO dto =
			QueryAnalysisWithListsDTO.builder()
				.name(name)
				.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					queryAnalysisService.update(id, dto)
			)
			.await()
			.indefinitely();
	}

	private void updateQueryAnalysisWithTwoItemLists(Long id, String name) {

		var annotatorIds = ANNOTATOR_NAME_TWO_ITEM_LIST.stream()
			.map(this::getAnnotator)
			.map(Annotator::getId)
			.collect(Collectors.toSet());

		var ruleIds = RULE_NAME_TWO_ITEM_LIST.stream()
			.map(this::getRule)
			.map(Rule::getId)
			.collect(Collectors.toSet());

		QueryAnalysisWithListsDTO dto =
			QueryAnalysisWithListsDTO.builder()
				.name(name)
				.annotatorsIds(annotatorIds)
				.rulesIds(ruleIds)
				.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					queryAnalysisService.update(id, dto)
			)
			.await()
			.indefinitely();
	}
}
