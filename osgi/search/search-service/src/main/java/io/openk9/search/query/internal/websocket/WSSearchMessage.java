package io.openk9.search.query.internal.websocket;

import io.openk9.search.api.query.SearchToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class WSSearchMessage {
	private List<SearchToken> searchQuery;
	private int[] range;
	private String token;
}
