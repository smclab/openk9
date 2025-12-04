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

package io.openk9.datasource.searcher.parser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.searcher.client.dto.ParserSearchToken;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.opensearch.index.query.BoolQueryBuilder;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class ParserContext {
	private List<ParserSearchToken> tokenTypeGroup;
	private BoolQueryBuilder mutableQuery;
	private TenantWithBucket tenantWithBucket;
	private JsonObject queryParserConfig;
	private JsonWebToken jwt;
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
