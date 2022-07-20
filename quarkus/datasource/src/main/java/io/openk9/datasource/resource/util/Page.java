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

package io.openk9.datasource.resource.util;

import io.openk9.datasource.model.util.K9Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.List;

@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
@Data
@Builder
public class Page<ENTITY> implements Iterable<ENTITY> {
	private int limit;
	private long count;
	private long afterId = 0;
	private long beforeId = 0;
	private List<ENTITY> content;

	public static <T> Page<T> emptyPage() {
		return (Page<T>)EMPTY;
	}

	public static final Page<?> EMPTY = Page.of(0, 0, 0, 0, List.of());

	public static <T extends K9Entity> Page<T> of(
		int limit, Long item1, List<T> item2) {
		Page<T> page = new Page<>();
		page.setLimit(limit);
		page.setCount(item1);
		page.setContent(item2);
		return page;
	}

	@Override
	public Iterator<ENTITY> iterator() {
		return content.iterator();
	}

}
