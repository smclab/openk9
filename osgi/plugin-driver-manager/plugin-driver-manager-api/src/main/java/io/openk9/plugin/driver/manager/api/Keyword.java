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

public abstract class Keyword {

	public abstract String getKeyword();
	public abstract String getReferenceKeyword();

	private static String _addPrefix(String keyword, String prefix) {
		return prefix == null || prefix.isEmpty() ? keyword :
			prefix + Strings.PERIOD + keyword;
	}
		
	public static Keyword sample(String keyword, String prefix) {
		return new SampleKeyword(_addPrefix(keyword, prefix));
	}

	public static Keyword link(String keyword, String link, String prefix) {
		return new LinkKeyword(_addPrefix(keyword, prefix), _addPrefix(link, prefix));
	}

	public static Keyword sample(String keyword) {
		return new SampleKeyword(keyword);
	}

	public static Keyword link(String keyword, String link) {
		return new LinkKeyword(keyword, link);
	}

	private static class SampleKeyword extends Keyword{

		private SampleKeyword(String keyword) {
			_keyword = keyword;
		}

		@Override
		public String getKeyword() {
			return _keyword;
		}

		@Override
		public String getReferenceKeyword() {
			return _keyword;
		}

		private final String _keyword;

	}

	private static class LinkKeyword extends Keyword {

		private LinkKeyword(String keyword, String referenceKeyword) {
			_keyword = keyword;
			_referenceKeyword = referenceKeyword;
		}

		@Override
		public String getKeyword() {
			return _keyword;
		}

		@Override
		public String getReferenceKeyword() {
			return _referenceKeyword;
		}

		private final String _keyword;
		private final String _referenceKeyword;

	}

}
