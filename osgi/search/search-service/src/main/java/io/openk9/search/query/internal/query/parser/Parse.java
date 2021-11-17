package io.openk9.search.query.internal.query.parser;

import io.openk9.search.api.query.parser.Tuple;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class Parse {

	public abstract Semantic getSemantics();

	public abstract Rule getRule();

	public abstract Tuple<Integer> getPos();

	public static Parse of(Rule rule, String child) {
		return new ParseImpl(rule, new ParseCat(child));
	}

	public static Parse of(Rule rule, String...strings) {
		return new ParseImpl(
			rule,
			Arrays
				.stream(strings)
				.map(ParseCat::new)
				.toArray(ParseCat[]::new)
		);
	}

	public static Parse of(Rule rule, Parse child) {
		return new ParseImpl(rule, child);
	}

	public static Parse of(Rule rule, Parse...children) {
		return new ParseImpl(rule, children);
	}

	public static Parse of(Rule rule, Tuple<Integer> pos, String child) {
		return new ParseImpl(rule, pos, new ParseCat(child));
	}

	public static Parse of(Rule rule, Tuple<Integer> pos, String...strings) {
		return new ParseImpl(
			rule,
			pos,
			Arrays
				.stream(strings)
				.map(ParseCat::new)
				.toArray(ParseCat[]::new)
		);
	}

	public static Parse of(Rule rule, Tuple<Integer> pos, Parse child) {
		return new ParseImpl(rule, pos, child);
	}

	public static Parse of(Rule rule, Tuple<Integer> pos, Parse...children) {
		return new ParseImpl(rule, pos, children);
	}

	private static class ParseCat extends Parse {

		public ParseCat(String children) {
			value = children;
		}

		@Override
		public Semantic getSemantics() {
			return Semantic.of(Map.of("token", value));
		}

		@Override
		public Rule getRule() {
			return null;
		}

		@Override
		public Tuple<Integer> getPos() {
			return Tuple.of();
		}

		private final String value;

		@Override
		public String toString() {
			return value;
		}
	}

	private static class ParseImpl extends Parse {

		private ParseImpl(Rule rule, Parse...children) {
			this(rule, Tuple.of(), children);
		}

		private ParseImpl(Rule rule, Tuple<Integer> pos, Parse...children) {
			this.rule = rule;
			this.pos = pos;
			this.children = List.of(children);
			semantics = this._computeSemantics();
			score = Float.NaN;
			denotation = null;
			_validateParse();
		}

		@Override
		public Semantic getSemantics() {
			return semantics;
		}

		private List<Parse> getChildren() {
			return children;
		}

		@Override
		public Rule getRule() {
			return rule;
		}

		@Override
		public Tuple<Integer> getPos() {
			return pos;
		}

		@Override
		public String toString() {

			String childrenString = children.stream().map(Parse::toString).collect(
				Collectors.joining(" "));

			return String.format("(%s %s)", rule.getLhs(), childrenString);

		}

		private Semantic _computeSemantics() {
			if (rule.isLexical()) {
				return rule.getSem();
			}
			else {

				SemanticTypes childSemantics =
					getChildren()
						.stream()
						.map(Parse::getSemantics)
						.map(Semantic::apply)
						.map(SemanticTypes::getSemanticTypes)
						.flatMap(Collection::stream)
						.collect(
							Collectors.collectingAndThen(
								Collectors.toList(), SemanticTypes::of));

				return rule.applySemantics(childSemantics);
			}
		}

		private void _validateParse() {

		}

		private Rule rule;
		private List<Parse> children;
		private Semantic semantics;
		private float score;
		private final Object denotation;
		private Tuple<Integer> pos;

	}

}