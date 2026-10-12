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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.ResultAclConfigService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.InputObject;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class ResultAclConfigGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "ResultAclConfigGraphqlTest - ";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc type";
	private static final String TITLE_NAME = ENTITY_NAME_PREFIX + "title";
	private static final String DATE_NAME = ENTITY_NAME_PREFIX + "date";
	private static final String ROLE_AUDITOR = "auditor";

	private static final String RESULT_ACL_CONFIG = "resultAclConfig";
	private static final String RESULT_ACL_CONFIG_DTO = "resultAclConfigDTO";
	private static final String ENTITY = "entity";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String FIELD = "field";
	private static final String MESSAGE = "message";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String ENABLED = "enabled";
	private static final String SUPER_USER_ROLES = "superUserRoles";
	private static final String VISIBLE_FIELD_IDS = "visibleFieldIds";
	private static final String VISIBLE_FIELDS = "visibleFields";
	private static final String PATCH = "patch";

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	ResultAclConfigService resultAclConfigService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	private DocType docType;
	private DocTypeField title;
	private DocTypeField date;

	@BeforeEach
	void setup() {
		docType = docTypeService.create(DocTypeDTO.builder()
				.name(DOC_TYPE_NAME)
				.build())
			.await().indefinitely();

		title = addField(TITLE_NAME, "resultAclGraphqlTitle");
		date = addField(DATE_NAME, "resultAclGraphqlDate");
	}

	@AfterEach
	void tearDown() {
		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		if (current != null) {
			EntitiesUtils.removeEntity(
				current, resultAclConfigService, sessionFactory);
		}

		docTypeService.deleteById(docType.getId()).await().indefinitely();
	}

	@Test
	void should_create_the_configuration_with_the_default_name()
		throws ExecutionException, InterruptedException {

		// 1. The tenant has no configuration yet
		assertNull(readCurrent());

		// 2. Save it, sending a different name
		var saved = save(
			inputObject(
				prop(NAME, ENTITY_NAME_PREFIX + "ignored"),
				prop(ENABLED, true),
				prop(SUPER_USER_ROLES, List.of(ROLE_AUDITOR)),
				prop(VISIBLE_FIELD_IDS, List.of(title.getId(), date.getId()))
			),
			false
		);

		assertTrue(saved.isNull(FIELD_VALIDATORS));
		assertEquals(
			ResultAclConfig.DEFAULT_NAME,
			saved.getJsonObject(ENTITY).getString(NAME));

		// 3. Read it back
		var current = readCurrent();

		assertTrue(current.getBoolean(ENABLED));
		assertEquals(
			List.of(ROLE_AUDITOR),
			roles(current));
		assertEquals(
			Set.of(title.getId(), date.getId()),
			visibleFieldIds(current));
	}

	@Test
	void should_update_the_same_configuration_on_a_second_save()
		throws ExecutionException, InterruptedException {

		// 1. Create the configuration
		var created = save(
			inputObject(
				prop(NAME, ResultAclConfig.DEFAULT_NAME),
				prop(ENABLED, true),
				prop(SUPER_USER_ROLES, List.of(ROLE_AUDITOR)),
				prop(VISIBLE_FIELD_IDS, List.of(title.getId()))
			),
			false
		);

		// 2. Save it again as a patch of the mode only
		var patched = save(
			inputObject(
				prop(NAME, ResultAclConfig.DEFAULT_NAME),
				prop(ENABLED, false)
			),
			true
		);

		assertTrue(patched.isNull(FIELD_VALIDATORS));
		assertEquals(
			created.getJsonObject(ENTITY).getString(ID),
			patched.getJsonObject(ENTITY).getString(ID));

		// 3. The roles and the visible fields are unchanged
		var current = readCurrent();

		assertFalse(current.getBoolean(ENABLED));
		assertEquals(
			List.of(ROLE_AUDITOR),
			roles(current));
		assertEquals(Set.of(title.getId()), visibleFieldIds(current));
	}

	@Test
	void should_return_the_field_validators_for_a_blank_role()
		throws ExecutionException, InterruptedException {

		var saved = save(
			inputObject(
				prop(NAME, ResultAclConfig.DEFAULT_NAME),
				prop(SUPER_USER_ROLES, List.of(" "))
			),
			false
		);

		assertTrue(saved.isNull(ENTITY));
		assertFalse(saved.getJsonArray(FIELD_VALIDATORS).isEmpty());
		assertNull(readCurrent());
	}

	private DocTypeField addField(String name, String fieldName) {
		return docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name(name)
					.fieldName(fieldName)
					.fieldType(FieldType.TEXT)
					.build())
			.await().indefinitely()
			.right;
	}

	private JsonObject save(InputObject resultAclConfigDTO, boolean patch)
		throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RESULT_ACL_CONFIG,
					args(
						arg(RESULT_ACL_CONFIG_DTO, resultAclConfigDTO),
						arg(PATCH, patch)
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

		assertFalse(response.hasError(), () -> String.valueOf(response.getErrors()));

		return response.getData().getJsonObject(RESULT_ACL_CONFIG);
	}

	private JsonObject readCurrent()
		throws ExecutionException, InterruptedException {

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					RESULT_ACL_CONFIG,
					field(ID),
					field(ENABLED),
					field(SUPER_USER_ROLES),
					field(VISIBLE_FIELDS,
						field(ID)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError(), () -> String.valueOf(response.getErrors()));

		var data = response.getData();

		return data.isNull(RESULT_ACL_CONFIG)
			? null
			: data.getJsonObject(RESULT_ACL_CONFIG);
	}

	private static List<String> roles(JsonObject resultAclConfig) {
		return resultAclConfig.getJsonArray(SUPER_USER_ROLES)
			.getValuesAs(JsonString::getString);
	}

	private static Set<Long> visibleFieldIds(JsonObject resultAclConfig) {
		return resultAclConfig.getJsonArray(VISIBLE_FIELDS)
			.getValuesAs(JsonObject.class)
			.stream()
			.map(field -> Long.parseLong(field.getString(ID)))
			.collect(Collectors.toSet());
	}

}
