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

package io.openk9.repository.http.internal.http;

import io.openk9.http.web.HttpRequest;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.client.Sort;
import io.vavr.Function4;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtil {

	public static Page getPage(HttpRequest httpRequest) {

		Tuple2<Integer, Integer> start =
			httpRequest
				.firstParam(_START)
				.map(Integer::valueOf)
				.map(v -> Tuple.of(_START_VALUE, v))
				.orElseGet(HttpRequestUtil::_defaultTuple);

		Tuple2<Integer, Integer> end =
			httpRequest
				.firstParam(_END)
				.map(Integer::valueOf)
				.map(v -> Tuple.of(_END_VALUE, v))
				.orElseGet(HttpRequestUtil::_defaultTuple);

		Tuple2<Integer, String> type =
			httpRequest
				.firstParam(_ORDER_TYPE)
				.map(v -> Tuple.of(_ORDER_TYPE_VALUE, v))
				.orElseGet(HttpRequestUtil::_defaultTuple);

		Tuple2<Integer, String> column =
			httpRequest
				.firstParam(_ORDER_COLUMN)
				.map(v -> Tuple.of(_ORDER_COLUMN_VALUE, v))
				.orElseGet(HttpRequestUtil::_defaultTuple);

		return _ACTIONS_MAP.getOrDefault(
			start._1 + end._1 + column._1 + type._1, _DEFAULT_ACTION)
			.apply(start._2,end._2, type._2, column._2);

	}

	private static <T> Tuple2<Integer, T> _defaultTuple() {
		return (Tuple2<Integer, T>)_DEFAULT_TUPLE;
	}

	public static final String _START = "start";
	public static final int _START_VALUE = 0b0_0_0_1;
	public static final String _END = "end";
	public static final int _END_VALUE = 0b0_0_1_0;
	public static final String _ORDER_TYPE = "orderType";
	public static final int _ORDER_TYPE_VALUE = 0b0_1_0_0;
	public static final String _ORDER_COLUMN = "orderColumn";
	public static final int _ORDER_COLUMN_VALUE = 0b1_0_0_0;

	private static final Function4<Integer, Integer, String, String, Page>
		_DEFAULT_ACTION = (start, end, type, column) -> Page.DEFAULT;

	private static final Tuple2<Integer, Object> _DEFAULT_TUPLE =
		Tuple.of(0, null);

	private static final Map<Integer, Function4<Integer, Integer, String, String, Page>> _ACTIONS_MAP =
		new HashMap<>(6, 1);

	static {

		_ACTIONS_MAP.put((
			_START_VALUE + _END_VALUE
		), (start, end, type, column) -> Page.of(start, end));

		_ACTIONS_MAP.put((
			_START_VALUE +
			_END_VALUE +
			_ORDER_COLUMN_VALUE
		), (start, end, type, column) -> Page.of(start, end, Sort.of(column)));

		_ACTIONS_MAP.put((
			_START_VALUE +
			_END_VALUE +
			_ORDER_COLUMN_VALUE +
			_ORDER_TYPE_VALUE
			), (start, end, type, column) -> Page.of(
				start, end, type.equals("asc")
				? Sort.asc(column)
				: Sort.desc(column))
		);

	}


}
