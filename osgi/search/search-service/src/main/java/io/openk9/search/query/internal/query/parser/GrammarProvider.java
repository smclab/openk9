package io.openk9.search.query.internal.query.parser;

import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.search.api.query.parser.Annotator;
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
import java.util.List;

@Component(
	immediate = true, service = GrammarProvider.class
)
public class GrammarProvider {

	@interface Config {
		String ruleJsonPath() default "";
	}

	@Activate
	void activate(Config config, BundleContext bundleContext)
		throws IOException {

		String ruleJsonPath = config.ruleJsonPath();

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

		_grammar = new Grammar(
			List.of(GrammarMixin.of(rules, _annotatorList)));
	}

	private List<Rule> _toJavaRules(JsonNode jsonNode) {

		ArrayNode arrayNode = jsonNode.toArrayNode();

		List<Rule> rules = new ArrayList<>();

		for (JsonNode node : arrayNode) {

			String lhs = node.get("lhs").asText();
			String rhs = node.get("rhs").asText();
			String sem = node.get("sem").asText();

			switch (sem) {
				case "IDENTITY":
					rules.add(Rule.of(lhs, rhs, Semantic.identity()));
					break;
				case "MERGE":
					rules.add(Rule.of(lhs, rhs, Semantic.of(SemanticTypes::merge)));
					break;
				default:
					if (sem.isBlank()) {
						rules.add(Rule.of(lhs, rhs));
					}
					else {

						int i = Integer.parseInt(sem);
						rules.add(
							Rule.of(
								lhs, rhs,
								Semantic.of(
									sems -> SemanticTypes.of(sems.get(i)))));
					}
			}

		}

		return rules;
	}

	@Modified
	void modified(Config config, BundleContext bundleContext)
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

	@Reference(
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private List<Annotator> _annotatorList;

	@Reference
	private JsonFactory _jsonFactory;

}
