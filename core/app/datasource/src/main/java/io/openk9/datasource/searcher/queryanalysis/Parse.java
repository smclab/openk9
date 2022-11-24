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

package io.openk9.datasource.searcher.queryanalysis;

import io.openk9.datasource.searcher.util.Tuple;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class Parse {

	public abstract JsonObject toJson();

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
		public JsonObject toJson() {
			return new JsonObject().put("value", _value);
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
		public JsonObject toJson() {

			JsonArray reduce =
				children
					.stream()
					.map(Parse::toJson)
					.reduce(
						new JsonArray(), JsonArray::add, (a, b) -> b);

			JsonObject result = new JsonObject();

			result.put("lhs", rule.getLhs());
			result.put("children", reduce);

			JsonArray posArrayNode = new JsonArray();

			if (!pos.isEmpty()) {
				for (Integer integer : pos.toArray()) {
					posArrayNode.add(integer.toString());
				}
			}

			result.put("pos", posArrayNode);

			return result;
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

				List<SemanticType> collect = getChildren()
					.stream()
					.map(Parse::getSemantics)
					.map(Semantic::apply)
					.filter(SemanticTypes::isNotEmpty)
					.map(SemanticTypes::getSemanticTypes)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());

				if (collect.isEmpty()) {
					return rule.applySemantics(SemanticTypes.of());
				}
				else {
					return rule.applySemantics(SemanticTypes.of(collect));
				}

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