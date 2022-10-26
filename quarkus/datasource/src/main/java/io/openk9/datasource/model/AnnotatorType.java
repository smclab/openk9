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

package io.openk9.datasource.model;

public enum AnnotatorType {

	NULL("null"),

	AGGREGATOR("aggregator"),
	NER("ner"),
	AUTOCOMPLETE("autocomplete"),
	DOCTYPE("doctype"),
	AUTOCORRECT("autocorrect"),
	STOPWORD("stopword"),
	TOKEN("token");

	AnnotatorType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	private final String type;

	public static AnnotatorType fromString(String type) {
		for (AnnotatorType annotatorType : AnnotatorType.values()) {
			if (annotatorType.getType().equals(type)) {
				return annotatorType;
			}
		}
		return null;
	}

}
