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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.ResultAclConfigDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.exception.InvalidDocTypeFieldSetException;
import io.openk9.datasource.service.exception.K9Error;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class ResultAclConfigServiceTest {

	private static final String ENTITY_NAME_PREFIX = "ResultAclConfigServiceTest - ";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc type";
	private static final String TITLE_NAME = ENTITY_NAME_PREFIX + "title";
	private static final String DATE_NAME = ENTITY_NAME_PREFIX + "date";
	private static final String BODY_NAME = ENTITY_NAME_PREFIX + "body";
	private static final String ROLE_AUDITOR = "auditor";
	private static final String ROLE_LEGAL = "legal";

	@Inject
	ResultAclConfigService resultAclConfigService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	private DocType docType;
	private DocTypeField title;
	private DocTypeField date;
	private DocTypeField body;

	@BeforeEach
	void setup() {
		docType = docTypeService.create(DocTypeDTO.builder()
				.name(DOC_TYPE_NAME)
				.build())
			.await().indefinitely();

		title = addField(TITLE_NAME, "resultAclTitle", FieldType.TEXT, false);
		date = addField(DATE_NAME, "resultAclDate", FieldType.DATE, null);
		body = addField(BODY_NAME, "resultAclBody", FieldType.TEXT, true);
	}

	@AfterEach
	void tearDown() {
		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		if (current != null) {
			EntitiesUtils.removeEntity(
				current, resultAclConfigService, sessionFactory);
		}

		docTypeService.deleteById(docType.getId()).await().indefinitely();
	}

	@Test
	void should_return_null_when_the_tenant_has_no_configuration() {
		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		assertNull(current);
	}

	@Test
	void should_create_the_configuration() {
		// create the configuration with a role and two visible fields
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(title.getId(), date.getId()))
				.build())
			.await().indefinitely();

		// read it back
		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		assertNotNull(current);
		assertTrue(current.isEnabled());
		assertEquals(
			List.of(ROLE_AUDITOR),
			ResultAclConfig.toRoleList(current.getSuperUserRoles()));
		assertEquals(
			Set.of(title.getId(), date.getId()),
			visibleFieldIds(current));
	}

	@Test
	void should_replace_every_value_on_update() {
		// create the configuration enabled, with a role and a visible field
		var created = resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		// update it without enabled: the mode falls back to disabled
		resultAclConfigService.update(created.getId(), ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.superUserRoles(List.of(ROLE_AUDITOR, ROLE_LEGAL))
				.visibleFieldIds(Set.of(date.getId()))
				.build())
			.await().indefinitely();

		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		assertEquals(created.getId(), current.getId());
		assertFalse(current.isEnabled());
		assertEquals(
			List.of(ROLE_AUDITOR, ROLE_LEGAL),
			ResultAclConfig.toRoleList(current.getSuperUserRoles()));
		assertEquals(Set.of(date.getId()), visibleFieldIds(current));
	}

	@Test
	void should_change_only_the_sent_values_on_patch() {
		// create the configuration enabled, with a role and two visible fields
		var created = resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(title.getId(), date.getId()))
				.build())
			.await().indefinitely();

		// patch only the mode
		resultAclConfigService.patch(created.getId(), ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(false)
				.build())
			.await().indefinitely();

		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		assertFalse(current.isEnabled());
		assertEquals(
			List.of(ROLE_AUDITOR),
			ResultAclConfig.toRoleList(current.getSuperUserRoles()));
		assertEquals(
			Set.of(title.getId(), date.getId()),
			visibleFieldIds(current));
	}

	@Test
	void should_reject_an_unknown_visible_field() {
		var dto = ResultAclConfigDTO.builder()
			.name(ResultAclConfig.DEFAULT_NAME)
			.visibleFieldIds(Set.of(title.getId(), Long.MAX_VALUE))
			.build();

		assertThrows(
			InvalidDocTypeFieldSetException.class,
			() -> resultAclConfigService.create(dto).await().indefinitely());
	}

	@Test
	void should_reject_an_excluded_visible_field() {
		var dto = ResultAclConfigDTO.builder()
			.name(ResultAclConfig.DEFAULT_NAME)
			.visibleFieldIds(Set.of(title.getId(), body.getId()))
			.build();

		assertThrows(
			InvalidDocTypeFieldSetException.class,
			() -> resultAclConfigService.create(dto).await().indefinitely());
	}

	@Test
	void should_reject_a_blank_or_multiline_role() {
		// a blank role
		var blankRole = resultAclConfigService.getValidator()
			.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.superUserRoles(List.of(" "))
				.build())
			.await().indefinitely();

		assertNull(blankRole.getEntity());
		assertFalse(blankRole.getFieldValidators().isEmpty());

		// a role spanning two lines
		var multilineRole = resultAclConfigService.getValidator()
			.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.superUserRoles(List.of(ROLE_AUDITOR + "\n" + ROLE_LEGAL))
				.build())
			.await().indefinitely();

		assertNull(multilineRole.getEntity());
		assertFalse(multilineRole.getFieldValidators().isEmpty());
	}

	@Test
	void should_list_only_the_selectable_fields() {
		// 1. Without a configuration every field but the excluded one is selectable
		var selectable = selectableFieldIds();

		assertTrue(selectable.contains(title.getId()));
		assertTrue(selectable.contains(date.getId()));
		assertFalse(selectable.contains(body.getId()));

		// 2. A visible field is no longer selectable
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		selectable = selectableFieldIds();

		assertFalse(selectable.contains(title.getId()));
		assertTrue(selectable.contains(date.getId()));
		assertFalse(selectable.contains(body.getId()));
	}

	@Test
	void should_not_delete_a_doc_type_with_a_visible_field() {
		// make a field of the doc type visible
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		// the doc type cannot be deleted while the field is visible
		var docTypeId = docType.getId();

		var error = assertThrows(
			K9Error.class,
			() -> docTypeService.deleteById(docTypeId).await().indefinitely());

		assertEquals(
			"There are some DocTypeFields referenced by other entities",
			error.getMessage());
	}

	private DocTypeField addField(
		String name, String fieldName, FieldType fieldType, Boolean exclude) {

		return docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name(name)
					.fieldName(fieldName)
					.fieldType(fieldType)
					.exclude(exclude)
					.build())
			.await().indefinitely()
			.right;
	}

	private Set<Long> selectableFieldIds() {
		return docTypeFieldService.findUnboundDocTypeFieldByResultAclConfig()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());
	}

	private Set<Long> visibleFieldIds(ResultAclConfig config) {
		return resultAclConfigService.getVisibleFields(config.getId())
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());
	}

}
