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

package io.openk9.plugin.driver.manager.api;

import io.openk9.common.api.constant.Strings;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode
public abstract class SearchKeyword {

	protected SearchKeyword(String keyword, String prefix, boolean text) {
		this.text = text;
		if (prefix == null || prefix.isEmpty()) {
			this.keyword = keyword;
		}
		else {
			this.keyword = prefix + Strings.PERIOD + keyword;
		}
	}

	protected SearchKeyword(String keyword, boolean text) {
		this(keyword, Strings.BLANK, text);
	}

	public String getKeyword() {
		return keyword;
	}

	public boolean isText() {
		return text;
	}

	public Map.Entry<String, Float> getFieldBoost() {
		return Map.entry(getKeyword(), 1.0f);
	}

	public static SearchKeyword boostText(String keyword, float boost) {
		return new BoostSearchKeyword(keyword, boost, true);
	}

	public static SearchKeyword boostNumber(String keyword, float boost) {
		return new BoostSearchKeyword(keyword, boost, false);
	}

	public static SearchKeyword boostNumber(
		String keyword, String prefix, float boost) {

		return new BoostSearchKeyword(keyword, prefix, boost, false);
	}

	public static SearchKeyword boostText(
		String keyword, String prefix, float boost) {

		return new BoostSearchKeyword(keyword, prefix, boost, true);
	}

	public static SearchKeyword number(String keyword) {
		return new BaseSearchKeyword(keyword, false);
	}

	public static SearchKeyword text(String keyword) {
		return new BaseSearchKeyword(keyword, true);
	}

	public static SearchKeyword text(String keyword, String prefix) {
		return new BaseSearchKeyword(keyword, prefix, true);
	}

	public static SearchKeyword number(String keyword, String prefix) {
		return new BaseSearchKeyword(keyword, prefix, false);
	}

	static class BoostSearchKeyword extends SearchKeyword {

		public BoostSearchKeyword(
			String keyword, String prefix, float boost, boolean text) {
			super(keyword, prefix, text);
			this.boost = boost;
		}

		public BoostSearchKeyword(String keyword, float boost, boolean text) {
			super(keyword, text);
			this.boost = boost;
		}

		@Override
		public String getKeyword() {
			return super.getKeyword();
		}

		public Map.Entry<String, Float> getFieldBoost() {
			return Map.entry(getKeyword(), boost);
		}

		private final float boost;

	}

	static class BaseSearchKeyword extends SearchKeyword {

		public BaseSearchKeyword(String keyword, String prefix, boolean text) {
			super(keyword, prefix, text);
		}

		public BaseSearchKeyword(String keyword, boolean text) {
			super(keyword, text);
		}

	}

	private final String keyword;
	private final boolean text;

}
