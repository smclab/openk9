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

package io.openk9.common.graphql.util.relay;

import org.eclipse.microprofile.graphql.Description;

import java.util.function.Function;

@Description("An edge in a connection")
public interface Edge<T> {

    /**
     * @return the node of data that this edge represents
     */
    @Description("The item at the end of the edge")
    T getNode();

    /**
     * @return the cursor for this edge node
     */
    @Description("cursor marks a unique position or index into the connection")
    String getCursor();

    <R> Edge<R> map(Function<T, R> mapper);

    static <T> Edge<T> of(T node, String cursor) {
        return new DefaultEdge<>(node, cursor);
    }

}