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

package io.openk9.json.internal;

import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonNode;

public class ArrayNodeWrapper extends JsonNodeWrapper implements ArrayNode {

	public ArrayNodeWrapper(
		com.fasterxml.jackson.databind.node.ArrayNode delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public ArrayNode add(JsonNode value) {
		return new ArrayNodeWrapper(
			delegate.add(((JsonNodeWrapper)value).getDelegate()));
	}

	@Override
	public ArrayNode add(String value) {
		return new ArrayNodeWrapper(delegate.add(value));
	}

	@Override
	public ArrayNode addPOJO(Object value) {
		return new ArrayNodeWrapper(delegate.addPOJO(value));
	}

	@Override
	public ArrayNode addAll(ArrayNode value) {
		return new ArrayNodeWrapper(
			delegate.addAll(((ArrayNodeWrapper)value).delegate));
	}

	private final com.fasterxml.jackson.databind.node.ArrayNode delegate;

	@Override
	public ArrayNode toArrayNode() {
		return this;
	}
}
