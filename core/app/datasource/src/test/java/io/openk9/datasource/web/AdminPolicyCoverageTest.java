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

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The resources of this package rely on the {@code administrator} HTTP
 * permission, whose paths are matched exactly. An endpoint that no policy entry
 * covers, and that no {@code @RolesAllowed} protects either, is an endpoint
 * served without credentials, so this test fails the build as soon as one
 * appears.
 */
class AdminPolicyCoverageTest {

	@Test
	void every_admin_endpoint_is_covered_by_the_policy() {
		// the endpoints of every resource that is not declared public
		var endpoints = WebEndpoints.adminEndpoints();

		Assertions.assertFalse(
			endpoints.isEmpty(),
			"No admin endpoint found: the discovery is broken, not the policy");

		// each of them must be protected, by annotation or by the policy
		var uncovered = new ArrayList<String>();

		for (var endpoint : endpoints) {
			if (endpoint.rolesAllowed()) {
				continue;
			}

			if (!AdminPolicyPaths.covers(endpoint.path())) {
				uncovered.add(endpoint.toString());
			}
		}

		Assertions.assertTrue(
			uncovered.isEmpty(),
			() -> """
				These endpoints are served without credentials, because no entry \
				of %s covers them:
				%s
				Declared entries: %s
				Add the path to the policy (an entry per resource plus its `/*` \
				wildcard) or annotate the resource with @RolesAllowed; if the \
				endpoint is public on purpose, add its resource to \
				WebEndpoints.PUBLIC_RESOURCES stating why."""
				.formatted(
					AdminPolicyPaths.PATHS_PROPERTY,
					String.join("\n", uncovered),
					AdminPolicyPaths.entries()));
	}

}
