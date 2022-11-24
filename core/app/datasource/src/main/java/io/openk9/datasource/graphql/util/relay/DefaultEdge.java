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

package io.openk9.datasource.graphql.util.relay;

import java.util.Objects;
import java.util.function.Function;

import static graphql.Assert.assertNotNull;

public class DefaultEdge<T> implements Edge<T> {

    private final T node;
    private final String cursor;

    public DefaultEdge(T node, String cursor) {
        this.cursor = assertNotNull(cursor, () -> "cursor cannot be null");
        this.node = node;
    }


    @Override
    public T getNode() {
        return node;
    }

    @Override
    public String getCursor() {
        return cursor;
    }

    @Override
    public <R> Edge<R> map(Function<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return new DefaultEdge<>(mapper.apply(node), cursor);
    }

    @Override
    public String toString() {
        return "DefaultEdge{" +
                "node=" + node +
                ", cursor=" + cursor +
                '}';
    }

}