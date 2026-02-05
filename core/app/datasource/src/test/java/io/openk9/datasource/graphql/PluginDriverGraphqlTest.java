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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.index.exception.SampleEndpointException;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.client.HttpPluginDriverClient;
import io.openk9.datasource.service.DocTypeService;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PluginDriverGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "PluginDriverGraphqlTest - ";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String JSON_CONFIG = "jsonConfig";
	private static final String JSON_CONFIG_VALUE =
		"{\"baseUri\":\"openk9-liferay-connector.k9-test:5000\",\"path\":\"/getDocuments\",\"method\":\"POST\"}";
	private static final Logger log = Logger.getLogger(PluginDriverGraphqlTest.class);
	private static final String MESSAGE = "message";
	private static final String NAME = "name";
	private static final String PLUGIN_DRIVER_NAME_ONE = ENTITY_NAME_PREFIX + "Plugin driver 1";
	private static final String PLUGIN_DRIVER_WITH_DOC_TYPE = "pluginDriverWithDocType";
	private static final String PLUGIN_WITH_DOC_TYPE_DTO = "pluginWithDocTypeDTO";
	private static final String PROVISIONING = "provisioning";
	private static final String TYPE = "type";

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	DocTypeService docTypeService;

	@Inject
	Mutiny.SessionFactory sf;

	@InjectMock
	HttpPluginDriverClient httpPluginDriverClient;

	private List<DocType> docTypeList = new ArrayList<>();

	@BeforeEach
	void setup() {
		docTypeList = EntitiesUtils.getAllEntities(docTypeService, sf);

	}

	@Test
	@DisplayName("Should fail during the creation of a PluginDriver without sample endpoint.")
	void should_fail_creating_plugin_driver_without_sample()
		throws ExecutionException, InterruptedException {

		// getSample mock to return a ValidationException
		when(httpPluginDriverClient.getSample(any()))
			.thenReturn(
				Uni.createFrom().failure(
					new ValidationException("Unexpected Response Status: 404, Message: Not Found")
				)
			);

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					PLUGIN_DRIVER_WITH_DOC_TYPE,
					args(
						arg(
							PLUGIN_WITH_DOC_TYPE_DTO,
							inputObject(
								prop(NAME,PLUGIN_DRIVER_NAME_ONE),
								prop(TYPE, PluginDriver.PluginDriverType.HTTP),
								prop(JSON_CONFIG, JSON_CONFIG_VALUE),
								prop(PROVISIONING, PluginDriver.Provisioning.USER)
							)
						)
					),
					field(
						"entity",
						field("id"),
						field(NAME)
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

		// check if SampleEndpointException is present in the response
		assertTrue(
			clientResponse.getErrors().stream()
				.map(error -> error.getExtensions().get("exception"))
				.anyMatch( exceptionClassName ->
					SampleEndpointException.class.getName().equalsIgnoreCase(
						(String) exceptionClassName
					)
				));
	}
}
