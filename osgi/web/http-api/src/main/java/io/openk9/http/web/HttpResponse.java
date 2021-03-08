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

package io.openk9.http.web;

import org.reactivestreams.Publisher;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Predicate;

public interface HttpResponse {

	/**
	 * Adds an outbound cookie
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse addCookie(Cookie cookie);

	/**
	 * Adds an outbound HTTP header, appending the value if the header already exist.
	 *
	 * @param name header name
	 * @param value header value
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse addHeader(CharSequence name, CharSequence value);

	/**
	 * Sets Transfer-Encoding header
	 *
	 * @param chunked true if Transfer-Encoding: chunked
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse chunkedTransfer(boolean chunked);

	/**
	 * Enables/Disables compression handling (gzip/deflate) for the underlying response
	 *
	 * @param compress should handle compression
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse compression(boolean compress);

	/**
	 * Returns true if headers and status have been sent to the client
	 *
	 * @return true if headers and status have been sent to the client
	 */
	boolean hasSentHeaders();

	/**
	 * Sets an outbound HTTP header, replacing any pre-existing value.
	 *
	 * @param name headers key
	 * @param value header value
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse header(CharSequence name, CharSequence value);

	/**
	 * Sets outbound HTTP headers, replacing any pre-existing value for these headers.
	 *
	 * @param headers netty headers map
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse headers(Iterable<Map.Entry<String, String>> headers);

	/**
	 * Sets the request {@code keepAlive} if true otherwise remove the existing connection keep alive header
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse keepAlive(boolean keepAlive);

	/**
	 * Returns the outbound HTTP headers, sent back to the clients
	 *
	 * @return headers sent back to the clients
	 */
	Iterable<Map.Entry<String, String>> responseHeaders();

	/**
	 * Sends the HTTP headers and empty content thus delimiting a full empty body http response.
	 *
	 * @return a {@link Publisher} successful on committed response
	 * @see #send(Publisher)
	 */
	Publisher<Void> send();

	/**
	 * Returns a {@link Publisher<Void>} successful on committed response
	 *
	 * @return a {@link Publisher<Void>} successful on committed response
	 */
	Publisher<Void> sendHeaders();

	/**
	 * Sends 404 status {@link HttpResponseStatus#NOT_FOUND}.
	 *
	 * @return a {@link Publisher} successful on flush confirmation
	 */
	Publisher<Void> sendNotFound();

	/**
	 * Sends redirect status {@link HttpResponseStatus#FOUND} along with a location
	 * header to the remote client.
	 *
	 * @param location the location to redirect to
	 *
	 * @return a {@link Publisher} successful on flush confirmation
	 */
	Publisher<Void> sendRedirect(String location);
	/**
	 * Adds "text/event-stream" content-type for Server-Sent Events
	 *
	 * @return this {@link HttpResponse}
	 */
	HttpResponse sse();

	/**
	 * Returns the assigned HTTP status
	 *
	 * @return the assigned HTTP status
	 */
	String status();

	/**
	 * Sets an HTTP status to be sent along with the headers
	 *
	 * @param status an HTTP status to be sent along with the headers
	 * @return this {@link HttpResponse}
	 */
	HttpResponse status(int status);

	String status(int status, String reason);

	Publisher<Void> send(Publisher<? extends ByteBuffer> dataStream);

	Publisher<Void> sendHttpMessage(
		Publisher<? extends HttpMessage> httpMessage);

	/**
	 * Sends data to the peer, listens for any error on write and closes on terminal signal
	 * (complete|error). <p>A new {@link Publisher<Void>} type (or the same) for typed send
	 * sequences.</p>
	 * <p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param dataStream the dataStream publishing OUT items to write on this channel
	 * @param predicate that returns true if explicit flush operation is needed after that buffer
	 *
	 * @return A new {@link Publisher<Void>} to append further send. It will emit a complete
	 * signal successful sequence write (e.g. after "flush") or any error during write.
	 */
	Publisher<Void> send(Publisher<? extends ByteBuffer> dataStream, Predicate<ByteBuffer> predicate);

	/**
	 * Sends bytes to the peer, listens for any error on write and closes on terminal
	 * signal (complete|error). If more than one publisher is attached (multiple calls to
	 * send()) completion occurs after all publishers complete.
	 * <p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param dataStream the dataStream publishing Buffer items to write on this channel
	 *
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any
	 * error during write
	 */
	Publisher<Void> sendByteArray(Publisher<? extends byte[]> dataStream);

	/**
	 * Sends content from given {@link Path} using
	 * {@link java.nio.channels.FileChannel#transferTo(long, long, WritableByteChannel)}
	 * support. If the system supports it and the path resolves to a local file
	 * system {@link File} then transfer will use zero-byte copy
	 * to the peer.
	 * <p>It will
	 * listen for any error on
	 * write and close
	 * on terminal signal (complete|error). If more than one publisher is attached
	 * (multiple calls to send()) completion occurs after all publishers complete.
	 * <p>
	 * Note: this will emit {@link io.netty.channel.FileRegion} in the outbound
	 * {@link io.netty.channel.ChannelPipeline}
	 * Note: Nesting any send* method is not supported.
	 *
	 * @param file the file Path
	 *
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any
	 * error during write
	 */
	Publisher<Void> sendFile(Path file);

	/**
	 * Sends content from the given {@link Path} using
	 * {@link java.nio.channels.FileChannel#transferTo(long, long, WritableByteChannel)}
	 * support, if the system supports it, the path resolves to a local file
	 * system {@link File}, compression and SSL/TLS is not enabled, then transfer will
	 * use zero-byte copy to the peer., otherwise chunked read/write will be used.
	 * <p>It will listens for any error on write and closes
	 * on terminal signal (complete|error). If more than one publisher is attached
	 * (multiple calls to send()) completion occurs after all publishers complete.</p>
	 * <p></p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param file the file Path
	 * @param position where to start
	 * @param count how much to transfer
	 *
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any
	 * error during write
	 */
	Publisher<Void> sendFile(Path file, long position, long count);
	/**
	 * Sends an object through Netty pipeline. If type of {@link Publisher}, sends all signals,
	 * flushing on complete by default. Write occur in FIFO sequence.
	 * <p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param dataStream the dataStream publishing items to write on this channel
	 * or a simple pojo supported by configured Netty handlers
	 * @param predicate that returns true if explicit flush operation is needed after that object
	 *
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any
	 * error during write
	 */
	Publisher<Void> sendObject(Publisher<?> dataStream, Predicate<Object> predicate);

	/**
	 * Sends data to the peer, listens for any error on write and closes on terminal signal
	 * (complete|error).
	 * <p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param message the object to publish
	 *
	 * @return A {@link Publisher} to signal successful sequence write (e.g. after "flush") or
	 * any error during write
	 */
	Publisher<Void> sendObject(Object message);

	/**
	 * Sends String to the peer, listens for any error on write and closes on terminal signal
	 * (complete|error). If more than one publisher is attached (multiple calls to send())
	 * completion occurs after all publishers complete.
	 * <p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param dataStream the dataStream publishing Buffer items to write on this channel
	 *
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any
	 * error during write
	 */
	default Publisher<Void> sendString(Publisher<? extends String> dataStream) {
		return sendString(dataStream, Charset.defaultCharset());
	}

	/**
	 * Sends String to the peer, listens for any error on write and closes on terminal signal
	 * (complete|error). If more than one publisher is attached (multiple calls to send())
	 * completion occurs after all publishers complete.
	 * <p>Note: Nesting any send* method is not supported.</p>
	 *
	 * @param dataStream the dataStream publishing Buffer items to write on this channel
	 * @param charset the encoding charset
	 *
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any
	 * error during write
	 */
	Publisher<Void> sendString(
		Publisher<? extends String> dataStream, Charset charset);


}
