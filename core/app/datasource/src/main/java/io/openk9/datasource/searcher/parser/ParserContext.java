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
	 * Get from context the Float value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extra properties of the token, get the Float value for this key; </li>
	 *  <li> if the key is not found in extra,
	 *  then is the Float is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param token the token
	 * @param jsonConfig the config
	 * @param key the key to take from context
	 * @return Optionally, the Float value.
	 */
	public static Optional<Float> getFloat(
		ParserSearchToken token, JsonObject jsonConfig, String key) {

		return getString(token, jsonConfig, key).map(Float::parseFloat);
	}

	/**
	 * Get from context the Integer value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extra properties of the token, get the Integer value for this key; </li>
	 *  <li> if the key is not found in extra,
	 *  then is the Integer is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param token the token
	 * @param jsonConfig the config
	 * @param key the key to take from context
	 * @return Optionally, the Integer value.
	 */
	public static Optional<Integer> getInteger(
		ParserSearchToken token, JsonObject jsonConfig, String key) {

		return getString(token, jsonConfig, key).map(Integer::parseInt);
	}

	/**
	 * Get from context the Boolean value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extra properties of the token, get the Boolean value for this key; </li>
	 *  <li> if the key is not found in extra,
	 *  then is the Boolean is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param token the token
	 * @param jsonConfig the config
	 * @param key the key to take from context
	 * @return Optionally, the Boolean value.
	 */
	public static Optional<Boolean> getBoolean(
		ParserSearchToken token, JsonObject jsonConfig, String key) {

		return getString(token, jsonConfig, key).map(Boolean::parseBoolean);
	}

	/**
	 * Get from context the String value mapped to key:
	 *
	 * <ol>
	 *  <li> if exist in the extra properties of the token, get the String value for this key; </li>
	 *  <li> if the key is not found in extra,
	 *  then is the String is taken from the queryParserConfig. </li>
	 * <ol/>
	 *
	 * @param token the token
	 * @param jsonConfig the config
	 * @param key the key to take from context
	 * @return Optionally, the String value.
	 */
	public static Optional<String> getString(
		ParserSearchToken token, JsonObject jsonConfig, String key) {

		Map<String, String> extra = token.getExtra();

		if (extra != null && !extra.isEmpty()) {
			String value = extra.get(key);
			if (value != null && !value.isBlank()) {
				return Optional.of(value);
			}
		}

		return Optional.ofNullable(jsonConfig.getString(key));
	}

}
