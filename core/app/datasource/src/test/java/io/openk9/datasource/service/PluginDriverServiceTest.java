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

import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class PluginDriverServiceTest {

	private static final String TENANT_ID = "public";
	private static final Logger log = Logger.getLogger(PluginDriverServiceTest.class);

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
		var pluginDriver = getInitPluginDriver();

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

	@Test
	void should_create_docTypes() {
		var pluginDriver = getInitPluginDriver();

		// retrieve the docTypes
		var pluginDriverDocTypesDTO = pluginDriverService.getDocTypes(pluginDriver.getId())
			.await().indefinitely();

		// removes the doctypes contained in the pluginDriver sample
		pluginDriverDocTypesDTO.docTypes().forEach(pluginDriverDocType -> {
			if(pluginDriverDocType.selected()){
				docTypeService.deleteById(pluginDriverDocType.docTypeId())
					.await()
					.indefinitely();
			}
		});

		// creates the doctypes contained in the pluginDriver sample
		var docTypes = pluginDriverService.createPluginDriverDocTypes(pluginDriver.getId())
			.await()
			.indefinitely();

		assertNotNull(docTypes);
		assertFalse(docTypes.isEmpty());
		assertEquals(2, docTypes.size());

		var docTypeNames = docTypes.stream().map(DocType::getName).toList();

		assertTrue(docTypeNames.contains("default"));
		assertTrue(docTypeNames.contains("sample"));

		docTypes.forEach(docType ->
			log.debug(String.format("document type %d: %s", docType.getId(), docType)));
	}

	@Test
	void should_create_a_USER_pluginDriver_when_provisioning_is_missing() {
		// create without provisioning
		var pluginDriver = pluginDriverService.create(
				DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
					.name("missing-provisioning-create")
					.provisioning(null)
					.build()
			)
			.await().indefinitely();

		// the default provisioning is applied
		assertEquals(PluginDriver.Provisioning.USER, pluginDriver.getProvisioning());

		// cleanup
		pluginDriverService.deleteById(pluginDriver.getId())
			.await().indefinitely();
	}

	@Test
	void should_set_USER_provisioning_on_update_when_provisioning_is_missing() {
		// 1. Create a SYSTEM pluginDriver
		var pluginDriver = pluginDriverService.create(
				DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
					.name("missing-provisioning-update")
					.provisioning(PluginDriver.Provisioning.SYSTEM)
					.build()
			)
			.await().indefinitely();

		assertEquals(PluginDriver.Provisioning.SYSTEM, pluginDriver.getProvisioning());

		// 2. Update it without provisioning
		var updated = pluginDriverService.update(
				pluginDriver.getId(),
				DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
					.name("missing-provisioning-update")
					.provisioning(null)
					.build()
			)
			.await().indefinitely();

		assertEquals(PluginDriver.Provisioning.USER, updated.getProvisioning());

		// cleanup
		pluginDriverService.deleteById(pluginDriver.getId())
			.await().indefinitely();
	}

	@AfterEach
	void tearDown() {

		docTypeService.findByName("unselected")
			.flatMap(docType -> docTypeService.deleteById(docType.getId()))
			.await().indefinitely();

	}

	private PluginDriver getInitPluginDriver() {
		return pluginDriverService.findByName(
				TENANT_ID,
				Initializer.INIT_DATASOURCE_PLUGIN
			)
			.await().indefinitely();
	}
}
