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

package io.openk9.datasource.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.openk9.datasource.model.DocType;

public record PluginDriverDocTypesDTO(List<PluginDriverDocType> docTypes) {

	public static PluginDriverDocTypesDTO join(PluginDriverDocTypesDTO... values) {
		List<PluginDriverDocType> newList = new ArrayList<>();

		for (PluginDriverDocTypesDTO value : values) {
			newList.addAll(value.docTypes());
		}

		return new PluginDriverDocTypesDTO(newList);
	}

	public static PluginDriverDocTypesDTO selectedDocTypes(List<DocType> docTypes) {
		return new PluginDriverDocTypesDTO(
			docTypes.stream()
				.map(PluginDriverDocType::selectedDocType)
				.collect(Collectors.toList())
		);
	}

	public static PluginDriverDocTypesDTO unselectedDocTypes(List<DocType> docTypes) {
		return new PluginDriverDocTypesDTO(
			docTypes.stream()
				.map(PluginDriverDocType::unselectedDocType)
				.collect(Collectors.toList())
		);
	}

	public record PluginDriverDocType(long docTypeId, String name, boolean selected) {
		private static PluginDriverDocType selectedDocType(DocType docType) {
			return new PluginDriverDocType(docType.getId(), docType.getName(), true);
		}

		private static PluginDriverDocType unselectedDocType(DocType docType) {
			return new PluginDriverDocType(docType.getId(), docType.getName(), false);
		}
	}

}

