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

package io.openk9.datasource.service;

import jakarta.inject.Inject;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.dto.base.DocTypeDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class PluginDriverServiceTest {

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	DocTypeService docTypeService;

	@BeforeEach
	void setup() {

		docTypeService.create(
			DocTypeDTO.builder()
				.name("unselected")
				.build()
		).await().indefinitely();
	}

	@Test
	void should_return_a_PluginDriverDocTypesDTO() {
		var pluginDriver = pluginDriverService.findByName(
				"public",
				Initializer.INIT_DATASOURCE_PLUGIN
			)
			.await().indefinitely();

		var pluginDriverDocTypesDTO = pluginDriverService.getDocTypes(pluginDriver.getId())
			.await().indefinitely();

		Assertions.assertTrue(
			pluginDriverDocTypesDTO.docTypes()
				.stream()
				.anyMatch(pluginDriverDocType ->
					pluginDriverDocType.name().equals("default") && pluginDriverDocType.selected())
		);

		Assertions.assertTrue(
			pluginDriverDocTypesDTO.docTypes()
				.stream()
				.anyMatch(pluginDriverDocType ->
					pluginDriverDocType.name().equals("sample") && pluginDriverDocType.selected())
		);

		Assertions.assertTrue(
			pluginDriverDocTypesDTO.docTypes()
				.stream()
				.anyMatch(pluginDriverDocType ->
					pluginDriverDocType.name().equals("unselected")
					&& !pluginDriverDocType.selected()
				)
		);
	}

	@AfterEach
	void tearDown() {

		docTypeService.findByName("unselected")
			.flatMap(docType -> docTypeService.deleteById(docType.getId()))
			.await().indefinitely();

	}
}
