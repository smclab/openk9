package io.openk9.search.query.internal.query.parser;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class Parse {

	public abstract Semantic getSemantics();

	public abstract List<Parse> getChildren();

	public abstract Rule getRule();

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

	private static class ParseCat extends Parse {

		public ParseCat(String children) {
			value = children;
		}

		@Override
		public Semantic getSemantics() {
			return Semantic.of(Map.of("token", value));
		}

		@Override
		public List<Parse> getChildren() {
			return List.of();
		}

		@Override
		public Rule getRule() {
			return null;
		}

		private final String value;

		@Override
		public String toString() {
			return value;
		}
	}

	private static class ParseImpl extends Parse {

		private ParseImpl(Rule rule, Parse...children) {
			this.rule = rule;
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

		@Override
		public List<Parse> getChildren() {
			return children;
		}

		@Override
		public Rule getRule() {
			return rule;
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

	}

}