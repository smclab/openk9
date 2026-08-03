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

package io.openk9.datasource.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Reads the admin HTTP permission paths declared for the production profile in
 * the module's {@code application.properties}, so that tests assert against the
 * configuration that ships in the image instead of a copy of it.
 */
final class AdminPolicyPaths {

	static final String PATHS_PROPERTY =
		"%prod.quarkus.http.auth.permission.administrator.paths";

	private static final List<String> ENTRIES = loadEntries();

	private AdminPolicyPaths() {
	}

	/**
	 * The declared paths, in the order they appear in the property.
	 */
	static List<String> entries() {
		return ENTRIES;
	}

	/**
	 * The declared paths as a single property value, ready to be fed back to
	 * Quarkus as a configuration override.
	 */
	static String joined() {
		return String.join(",", ENTRIES);
	}

	/**
	 * Whether the given path is protected by the policy. Mirrors the Quarkus
	 * matching rules: an entry is either an exact path or a {@code /*} prefix
	 * that matches at any depth.
	 */
	static boolean covers(String path) {
		var normalized = normalize(path);

		for (String entry : ENTRIES) {
			var normalizedEntry = normalize(entry);

			if (normalizedEntry.endsWith("/*")) {
				var prefix = normalizedEntry.substring(
					0, normalizedEntry.length() - 1);

				if (normalized.startsWith(prefix)) {
					return true;
				}
			}
			else if (normalizedEntry.equals(normalized)) {
				return true;
			}
		}

		return false;
	}

	private static List<String> loadEntries() {
		var value = findPropertyValue();
		var entries = new ArrayList<String>();

		for (String entry : value.split(",")) {
			var trimmed = entry.trim();

			if (!trimmed.isEmpty()) {
				entries.add(trimmed);
			}
		}

		return Collections.unmodifiableList(entries);
	}

	/**
	 * Looks the property up in every {@code application.properties} on the
	 * classpath: both the main and the test resources are there, and only the
	 * main one declares the production profile keys.
	 */
	private static String findPropertyValue() {
		try {
			var resources = Thread.currentThread()
				.getContextClassLoader()
				.getResources("application.properties");

			while (resources.hasMoreElements()) {
				var properties = new Properties();

				try (InputStream stream = resources.nextElement().openStream()) {
					properties.load(stream);
				}

				var value = properties.getProperty(PATHS_PROPERTY);

				if (value != null) {
					return value;
				}
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		throw new IllegalStateException(
			"No application.properties on the classpath declares "
				+ PATHS_PROPERTY);
	}

	private static String normalize(String path) {
		var normalized = path.strip();

		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}

		while (normalized.length() > 1 && normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}

		return normalized;
	}

}
