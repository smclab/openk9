package io.openk9.search.query.internal.query.parser;

import io.openk9.search.api.query.parser.Annotator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class GrammarMixin {
	private List<Rule> rules;
	private List<Annotator> annotators;
}
