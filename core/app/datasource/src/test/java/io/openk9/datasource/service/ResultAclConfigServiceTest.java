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
import io.openk9.datasource.actor.ActorSystemProvider;
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
import io.quarkus.test.junit.mockito.InjectSpy;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class ResultAclConfigServiceTest {

	private static final String ENTITY_NAME_PREFIX = "ResultAclConfigServiceTest - ";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc type";
	private static final String TITLE_NAME = ENTITY_NAME_PREFIX + "title";
	private static final String DATE_NAME = ENTITY_NAME_PREFIX + "date";
	private static final String BODY_NAME = ENTITY_NAME_PREFIX + "body";
	private static final String TITLE_KEYWORD_NAME =
		ENTITY_NAME_PREFIX + "title keyword";
	private static final String META_NAME = ENTITY_NAME_PREFIX + "meta";
	private static final String META_AUTHOR_NAME =
		ENTITY_NAME_PREFIX + "meta author";
	private static final String RAW_NAME = ENTITY_NAME_PREFIX + "raw";
	private static final String SUMMARY_NAME = ENTITY_NAME_PREFIX + "summary";
	private static final String SUMMARY_I18N_NAME =
		ENTITY_NAME_PREFIX + "summary i18n";
	private static final String SUMMARY_IT_NAME =
		ENTITY_NAME_PREFIX + "summary it";
	private static final String SUMMARY_BASE_NAME =
		ENTITY_NAME_PREFIX + "summary base";
	private static final String ROLE_AUDITOR = "auditor";
	private static final String TENANT_ID = "public";
	private static final String ROLE_LEGAL = "legal";

	@Inject
	ResultAclConfigService resultAclConfigService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@InjectSpy
	ActorSystemProvider actorSystemProvider;

	private DocType docType;
	private DocTypeField title;
	private DocTypeField date;
	private DocTypeField body;
	private DocTypeField titleKeyword;
	private DocTypeField meta;
	private DocTypeField metaAuthor;
	private DocTypeField raw;
	private DocTypeField summary;
	private DocTypeField summaryI18n;
	private DocTypeField summaryIt;
	private DocTypeField summaryBase;

	@BeforeEach
	void setup() {
		docType = docTypeService.create(DocTypeDTO.builder()
				.name(DOC_TYPE_NAME)
				.build())
			.await().indefinitely();

		title = addField(TITLE_NAME, "resultAclTitle", FieldType.TEXT, false);
		date = addField(DATE_NAME, "resultAclDate", FieldType.DATE, null);
		body = addField(BODY_NAME, "resultAclBody", FieldType.TEXT, true);

		// a multi-field of the title
		titleKeyword = addSubField(
			title, TITLE_KEYWORD_NAME, "keyword", FieldType.KEYWORD);

		// a container with a sub-field, and an object without sub-fields
		meta = addField(META_NAME, "resultAclMeta", FieldType.OBJECT, null);
		metaAuthor = addSubField(
			meta, META_AUTHOR_NAME, "author", FieldType.KEYWORD);
		raw = addField(RAW_NAME, "resultAclRaw", FieldType.OBJECT, null);

		// an i18n field with its variants
		summary = addField(
			SUMMARY_NAME, "resultAclSummary", FieldType.I18N, null);
		summaryI18n = addSubField(
			summary, SUMMARY_I18N_NAME, "i18n", FieldType.OBJECT);
		summaryIt = addSubField(
			summaryI18n, SUMMARY_IT_NAME, "it", FieldType.TEXT);
		summaryBase = addSubField(
			summary, SUMMARY_BASE_NAME, "base", FieldType.TEXT);
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
	void should_reject_a_field_holding_no_value_of_its_own() {
		// a container with sub-fields, an object without sub-fields, a variant
		// of an i18n field and a multi-field, each alone
		for (var field : List.of(meta, raw, summaryI18n, summaryIt, summaryBase,
			titleKeyword)) {

			var dto = ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.visibleFieldIds(Set.of(field.getId()))
				.build();

			assertThrows(
				InvalidDocTypeFieldSetException.class,
				() -> resultAclConfigService.create(dto).await().indefinitely(),
				field.getName());
		}

		// nothing has been saved
		assertNull(resultAclConfigService.findCurrent().await().indefinitely());
	}

	@Test
	void should_accept_the_most_specific_fields_holding_a_value() {
		// a sub-field of a container and an i18n field
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.visibleFieldIds(Set.of(metaAuthor.getId(), summary.getId()))
				.build())
			.await().indefinitely();

		var current = resultAclConfigService.findCurrent()
			.await().indefinitely();

		assertEquals(
			Set.of(metaAuthor.getId(), summary.getId()),
			visibleFieldIds(current));
	}

	@Test
	void should_read_disabled_search_settings_unless_the_mode_is_enabled() {
		// 1. Without a configuration
		assertEquals(
			ResultAclConfig.SearchSettings.DISABLED,
			resultAclConfigService.findSearchSettings(TENANT_ID)
				.await().indefinitely());

		// 2. With the mode disabled, whatever the roles and the fields
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(false)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		assertEquals(
			ResultAclConfig.SearchSettings.DISABLED,
			resultAclConfigService.findSearchSettings(TENANT_ID)
				.await().indefinitely());
	}

	@Test
	void should_read_the_search_settings_of_an_enabled_configuration() {
		// setup — two roles and two leaves
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR, ROLE_LEGAL))
				.visibleFieldIds(Set.of(title.getId(), date.getId()))
				.build())
			.await().indefinitely();

		// action
		var settings = resultAclConfigService.findSearchSettings(TENANT_ID)
			.await().indefinitely();

		// assertion
		assertTrue(settings.enabled());
		assertEquals(Set.of(ROLE_AUDITOR, ROLE_LEGAL), settings.superUserRoles());
		assertEquals(
			Set.of(title.getId(), date.getId()),
			settings.visibleFieldIds());
	}

	@Test
	void should_list_only_the_selectable_fields() {
		// 1. Without a configuration only the most specific fields holding a
		// value are selectable
		var selectable = selectableFieldIds();

		assertTrue(selectable.contains(title.getId()));
		assertTrue(selectable.contains(date.getId()));
		assertTrue(selectable.contains(metaAuthor.getId()));
		assertTrue(selectable.contains(summary.getId()));

		assertFalse(selectable.contains(body.getId()));
		assertFalse(selectable.contains(raw.getId()));
		assertFalse(selectable.contains(titleKeyword.getId()));
		assertFalse(selectable.contains(meta.getId()));
		assertFalse(selectable.contains(summaryI18n.getId()));
		assertFalse(selectable.contains(summaryIt.getId()));
		assertFalse(selectable.contains(summaryBase.getId()));

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

	@Test
	void should_ask_for_a_cache_invalidation_when_only_the_visible_fields_change() {
		// setup — the configuration enabled, with a role and a visible field
		var created = resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		clearInvocations(actorSystemProvider);

		// action — the same configuration with another visible field
		resultAclConfigService.update(created.getId(), ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(date.getId()))
				.build())
			.await().indefinitely();

		// assertion — the cache invalidation reaches the actor system, so a
		// search no longer reads the previous visible fields
		verify(actorSystemProvider, atLeastOnce()).getActorSystem();
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

	private DocTypeField addSubField(
		DocTypeField parent, String name, String fieldName,
		FieldType fieldType) {

		return EntitiesUtils.createSubField(
			parent.getId(),
			DocTypeFieldDTO.builder()
				.name(name)
				.fieldName(fieldName)
				.fieldType(fieldType)
				.build(),
			docTypeFieldService);
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
