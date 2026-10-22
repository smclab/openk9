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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.annotation.security.RolesAllowed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebResourcesRolesAllowedTest {

	/**
	 * Resources that are anonymous by design, because the public search
	 * frontend calls them before the user logs in: {@code BucketResource}
	 * serves {@code /buckets/current}, {@code DateFilterResource} the date
	 * filter, {@code KeycloakSettingsResource} the OAuth2 settings.
	 *
	 * <p>Add a resource here only when its endpoints are meant to be public,
	 * and say why.
	 */
	private static final Set<Class<?>> PUBLIC_RESOURCES = Set.of(
		BucketResource.class,
		DateFilterResource.class,
		KeycloakSettingsResource.class
	);

	private static final String PACKAGE_NAME =
		WebResourcesRolesAllowedTest.class.getPackageName();

	@Test
	void should_protect_every_non_public_resource() throws Exception {
		// collect the resources without @RolesAllowed on the class
		var unprotected = new ArrayList<String>();

		for (Class<?> resource : resourceClasses()) {
			if (!PUBLIC_RESOURCES.contains(resource)
				&& !resource.isAnnotationPresent(RolesAllowed.class)) {

				unprotected.add(resource.getSimpleName());
			}
		}

		// every resource is either protected or public by design
		Assertions.assertEquals(
			List.of(), unprotected,
			"Add @RolesAllowed(\"k9-admin\") or list the resource in "
				+ "PUBLIC_RESOURCES with the reason");
	}

	/**
	 * The {@code @Path} classes of this package, read from the main output of
	 * the module so that the test classes are left out.
	 */
	private static List<Class<?>> resourceClasses() throws Exception {
		var directory = Path.of("target", "classes")
			.resolve(PACKAGE_NAME.replace('.', '/'));
		var classLoader = WebResourcesRolesAllowedTest.class.getClassLoader();
		var classes = new ArrayList<Class<?>>();

		try (Stream<Path> files = Files.list(directory)) {
			var names = files
				.map(file -> file.getFileName().toString())
				.filter(name -> name.endsWith(".class"))
				.filter(name -> !name.contains("$"))
				.sorted()
				.toList();

			for (String name : names) {
				var className = PACKAGE_NAME + "."
					+ name.substring(0, name.length() - ".class".length());
				var candidate = Class.forName(className, false, classLoader);

				if (candidate.isAnnotationPresent(jakarta.ws.rs.Path.class)) {
					classes.add(candidate);
				}
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(
				"Cannot read the compiled classes in "
					+ directory.toAbsolutePath()
					+ ": the test must run from the module directory", e);
		}

		// guard against a vacuous pass
		Assertions.assertFalse(
			classes.isEmpty(), "No JAX-RS resource found in " + PACKAGE_NAME);

		return classes;
	}

}
