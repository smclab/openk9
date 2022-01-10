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
		return new ParseImpl(rule, pos, new ParseCat(child, pos));
	}

	public static Parse of(Rule rule, Tuple<Integer> pos, String...strings) {
		return new ParseImpl(
			rule,
			pos,
			Arrays
				.stream(strings)
				.map(child -> new ParseCat(child, pos))
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
			_value = children;
			_pos = Tuple.of();
		}

		public ParseCat(String children, Tuple<Integer> pos) {
			_value = children;
			_pos = pos;
		}

		@Override
		public Semantic getSemantics() {
			return Semantic.of(_pos, Map.of("token", _value));
		}

		@Override
		public Rule getRule() {
			return null;
		}

		@Override
		public Tuple<Integer> getPos() {
			return Tuple.of();
		}

		private final String _value;
		private final Tuple<Integer> _pos;

		@Override
		public String toString() {
			return "ParseCat(" + _value + ")";
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

			return String.format("Parse(%s %s pos: %s)", rule.getLhs(), childrenString, pos);

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