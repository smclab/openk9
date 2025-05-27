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
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SearchConfigServiceTest {

	private static final String ENTITY_NAME_PREFIX = "SearchConfigServiceTest - ";
	private static final String JSON_CONFIG = "{\n\t\"boost\": 50,\n\t\"valuesQueryType\": \"MUST\",\n\t\"globalQueryType\": \"MUST\",\n\t\"fuzziness\":\"ZERO\"\n}\n";
	private static final String QUERY_PARSER_ONE_NAME = ENTITY_NAME_PREFIX + "Query parser 1";
	private static final String QUERY_PARSER_TWO_NAME = ENTITY_NAME_PREFIX + "Query parser 2";
	private static final String QUERY_PARSER_THREE_NAME = ENTITY_NAME_PREFIX + "Query parser 3";
	private static final String SEARCH_CONFIG_NAME = ENTITY_NAME_PREFIX + "Search config 1";
	private static final String TYPE_ENTITY = "ENTITY";

	private static final Logger log = Logger.getLogger(SearchConfigServiceTest.class);

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_create_search_config_with_query_analysis() {

		QueryParserConfigDTO queryParserDTOOne = QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_ONE_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();

		QueryParserConfigDTO queryParserDTOTwo = QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_TWO_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();

		QueryParserConfigDTO queryParserDTOThree = QueryParserConfigDTO.builder()
			.name(QUERY_PARSER_THREE_NAME)
			.type(TYPE_ENTITY)
			.jsonConfig(JSON_CONFIG)
			.build();

		List<QueryParserConfigDTO> configDTOList =
			List.of(queryParserDTOOne, queryParserDTOTwo, queryParserDTOThree);

		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(SEARCH_CONFIG_NAME)
			.minScore(0F)
			.minScoreSuggestions(false)
			.minScoreSearch(false)
			.queryParsers(configDTOList)
			.build();

		searchConfigService.create(dto)
			.await()
			.indefinitely();

		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(sessionFactory, searchConfigService, SEARCH_CONFIG_NAME);

		Set<QueryParserConfig> queryParserConfigs = searchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				searchConfig
			)
		);

		assertEquals(3, queryParserConfigs.size());

		List<String> queryParserExpectedNames =
			List.of(QUERY_PARSER_ONE_NAME, QUERY_PARSER_TWO_NAME, QUERY_PARSER_THREE_NAME);
		List<String> queryParserActualNames = queryParserConfigs.stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(queryParserExpectedNames, queryParserActualNames);

		// removes entity
		EntitiesUtils.removeSearchConfig(sessionFactory, searchConfigService, searchConfig.getName());
	}

	@Test
	void should_create_search_config_with_no_query_analysis() {

		SearchConfigWithQueryParsersDTO dto = SearchConfigWithQueryParsersDTO.builder()
			.name(SEARCH_CONFIG_NAME)
			.minScore(0F)
			.minScoreSuggestions(false)
			.minScoreSearch(false)
			.build();

		searchConfigService.create(dto)
			.await()
			.indefinitely();

		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(sessionFactory, searchConfigService, SEARCH_CONFIG_NAME);

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
}
