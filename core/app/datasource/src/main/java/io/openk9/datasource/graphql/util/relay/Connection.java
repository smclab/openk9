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

import org.eclipse.microprofile.graphql.Description;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Description("A connection to a list of items.")
public interface Connection<T> {

    /**
     * @return a list of {@link graphql.relay.Edge}s that are really a node of data and its cursor
     */
    @Description("A list of edges.")
    List<Edge<T>> getEdges();

    /**
     * @return {@link graphql.relay.PageInfo} pagination data about that list of edges
     */
    @Description("details about this specific page")
    PageInfo getPageInfo();

    <R> Connection<R> map(Function<T, R> mapper);

    Connection<T> filter(Predicate<T> filter);

    static <R> Connection<R> of(List<Edge<R>> edges, PageInfo pageInfo) {
        return new DefaultConnection<>(edges, pageInfo);
    }

}
