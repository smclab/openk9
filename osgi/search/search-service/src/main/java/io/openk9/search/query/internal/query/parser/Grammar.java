package io.openk9.search.query.internal.query.parser;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.query.internal.query.parser.util.Itertools;
import io.openk9.search.query.internal.query.parser.util.Utils;
import io.openk9.search.query.internal.query.parser.util.Tuple ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Grammar {

	public Grammar(List<GrammarMixin> bases) {
		this(bases, "$ROOT");
	}

	public Grammar(List<GrammarMixin> bases, String startSymbol) {
		for (GrammarMixin base : bases) {
			rules.addAll(base.getRules());
			annotators.addAll(base.getAnnotators());
		}

		this.startSymbol = startSymbol;

		for (Rule rule : rules) {
			addRule(rule);
		}

	}

	public List<Parse> parseInput(String input) {
		return parseInput(-1, input);
	}

	public List<Parse> parseInput(long tenantId, String input) {

		String[] tokens = input.split(" ");

		Map<Tuple, List<Parse>> chart = new HashMap<>();

		for (int j = 1; j < tokens.length + 1; j++) {
			for (int i = j - 1; i != -1 ; i--) {
				applyAnnotators(chart, tokens, i, j, tenantId);
				applyLexicalRules(chart, tokens, i, j);
				applyBinaryRules(chart, i, j);
				applyUnaryRules(chart, i, j);
			}
		}

		List<Parse> parses = chart.getOrDefault(
			Tuple.of(0, tokens.length), List.of());

		if (startSymbol != null && !startSymbol.isBlank()) {
			parses = parses
				.stream()
				.filter(parse ->
					parse.getRule().getLhs().equals(startSymbol))
				.collect(Collectors.toList());
		}

		return parses;

	}

	@Override
	public String toString() {
		return "Grammar{" +
			   "startSymbol='" + startSymbol + '\'' +
			   ", lexicalRules=" + lexicalRules +
			   ", unaryRules=" + unaryRules +
			   ", binaryRules=" + binaryRules +
			   ", categories=" + categories +
			   ", rules=" + rules +
			   ", annotators=" + annotators +
			   '}';
	}

	private void applyUnaryRules(
		Map<Tuple, List<Parse>> chart, int i, int j) {

		Tuple chartKey = Tuple.of(i, j);

		List<Parse> parseList = chart.getOrDefault(chartKey, List.of());

		for (int i1 = 0; i1 < parseList.size(); i1++) {
			Parse parse = parseList.get(i1);
			for (Rule rule : unaryRules.getOrDefault(
				Tuple.of(parse.getRule().getLhs()), List.of())) {
				chart
					.computeIfAbsent(chartKey, k -> new ArrayList<>())
					.add(Parse.of(rule, parse));
			}
		}

	}

	private void applyBinaryRules(
		Map<Tuple, List<Parse>> chart, int i, int j) {

		Tuple chartKey = Tuple.of(i, j);

		for (int k = i + 1; k < j; k++) {

			Iterable<List<Parse>> product = Itertools.product(
				chart.getOrDefault(Tuple.of(i, k), List.of()),
				chart.getOrDefault(Tuple.of(k, j), List.of()));

			for (List<Parse> parses : product) {
				Parse parse1 = parses.get(0);
				Parse parse2 = parses.get(1);

				for (Rule rule : binaryRules.getOrDefault(
					Tuple.of(
						parse1.getRule().getLhs(),
						parse2.getRule().getLhs()), List.of())) {

					chart
						.computeIfAbsent(chartKey, key -> new ArrayList<>())
						.add(Parse.of(rule, parse1, parse2));

				}

			}
		}

	}

	private void applyLexicalRules(
		Map<Tuple, List<Parse>> chart, String[] tokens, int i, int j) {

		tokens = Arrays.stream(tokens, i, j).toArray(String[]::new);

		Tuple tokenKey = Utils.toTuple(tokens);

		Tuple chartKey = Tuple.of(i, j);

		for (Rule rule : lexicalRules.getOrDefault(tokenKey, List.of())) {
			chart
				.computeIfAbsent(chartKey, (k) -> new ArrayList<>())
				.add(Parse.of(rule, tokens));
		}

	}

	private void applyAnnotators(
		Map<Tuple, List<Parse>> chart, String[] tokens, int i, int j, long tenantId) {

		tokens = Arrays.stream(tokens, i, j).toArray(String[]::new);

		Tuple chartKey = Tuple.of(i, j);

		for (Annotator annotator : annotators) {
			for (CategorySemantics categorySemantics : annotator.annotate(tenantId, tokens)) {

				String category = categorySemantics.getCategory();

				Map<String, Object> semantics =
					categorySemantics.getSemantics();

				Rule rule = new Rule(
					category, tokens, Semantic.of(semantics));

				chart.computeIfAbsent(
					chartKey, (k) -> new ArrayList<>())
					.add(Parse.of(rule, tokens));


			}
		}

	}

	private void addRule(Rule rule) {

		if (rule.containsOptionals()) {
			addRuleContainingOptional(rule);
		}
		else if (rule.isLexical()) {
			Tuple rhs = rule.getRhsTuple();
			lexicalRules.computeIfAbsent(rhs, (k) -> new ArrayList<>())
				.add(rule);
		}
		else if (rule.isUnary()) {
			Tuple rhs = rule.getRhsTuple();
			unaryRules.computeIfAbsent(rhs, (k) -> new ArrayList<>())
				.add(rule);
		}
		else if (rule.isBinary()) {
			Tuple rhs = rule.getRhsTuple();
			binaryRules.computeIfAbsent(rhs, (k) -> new ArrayList<>())
				.add(rule);
		}
		else if (rule.isCat()) {
			addNAryRule(rule);
		}
		else {
			throw new RuntimeException(
				String.format(
					"RHS mixes terminals and non-terminals: %s", rule));
		}

	}

	private void addNAryRule(Rule rule) {

		String category =
			addCategory(String.join("_", rule.getLhs(), rule.getRhs()[0]));

		addRule(
			new Rule(
				category,
				Arrays.stream(rule.getRhs()).skip(1).toArray(String[]::new),
				Semantic.of(sems -> sems)
			));

		addRule(
			new Rule(
				rule.getLhs(),
				new String[]{rule.getRhs()[0], category},
				Semantic.of(sems -> rule.applySemantics(
					SemanticTypes.of(sems.get(0), sems.get(1))).apply())
			));

	}

	private String addCategory(String baseName) {

		if (Rule.isCat(baseName)) {
			String name = categories.stream()
				.collect(Collectors.joining("_", baseName, ""));
			categories.add(name);
			return name;
		}

		return null;
	}

	private void addRuleContainingOptional(Rule rule) {
		String[] rhs = rule.getRhs();
		int first =
			IntStream
				.range(0, rhs.length)
				.filter(i -> Rule.isOptional(rhs[i]))
				.findFirst()
				.orElse(-1);

		String[] prefix;

		if (first == 0) {
			prefix = new String[0];
		}
		else {
			prefix = Arrays.stream(rhs, 0, first).toArray(String[]::new);
		}
		String[] suffix = Arrays.stream(rhs, first + 1, rhs.length).toArray(String[]::new);
		String deoptionalized = rhs[first].substring(1);

		Semantic sem = rule.getSem();

		addRule(
			new Rule(
				rule.getLhs(),
				Stream
					.of(Arrays.stream(prefix), Stream.of(deoptionalized), Arrays.stream(suffix))
					.flatMap(Function.identity()).toArray(String[]::new),
				rule.getSem())
		);

		if (rule.getSem() instanceof Semantic.FunctionSemantic) {
			sem = Semantic.of(sems -> {

				int index = Math.min(first, sems.size());

				List<SemanticType> semanticTypes = sems.getSemanticTypes();

				Stream<SemanticType> streamPrefix = semanticTypes.stream().limit(index);
				Stream<SemanticType> streamSuffix =
					semanticTypes.stream().skip(index);

				SemanticType[] strings =
					Stream
						.of(streamPrefix, Stream.of(SemanticType.of()), streamSuffix)
						.flatMap(Function.identity())
						.toArray(SemanticType[]::new);

				return rule.getSem().apply(SemanticTypes.of(strings));
			});
		}

		addRule(
			new Rule(
				rule.getLhs(),
				Stream.concat(Arrays.stream(prefix), Arrays.stream(suffix)).toArray(String[]::new),
				sem)
		);

	}

	private String startSymbol;
	private final Map<Tuple, List<Rule>> lexicalRules = new HashMap<>();
	private final Map<Tuple, List<Rule>> unaryRules = new HashMap<>();
	private final Map<Tuple, List<Rule>> binaryRules = new HashMap<>();
	private final Set<String> categories = new HashSet<>();
	private final List<Rule> rules = new ArrayList<>();
	private final List<Annotator> annotators = new ArrayList<>();

}
