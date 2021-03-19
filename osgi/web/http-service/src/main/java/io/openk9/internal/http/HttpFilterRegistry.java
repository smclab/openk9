package io.openk9.internal.http;


import io.openk9.http.web.HttpFilter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpHandlerWrapper;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = HttpFilterRegistry.class
)
public class HttpFilterRegistry {

	public HttpHandler getHttpFilter(
		String path, HttpHandler httpHandler) {

		LinkedList<HttpFilter> httpFilterList = _httpFilterMap
			.entrySet()
			.stream()
			.filter(entry -> path.matches(entry.getKey()))
			.map(Map.Entry::getValue)
			.flatMap(Collection::stream)
			.sorted()
			.collect(Collectors.toCollection(LinkedList::new));

		if (httpFilterList.isEmpty()) {
			return httpHandler;
		}
		else {

			httpFilterList.addLast(
				(httpRequest, httpResponse, chain) ->
					httpHandler.apply(httpRequest, httpResponse));

			BiFunction<HttpRequest, HttpResponse, Publisher<Void>>
				httpFilter = (httpRequest, httpResponse) -> {

				Iterator<HttpFilter> iterator = httpFilterList.iterator();

				return _chain(iterator.next(), iterator).apply(httpRequest, httpResponse);

			};

			return new HttpHandlerWrapper(httpHandler) {
				@Override
				public Publisher<Void> apply(
					HttpRequest req, HttpResponse res) {
					return httpFilter.apply(req, res);
				}
			};

		}

	}

	private BiFunction<HttpRequest, HttpResponse, Publisher<Void>> _chain(
		HttpFilter prev, Iterator<HttpFilter> iterator) {

		if (iterator.hasNext()) {
			return (httpRequest, httpResponse) ->
				prev.doFilter(
					httpRequest, httpResponse,
					_chain(iterator.next(), iterator));
		}

		return (httpRequest, httpResponse) ->
			prev.doFilter(httpRequest, httpResponse, null);

	}

	@Reference(
		service = HttpFilter.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		bind = "addHttpFilter",
		unbind = "removeHttpFilter",
		policyOption = ReferencePolicyOption.GREEDY,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addHttpFilter(
		HttpFilter httpFilter, Map<String, Object> props) {

		String[] urlPatterns = _getUrlPatterns(props);

		for (String urlPattern : urlPatterns) {
			_httpFilterMap
				.computeIfAbsent(urlPattern, key -> new ArrayList<>())
				.add(httpFilter);
		}

	}

	public void removeHttpFilter(
		HttpFilter httpFilter, Map<String, Object> props) {

		String[] urlPatterns = _getUrlPatterns(props);

		for (String urlPattern : urlPatterns) {
			List<HttpFilter> httpFilters = _httpFilterMap.get(urlPattern);
			if (httpFilters != null) {
				httpFilters.remove(httpFilter);
			}
		}

	}

	private String[] _getUrlPatterns(Map<String, Object> props) {

		Object urlPatternsObj = props.get(HttpFilter.URL_PATTERNS);

		if (urlPatternsObj == null) {
			return DEFAULT_URL_PATTERNS;
		}
		else if (urlPatternsObj instanceof String[]) {
			return (String[])urlPatternsObj;
		}
		else if (urlPatternsObj instanceof String) {
			return new String[] {(String)urlPatternsObj};
		}

		if (_log.isWarnEnabled()) {
			_log.warn(
				"url.patterns must be an String[] or String. props: " + props);
			_log.warn("return default urlPatterns: " + ALL_MATCH);
		}

		return DEFAULT_URL_PATTERNS;

	}

	private final Map<String, List<HttpFilter>> _httpFilterMap =
		new HashMap<>();

	private static final String ALL_MATCH = "/*";

	private static final String[] DEFAULT_URL_PATTERNS =
		new String[] {ALL_MATCH};

	private static final Logger _log =
		LoggerFactory.getLogger(HttpFilterRegistry.class);

}