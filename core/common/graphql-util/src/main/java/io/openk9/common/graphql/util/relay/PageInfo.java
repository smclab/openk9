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

@Description("Information about pagination in a connection.")
public interface PageInfo {

    /**
     * @return cursor to the first edge, or null if this page is empty.
     */
    @Description("When paginating backwards, the cursor to continue.")
    String getStartCursor();

    /**
     * @return cursor to the last edge, or null if this page is empty.
     */
    @Description("When paginating forwards, the cursor to continue.")
    String getEndCursor();

    /**
     * @return true if and only if this page is not the first page. only meaningful when you gave the {@code last} argument.
     */
    @Description("When paginating backwards, are there more items?")
    boolean isHasPreviousPage();

    /**
     * @return true if and only if this page is not the last page. only meaningful when you gave the {@code first} argument.
     */
    @Description("When paginating forwards, are there more items?")
    boolean isHasNextPage();
}