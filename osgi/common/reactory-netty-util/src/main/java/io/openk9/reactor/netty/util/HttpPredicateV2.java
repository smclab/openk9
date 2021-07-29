package io.openk9.reactor.netty.util;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import reactor.netty.http.server.HttpServerRequest;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class HttpPredicateV2
	implements Predicate<HttpServerRequest>, Function<Object, Map<String, String>> {

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for PATCH Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> patch(String uri) {
		return http(uri, null, HttpMethod.PATCH);
	}

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for DELETE Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> delete(String uri) {
		return http(uri, null, HttpMethod.DELETE);
	}

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for GET Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> get(String uri) {
		return http(uri, null, HttpMethod.GET);
	}

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for HEAD Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> head(String uri) {
		return http(uri, null, HttpMethod.HEAD);
	}

	/**
	 * Creates a {@link Predicate} based on a URI template.
	 * This will listen for all Methods.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link HttpPredicateV2}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> http(String uri,
													@Nullable HttpVersion protocol,
													HttpMethod method) {
		return new HttpPredicateV2(uri, protocol, method);
	}

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for OPTIONS Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> options(String uri) {
		return http(uri, null, HttpMethod.OPTIONS);
	}

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for POST Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> post(String uri) {
		return http(uri, null, HttpMethod.POST);
	}

	/**
	 * An alias for {@link HttpPredicateV2#get} prefix ([prefix]/**), useful for file system
	 * mapping.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for WebSocket Method.
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> prefix(String prefix) {
		return prefix(prefix, HttpMethod.GET);
	}

	/**
	 * An alias for {@link HttpPredicateV2#get} prefix (/**), useful for file system mapping.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for WebSocket Method.
	 *
	 * @return The new {@link Predicate}.
	 *
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> prefix(String prefix, HttpMethod method) {
		requireNonNull(prefix, "Prefix must be provided");
		requireNonNull(method, "method");

		String target = prefix.startsWith("/") ? prefix : "/".concat(prefix);
		//target = target.endsWith("/") ? target :  prefix.concat("/");
		return new HttpPrefixPredicate(target, method);
	}

	/**
	 * An alias for {@link HttpPredicateV2#http}.
	 * <p>
	 * Creates a {@link Predicate} based on a URI template filtering .
	 * <p>
	 * This will listen for PUT Method.
	 *
	 * @param uri The string to compile into a URI template and use for matching
	 *
	 * @return The new {@link Predicate}.
	 *
	 * @see Predicate
	 */
	public static Predicate<HttpServerRequest> put(String uri) {
		return http(uri, null, HttpMethod.PUT);
	}

	final HttpVersion     protocol;
	final HttpMethod      method;
	final String          uri;
	final UriPathTemplate template;

	public HttpPredicateV2(String uri,
						   @Nullable HttpVersion protocol,
						   HttpMethod method) {
		this.protocol = protocol;
		this.uri = requireNonNull(uri, "uri");
		this.method = requireNonNull(method, "method");
		this.template = new UriPathTemplate(uri);
	}

	@Override
	public Map<String, String> apply(Object key) {
		Map<String, String> headers = template.match(key.toString());
		if (null != headers && !headers.isEmpty()) {
			return headers;
		}
		return null;
	}

	@Override
	public final boolean test(HttpServerRequest key) {
		return (protocol == null || protocol.equals(key.version())) && method.equals(key.method()) &&
			   template.matches(key.uri());
	}

	/**
	 * Represents a URI template. A URI template is a URI-like String that contains
	 * variables enclosed by braces (<code>{</code>, <code>}</code>), which can be
	 * expanded to produce an actual URI.
	 *
	 * @author Arjen Poutsma
	 * @author Juergen Hoeller
	 * @author Jon Brisbin
	 * @see <a href="https://tools.ietf.org/html/rfc6570">RFC 6570: URI Templates</a>
	 */
	static final class UriPathTemplate {

		private static final Pattern FULL_SPLAT_PATTERN     =
			Pattern.compile("[\\*][\\*]");
		private static final String  FULL_SPLAT_REPLACEMENT = ".*";

		private static final Pattern NAME_SPLAT_PATTERN     =
			Pattern.compile("\\{([^/]+?)\\}[\\*][\\*]");

		private static final Pattern NAME_PATTERN           = Pattern.compile("\\{([^/]+?)\\}");
		// JDK 6 doesn't support named capture groups

		private static final Pattern URL_PATTERN            =
			Pattern.compile("(?:(\\w+)://)?((?:\\[.+?])|(?<!\\[)(?:[^/?]+?))(?::(\\d{2,5}))?([/?].*)?");

		private final List<String> pathVariables = new ArrayList<>();

		private final Pattern uriPattern;

		private static String getNameSplatReplacement(String name) {
			return "(?<" + name + ">.*)";
		}

		private static String getNameReplacement(String name) {
			return "(?<" + name + ">[^\\/]*)";
		}

		static String filterQueryParams(String uri) {
			int hasQuery = uri.lastIndexOf('?');
			if (hasQuery != -1) {
				return uri.substring(0, hasQuery);
			}
			else {
				return uri;
			}
		}

		static String filterHostAndPort(String uri) {
			if (uri.startsWith("/")) {
				return uri;
			}
			else {
				Matcher matcher = URL_PATTERN.matcher(uri);
				if (matcher.matches()) {
					String path = matcher.group(4);
					return path == null ? "/" : path;
				}
				else {
					throw new IllegalArgumentException("Unable to parse url [" + uri + "]");
				}
			}
		}

		/**
		 * Creates a new {@code UriPathTemplate} from the given {@code uriPattern}.
		 *
		 * @param uriPattern The pattern to be used by the template
		 */
		UriPathTemplate(String uriPattern) {
			String s = "^" + filterQueryParams(filterHostAndPort(uriPattern));

			Matcher m = NAME_SPLAT_PATTERN.matcher(s);
			while (m.find()) {
				for (int i = 1; i <= m.groupCount(); i++) {
					String name = m.group(i);
					pathVariables.add(name);
					s = m.replaceFirst(getNameSplatReplacement(name));
					m.reset(s);
				}
			}

			m = NAME_PATTERN.matcher(s);
			while (m.find()) {
				for (int i = 1; i <= m.groupCount(); i++) {
					String name = m.group(i);
					pathVariables.add(name);
					s = m.replaceFirst(getNameReplacement(name));
					m.reset(s);
				}
			}

			m = FULL_SPLAT_PATTERN.matcher(s);
			while (m.find()) {
				s = m.replaceAll(FULL_SPLAT_REPLACEMENT);
				m.reset(s);
			}

			this.uriPattern = Pattern.compile(s + "$");
		}

		/**
		 * Tests the given {@code uri} against this template, returning {@code true} if
		 * the uri matches the template, {@code false} otherwise.
		 *
		 * @param uri The uri to match
		 *
		 * @return {@code true} if there's a match, {@code false} otherwise
		 */
		public boolean matches(String uri) {
			return matcher(uri).matches();
		}

		/**
		 * Matches the template against the given {@code uri} returning a map of path
		 * parameters extracted from the uri, keyed by the names in the template. If the
		 * uri does not match, or there are no path parameters, an empty map is returned.
		 *
		 * @param uri The uri to match
		 *
		 * @return the path parameters from the uri. Never {@code null}.
		 */
		final Map<String, String> match(String uri) {
			Map<String, String> pathParameters = new HashMap<>(pathVariables.size());

			Matcher m = matcher(uri);
			if (m.matches()) {
				int i = 1;
				for (String name : pathVariables) {
					String val = m.group(i++);
					pathParameters.put(name, val);
				}
			}
			return pathParameters;
		}

		private Matcher matcher(String uri) {
			uri = filterQueryParams(filterHostAndPort(uri));
			return uriPattern.matcher(uri);
		}

	}

	static final class HttpPrefixPredicate implements Predicate<HttpServerRequest> {

		final HttpMethod method;
		final String     prefix;

		public HttpPrefixPredicate(String prefix, HttpMethod method) {
			this.prefix = prefix;
			this.method = method;
		}

		@Override
		public boolean test(HttpServerRequest key) {
			return method.equals(key.method()) && key.uri().startsWith(prefix);
		}
	}
}