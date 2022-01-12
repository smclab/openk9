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

	protected SearchKeyword(String keyword, String prefix, Type type) {
		this(Keyword.sample(keyword, prefix), type);
	}

	protected SearchKeyword(String keyword, Type type) {
		this(keyword, Strings.BLANK, type);
	}

	protected SearchKeyword(Keyword keywordObject, Type type) {
		this.type = type;
		this.keywordObject = keywordObject;
	}

	public String getKeyword() {
		return keywordObject.getKeyword();
	}

	public String getReferenceKeyword() {
		return keywordObject.getReferenceKeyword();
	}

	public Type getType() {
		return type;
	}

	public Map.Entry<String, Float> getFieldBoost() {
		return Map.entry(getKeyword(), 1.0f);
	}

	public static SearchKeyword boostText(String keyword, float boost) {
		return new BoostSearchKeyword(keyword, boost, Type.TEXT);
	}

	public static SearchKeyword boostNumber(String keyword, float boost) {
		return new BoostSearchKeyword(keyword, boost, Type.NUMBER);
	}

	public static SearchKeyword boostNumber(
		String keyword, String prefix, float boost) {

		return new BoostSearchKeyword(keyword, prefix, boost, Type.NUMBER);
	}

	public static SearchKeyword boostText(
		String keyword, String prefix, float boost) {

		return new BoostSearchKeyword(keyword, prefix, boost, Type.TEXT);
	}

	public static SearchKeyword boostDate(
		String keyword, String prefix, float boost) {

		return new BoostSearchKeyword(keyword, prefix, boost, Type.DATE);
	}

	public static SearchKeyword number(String keyword) {
		return new BaseSearchKeyword(keyword, Type.NUMBER);
	}

	public static SearchKeyword text(String keyword) {
		return new BaseSearchKeyword(keyword, Type.TEXT);
	}

	public static SearchKeyword text(String keyword, String prefix) {
		return new BaseSearchKeyword(keyword, prefix, Type.TEXT);
	}

	public static SearchKeyword number(String keyword, String prefix) {
		return new BaseSearchKeyword(keyword, prefix, Type.NUMBER);
	}

	public static SearchKeyword date(String keyword) {
		return new BaseSearchKeyword(keyword, Type.DATE);
	}

	public static SearchKeyword date(String keyword, String prefix) {
		return new BaseSearchKeyword(keyword, prefix, Type.DATE);
	}

	public static SearchKeyword autocomplete(String keyword) {
		return new BaseSearchKeyword(keyword, Type.AUTOCOMPLETE);
	}

	public static SearchKeyword autocomplete(String keyword, String prefix) {
		return new BaseSearchKeyword(keyword, prefix, Type.AUTOCOMPLETE);
	}

	public static SearchKeyword autocomplete(Keyword keyword) {
		return new BaseSearchKeyword(keyword, Type.AUTOCOMPLETE);
	}

	static class BoostSearchKeyword extends SearchKeyword {

		public BoostSearchKeyword(
			String keyword, String prefix, float boost, Type type) {
			super(keyword, prefix, type);
			this.boost = boost;
		}

		public BoostSearchKeyword(String keyword, float boost, Type type) {
			super(keyword, type);
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

		public BaseSearchKeyword(String keyword, String prefix, Type type) {
			super(keyword, prefix, type);
		}

		public BaseSearchKeyword(Keyword keyword, Type type) {
			super(keyword, type);
		}

		public BaseSearchKeyword(String keyword, Type type) {
			super(keyword, type);
		}

	}

	private final Type type;
	private final Keyword keywordObject;

	public enum Type {
		DATE,TEXT,NUMBER,AUTOCOMPLETE
	}

}
