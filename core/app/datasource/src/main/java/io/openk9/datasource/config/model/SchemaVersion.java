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

package io.openk9.datasource.config.model;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The package schema version as an immutable {@code MAJOR.MINOR} pair, together
 * with the compatibility policy the importer applies to an incoming package.
 * <p>
 * Policy: an incoming version is accepted only when its {@code MAJOR} equals the
 * runtime's and its {@code MINOR} is less than or equal to the runtime's. A newer
 * minor (a package produced by a more recent runtime) is rejected — forward
 * compatibility is not assumed; an older-or-equal minor of the same major is
 * accepted; any major difference is rejected. Parsing is strict
 * {@code MAJOR.MINOR}: a null, blank, single-component ({@code "1"}) or
 * three-component ({@code "1.0.0"}) value is not a version and is rejected.
 */
public record SchemaVersion(int major, int minor) {

	private static final Pattern MAJOR_MINOR =
		Pattern.compile("(\\d{1,9})\\.(\\d{1,9})");

	/** The version this runtime produces on export and imports against. */
	public static final SchemaVersion CURRENT =
		parse(ConfigPackage.CURRENT_SCHEMA_VERSION).orElseThrow();

	/**
	 * Parses a strict {@code MAJOR.MINOR} string, or empty when it is null or not
	 * a well-formed two-component numeric version.
	 */
	public static Optional<SchemaVersion> parse(String raw) {
		if (raw == null) {
			return Optional.empty();
		}
		Matcher matcher = MAJOR_MINOR.matcher(raw.trim());
		if (!matcher.matches()) {
			return Optional.empty();
		}
		return Optional.of(new SchemaVersion(
			Integer.parseInt(matcher.group(1)),
			Integer.parseInt(matcher.group(2))));
	}

	/**
	 * Whether an {@code incoming} version is compatible with this one taken as the
	 * runtime gate: same major, and a minor no newer than this.
	 */
	public boolean accepts(SchemaVersion incoming) {
		return incoming != null
			&& incoming.major == this.major
			&& incoming.minor <= this.minor;
	}

}
