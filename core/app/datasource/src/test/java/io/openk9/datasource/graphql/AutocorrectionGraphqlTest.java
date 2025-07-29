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

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SortType;
import io.openk9.datasource.model.SuggestMode;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.AutocorrectionService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
public class AutocorrectionGraphqlTest {

	private static final Logger log = Logger.getLogger(AutocorrectionGraphqlTest.class);
	private static final String AUTOCORRECTION = "autocorrection";
	private static final String AUTOCORRECTION_DOC_TYPE_FIELD = "autocorrectionDocTypeField";
	private static final String AUTOCORRECTION_DOC_TYPE_FIELD_ID = "autocorrectionDocTypeFieldId";
	private static final String AUTOCORRECTION_DTO = "autocorrectionDTO";
	private static final String ENTITY_NAME_PREFIX = "AutocorrectionGraphqlTest - ";
	private static final String AUTOCORRECTION_NAME_ONE = ENTITY_NAME_PREFIX + "Autocorrection 1";
	private static final String AUTOCORRECTION_NAME_TWO = ENTITY_NAME_PREFIX + "Autocorrection 2";
	private static final String ENTITY = "entity";
	private static final String ENABLE_SEARCH_WITH_CORRECTION = "enableSearchWithCorrection";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String MAX_EDIT = "maxEdit";
	private static final int MAX_EDIT_VALUE = 2;
	private static final String MESSAGE = "message";
	private static final String MIN_WORD_LENGTH = "minWordLength";
	private static final int MIN_WORD_LENGTH_VALUE = 3;
	private static final String NAME = "name";
	private static final String PREFIX_LENGTH = "prefixLength";
	private static final int PREFIX_LENGTH_VALUE = 3;
	private static final String SORT = "sort";
	private static final String SUGGEST_MODE = "suggestMode";

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		var docTypeFields = getAllDocTypeFields();

		var docTypeFieldId = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.map(K9Entity::getId)
			.findFirst()
			.orElse(0L);

		AutocorrectionDTO autocorrectionDTOTwo = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_TWO)
			.autocorrectionDocTypeFieldId(docTypeFieldId)
			.maxEdit(MAX_EDIT_VALUE)
			.minWordLength(MIN_WORD_LENGTH_VALUE)
			.prefixLength(PREFIX_LENGTH_VALUE)
			.build();

		EntitiesUtils.createEntity(
			autocorrectionDTOTwo,
			autocorrectionService,
			sessionFactory
		);
	}

	@Test
	void should_retrieve_autocorrection_two() throws ExecutionException, InterruptedException {
		var autocorrectionTwo = EntitiesUtils.getAutocorrection(
			AUTOCORRECTION_NAME_TWO,
			autocorrectionService,
			sessionFactory
		);

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					AUTOCORRECTION,
					args(
						arg(ID, autocorrectionTwo.getId())
					),
					field(ID),
					field(NAME),
					field(SORT),
					field(SUGGEST_MODE),
					field(PREFIX_LENGTH),
					field(MIN_WORD_LENGTH),
					field(MAX_EDIT),
					field(ENABLE_SEARCH_WITH_CORRECTION),
					field(
						AUTOCORRECTION_DOC_TYPE_FIELD,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var autocorrectionJson = response.getData().getJsonObject(AUTOCORRECTION);

		assertEquals(
			AUTOCORRECTION_NAME_TWO,
			autocorrectionJson.getString(NAME)
		);
	}

	@Test
	void should_create_autocorrection_one() throws ExecutionException, InterruptedException {
		var docTypeFields = getAllDocTypeFields();

		docTypeFields.forEach(docTypeField ->
			log.debug(
				String.format(
					"Field name: %s, type: %s.",
					docTypeField.getName(),
					docTypeField.getFieldType().name()
				)
			)
		);

		var docTypeField = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.findFirst();

		assertTrue(docTypeField.isPresent());
		var docTypeFieldId = docTypeField.get().getId();

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					AUTOCORRECTION,
					args(
						arg(
							AUTOCORRECTION_DTO,
							inputObject(
								prop(NAME, AUTOCORRECTION_NAME_ONE),
								prop(SORT, SortType.SCORE),
								prop(SUGGEST_MODE, SuggestMode.MISSING),
								prop(PREFIX_LENGTH, PREFIX_LENGTH_VALUE),
								prop(MIN_WORD_LENGTH, MIN_WORD_LENGTH_VALUE),
								prop(MAX_EDIT, MAX_EDIT_VALUE),
								prop(AUTOCORRECTION_DOC_TYPE_FIELD_ID, docTypeFieldId)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(SORT),
						field(SUGGEST_MODE),
						field(PREFIX_LENGTH),
						field(MIN_WORD_LENGTH),
						field(MAX_EDIT),
						field(ENABLE_SEARCH_WITH_CORRECTION),
						field(
							AUTOCORRECTION_DOC_TYPE_FIELD,
							field(ID),
							field(NAME)
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

		var response = clientResponse.getData().getJsonObject(AUTOCORRECTION);

		Autocorrection autocorrection =
			EntitiesUtils.getAutocorrection(
				AUTOCORRECTION_NAME_ONE,
				autocorrectionService,
				sessionFactory
			);

		log.debug(String.format("Autocorrection: %s", autocorrection.toString()));

		assertEquals(AUTOCORRECTION_NAME_ONE, autocorrection.getName());
		assertEquals(SortType.SCORE, autocorrection.getSort());
		assertEquals(SuggestMode.MISSING, autocorrection.getSuggestMode());
		assertEquals(PREFIX_LENGTH_VALUE, autocorrection.getPrefixLength());
		assertEquals(MIN_WORD_LENGTH_VALUE, autocorrection.getMinWordLength());
		assertEquals(MAX_EDIT_VALUE, autocorrection.getMaxEdit());
		assertNotNull(autocorrection.getAutocorrectionDocTypeField());
		assertEquals(
			docTypeFieldId,
			autocorrection.getAutocorrectionDocTypeField().getId()
		);

		EntitiesUtils.removeEntity(
			AUTOCORRECTION_NAME_ONE,
			autocorrectionService,
			sessionFactory
		);
	}

	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(
			AUTOCORRECTION_NAME_TWO,
			autocorrectionService,
			sessionFactory
		);
	}

	private List<DocTypeField> getAllDocTypeFields() {
		return sessionFactory.withTransaction(session ->
				docTypeFieldService.findAll()
			)
			.await().indefinitely();
	}
}
