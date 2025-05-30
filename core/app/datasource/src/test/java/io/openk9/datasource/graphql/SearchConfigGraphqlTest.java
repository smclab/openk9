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

package io.openk9.datasource.graphql;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.SearchConfig;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.service.SearchConfigService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.InputObject;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class SearchConfigGraphqlTest {


	private static final String ENTITY = "entity";
	private static final String ENTITY_NAME_PREFIX = "SearchConfigGraphqlTest - ";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String JSON_CONFIG = "jsonConfig";
	private static final String MESSAGE = "message";
	private static final String MIN_SCORE = "minScore";
	private static final String MIN_SCORE_SUGGESTIONS = "minScoreSuggestions";
	private static final String MIN_SCORE_SEARCH = "minScoreSearch";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String QUERY_PARSERS = "queryParsers";
	private static final String QUERY_PARSER_ONE_NAME = ENTITY_NAME_PREFIX + "Query parser 1";
	private static final String QUERY_PARSER_TWO_NAME = ENTITY_NAME_PREFIX + "Query parser 2";
	private static final String QUERY_PARSER_THREE_NAME = ENTITY_NAME_PREFIX + "Query parser 3";
	private static final String QUERY_PARSER_FOUR_NAME = ENTITY_NAME_PREFIX + "Query parser 4";
	private static final String SEARCH_CONFIG_ONE_NAME = ENTITY_NAME_PREFIX + "Search config 1";
	private static final String SEARCH_CONFIG_TWO_NAME = ENTITY_NAME_PREFIX + "Search config 2";
	private static final String SEARCH_CONFIG_WITH_QUERY_PARSERS = "searchConfigWithQueryParsers";
	private static final String SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO = "searchConfigWithQueryParsersDTO";
	private static final String TYPE = "type";
	private static final String TYPE_ENTITY = "ENTITY";
	// QueryParserConfigDTO for entity creation and list
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
	private static final List<QueryParserConfigDTO> PARSER_CONFIG_DTO_LIST =
		List.of(QUERY_PARSER_DTO_ONE, QUERY_PARSER_DTO_TWO, QUERY_PARSER_DTO_THREE);
	// InputObject for mutations and list
	private static final InputObject QUERY_PARSER_INPUT_ONE = inputObject(
		prop(NAME, QUERY_PARSER_ONE_NAME),
		prop(TYPE, TYPE_ENTITY)
	);
	private static final InputObject QUERY_PARSER_INPUT_TWO = inputObject(
		prop(NAME, QUERY_PARSER_TWO_NAME),
		prop(TYPE, TYPE_ENTITY)
	);
	private static final InputObject QUERY_PARSER_INPUT_THREE = inputObject(
		prop(NAME, QUERY_PARSER_THREE_NAME),
		prop(TYPE, TYPE_ENTITY)
	);
	private static final InputObject QUERY_PARSER_INPUT_FOUR = inputObject(
		prop(NAME, QUERY_PARSER_FOUR_NAME),
		prop(TYPE, TYPE_ENTITY)
	);
	private static final List<InputObject> QUERY_PARSERS_INPUT_LIST =
		List.of(QUERY_PARSER_INPUT_ONE, QUERY_PARSER_INPUT_TWO, QUERY_PARSER_INPUT_THREE);

	private static final Logger log = Logger.getLogger(SearchConfigGraphqlTest.class);

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	SearchConfigService searchConfigService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		// creates searchConfigTwo with 3 queryParsers
		EntitiesUtils.createSearchConfig(
			searchConfigService,
			SEARCH_CONFIG_TWO_NAME,
			PARSER_CONFIG_DTO_LIST
		);
	}

	@Test
	void should_create_search_config_with_query_analysis()
		throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_ONE_NAME),
								prop(MIN_SCORE, 0F),
								prop(MIN_SCORE_SUGGESTIONS, false),
								prop(MIN_SCORE_SEARCH, false),
								prop(QUERY_PARSERS, QUERY_PARSERS_INPUT_LIST)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_ONE_NAME
			);

		var queryParserConfigs = searchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				searchConfig
			)
		);

		assertEquals(QUERY_PARSERS_INPUT_LIST.size(), queryParserConfigs.size());

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
	void should_create_search_config_with_empty_query_analysis()
		throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_ONE_NAME),
								prop(MIN_SCORE, 0F),
								prop(MIN_SCORE_SUGGESTIONS, false),
								prop(MIN_SCORE_SEARCH, false),
								prop(
									QUERY_PARSERS,
									new ArrayList<>()
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_ONE_NAME
			);

		var queryParserConfigs = searchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				searchConfig
			)
		);

		assertEquals(0, queryParserConfigs.size());

		// removes entity
		EntitiesUtils.removeSearchConfig(sessionFactory, searchConfigService, searchConfig.getName());
	}

	@Test
	void should_patch_search_config_with_query_analysis()
		throws ExecutionException, InterruptedException {

		float minScoreNew = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
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

		// creates mutation
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(ID, searchConfig.getId()),
						arg(PATCH, true),
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_TWO_NAME),
								prop(MIN_SCORE, minScoreNew),
								prop(MIN_SCORE_SUGGESTIONS, true),
								prop(MIN_SCORE_SEARCH, true),
								prop(
									QUERY_PARSERS,
									List.of(QUERY_PARSER_INPUT_FOUR)
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var actualSearchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				searchConfig.getName()
			);

		var queryParserConfigs = actualSearchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				actualSearchConfig
			)
		);

		var patchedParserActualNames = actualSearchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(1, queryParserConfigs.size());
		assertEquals(minScoreNew, actualSearchConfig.getMinScore());
		assertTrue(patchedParserActualNames.contains(QUERY_PARSER_FOUR_NAME));
		assertTrue(actualSearchConfig.isMinScoreSuggestions());
		assertTrue(actualSearchConfig.isMinScoreSearch());
	}

	@Test
	void should_patch_search_config_with_empty_query_analysis()
		throws ExecutionException, InterruptedException {

		float minScoreNew = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
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

		// creates mutation
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(ID, searchConfig.getId()),
						arg(PATCH, true),
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_TWO_NAME),
								prop(MIN_SCORE, minScoreNew),
								prop(MIN_SCORE_SUGGESTIONS, true),
								prop(MIN_SCORE_SEARCH, true),
								prop(
									QUERY_PARSERS,
									new ArrayList<>()
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var actualSearchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				searchConfig.getName()
			);

		var queryParserConfigs = actualSearchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				actualSearchConfig
			)
		);

		assertEquals(0, queryParserConfigs.size());
		assertEquals(minScoreNew, actualSearchConfig.getMinScore());
		assertTrue(actualSearchConfig.isMinScoreSuggestions());
		assertTrue(actualSearchConfig.isMinScoreSearch());
	}

	@Test
	void should_not_change_patching_search_config_with_no_query_analysis()
		throws ExecutionException, InterruptedException {

		float minScoreNew = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
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

		// creates mutation
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(ID, searchConfig.getId()),
						arg(PATCH, true),
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_TWO_NAME),
								prop(MIN_SCORE, minScoreNew),
								prop(MIN_SCORE_SUGGESTIONS, true),
								prop(MIN_SCORE_SEARCH, true)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var actualSearchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				searchConfig.getName()
			);

		var queryParserConfigs = actualSearchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				actualSearchConfig
			)
		);

		var patchedParserActualNames = actualSearchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(PARSER_CONFIG_DTO_LIST.size(), queryParserConfigs.size());
		assertEquals(minScoreNew, actualSearchConfig.getMinScore());
		assertTrue(initialParserExpectedNames.containsAll(patchedParserActualNames));
		assertTrue(actualSearchConfig.isMinScoreSuggestions());
		assertTrue(actualSearchConfig.isMinScoreSearch());
	}

	@Test
	void should_update_search_config_with_query_analysis()
		throws ExecutionException, InterruptedException {

		float minScoreNew = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
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

		// creates mutation
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(ID, searchConfig.getId()),
						arg(PATCH, false),
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_TWO_NAME),
								prop(MIN_SCORE, minScoreNew),
								prop(MIN_SCORE_SUGGESTIONS, true),
								prop(MIN_SCORE_SEARCH, true),
								prop(
									QUERY_PARSERS,
									List.of(QUERY_PARSER_INPUT_FOUR)
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var actualSearchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				searchConfig.getName()
			);

		var queryParserConfigs = actualSearchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				actualSearchConfig
			)
		);

		var updatedParserActualNames = actualSearchConfig.getQueryParserConfigs().stream()
			.map(QueryParserConfig::getName)
			.toList();

		assertEquals(1, queryParserConfigs.size());
		assertEquals(minScoreNew, actualSearchConfig.getMinScore());
		assertTrue(updatedParserActualNames.contains(QUERY_PARSER_FOUR_NAME));
		assertTrue(actualSearchConfig.isMinScoreSuggestions());
		assertTrue(actualSearchConfig.isMinScoreSearch());
	}

	@Test
	void should_update_search_config_with_empty_query_analysis()
		throws ExecutionException, InterruptedException {

		float minScoreNew = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
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

		// creates mutation
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(ID, searchConfig.getId()),
						arg(PATCH, false),
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_TWO_NAME),
								prop(MIN_SCORE, minScoreNew),
								prop(MIN_SCORE_SUGGESTIONS, true),
								prop(MIN_SCORE_SEARCH, true),
								prop(
									QUERY_PARSERS,
									new ArrayList<>()
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var actualSearchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				searchConfig.getName()
			);

		var queryParserConfigs = actualSearchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				actualSearchConfig
			)
		);

		assertEquals(0, queryParserConfigs.size());
		assertEquals(minScoreNew, actualSearchConfig.getMinScore());
		assertTrue(actualSearchConfig.isMinScoreSuggestions());
		assertTrue(actualSearchConfig.isMinScoreSearch());
	}

	@Test
	void should_update_search_config_with_no_query_analysis()
		throws ExecutionException, InterruptedException {

		float minScoreNew = 5F;

		// check initial state
		SearchConfig searchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				SEARCH_CONFIG_TWO_NAME
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

		// creates mutation
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					SEARCH_CONFIG_WITH_QUERY_PARSERS,
					args(
						arg(ID, searchConfig.getId()),
						arg(PATCH, false),
						arg(
							SEARCH_CONFIG_WITH_QUERY_PARSERS_DTO,
							inputObject(
								prop(NAME, SEARCH_CONFIG_TWO_NAME),
								prop(MIN_SCORE, minScoreNew),
								prop(MIN_SCORE_SUGGESTIONS, true),
								prop(MIN_SCORE_SEARCH, true)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var searchConfigResponse =
			response.getData().getJsonObject(SEARCH_CONFIG_WITH_QUERY_PARSERS);

		assertNotNull(searchConfigResponse);
		assertTrue(searchConfigResponse.isNull(FIELD_VALIDATORS));

		var actualSearchConfig =
			EntitiesUtils.getSearchConfig(
				sessionFactory,
				searchConfigService,
				searchConfig.getName()
			);

		var queryParserConfigs = actualSearchConfig.getQueryParserConfigs();

		log.info(
			String.format(
				"searchConfig (%d): %s",
				queryParserConfigs.size(),
				actualSearchConfig
			)
		);

		assertEquals(0, queryParserConfigs.size());
		assertEquals(minScoreNew, actualSearchConfig.getMinScore());
		assertTrue(actualSearchConfig.isMinScoreSuggestions());
		assertTrue(actualSearchConfig.isMinScoreSearch());
	}

	@AfterEach
	void tearDown() {
		// removes searchConfigTwo with 3 queryParsers
		EntitiesUtils.removeSearchConfig(
			sessionFactory,
			searchConfigService,
			SEARCH_CONFIG_TWO_NAME
		);
	}
}
