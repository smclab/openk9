/**
 * whether an anonymous user must log in before chatting. Read from the
 * `window.OPENK9_REQUIRE_LOGIN` global set by whoever embeds/deploys talk-to.
 * Only `true` turns it on: anonymous chat stays the default.
 */
export function isLoginRequired(): boolean {
	return typeof window !== "undefined" && window.OPENK9_REQUIRE_LOGIN === true;
}
