package io.openk9.search.query.internal.query.parser;

import io.openk9.search.api.query.parser.Tuple;
import io.openk9.search.query.internal.query.parser.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
public class Rule {

	public static Rule of(String lhs, String[] rhs, Semantic sem) {
		return new Rule(lhs, rhs, sem);
	}

	public static Rule of(String lhs, String[] rhs) {
		return new Rule(lhs, rhs);
	}

	public static Rule of(String lhs, String rhs, Semantic sem) {
		return new Rule(lhs, rhs, sem);
	}

	public static Rule of(String lhs, String rhs) {
		return new Rule(lhs, rhs, Semantic.of());
	}

	public Rule(String lhs, String[] rhs, Semantic sem) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.sem = sem;
		_validateRule();
	}

	public Rule(String lhs, String[] rhs) {
		this(lhs, rhs, Semantic.of());
	}

	public Rule(String lhs, String rhs) {
		this(lhs, Utils.split(rhs));
	}

	public Rule(String lhs, String rhs, Semantic sem) {
		this(lhs, Utils.split(rhs), sem);
	}

	public Semantic applySemantics(SemanticTypes sems) {

		if (this.sem instanceof Semantic.FunctionSemantic) {
			return Semantic.of(this.sem.getPos(), s -> this.sem.apply(sems));
		}
		return this.sem;
	}

	/**
	 *
	 * @return Returns true iff the given Rule is a lexical rule, i.e., contains only
	 * 	words (terminals) on the RHS.
	 */
	public boolean isLexical() {
		return Arrays.stream(rhs).noneMatch(Rule::isCat);
	}

	/**
	 *
	 * @return Returns true iff the given Rule is a lexical rule, i.e., contains only
	 * 	words (terminals) on the RHS.
	 */
	public boolean isCat() {
		return Arrays.stream(rhs).allMatch(Rule::isCat);
	}

	/**
	 *
	 * @return Returns true iff the given Rule is a unary compositional rule, i.e.,
	 * contains only a single category (non-terminal) on the RHS.
	 */
	public boolean isUnary() {
		return rhs.length == 1 && isCat(rhs[0]);
	}

	/**
	 *
	 * @return Returns true iff the given Rule is a binary compositional rule, i.e.,
	 * contains exactly two categories (non-terminals) on the RHS.
	 */
	public boolean isBinary() {
		return rhs.length == 2 && isCat(rhs[0]) && isCat(rhs[1]);
	}

	public boolean containsOptionals() {
		return Arrays.stream(rhs).anyMatch(Rule::isOptional);
	}

	public Tuple getRhsTuple() {
		return Utils.toTuple(rhs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Rule rule = (Rule) o;
		return lhs.equals(rule.lhs) && Arrays.equals(rhs, rule.rhs);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(lhs);
		result = 31 * result + Arrays.hashCode(rhs);
		return result;
	}

	/*
	return 'Rule' + str((self.lhs, ' '.join(self.rhs), self.sem))
	 */

	@Override
	public String toString() {

		if (sem instanceof Semantic.NullSemantic) {
			return "Rule " + String.join(" ", lhs, String.join(" ", rhs));
		}
		else {
			return "Rule " + String.join(
				" ", lhs, String.join(" ", rhs), sem.toString());
		}

	}

	private void _validateRule() {

	}

	private String lhs;
	private String[] rhs;
	private Semantic sem;

	public static boolean isCat(String label) {
		return label.startsWith("$");
	}

	public static boolean isOptional(String label) {
		return label.startsWith("?") && label.length() > 1;
	}

}