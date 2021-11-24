package io.openk9.search.query.internal.query.parser.annotator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class Token {
	private final String token;
	private final boolean stopword;
}