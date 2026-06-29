export const DEFAULT_NUMBER_OF_SOURCES = 8;

/**
 * how many sources to display under each answer. Read from the
 * `window.OPENK9_NUMBER_OF_SOURCES` global set by whoever embeds/deploys talk-to.
 * `0` hides the sources block entirely. Invalid or missing values fall back to
 * {@link DEFAULT_NUMBER_OF_SOURCES}.
 */
export function getNumberOfSources(): number {
	const value =
		typeof window !== "undefined" ? window.OPENK9_NUMBER_OF_SOURCES : undefined;
	if (typeof value !== "number" || !Number.isFinite(value) || value < 0) {
		return DEFAULT_NUMBER_OF_SOURCES;
	}
	return Math.floor(value);
}
