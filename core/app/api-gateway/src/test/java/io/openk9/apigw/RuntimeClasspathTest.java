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

package io.openk9.apigw;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

@DisplayName("Runtime classpath")
class RuntimeClasspathTest {

	// Jib copies the whole runtime classpath into the image and starts the
	// application with java -cp, which DevTools takes for a development run:
	// it would enable its restarter and its development property defaults,
	// such as stack traces in the error responses.
	@Test
	@DisplayName("does not contain Spring Boot DevTools")
	void doesNotContainDevTools() {
		assertThat(
			ClassUtils.isPresent(
				"org.springframework.boot.devtools.restart.Restarter", null)
		).isFalse();
	}

}
