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

package io.openk9.sql.api.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Page {

	public static Page of(int page, int size) {
		return new Page(page, size, Collections.emptyList());
	}

	public static Page of(int page, int size, Sort...sorts) {
		return new Page(page, size, new HashSet<>(Arrays.asList(sorts)));
	}

	private final int page;
	private final int size;
	private final Collection<Sort> order;

	public static final Page DEFAULT = Page.of(0, 0);
	public static final Page ALL_POS = Page.of(-1, -1);

}
