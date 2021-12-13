package io.openk9.search.query.internal.query.parser;

import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.query.internal.query.parser.annotator.AggregatorAnnotator;
import io.openk9.search.query.internal.query.parser.annotator.AnnotatorConfig;
import io.openk9.search.query.internal.query.parser.annotator.NerAnnotator;
import org.elasticsearch.common.unit.Fuzziness;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = {
		GrammarProvider.class,
		AnnotatorConfig.class
	}
)
@Designate(ocd = GrammarProvider.Config.class)
public class GrammarProvider implements AnnotatorConfig {

	@ObjectClassDefinition(
		name = "Annotator Configuration"
	)
	@interface Config {
		AnnotatorConfig.InternalFuzziness nerAnnotatorFuzziness() default AnnotatorConfig.InternalFuzziness.ONE;

		AnnotatorConfig.InternalFuzziness restFuzziness() default AnnotatorConfig.InternalFuzziness.AUTO;

		int nerSize() default 5;

		int restSize() default 10;

		long timeoutMs() default 10_000;

		String[] aggregatorAnnotator() default {

		};

		String[] nerAnnotator() default {
			"person", "loc", "organization"
		};

		String[] stopWords() default {
			"ad", "al", "allo", "ai", "agli", "all", "agl", "alla", "alle",
			"con", "col", "coi", "da", "dal", "dallo", "dai", "dagli", "dall",
			"dagl", "dalla", "dalle", "di", "del", "dello", "dei", "degli",
			"dell", "degl", "della", "delle", "in", "nel", "nello", "nei", "negli",
			"nell", "negl", "nella", "nelle", "su", "sul", "sullo", "sui", "sugli",
			"sull", "sugl", "sulla", "sulle", "per", "tra", "contro", "io", "tu",
			"lui", "lei", "noi", "voi", "loro", "mio", "mia", "miei", "mie", "tuo",
			"tua", "tuoi", "tue", "suo", "sua", "suoi", "sue", "nostro", "nostra",
			"nostri", "nostre", "vostro", "vostra", "vostri", "vostre", "mi", "ti",
			"ci", "vi", "lo", "la", "li", "le", "gli", "ne", "il", "un", "uno", "una",
			"ma", "ed", "se", "perché", "anche", "come", "dov", "dove", "che", "chi", "cui",
			"non", "più", "quale", "quanto", "quanti", "quanta", "quante", "quello", "quelli",
			"quella", "quelle", "questo", "questi", "questa", "queste", "si", "tutto", "tutti",
			"a", "c", "e", "i", "l", "o", "ho", "hai", "ha", "abbiamo", "avete", "hanno", "abbia",
			"abbiate", "abbiano", "avrò", "avrai", "avrà", "avremo", "avrete", "avranno", "avrei",
			"avresti", "avrebbe", "avremmo", "avreste", "avrebbero", "avevo", "avevi", "aveva",
			"avevamo", "avevate", "avevano", "ebbi", "avesti", "ebbe", "avemmo", "aveste", "ebbero",
			"avessi", "avesse", "avessimo", "avessero", "avendo", "avuto", "avuta", "avuti", "avute",
			"sono", "sei", "è", "siamo", "siete", "sia", "siate", "siano", "sarò", "sarai", "sarà", "saremo",
			"sarete", "saranno", "sarei", "saresti", "sarebbe", "saremmo", "sareste", "sarebbero", "ero", "eri",
			"era", "eravamo", "eravate", "erano", "fui", "fosti", "fu", "fummo", "foste", "furono", "fossi",
			"fosse", "fossimo", "fossero", "essendo", "faccio", "fai", "facciamo", "fanno", "faccia", "facciate",
			"facciano", "farò", "farai", "farà", "faremo", "farete", "faranno", "farei", "faresti", "farebbe",
			"faremmo", "fareste", "farebbero", "facevo", "facevi", "faceva", "facevamo", "facevate", "facevano", "feci",
			"facesti", "fece", "facemmo", "faceste", "fecero", "facessi", "facesse", "facessimo", "facessero", "facendo",
			"sto", "stai", "sta", "stiamo", "stanno", "stia", "stiate", "stiano", "starò", "starai", "starà", "staremo",
			"starete", "staranno", "starei", "staresti", "starebbe", "staremmo", "stareste", "starebbero", "stavo", "stavi",
			"stava", "stavamo", "stavate", "stavano", "stetti", "stesti", "stette", "stemmo", "steste", "stettero", "stessi",
			"stesse", "stessimo", "stessero", "stando"
		};

		String ruleJsonPath() default "";

	}

	@Activate
	void activate(Config config, BundleContext bundleContext)
		throws IOException {

		_config = config;

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

		String[] nerAnnotator = nerAnnotator();

		Stream<Annotator> nerAnnotatorStream =
			Arrays
				.stream(nerAnnotator)
				.map(keyword -> new NerAnnotator(keyword, this,
					_restHighLevelClientProvider));

		String[] aggregatorAnnotator = this.aggregatorAnnotator();

		Stream<Annotator> aggregatorAnnotatorStream =
			Arrays
				.stream(aggregatorAnnotator)
				.map(keyword -> new AggregatorAnnotator(keyword, this,
					_restHighLevelClientProvider));

		List<Annotator> newAnnotators =
			Stream.of(
				_annotatorList.stream(), nerAnnotatorStream, aggregatorAnnotatorStream)
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

						String[] split = sem.split(",");

						List<Integer> collect =
							Arrays.stream(split)
								.map(Integer::parseInt)
								.collect(Collectors.toList());

						Function<SemanticTypes, SemanticTypes> function =
							s -> SemanticTypes.of(
								collect.stream().map(s::get).toArray(SemanticType[]::new));

						rules.add(Rule.of(lhs, rhs, Semantic.of(function)));

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

	@Override
	public String[] nerAnnotator() {
		return _config.nerAnnotator();
	}

	@Override
	public String[] aggregatorAnnotator() {
		return _config.aggregatorAnnotator();
	}

	@Override
	public String[] stopWords() {
		return _config.stopWords();
	}

	@Override
	public Fuzziness nerAnnotatorFuzziness() {
		return _config.nerAnnotatorFuzziness().getFuzziness();
	}

	@Override
	public Fuzziness restFuzziness() {
		return _config.restFuzziness().getFuzziness();
	}

	@Override
	public int restSize() {
		return _config.restSize();
	}

	@Override
	public int nerSize() {
		return _config.nerSize();
	}

	@Override
	public Duration timeout() {
		return Duration.ofMillis(_config.timeoutMs());
	}

	private transient Grammar _grammar;

	private Config _config;

	@Reference(
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private List<Annotator> _annotatorList;

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference
	private JsonFactory _jsonFactory;

}
