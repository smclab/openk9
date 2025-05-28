package io.openk9.datasource.service;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.dto.request.SearchConfigWithQueryParsersDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class SearchConfigServiceTest {

	private static final String ENTITY_NAME_PREFIX = "SearchConfigServiceTest - ";
	private static final String JSON_CONFIG = "{\n\t\"boost\": 50,\n\t\"valuesQueryType\": \"MUST\",\n\t\"globalQueryType\": \"MUST\",\n\t\"fuzziness\":\"ZERO\"\n}\n";
	private static final String QUERY_PARSER_ONE_NAME = ENTITY_NAME_PREFIX + "Query parser 1";
	private static final String QUERY_PARSER_TWO_NAME = ENTITY_NAME_PREFIX + "Query parser 2";
	private static final String QUERY_PARSER_THREE_NAME = ENTITY_NAME_PREFIX + "Query parser 3";
	private static final String QUERY_PARSER_FOUR_NAME = ENTITY_NAME_PREFIX + "Query parser 4";
	private static final String SEARCH_CONFIG_ONE_NAME = ENTITY_NAME_PREFIX + "Search config 1";
	private static final String SEARCH_CONFIG_TWO_NAME = ENTITY_NAME_PREFIX + "Search config 2";
	private static final String SEARCH_CONFIG_THREE_NAME = ENTITY_NAME_PREFIX + "Search config 3";
	private static final String TYPE_ENTITY = "ENTITY";
	private static final QueryParserConfigDTO QUERY_PARSER_DTO_ONE =
		QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_ONE_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();
	private static final QueryParserConfigDTO QUERY_PARSER_DTO_TWO =
		QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_TWO_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();
	private static final QueryParserConfigDTO QUERY_PARSER_DTO_THREE =
		QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_THREE_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();
	private static final QueryParserConfigDTO QUERY_PARSER_DTO_FOUR =
		QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_FOUR_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();
	private static final List<QueryParserConfigDTO> PARSER_CONFIG_DTO_LIST =
		List.of(QUERY_PARSER_DTO_ONE, QUERY_PARSER_DTO_TWO, QUERY_PARSER_DTO_THREE);

	private static final Logger log = Logger.getLogger(SearchConfigServiceTest.class);

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		// creates searchConfigTwo with no queryParsers
		EntitiesUtils.createSearchConfig(searchConfigService, SEARCH_CONFIG_TWO_NAME, null);
		// creates searchConfigThree with 3 queryParsers
		EntitiesUtils.createSearchConfig(
			searchConfigService,
			SEARCH_CONFIG_THREE_NAME,
			PARSER_CONFIG_DTO_LIST
		);
	}

	@Test
	void should_create_search_config_with_query_analysis() {

		EntitiesUtils.createSearchConfig(
			searchConfigService,
			SEARCH_CONFIG_ONE_NAME,
			PARSER_CONFIG_DTO_LIST
		);

		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_ONE_NAME
			);

		Set<QueryParserConfig> queryParserConfigs = searchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				searchConfig
			)
		);

		assertEquals(PARSER_CONFIG_DTO_LIST.size(), queryParserConfigs.size());

		List<String> queryParserExpectedNames =
			List.of(QUERY_PARSER_ONE_NAME, QUERY_PARSER_TWO_NAME, QUERY_PARSER_THREE_NAME);
		List<String> queryParserActualNames = queryParserConfigs.stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertTrue(queryParserExpectedNames.containsAll(queryParserActualNames));

		// removes entity
		EntitiesUtils.removeSearchConfig(sessionFactory, searchConfigService, searchConfig.getName());
	}

	@Test
	void should_create_search_config_with_empty_query_analysis() {

		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(SEARCH_CONFIG_ONE_NAME)
			.minScore(0F)
			.minScoreSuggestions(false)
			.minScoreSearch(false)
			.build();

		searchConfigService.create(dto)
			.await()
			.indefinitely();

		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(sessionFactory, searchConfigService, SEARCH_CONFIG_ONE_NAME);

		log.info(
			String.format(
				"searchConfig (%d): %s",
				searchConfig.getQueryParserConfigs().size(),
				searchConfig
			)
		);

		assertEquals(0, searchConfig.getQueryParserConfigs().size());

		// removes entity
		EntitiesUtils.removeSearchConfig(sessionFactory, searchConfigService,searchConfig);
	}

	@Test
	void should_patch_search_config_two_with_query_analysis() {
		float minScorePatched = 5F;
		var queryParserDTOList = List.of(QUERY_PARSER_DTO_ONE, QUERY_PARSER_DTO_TWO);

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
			);

		assertEquals(0, searchConfig.getQueryParserConfigs().size());
		assertEquals(0F, searchConfig.getMinScore());
		assertFalse(searchConfig.isMinScoreSuggestions());
		assertFalse(searchConfig.isMinScoreSearch());

		// creates searchConfigDTO with queryParserConfigDTO to patch
		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(searchConfig.getName())
			.minScore(minScorePatched)
			.minScoreSuggestions(true)
			.minScoreSearch(true)
			.queryParsers(queryParserDTOList)
			.build();

		// patch the searchConfiguration
		searchConfigService.patch(searchConfig.getId(), dto)
			.await()
			.indefinitely();

		SearchConfig searchConfigPatched =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
			);

		var patchedParserExpectedNames = queryParserDTOList.stream()
			.map(QueryParserConfigDTO::getName)
			.toList();
		var patchedParserActualNames = searchConfigPatched.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		// check patched searchConfiguration
		assertEquals(queryParserDTOList.size(), searchConfigPatched.getQueryParserConfigs().size());
		assertEquals(minScorePatched, searchConfigPatched.getMinScore());
		assertTrue(patchedParserExpectedNames.containsAll(patchedParserActualNames));
		assertTrue(searchConfigPatched.isMinScoreSuggestions());
		assertTrue(searchConfigPatched.isMinScoreSearch());
	}

	@Test
	void should_patch_search_config_three_with_query_analysis() {
		float minScorePatched = 5F;
		var queryParserDTOList = List.of(QUERY_PARSER_DTO_FOUR);

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_THREE_NAME
			);

		var initialParserExpectedNames = PARSER_CONFIG_DTO_LIST.stream()
			.map(QueryParserConfigDTO::getName)
			.toList();
		var initialParserActualNames = searchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(PARSER_CONFIG_DTO_LIST.size(), searchConfig.getQueryParserConfigs().size());
		assertEquals(0F, searchConfig.getMinScore());
		assertTrue(initialParserExpectedNames.containsAll(initialParserActualNames));
		assertFalse(searchConfig.isMinScoreSuggestions());
		assertFalse(searchConfig.isMinScoreSearch());

		// creates searchConfigDTO with queryParserConfigDTO to patch
		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(searchConfig.getName())
			.minScore(minScorePatched)
			.minScoreSuggestions(true)
			.minScoreSearch(true)
			.queryParsers(queryParserDTOList)
			.build();

		// patch the searchConfiguration
		searchConfigService.patch(searchConfig.getId(), dto)
			.await()
			.indefinitely();

		SearchConfig searchConfigPatched =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_THREE_NAME
			);

		var patchedParserExpectedNames = queryParserDTOList.stream()
			.map(QueryParserConfigDTO::getName)
			.toList();
		var patchedParserActualNames = searchConfigPatched.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		// check patched searchConfiguration
		assertEquals(queryParserDTOList.size(), searchConfigPatched.getQueryParserConfigs().size());
		assertEquals(minScorePatched, searchConfigPatched.getMinScore());
		assertTrue(patchedParserExpectedNames.containsAll(patchedParserActualNames));
		assertTrue(searchConfigPatched.isMinScoreSuggestions());
		assertTrue(searchConfigPatched.isMinScoreSearch());
	}

	@Test
	void should_patch_search_config_three_with_empty_query_analysis() {
		float minScorePatched = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_THREE_NAME
			);

		var initialParserExpectedNames = PARSER_CONFIG_DTO_LIST.stream()
			.map(QueryParserConfigDTO::getName)
			.toList();
		var initialParserActualNames = searchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(PARSER_CONFIG_DTO_LIST.size(), searchConfig.getQueryParserConfigs().size());
		assertEquals(0F, searchConfig.getMinScore());
		assertTrue(initialParserExpectedNames.containsAll(initialParserActualNames));
		assertFalse(searchConfig.isMinScoreSuggestions());
		assertFalse(searchConfig.isMinScoreSearch());

		// creates searchConfigDTO with queryParserConfigDTO to patch
		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(searchConfig.getName())
			.minScore(minScorePatched)
			.minScoreSuggestions(true)
			.minScoreSearch(true)
			// set queryParsers to empty list
			.queryParsers(new ArrayList<>())
			.build();

		// patch the searchConfiguration
		searchConfigService.patch(searchConfig.getId(), dto)
			.await()
			.indefinitely();

		SearchConfig searchConfigPatched =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_THREE_NAME
			);

		// check patched searchConfiguration
		assertEquals(0, searchConfigPatched.getQueryParserConfigs().size());
		assertEquals(minScorePatched, searchConfigPatched.getMinScore());
		assertTrue(searchConfigPatched.isMinScoreSuggestions());
		assertTrue(searchConfigPatched.isMinScoreSearch());
	}

	@Test
	void should_not_change_patching_search_config_three_with_no_query_analysis() {
		float minScorePatched = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_THREE_NAME
			);

		var initialParserExpectedNames = PARSER_CONFIG_DTO_LIST.stream()
			.map(QueryParserConfigDTO::getName)
			.toList();
		var initialParserActualNames = searchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(PARSER_CONFIG_DTO_LIST.size(), searchConfig.getQueryParserConfigs().size());
		assertEquals(0F, searchConfig.getMinScore());
		assertTrue(initialParserExpectedNames.containsAll(initialParserActualNames));
		assertFalse(searchConfig.isMinScoreSuggestions());
		assertFalse(searchConfig.isMinScoreSearch());

		// creates searchConfigDTO with queryParserConfigDTO to patch
		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(searchConfig.getName())
			.minScore(minScorePatched)
			.minScoreSuggestions(true)
			.minScoreSearch(true)
			.build();

		// patch the searchConfiguration
		searchConfigService.patch(searchConfig.getId(), dto)
			.await()
			.indefinitely();

		SearchConfig searchConfigPatched =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_THREE_NAME
			);

		var patchedParserExpectedNames = PARSER_CONFIG_DTO_LIST.stream()
			.map(QueryParserConfigDTO::getName)
			.toList();
		var patchedParserActualNames = searchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		// check patched searchConfiguration
		assertEquals(
			PARSER_CONFIG_DTO_LIST.size(),
			searchConfigPatched.getQueryParserConfigs().size()
		);
		assertEquals(minScorePatched, searchConfigPatched.getMinScore());
		assertTrue(patchedParserExpectedNames.containsAll(patchedParserActualNames));
		assertTrue(searchConfigPatched.isMinScoreSuggestions());
		assertTrue(searchConfigPatched.isMinScoreSearch());
	}

	@AfterEach
	void tearDown() {
		// removes searchConfigTwo with no queryParsers
		EntitiesUtils.removeSearchConfig(
			sessionFactory,
			searchConfigService,
			SEARCH_CONFIG_TWO_NAME
		);
		// removes searchConfigThree with 3 queryParsers
		EntitiesUtils.removeSearchConfig(
			sessionFactory,
			searchConfigService,
			SEARCH_CONFIG_THREE_NAME
		);
	}
}
