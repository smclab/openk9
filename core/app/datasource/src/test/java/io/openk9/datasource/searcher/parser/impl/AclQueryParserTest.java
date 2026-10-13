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
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	void should_use_the_complete_path_in_the_acl_filter() {
		// set up the ACL chain acl -> roles -> keyword of the "default" docType,
		// the hierarchy generated from the index mapping
		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = true;

		var defaultDocType = new DocType();
		defaultDocType.setName(DocType.DEFAULT_NAME);

		var acl = new DocTypeField();
		acl.setFieldName("acl");
		acl.setFieldType(FieldType.OBJECT);
		acl.setDocType(defaultDocType);

		var roles = new DocTypeField();
		roles.setFieldName("roles");
		roles.setFieldType(FieldType.TEXT);
		roles.setParentDocTypeField(acl);
		roles.setDocType(defaultDocType);

		var keyword = new DocTypeField();
		keyword.setFieldName("keyword");
		keyword.setFieldType(FieldType.KEYWORD);
		keyword.setParentDocTypeField(roles);
		keyword.setDocType(defaultDocType);

		var aclMapping = AclMapping.of(
			null, null, keyword, UserField.ROLES);
		var bucket = new Bucket();
		bucket.setDatasources(
			Set.of(createDatasourceWithAcl(aclMapping)));

		var tenantWithBucket = new TenantWithBucket("1", bucket);

		var parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.extraParams(
				Map.of("openk9.acl.extra-roles", List.of("role-a")))
			.build();

		// action
		var boolQuery = aclQueryParser.getBoolQuery(parserContext);

		// assertion — pins which field the terms clause names. The chain here
		// is complete, so this documents the contract rather than guarding the
		// unloaded-ancestor case.
		var queryString = boolQuery.toString();
		assertTrue(queryString.contains("acl.roles.keyword"));
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

	@Test
	void should_filter_the_query_when_no_acl_mode_is_set() {
		// setup — the context every caller builds today
		var mutableQuery = QueryBuilders.boolQuery();

		// action
		applyAcl(mutableQuery, null);

		// assertion — the ACL clause itself is the filter
		assertEquals(1, mutableQuery.filter().size());
		var filter = (BoolQueryBuilder) mutableQuery.filter().getFirst();
		assertNull(filter.queryName());
		assertTrue(filter.toString().contains("acl.public"));
	}

	@Test
	void should_name_the_acl_inside_an_always_true_filter_when_obscuring() {
		// setup
		var mutableQuery = QueryBuilders.boolQuery();

		// action
		applyAcl(mutableQuery, ParserContext.AclMode.OBSCURE);

		// assertion — the filter is "ACL OR NOT ACL": it lets every document
		// through, and the named ACL tells the authorized ones apart
		assertEquals(1, mutableQuery.filter().size());
		var filter = (BoolQueryBuilder) mutableQuery.filter().getFirst();
		assertEquals("1", filter.minimumShouldMatch());
		assertEquals(2, filter.should().size());

		var namedAcl = (BoolQueryBuilder) filter.should().get(0);
		assertEquals(AclQueryParser.AUTHORIZED_QUERY_NAME, namedAcl.queryName());
		assertTrue(namedAcl.toString().contains("acl.public"));

		var notAcl = (BoolQueryBuilder) filter.should().get(1);
		assertEquals(1, notAcl.mustNot().size());
		var negatedAcl = assertInstanceOf(
			BoolQueryBuilder.class, notAcl.mustNot().getFirst());
		assertNull(negatedAcl.queryName());
		assertTrue(negatedAcl.toString().contains("acl.public"));
	}

	@Test
	void should_add_no_acl_clause_for_a_super_user() {
		// setup
		var mutableQuery = QueryBuilders.boolQuery();

		// action
		applyAcl(mutableQuery, ParserContext.AclMode.NONE);

		// assertion
		assertTrue(mutableQuery.filter().isEmpty());
		assertFalse(mutableQuery.toString().contains("acl.public"));
	}

	private static void applyAcl(
		BoolQueryBuilder mutableQuery, ParserContext.AclMode aclMode) {

		var aclQueryParser = new AclQueryParser();
		aclQueryParser.extraRolesEnabled = false;

		var parserContext = ParserContext.builder()
			.tenantWithBucket(new TenantWithBucket("1", new Bucket()))
			.mutableQuery(mutableQuery)
			.aclMode(aclMode)
			.build();

		aclQueryParser.apply(parserContext).await().indefinitely();
	}
}
