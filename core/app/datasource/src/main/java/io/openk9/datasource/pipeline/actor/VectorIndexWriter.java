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

package io.openk9.datasource.pipeline.actor;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;
import io.vertx.core.json.Json;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.indices.PutIndexTemplateResponse;
import org.opensearch.client.transport.endpoints.BooleanResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VectorIndexWriter extends AbstractBehavior<Writer.Command> {

	private final static org.jboss.logging.Logger log =
		Logger.getLogger(VectorIndexWriter.class);

	private final OpenSearchAsyncClient asyncClient;
	private final String vectorIndexName;
	private final ActorRef<Writer.Response> replyTo;
	private final String templateName;
	private boolean indexTemplateCreated = false;
	private int vectorSize;

	public VectorIndexWriter(
		ActorContext<Writer.Command> context,
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		super(context);
		this.asyncClient = CDI.current().select(OpenSearchAsyncClient.class).get();
		this.vectorIndexName = scheduler.getVectorIndexName();
		this.templateName = vectorIndexName + "-template";
		this.replyTo = replyTo;

	}

	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler, ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx -> new VectorIndexWriter(ctx, scheduler, replyTo));
	}

	protected static List<Object> getChunks(byte[] json) {
		return JsonPath.using(Configuration.defaultConfiguration())
			.parseUtf8(json)
			.read("$.*");
	}

	protected static int getVectorSize(byte[] json) {
		List<List<Float>> jsonPathResult = JsonPath.using(Configuration.defaultConfiguration())
			.parseUtf8(json)
			.read("$.[:1].vector");

		return jsonPathResult.stream().mapToInt(Collection::size).findAny().orElse(0);
	}

	@Override
	public Receive<Writer.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Writer.Start.class, this::onStart)
			.onMessage(CheckIndexTemplate.class, this::onCheckIndexTemplate)
			.onMessage(CheckIndexTemplateResponse.class, this::onCheckIndexTemplateResponse)
			.onMessage(PutTemplateResponse.class, this::onPutTemplateResponse)
			.onMessage(IndexDocument.class, this::onIndexDocument)
			.onMessage(IndexDocumentResponse.class, this::onIndexDocumentResponse)
			.build();
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var data = start.dataPayload();
		var heldMessage = start.heldMessage();

		getContext().getSelf().tell(new CheckIndexTemplate(data, heldMessage));

		return this;
	}

	private Behavior<Writer.Command> onCheckIndexTemplate(CheckIndexTemplate checkIndexTemplate) {

		var embeddedChunks = checkIndexTemplate.embeddedChunks();
		var heldMessage = checkIndexTemplate.heldMessage();

		if (indexTemplateCreated) {

			getContext().getSelf().tell(new IndexDocument(
				embeddedChunks, heldMessage)
			);

			return this;
		}

		try {

			getContext().pipeToSelf(
				asyncClient.indices().existsIndexTemplate(req -> req.name(templateName)),
				(r, t) -> new CheckIndexTemplateResponse(
					embeddedChunks, heldMessage, r, t)
			);

		}
		catch (IOException e) {

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));

		}

		return this;
	}

	private Behavior<Writer.Command> onCheckIndexTemplateResponse(CheckIndexTemplateResponse response) {

		var exists = response.exists();
		var throwable = response.throwable();
		var heldMessage = response.heldMessage();

		if (throwable != null) {

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

			return this;

		}

		if (exists.value()) {

			this.indexTemplateCreated = true;

			getContext().getSelf().tell(new IndexDocument(response.embeddedChunks(), heldMessage));

		}
		else {

			this.vectorSize = getVectorSize(response.embeddedChunks());

			try {

				getContext().pipeToSelf(
					asyncClient.indices().putIndexTemplate(req -> req
						.name(templateName)
						.indexPatterns(vectorIndexName)
						.template(template -> template
							.settings(settings -> settings
								.knn(true))
							.mappings(mapping -> mapping
								.properties("indexName", p -> p
									.text(text -> text.fields(
										"keyword",
										Property.of(field -> field
											.keyword(keyword -> keyword.ignoreAbove(256)))
									)))
								.properties("contentId", p -> p
									.text(text -> text.fields(
										"keyword",
										Property.of(field -> field
											.keyword(keyword -> keyword.ignoreAbove(256)))
									)))
								.properties("number", p -> p
									.integer(int_ -> int_))
								.properties("total", p -> p
									.integer(int_ -> int_))
								.properties("chunkText", p -> p
									.text(text -> text.fields(
										"keyword",
										Property.of(field -> field
											.keyword(keyword -> keyword.ignoreAbove(256)))
									)))
								.properties("title", p -> p
									.text(text -> text.fields(
										"keyword",
										Property.of(field -> field
											.keyword(keyword -> keyword.ignoreAbove(256)))
									)))
								.properties("url", p -> p
									.text(text -> text.fields(
										"keyword",
										Property.of(field -> field
											.keyword(keyword -> keyword.ignoreAbove(256)))
									)))
								.properties("vector", p -> p
									.knnVector(knn -> knn.dimension(this.vectorSize)))
							)
						)
					),
					(r, t) -> new PutTemplateResponse(response.embeddedChunks(), heldMessage, r, t)
				);

			}
			catch (IOException e) {

				replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));

			}

		}

		return this;
	}

	private Behavior<Writer.Command> onPutTemplateResponse(PutTemplateResponse putTemplateResponse) {

		var embeddedChunks = putTemplateResponse.embeddedChunks();
		var heldMessage = putTemplateResponse.heldMessage();
		var throwable = putTemplateResponse.throwable();

		if (throwable != null) {

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

			return this;
		}

		getContext().getSelf().tell(new IndexDocument(embeddedChunks, heldMessage));

		return this;
	}

	private Behavior<Writer.Command> onIndexDocument(IndexDocument indexDocument) {

		var embeddedChunks = indexDocument.embeddedChunks();
		var heldMessage = indexDocument.heldMessage();

		var bulkOperations = new ArrayList<BulkOperation>();

		for (Object chunk : getChunks(embeddedChunks)) {

			var bulkOperation = new BulkOperation.Builder()
				.index(new IndexOperation.Builder<>()
					.index(vectorIndexName)
					.document(chunk)
					.build())
				.build();

			bulkOperations.add(bulkOperation);

		}

		var bulkRequest = new BulkRequest.Builder()
			.index(vectorIndexName)
			.operations(bulkOperations)
			.build();

		try {

			getContext().pipeToSelf(
				asyncClient.bulk(bulkRequest),
				(bulkResponse, exception) -> new IndexDocumentResponse(
					embeddedChunks, heldMessage, bulkResponse, (Exception) exception)
			);

		}
		catch (IOException e) {
			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;

	}

	private Behavior<Writer.Command> onIndexDocumentResponse(
		IndexDocumentResponse brc) {

		var bulkResponse = brc.bulkResponse;
		var heldMessage = brc.heldMessage;
		var embeddedChunks = brc.embeddedChunks;
		var throwable = brc.exception;

		if (bulkResponse != null) {

			if (bulkResponse.errors()) {
				String reasons = bulkResponse.items()
					.stream()
					.map(BulkResponseItem::error)
					.filter(Objects::nonNull)
					.map(ErrorCause::reason)
					.collect(Collectors.joining());

				log.warnf("Bulk request error: %s", reasons);
				replyTo.tell(new Writer.Failure(
					new WriterException(reasons),
					heldMessage
				));
			}

		}

		if (throwable != null) {
			log.warn("Error on bulk request", throwable);
			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));
		}
		else {
			var dataPayload = Json.encodeToBuffer(embeddedChunks).getBytes();
			replyTo.tell(new Writer.Success(dataPayload, heldMessage));
		}

		return this;
	}

	private record CheckIndexTemplate(
		byte[] embeddedChunks,
		HeldMessage heldMessage
	) implements Writer.Command {}

	private record CheckIndexTemplateResponse(
		byte[] embeddedChunks,
		HeldMessage heldMessage,
		BooleanResponse exists,
		Throwable throwable
	)
		implements Writer.Command {}

	private record PutTemplateResponse(
		byte[] embeddedChunks,
		HeldMessage heldMessage,
		PutIndexTemplateResponse response,
		Throwable throwable
	)
		implements Writer.Command {}

	private record IndexDocument(
		byte[] embeddedChunks,
		HeldMessage heldMessage
	)
		implements Writer.Command {}

	private record IndexDocumentResponse(
		byte[] embeddedChunks,
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Exception exception
	) implements Writer.Command {}

}
