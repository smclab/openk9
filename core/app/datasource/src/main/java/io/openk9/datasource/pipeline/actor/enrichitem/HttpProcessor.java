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

package io.openk9.datasource.pipeline.actor.enrichitem;

import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.pipeline.actor.common.Http;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.web.dto.EnricherInputDTO;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.RecipientRef;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

public class HttpProcessor extends AbstractBehavior<HttpProcessor.Command> {

	public HttpProcessor(
		ActorContext<Command> context,
		RecipientRef<Token.Command> tokenActorRef, boolean async) {
		super(context);
		this.async = async;
		this.tokenActorRef = tokenActorRef;
		this.tokenResponseAdapter =
			context.messageAdapter(
				Token.Response.class,
				TokenResponseWrapper::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.build();
	}


	private Behavior<Command> onResponseWrapper(ResponseWrapper responseWrapper) {

		Http.Response response = responseWrapper.response;

		ActorRef<Response> replyTo = responseWrapper.replyTo;

		if (response instanceof Http.OK) {

			if (async) {

				return Behaviors.receive(Command.class)
					.onMessage(TokenResponseWrapper.class, wrapper -> onTokenResponseWrapper(wrapper, replyTo))
					.build();

			}
			else {
				Http.OK ok = (Http.OK) response;
				byte[] body = ok.body();

				Body bodyResponse;

				if (body == null || body.length == 0) {
					bodyResponse = new Body(EMPTY_JSON);
				}
				else {
					bodyResponse = new Body(body);
				}

				replyTo.tell(bodyResponse);

			}

		}
		else {
			replyTo.tell(new Error(response.toString()));
		}

		return Behaviors.stopped();

	}

	private Behavior<Command> onTokenResponseWrapper(
		TokenResponseWrapper wrapper, ActorRef<Response> replyTo) {

		Token.Response response = wrapper.response;

		if (response instanceof Token.TokenCallback) {
			Token.TokenCallback tokenCallback =(Token.TokenCallback)response;
			byte[] jsonObject = tokenCallback.jsonObject();
			replyTo.tell(new Body(jsonObject));
		}
		else if (response instanceof Token.TokenState) {
			Token.TokenState tokenState = (Token.TokenState)response;
			if (tokenState == Token.TokenState.EXPIRED) {
				replyTo.tell(new Error("Token expired"));
				return Behaviors.stopped();
			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> started(
		ResourceUri resourceUri, EnricherInputDTO enricherInputDTO, ActorRef<Response> replyTo) {

		ActorRef<Http.Response> responseActorRef =
			getContext().messageAdapter(
				Http.Response.class,
				param -> new ResponseWrapper(param, replyTo)
			);

		ActorRef<Http.Command> commandActorRef =
			getContext().spawnAnonymous(Http.create());

		commandActorRef.tell(new Http.POST(responseActorRef, resourceUri, enricherInputDTO));

		return newReceiveBuilder()
			.onMessage(ResponseWrapper.class, this::onResponseWrapper)
			.build();
	}

	private Behavior<Command> onStart(Start start) {

		ResourceUri resourceUri = start.resourceUri;
		ActorRef<Response> replyTo = start.replyTo;

		if (!isValidUrl(resourceUri)) {
			replyTo.tell(new Error("Invalid URL: " + resourceUri));
			return Behaviors.stopped();
		}

		EnricherInputDTO enricherInputDTO = start.enricherInputDTO;

		if (async) {

			tokenActorRef.tell(new Token.Generate(start.expiredDate, tokenResponseAdapter));

			return waitGenerateToken(resourceUri, enricherInputDTO, replyTo);

		}

		return started(resourceUri, enricherInputDTO, replyTo);

	}

	private boolean isValidUrl(ResourceUri resourceUri) {
		var uri = resourceUri.getPath() != null
			? resourceUri.getBaseUri() + resourceUri.getPath()
			: resourceUri.getBaseUri();

		try {
			new URL(uri);
			return true;
		}
		catch (MalformedURLException e) {
			return false;
		}
	}

	private Behavior<Command> waitGenerateToken(
		ResourceUri resourceUri, EnricherInputDTO enricherInputDTO, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				TokenResponseWrapper.class,
				wrapper -> {

					Token.Response response = wrapper.response;

					if (response instanceof Token.TokenGenerated) {
						Token.TokenGenerated tokenGenerated =
							(Token.TokenGenerated) response;
						enricherInputDTO.setReplyTo(tokenGenerated.token());

						return started(resourceUri, enricherInputDTO, replyTo);
					}
					else {
						return Behaviors.same();
					}
				}
			)
			.build();
	}

	public static Behavior<Command> create(boolean async, RecipientRef<Token.Command> tokenActorRef) {
		return Behaviors.setup(param -> new HttpProcessor(param, tokenActorRef, async));
	}

	private final boolean async;
	private final RecipientRef<Token.Command> tokenActorRef;
	private final ActorRef<Token.Response> tokenResponseAdapter;

	public sealed interface Command extends CborSerializable {}
	public record Start(
		ResourceUri resourceUri, EnricherInputDTO enricherInputDTO, LocalDateTime expiredDate,
		ActorRef<Response> replyTo) implements Command {}
	private record TokenResponseWrapper(Token.Response response) implements Command {}
	private record ResponseWrapper(Http.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record Error(String message) implements Response {}
	public record Body(byte[] body) implements Response {}

	private static final byte[] EMPTY_JSON = new byte[] {123, 125}; // "{}";

}
