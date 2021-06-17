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

package io.openk9.internal.http.ws;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import io.openk9.http.socket.CloseStatus;
import io.openk9.http.socket.WebSocketMessage;
import io.openk9.http.socket.WebSocketSession;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.internal.http.HttpRequestImpl;
import io.openk9.internal.http.HttpResponseImpl;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class WebSocketSessionFactory {

	private static class WebSocketSessionImpl implements WebSocketSession {

		private WebSocketSessionImpl(
			WebsocketInbound inbound, WebsocketOutbound outbound,
			HttpServerRequest request,
			HttpServerResponse response) {
			_inbound = inbound;
			_outbound = outbound;
			_id = UUID.randomUUID().toString();
			_request = request;
			_response = response;
		}

		@Override
		public Publisher<Void> send(Publisher<WebSocketMessage> messages) {

			Flux<WebSocketFrame> frames =
				Flux
					.from(messages)
					.map(this::_toFrame);

			return _outbound
				.sendObject(frames)
				.then();
		}

		@Override
		public Publisher<WebSocketMessage> receive() {
			return _inbound
				.aggregateFrames()
				.receiveFrames()
				.map(this::_toMessage);
		}

		@Override
		public WebSocketMessage textMessage(String payload) {
			return new TextWebSocketMessage(
				payload == null  || payload.isEmpty()
					? Unpooled.EMPTY_BUFFER
					: Unpooled.copiedBuffer(payload, CharsetUtil.UTF_8));
		}

		@Override
		public WebSocketMessage byteMessage(ByteBuffer byteBuffer) {
			return new BinaryWebSocketMessage(wrappedBuffer((byteBuffer)));
		}

		@Override
		public WebSocketMessage pingMessage(ByteBuffer byteBuffer) {
			return new PingWebSocketMessage(wrappedBuffer(byteBuffer));
		}

		@Override
		public WebSocketMessage pongMessage(ByteBuffer byteBuffer) {
			return new PongWebSocketMessage(wrappedBuffer(byteBuffer));
		}

		@Override
		public String getSessionId() {
			return _id;
		}

		@Override
		public Mono<Void> close(CloseStatus status) {
			return _outbound.sendClose(status.getCode(), status.getReason());
		}

		@Override
		public String getHeader(String name) {
			return _inbound.headers().get(name);
		}

		@Override
		public String getHeader(String name, String defaultValue) {
			return _inbound.headers().get(name, defaultValue);
		}

		@Override
		public HttpRequest getRequest() {
			return new HttpRequestImpl(_request);
		}

		@Override
		public HttpResponse getResponse() {
			return new HttpResponseImpl(_response);
		}

		private WebSocketMessage _toMessage(WebSocketFrame webSocketFrame) {
			return messageTypes
				.get(webSocketFrame.getClass())
				.apply(wrappedBuffer(webSocketFrame.content()));
		}

		private WebSocketFrame _toFrame(WebSocketMessage message) {
			ByteBuf byteBuf = ((WebSocketMessageImpl)message).getByteBuf();
			if (message instanceof TextWebSocketMessage) {
				return new TextWebSocketFrame(byteBuf);
			}
			else if (message instanceof BinaryWebSocketMessage) {
				return new BinaryWebSocketFrame(byteBuf);
			}
			else if (message instanceof PingWebSocketMessage) {
				return new PingWebSocketFrame(byteBuf);
			}
			else if (message instanceof PongWebSocketMessage) {
				return new PongWebSocketFrame(byteBuf);
			}
			else {
				throw new IllegalArgumentException(
					"Unexpected message type: " + message.getClass());
			}
		}

		private static class TextWebSocketMessage
			extends WebSocketMessageImpl {

			private TextWebSocketMessage(ByteBuf byteBuf) {
				super(byteBuf);
			}

			@Override
			public String toString() {
				return getPayloadAsString();
			}
		}

		private static class BinaryWebSocketMessage
			extends WebSocketMessageImpl {

			private BinaryWebSocketMessage(ByteBuf byteBuf) {
				super(byteBuf);
			}

		}

		private static class PingWebSocketMessage
			extends WebSocketMessageImpl {

			private PingWebSocketMessage(ByteBuf byteBuf) {
				super(byteBuf);
			}
		}

		private static class PongWebSocketMessage
			extends WebSocketMessageImpl {

			private PongWebSocketMessage(ByteBuf byteBuf) {
				super(byteBuf);
			}
		}

		private final WebsocketInbound _inbound;

		private final WebsocketOutbound _outbound;

		private final String _id;

		private final HttpServerRequest _request;

		private final HttpServerResponse _response;

		private static final Map<Class, Function<ByteBuf, WebSocketMessage>>
			messageTypes;

		static {

			messageTypes = new HashMap<>(8);

			messageTypes.put(
				TextWebSocketFrame.class, TextWebSocketMessage::new);
			messageTypes.put(
				BinaryWebSocketFrame.class, BinaryWebSocketMessage::new);
			messageTypes.put(
				PingWebSocketFrame.class, PingWebSocketMessage::new);
			messageTypes.put(
				PongWebSocketFrame.class, PongWebSocketMessage::new);

		}

	}

	public static WebSocketSession createWebSocketSession(
		WebsocketInbound inbound, WebsocketOutbound outbound,
		HttpServerRequest request, HttpServerResponse response) {

		return new WebSocketSessionImpl(
			inbound, outbound, request, response);
	}

}
