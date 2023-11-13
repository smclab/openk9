package io.openk9.datasource.searcher.parser;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.util.JWT;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class ParserContext {
	private List<ParserSearchToken> tokenTypeGroup;
	private BoolQueryBuilder mutableQuery;
	private Bucket currentTenant;
	private JsonObject queryParserConfig;
	private JWT jwt;
	private Map<String, List<String>> extraParams;
	private String language;

	/**
	 * Get from context the String value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extraParams, the first String from the list is taken; </li>
	 *  <li> if the key is not found in extraParams,
	 *  then is the String is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param key the key to take from context
	 * @return Optionally, the String value.
	 */
	public Optional<String> getString(String key) {
		return getString(this, key);
	}

	/**
	 * Get from context the Float value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extraParams, the first Float from the list is taken; </li>
	 *  <li> if the key is not found in extraParams,
	 *  then is the Float is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param key the key to take from context
	 * @return Optionally, the Float value.
	 */
	public Optional<Float> getFloat(String key) {
		return getString(this, key).map(Float::parseFloat);
	}

	/**
	 * Get from context the Integer value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extraParams, the first Integer from the list is taken; </li>
	 *  <li> if the key is not found in extraParams,
	 *  then is the Integer is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param key the key to take from context
	 * @return Optionally, the Integer value.
	 */
	public Optional<Integer> getInteger(String key) {
		return getString(this, key).map(Integer::parseInt);
	}

	/**
	 * Get from context the Boolean value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extraParams, the first Boolean from the list is taken; </li>
	 *  <li> if the key is not found in extraParams,
	 *  then is the Boolean is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param key the key to take from context
	 * @return Optionally, the Boolean value.
	 */
	public Optional<Boolean> getBoolean(String key) {
		return getString(key).map(Boolean::parseBoolean);
	}


	private static Optional<String> getString(ParserContext context, String key) {
		Map<String, List<String>> extra = context.getExtraParams();

		if (extra != null && !extra.isEmpty()) {
			List<String> values = extra.get(key);
			if (values != null && values.iterator().hasNext()) {
				return Optional.ofNullable(values.iterator().next());
			}
		}

		JsonObject jsonConfig = context.getQueryParserConfig();
		return Optional.ofNullable(jsonConfig.getString(key));
	}

}
