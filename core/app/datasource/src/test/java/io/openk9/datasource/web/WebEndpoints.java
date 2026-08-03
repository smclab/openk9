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
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.HttpMethod;

/**
 * Discovers the endpoints exposed by the JAX-RS resources of this package by
 * reading the compiled classes. The tests that check the admin authorization
 * policy work off this list, so an endpoint added tomorrow is checked without
 * anyone having to remember to update a table.
 */
final class WebEndpoints {

	/**
	 * Resources that are anonymous by design and must stay out of the admin
	 * policy. {@code BucketResource} serves the public search frontend under
	 * {@code /buckets/current}, and the deprecated {@code DateFilterResource}
	 * serves its date facets: both resolve the bucket from the virtual host.
	 *
	 * <p>Add a resource here only when its endpoints are meant to be public,
	 * and say why.
	 */
	static final Set<String> PUBLIC_RESOURCES = Set.of(
		BucketResource.class.getName(),
		DateFilterResource.class.getName());

	private static final String PACKAGE_NAME = WebEndpoints.class.getPackageName();

	private WebEndpoints() {
	}

	/**
	 * Every endpoint of every JAX-RS resource of this package that is not in
	 * {@link #PUBLIC_RESOURCES}.
	 */
	static List<Endpoint> adminEndpoints() {
		var endpoints = new ArrayList<Endpoint>();

		for (Class<?> resource : resourceClasses()) {
			if (PUBLIC_RESOURCES.contains(resource.getName())) {
				continue;
			}

			endpoints.addAll(endpointsOf(resource));
		}

		endpoints.sort(Comparator.comparing(Endpoint::path)
			.thenComparing(Endpoint::httpMethod));

		return endpoints;
	}

	private static List<Class<?>> resourceClasses() {
		var classes = new ArrayList<Class<?>>();
		var classLoader = WebEndpoints.class.getClassLoader();

		try (Stream<Path> files = Files.list(packageDirectory())) {
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
			throw new UncheckedIOException(e);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}

		if (classes.isEmpty()) {
			throw new IllegalStateException(
				"No JAX-RS resource found in " + PACKAGE_NAME
					+ ": has the module been compiled?");
		}

		return classes;
	}

	/**
	 * The directory holding the compiled classes of this package: the main
	 * output of the module, so that the test classes are left out. It is
	 * resolved from the build layout rather than from the class loader, because
	 * under {@code @QuarkusTest} the classes come from the Quarkus runtime
	 * class loader, which serves them under its own URL protocol.
	 */
	private static Path packageDirectory() {
		var directory = Path.of("target", "classes")
			.resolve(PACKAGE_NAME.replace('.', '/'));

		if (!Files.isDirectory(directory)) {
			throw new IllegalStateException(
				"Cannot find the compiled classes of " + PACKAGE_NAME + " in "
					+ directory.toAbsolutePath()
					+ ": the test must run from the module directory");
		}

		return directory;
	}

	private static List<Endpoint> endpointsOf(Class<?> resource) {
		var classPath = resource.getAnnotation(jakarta.ws.rs.Path.class).value();
		var endpoints = new ArrayList<Endpoint>();

		for (Method method : resource.getDeclaredMethods()) {
			var httpMethod = httpMethodOf(method);

			if (httpMethod == null) {
				continue;
			}

			var methodPath = method.getAnnotation(jakarta.ws.rs.Path.class);
			var path = methodPath == null
				? classPath
				: classPath + "/" + methodPath.value();

			endpoints.add(new Endpoint(
				httpMethod, path.replaceAll("/{2,}", "/"),
				resource.getSimpleName() + "#" + method.getName(),
				resource.isAnnotationPresent(RolesAllowed.class)
					|| method.isAnnotationPresent(RolesAllowed.class)));
		}

		return endpoints;
	}

	/**
	 * The HTTP verb of a resource method, read from the annotation that is
	 * itself annotated with {@link HttpMethod} — the same rule JAX-RS uses, so
	 * custom verbs are picked up too.
	 */
	private static String httpMethodOf(Method method) {
		for (var annotation : method.getAnnotations()) {
			var httpMethod = annotation.annotationType()
				.getAnnotation(HttpMethod.class);

			if (httpMethod != null) {
				return httpMethod.value();
			}
		}

		return null;
	}

	/**
	 * A single endpoint: its verb, its declared path (path parameters are left
	 * as templates), the resource method that declares it, and whether it is
	 * already protected by {@code @RolesAllowed} instead of by the policy.
	 */
	record Endpoint(
		String httpMethod, String path, String declaredBy,
		boolean rolesAllowed) {

		@Override
		public String toString() {
			return httpMethod + " " + path + " (" + declaredBy + ")";
		}

	}

}
