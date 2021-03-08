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

import io.openk9.http.web.body.FileUpload;
import org.reactivestreams.Publisher;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface HttpRequest {

	/**
	 * URI parameter captured via {} "/test/{var}"
	 *
	 * @param key param var name
	 *
	 * @return the param captured value
	 */
	String pathParam(CharSequence key);

	Map<String, String> pathParams();

	Optional<String> firstParam(String key);

	List<String> params(String key);

	Map<String, List<String>> params();

	Iterable<Map.Entry<String, String>> requestHeaders();

	Publisher<Map<String, List<String>>> bodyAttributes();

	Publisher<Map<String, List<byte[]>>> bodyAttributesBytes();

	Publisher<Map<String, String>> bodyAttributesFirst();

	Publisher<List<String>> bodyAttribute(String key);

	Publisher<Optional<String>> bodyAttributeFirst(String key);

	Publisher<String> bodyAttributeFirst(String key, String defaultValue);

	Publisher<Map<String, byte[]>> bodyAttributesBytesFirst();

	Publisher<List<byte[]>> bodyAttributeBytes(String key);

	Publisher<Optional<byte[]>> bodyAttributeBytesFirst(String key);

	Publisher<byte[]> bodyAttributeBytesFirst(
		String key, byte[] defaultValue);

	Publisher<List<FileUpload>> fileUploads();

	Publisher<List<FileUpload>> fileUploads(boolean useDisk);

	/**
	 * Returns the current protocol scheme
	 *
	 * @return the protocol scheme
	 */
	String scheme();

	/**
	 * Returns the address of the host peer.
	 *
	 * @return the host's address
	 */
	InetSocketAddress hostAddress();

	/**
	 * Returns the address of the remote peer.
	 *
	 * @return the peer's address
	 */
	InetSocketAddress remoteAddress();


	/**
	 * Returns resolved HTTP cookies
	 *
	 * @return Resolved HTTP cookies
	 */
	Map<CharSequence, Set<Cookie>> cookies();


	/**
	 * Is the request keep alive
	 *
	 * @return is keep alive
	 */
	boolean isKeepAlive();

	/**
	 * Returns true if websocket connection (upgraded)
	 *
	 * @return true if websocket connection
	 */
	boolean isWebsocket();

	/**
	 * Returns the resolved request method (HTTP 1.1 etc)
	 *
	 * @return the resolved request method (HTTP 1.1 etc)
	 */
	int method();

	/**
	 * Returns a normalized {@link #uri()} without the leading and trailing '/' if present
	 *
	 * @return a normalized {@link #uri()} without the leading and trailing
	 */
	default String path() {
		String uri = URI.create(uri()).getPath();
		if (!uri.isEmpty()) {
			if(uri.charAt(0) == '/'){
				uri = uri.substring(1);
				if(uri.length() <= 1){
					return uri;
				}
			}
			if(uri.charAt(uri.length() - 1) == '/'){
				return uri.substring(0, uri.length() - 1);
			}
		}
		return uri;
	}

	/**
	 * Returns the resolved target address
	 *
	 * @return the resolved target address
	 */
	String uri();

	/**
	 * Returns the resolved request version (HTTP 1.1 etc)
	 *
	 * @return the resolved request version (HTTP 1.1 etc)
	 */
	String version();

	Publisher<HttpMessage> receive();

	Publisher<byte[]> aggregateBodyToByteArray();

	Publisher<String> aggregateBodyToString();

	Publisher<InputStream> aggregateBodyToInputStream();
}
