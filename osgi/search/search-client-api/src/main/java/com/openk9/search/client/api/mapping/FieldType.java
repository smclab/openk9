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

package com.openk9.search.client.api.mapping;

public enum FieldType {

	NULL("null"),

	BINARY("binary"),

	BOOLEAN("boolean"),

	KEYWORD("keyword"),
	CONSTANT_KEYWORD("constant_keyword"),
	WILDCARD("wildcard"),

	// DATE

	DATE("date"),
	DATE_NANOS("date_nanos"),

	// NUMBERS

	LONG("long"),
	INTEGER("integer"),
	SHORT("short"),
	BYTE("byte"),
	DOUBLE("double"),
	FLOAT("float"),
	HALF_FLOAT("half_float"),
	SCALED_FLOAT("scaled_float"),
	UNSIGNED_LONG("unsigned_long"),

	OBJECT("object"),
	FLATTENED("flattened"),
	NESTED("nested"),
	JOIN("join"),

	LONG_RANGE("long_range"),
	DOUBLE_RANGE("double_range"),
	DATE_RANGE("date_range"),
	IP_RANGE("ip_range"),
	IP("ip"),
	VERSION("version"),
	MURMUR3("murmur3"),
	HISTOGRAM("histogram"),
	TEXT("text"),
	ANNOTATED_TEXT("annotated-text"),
	COMPLETION("completion"),
	SEARCH_AS_YOU_TYPE("search_as_you_type"),
	TOKEN_COUNT("token_count"),
	DENSE_VECTOR("dense_vector"),
	SPARSE_VECTOR("sparse_vector"),
	RANK_FEATURE("rank_feature"),
	RANK_FEATURES("rank_features"),
	GEO_POINT("geo_point"),
	GEO_SHAPE("geo_shape"),
	POINT("point"),
	SHAPE("shape"),
	PERCOLATOR("percolator");

	FieldType(String type) {
		_type = type;
	}

	public String getType() {
		return _type;
	}

	private final String _type;

}
