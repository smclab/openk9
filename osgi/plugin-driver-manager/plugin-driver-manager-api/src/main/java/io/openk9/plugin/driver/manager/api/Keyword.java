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
