package io.openk9.search.query.internal.query.parser;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface Semantic extends Function<SemanticTypes, SemanticTypes> {

	@Override
	SemanticTypes apply(SemanticTypes semanticTypes);

	SemanticTypes apply();

	class FunctionSemantic implements Semantic {

		public FunctionSemantic(Function<SemanticTypes, SemanticTypes> semantic) {
			_semantic = semantic;
		}

		@Override
		public SemanticTypes apply() {
			return _semantic.apply(SemanticTypes.of());
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


	}

	class MapSemantic implements Semantic {

		public MapSemantic(Map<String, Object>[] semantic) {
			_semantic = SemanticType.of(semantic);
		}

		@Override
		public SemanticTypes apply() {
			return SemanticTypes.of(SemanticType.of(_semantic));
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


	}

	class NullSemantic implements Semantic {

		@Override
		public SemanticTypes apply() {
			return SemanticTypes.of();
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

	static Semantic identity() {
		return new FunctionSemantic(Function.identity());
	}

	Semantic _NULL_SEMANTIC = new NullSemantic();

}
