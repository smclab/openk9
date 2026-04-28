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

package io.openk9.datasource.searcher.parser.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AclQueryParserTest {

	@Test
	void should_return_public_acl_when_extra_roles_disabled() {
		// setup
		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = false;

		var bucket = new Bucket();
		var tenantWithBucket = new TenantWithBucket("1", bucket);
		var parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.build();

		// action
		var boolQuery = aclQueryParser.getBoolQuery(parserContext);

		// assertion
		var queryString = boolQuery.toString();
		assertNotNull(queryString);
		assertTrue(queryString.contains("acl.public"));
		assertEquals(1, boolQuery.should().size());
	}

	private static Datasource createDatasourceWithAcl(
		AclMapping aclMapping) {

		var dataIndex = new DataIndex();
		dataIndex.setName("test-index");

		var pluginDriver = new PluginDriver();
		pluginDriver.setAclMappings(Set.of(aclMapping));

		var datasource = new Datasource();
		datasource.setDataIndex(dataIndex);
		datasource.setPluginDriver(pluginDriver);

		return datasource;
	}

	@Test
	void should_include_extra_roles_when_extra_roles_enabled() {
		// setup
		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = true;

		var docTypeField = new DocTypeField();
		docTypeField.setFieldName("groups");
		docTypeField.setFieldType(FieldType.KEYWORD);

		var aclMapping = AclMapping.of(
			null, null, docTypeField, UserField.ROLES);
		var bucket = new Bucket();
		bucket.setDatasources(
			Set.of(createDatasourceWithAcl(aclMapping)));

		var tenantWithBucket = new TenantWithBucket("1", bucket);

		var extraRoles = List.of("role-a", "role-b");
		var parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.extraParams(
				Map.of("openk9.acl.extra-roles", extraRoles))
			.build();

		// action
		var boolQuery = aclQueryParser.getBoolQuery(parserContext);

		// assertion
		var queryString = boolQuery.toString();
		assertNotNull(queryString);
		assertTrue(queryString.contains("acl.public"));
		assertTrue(queryString.contains("role-a"));
		assertTrue(queryString.contains("role-b"));
	}

	@Test
	void should_not_include_extra_roles_when_disabled_with_extra_params_present() {
		// setup — extra params present but extraRolesEnabled is false
		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = false;

		var docTypeField = new DocTypeField();
		docTypeField.setFieldName("groups");
		docTypeField.setFieldType(FieldType.KEYWORD);

		var aclMapping = AclMapping.of(
			null, null, docTypeField, UserField.ROLES);
		var bucket = new Bucket();
		bucket.setDatasources(
			Set.of(createDatasourceWithAcl(aclMapping)));

		var tenantWithBucket = new TenantWithBucket("1", bucket);

		var extraRoles = List.of("role-a");
		var parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.extraParams(
				Map.of("openk9.acl.extra-roles", extraRoles))
			.build();

		// action
		var boolQuery = aclQueryParser.getBoolQuery(parserContext);

		// assertion
		var queryString = boolQuery.toString();
		assertNotNull(queryString);
		assertTrue(queryString.contains("acl.public"));
		assertFalse(queryString.contains("role-a"));
	}

	@Test
	void should_return_public_acl_when_no_extra_params() {
		// setup — extraRolesEnabled=true but extraParams is empty
		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = true;

		var docTypeField = new DocTypeField();
		docTypeField.setFieldName("groups");
		docTypeField.setFieldType(FieldType.KEYWORD);

		var aclMapping = AclMapping.of(
			null, null, docTypeField, UserField.ROLES);
		var bucket = new Bucket();
		bucket.setDatasources(
			Set.of(createDatasourceWithAcl(aclMapping)));

		var tenantWithBucket = new TenantWithBucket("1", bucket);

		var parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.extraParams(Map.of())
			.build();

		// action
		var boolQuery = aclQueryParser.getBoolQuery(parserContext);

		// assertion
		var queryString = boolQuery.toString();
		assertNotNull(queryString);
		assertTrue(queryString.contains("acl.public"));
	}

	@Test
	void should_return_minimum_should_match_one() {
		// setup
		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = false;

		var bucket = new Bucket();
		var tenantWithBucket = new TenantWithBucket("1", bucket);
		var parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.build();

		// action
		var boolQuery = aclQueryParser.getBoolQuery(parserContext);

		// assertion
		assertEquals(1, boolQuery.should().size());
	}
}
