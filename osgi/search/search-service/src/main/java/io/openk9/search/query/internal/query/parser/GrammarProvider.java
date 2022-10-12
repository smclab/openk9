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

package io.openk9.search.query.internal.query.parser;

import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.query.internal.query.parser.annotator.AggregatorAnnotator;
import io.openk9.search.query.internal.query.parser.annotator.AnnotatorConfig;
import io.openk9.search.query.internal.query.parser.annotator.AutoCompleteAnnotator;
import io.openk9.search.query.internal.query.parser.annotator.AutoCompleteNerAnnotator;
import io.openk9.search.query.internal.query.parser.annotator.BaseAutoCompleteNerAnnotator;
import io.openk9.search.query.internal.query.parser.annotator.NerAnnotator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = GrammarProvider.class,
	configurationPid = AnnotatorConfig.PID
)
public class GrammarProvider {

	@Activate
	void activate(AnnotatorConfig config, BundleContext bundleContext)
		throws IOException {

		_config = config;

		String ruleJsonPath = _config.ruleJsonPath();

		List<Rule> rules = List.of();

		if (!ruleJsonPath.isBlank() && Files.exists(Paths.get(ruleJsonPath))) {

			byte[] bytes = Files.readAllBytes(Paths.get(ruleJsonPath));

			JsonNode jsonNode = _jsonFactory.fromJsonToJsonNode(bytes);

			rules = _toJavaRules(jsonNode);
		}
		else {
			URL resource = bundleContext.getBundle().getResource("rule.json");

			try(InputStream is = resource.openStream()) {

				byte[] bytes = is.readAllBytes();

				JsonNode jsonNode = _jsonFactory.fromJsonToJsonNode(bytes);

				rules = _toJavaRules(jsonNode);

			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}

		String[] nerAnnotator = _config.nerAnnotator();

		Stream<Annotator> nerAnnotatorStream =
			Arrays
				.stream(nerAnnotator)
				.map(keyword -> new NerAnnotator(keyword, _config,
					_restHighLevelClientProvider));

		String[] aggregatorAnnotator = _config.aggregatorAnnotator();

		Stream<Annotator> aggregatorAnnotatorStream =
			Arrays
				.stream(aggregatorAnnotator)
				.map(keyword -> new AggregatorAnnotator(keyword, _config,
					_restHighLevelClientProvider));

		String[] autocompleteAnnotator = _config.autocompleteAnnotator();

		Stream<Annotator> autocompleteAnnotatorStream =
			Arrays
				.stream(autocompleteAnnotator)
				.map(keyword -> new AutoCompleteAnnotator(keyword, _config,
					_restHighLevelClientProvider));

		String[] autocompleteEntityTypes =
			_config.autocompleteEntityTypes();

		Stream<Annotator> autocompleteAnnotatorNerStream =
			Arrays
				.stream(autocompleteEntityTypes)
				.map(keyword -> new AutoCompleteNerAnnotator(keyword, _config,
					_restHighLevelClientProvider));

		List<Annotator> newAnnotators =
			Stream.of(
				_annotatorList.stream(), nerAnnotatorStream,
				aggregatorAnnotatorStream, autocompleteAnnotatorStream,
					autocompleteAnnotatorNerStream
			)
				.flatMap(Function.identity())
				.collect(Collectors.toList());

		_grammar = new Grammar(
			List.of(GrammarMixin.of(rules, newAnnotators)));
	}

	private List<Rule> _toJavaRules(JsonNode jsonNode) {

		ArrayNode arrayNode = jsonNode.toArrayNode();

		List<Rule> rules = new ArrayList<>();

		for (JsonNode node : arrayNode) {

			String lhs = node.get("lhs").asText();
			String rhs = node.get("rhs").asText();

			rules.add(Rule.of(lhs, rhs, Semantic.identity()));

		}

		return rules;
	}

	@Modified
	void modified(AnnotatorConfig config, BundleContext bundleContext)
		throws IOException {
		deactivate();
		activate(config, bundleContext);
	}

	@Deactivate
	void deactivate() {
		_grammar = null;
	}

	public Grammar getGrammar() {
		return _grammar;
	}

	private transient Grammar _grammar;

	private AnnotatorConfig _config;

	@Reference(
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private List<Annotator> _annotatorList;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

}
