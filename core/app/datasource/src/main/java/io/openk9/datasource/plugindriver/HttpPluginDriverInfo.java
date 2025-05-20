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

package io.openk9.datasource.plugindriver;

import io.vertx.core.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@With
@Builder
public class HttpPluginDriverInfo {
	private boolean secure;
	private String baseUri;
	private String path;
	private Method method;
	private Map<String, Object> body;

	public enum Method {
		GET(HttpMethod.GET), POST(HttpMethod.POST),
		PUT(HttpMethod.PUT), DELETE(HttpMethod.DELETE),
		PATCH(HttpMethod.PATCH), HEAD(HttpMethod.HEAD),
		OPTIONS(HttpMethod.OPTIONS);

		Method(HttpMethod httpMethod) {
			this.httpMethod = httpMethod;
		}

		public HttpMethod getHttpMethod() {
			return httpMethod;
		}

		public static Optional<Method> fromString(String input) {
			if (input == null) {
				return Optional.empty();
			}

			return Arrays.stream(values())
				.filter(m -> m.name().equalsIgnoreCase(input))
				.findFirst();
		}

		private HttpMethod httpMethod;

	}

	public enum Schema {
		HTTP, HTTPS
	}

}
