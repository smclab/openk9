/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.BooleanOperator;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.util.Fuzziness;
import io.openk9.datasource.service.AutocompleteService;
import io.openk9.datasource.service.DocTypeFieldService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AutocompleteGraphqlTest {

	private static final Logger log = Logger.getLogger(AutocompleteGraphqlTest.class);

	private static final String AUTOCOMPLETE = "autocomplete";
	private static final String AUTOCOMPLETE_DTO = "autocompleteDTO";
	private static final String ENTITY_NAME_PREFIX = "AutocompleteGraphqlTest - ";
	private static final String AUTOCOMPLETE_NAME_ONE = ENTITY_NAME_PREFIX + "Autocomplete 1";
	private static final String AUTOCOMPLETE_NAME_TWO = ENTITY_NAME_PREFIX + "Autocomplete 2";
	private static final String DOC_TYPE_FIELD_NAME_ONE = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_NAME_TWO = ENTITY_NAME_PREFIX + "Doc type field 2";
	private static final String EDGES = "edges";
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_IDS = "fieldIds";
	private static final String FIELD_TYPE = "fieldType";
	private static final String FIELDS = "fields";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String FUZZINESS = "fuzziness";
	private static final String FUZZINESS_VALUE = "2";
	private static final String ID = "id";
	private static final String MESSAGE = "message";
	private static final String MINIMUM_SHOULD_MATCH = "minimumShouldMatch";
	private static final String MINIMUM_SHOULD_MATCH_VALUE = "50%";
	private static final String NAME = "name";
	private static final String NODE = "node";
	private static final String OPERATOR = "operator";
	private static final String PERFECT_MATCH_INCLUDED = "perfectMatchIncluded";
	private static final String RESULT_SIZE = "resultSize";
	private static final int RESULT_SIZE_VALUE = 5;

	@Inject
	AutocompleteService service;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sf;



	@BeforeEach
	void setup() {
		// Create DocTypeField one and two as child of the first sample field of type TEXT
		var allDocTypeFields = EntitiesUtils.getAllEntities(docTypeFieldService, sf);

		var firstSampleTextField = allDocTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.findFirst();

		var docTypeFieldId = 0L;

		if (firstSampleTextField.isPresent()) {
			docTypeFieldId = firstSampleTextField.get().getId();
		}

		DocTypeFieldDTO fieldDtoOne = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_ONE)
			.fieldName("sample.searchasyoutypeone")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();
		DocTypeFieldDTO fieldDtoTwo = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_TWO)
			.fieldName("sample.searchasyoutypetwo")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();

		EntitiesUtils.createSubField(docTypeFieldId, fieldDtoOne, docTypeFieldService);
		EntitiesUtils.createSubField(docTypeFieldId, fieldDtoTwo, docTypeFieldService);

		var fieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeFieldWithParent(docTypeFieldService, sf).stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		AutocompleteDTO autocompleteDTOTwo = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_TWO)
			.fieldIds(fieldIds)
			.perfectMatchIncluded(true)
			.build();

		EntitiesUtils.createEntity(autocompleteDTOTwo, service, sf);
	}

	@Test
	void should_retrieve_autocomplete_two() throws ExecutionException, InterruptedException {
		var autocompleteTwo = EntitiesUtils.getEntity(AUTOCOMPLETE_NAME_TWO, service, sf);

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					AUTOCOMPLETE,
					args(
						arg(ID, autocompleteTwo.getId())
					),
					field(ID),
					field(NAME),
					field(RESULT_SIZE),
					field(FUZZINESS),
					field(MINIMUM_SHOULD_MATCH),
					field(OPERATOR),
					field(PERFECT_MATCH_INCLUDED),
					field(
						FIELDS,
						field(
							EDGES,
							field(
								NODE,
								field(ID),
								field(NAME),
								field(FIELD_TYPE)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var autocompleteJson = response.getData().getJsonObject(AUTOCOMPLETE);

		assertEquals(
			AUTOCOMPLETE_NAME_TWO,
			autocompleteJson.getString(NAME)
		);
		assertTrue(autocompleteJson.getBoolean(PERFECT_MATCH_INCLUDED));
	}

	@Test
	void should_create_autocorrection_one() throws ExecutionException, InterruptedException {
		var fieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeFieldWithParent(docTypeFieldService, sf)
				.stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					AUTOCOMPLETE,
					args(
						arg(
							AUTOCOMPLETE_DTO,
							inputObject(
								prop(NAME, AUTOCOMPLETE_NAME_ONE),
								prop(RESULT_SIZE, RESULT_SIZE_VALUE),
								prop(FUZZINESS, Fuzziness.TWO),
								prop(MINIMUM_SHOULD_MATCH, MINIMUM_SHOULD_MATCH_VALUE),
								prop(OPERATOR, BooleanOperator.AND),
								prop(FIELD_IDS, fieldIds)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(RESULT_SIZE),
						field(FUZZINESS),
						field(MINIMUM_SHOULD_MATCH),
						field(OPERATOR),
						field(
							FIELDS,
							field(
								EDGES,
								field(
									NODE,
									field(ID),
									field(NAME),
									field(FIELD_TYPE)
								)
							)
						)
					),
					field(
						FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var clientResponse = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", clientResponse));

		assertFalse(clientResponse.hasError());
		assertTrue(clientResponse.hasData());

		var autocomplete =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_NAME_ONE, service, sf);

		log.debug(String.format("Autocorrection: %s", autocomplete.toString()));

		assertEquals(AUTOCOMPLETE_NAME_ONE, autocomplete.getName());
		assertEquals(RESULT_SIZE_VALUE, autocomplete.getResultSize());
		assertEquals(FUZZINESS_VALUE, autocomplete.getFuzziness().getValue());
		assertEquals(MINIMUM_SHOULD_MATCH_VALUE, autocomplete.getMinimumShouldMatch());
		assertEquals(BooleanOperator.AND, autocomplete.getOperator());
		assertNotNull(autocomplete.getFields());
		assertEquals(fieldIds.size(), autocomplete.getFields().size());

		EntitiesUtils.removeEntity(AUTOCOMPLETE_NAME_ONE, service, sf);
	}

	@AfterEach
	void tearDown() {
		try {
			EntitiesUtils.removeEntity(AUTOCOMPLETE_NAME_ONE, service, sf);
		}
		catch (Exception e) {
			log.debugf("Autocomplete with name \"%s\" does not exist.", AUTOCOMPLETE_NAME_ONE);
		}
		EntitiesUtils.removeEntity(AUTOCOMPLETE_NAME_TWO, service, sf);

		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_ONE, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_TWO, docTypeFieldService, sf);
	}
}
