package io.openk9.datasource.graphql;

import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.EmbeddingModelDTO;
import io.openk9.datasource.service.EmbeddingModelService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

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
public class EmbeddingModelGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "EmbeddingModelGraphqlTest - ";
	private static final String EMBEDDING_MODEL_ONE_NAME = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String MESSAGE = "message";
	private static final String MODEL = "model";
	private static final String MODEL_VALUE = "model";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String TYPE_VALUE = "type";
	private static final Logger log = Logger.getLogger(EmbeddingModelGraphqlTest.class);

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_get_embedding_model_one() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					"embeddingModel",
					args(
						arg(
							"embeddingModelDTO",
							inputObject(
								prop(NAME, EMBEDDING_MODEL_ONE_NAME),
								prop("apiUrl", "embedding-model.local"),
								prop("apiKey", "secret-key"),
								prop("vectorSize", 1500),
								prop(TYPE, TYPE_VALUE),
								prop(MODEL, MODEL_VALUE)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(MODEL)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasource =
			response.getData().getJsonObject("embeddingModel");

		assertNotNull(datasource);

		assertTrue(datasource.isNull(FIELD_VALIDATORS));

		// check if the embeddingModel have the type and model fields
		var embeddingModelOne = getEmbeddingModelOne();

		assertEquals(TYPE_VALUE, embeddingModelOne.getType());
		assertEquals(MODEL_VALUE, embeddingModelOne.getModel());

		// remove the embeddingModelOne
		removeEmbeddingModelOne();
	}

	private EmbeddingModel getEmbeddingModelOne() {
		return sessionFactory.withTransaction(
			s -> embeddingModelService.findByName(s, EMBEDDING_MODEL_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private void removeEmbeddingModelOne() {
		var embeddingModelOne = getEmbeddingModelOne();

		sessionFactory.withTransaction(
				session -> embeddingModelService.deleteById(embeddingModelOne.getId())
			)
			.await()
			.indefinitely();
	}
}
