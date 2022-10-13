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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static graphql.Assert.assertNotNull;

public class DefaultConnection<T> implements Connection<T> {

    private final List<Edge<T>> edges;
    private final PageInfo pageInfo;

    /**
     * A connection consists of a list of edges and page info
     *
     * @param edges    a non null list of edges
     * @param pageInfo a non null page info
     *
     * @throws IllegalArgumentException if edges or page info is null. use {@link Collections#emptyList()} for empty edges.
     */
    public DefaultConnection(List<Edge<T>> edges, PageInfo pageInfo) {
        this.edges = List.copyOf(assertNotNull(edges, () -> "edges cannot be null"));
        this.pageInfo = assertNotNull(pageInfo, () -> "page info cannot be null");
    }

    @Override
    public List<Edge<T>> getEdges() {
        return edges;
    }

    @Override
    public PageInfo getPageInfo() {
        return pageInfo;
    }

    @Override
    public <R> Connection<R> map(Function<T, R> mapper) {

        Objects.requireNonNull(mapper, "mapper is null");

        return edges
            .stream()
            .map(edge -> edge.map(mapper))
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                newEdges -> Connection.of(newEdges, pageInfo))
            );

    }

    @Override
    public String toString() {
        return "DefaultConnection{" +
                "edges=" + edges +
                ", pageInfo=" + pageInfo +
                '}';
    }
}
