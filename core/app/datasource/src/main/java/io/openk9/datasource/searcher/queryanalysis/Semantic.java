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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface Semantic extends Function<SemanticTypes, SemanticTypes> {

	@Override
	SemanticTypes apply(SemanticTypes semanticTypes);

	SemanticTypes apply();

	Tuple<Integer> getPos();

	class FunctionSemantic implements Semantic {

		public FunctionSemantic(Function<SemanticTypes, SemanticTypes> semantic) {
			_semantic = semantic;
			_pos = Tuple.of();
		}

		public FunctionSemantic(
			Function<SemanticTypes, SemanticTypes> semantic,
			Tuple<Integer> pos) {
			_semantic = semantic;
			_pos = pos;
		}

		@Override
		public SemanticTypes apply() {
			return _semantic.apply(SemanticTypes.of());
		}

		@Override
		public Tuple<Integer> getPos() {
			return _pos;
		}

		@Override
		public SemanticTypes apply(SemanticTypes semanticTypes) {
			return _semantic.apply(semanticTypes);
		}

		@Override
		public String toString() {
			return "FunctionSemantic";
		}

		private final Function<SemanticTypes, SemanticTypes> _semantic;
		private final Tuple<Integer> _pos;


	}

	class MapSemantic implements Semantic {

		public MapSemantic(Map<String, Object>[] semantic) {
			_semantic = SemanticType.of(semantic);
			_pos = Tuple.of();
		}

		public MapSemantic(Map<String, Object>[] semantic, Tuple<Integer> pos) {
			_semantic = SemanticType.of(pos, semantic);
			_pos = pos;
		}

		@Override
		public SemanticTypes apply() {
			return SemanticTypes.of(_semantic);
		}

		@Override
		public Tuple<Integer> getPos() {
			return _pos;
		}

		@Override
		public SemanticTypes apply(SemanticTypes semanticTypes) {
			return apply();
		}

		@Override
		public String toString() {
			return "MapSemantic{" + _semantic + "}";
		}

		private final SemanticType _semantic;
		private final Tuple<Integer> _pos;


	}

	class NullSemantic implements Semantic {

		@Override
		public SemanticTypes apply() {
			return SemanticTypes.of();
		}

		@Override
		public Tuple<Integer> getPos() {
			return Tuple.of();
		}

		@Override
		public SemanticTypes apply(SemanticTypes semanticTypes) {
			return SemanticTypes.of();
		}

		@Override
		public String toString() {
			return "";
		}

	}

	static Semantic of() {
		return _NULL_SEMANTIC;
	}

	@SafeVarargs
	static Semantic of(Map<String, Object>...sem) {
		return sem == null ? _NULL_SEMANTIC : new MapSemantic(sem);
	}

	static Semantic of(Function<SemanticTypes, SemanticTypes> function) {
		Objects.requireNonNull(function, "function is null");
		return new FunctionSemantic(function);
	}

	@SafeVarargs
	static Semantic of(
		Tuple<Integer> pos, Map<String, Object>...sem) {
		Objects.requireNonNull(pos, "pos is null");
		return sem == null ? _NULL_SEMANTIC : new MapSemantic(sem, pos);
	}

	static Semantic of(
		Tuple<Integer> pos, Function<SemanticTypes, SemanticTypes> function) {
		Objects.requireNonNull(function, "function is null");
		Objects.requireNonNull(pos, "pos is null");
		return new FunctionSemantic(function, pos);
	}

	static Semantic identity() {
		return new FunctionSemantic(Function.identity());
	}

	Semantic _NULL_SEMANTIC = new NullSemantic();

}
