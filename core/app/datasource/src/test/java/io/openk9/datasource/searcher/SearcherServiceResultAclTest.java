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

package io.openk9.datasource.searcher;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.impl.AclQueryParser;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.grpc.QueryParserRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearcherServiceResultAclTest {

	private static final String ROLE_AUDITOR = "auditor";
	private static final String ROLE_SALES = "sales";

	private static final ResultAclConfig.SearchSettings ENABLED =
		new ResultAclConfig.SearchSettings(true, Set.of(ROLE_AUDITOR), Set.of());

	private static final Map<QueryParserType, List<ParserSearchToken>> TEXT_ONLY =
		Map.of(QueryParserType.TEXT, List.of(new ParserSearchToken()));

	private static final Map<QueryParserType, List<ParserSearchToken>> TEXT_AND_KNN =
		Map.of(
			QueryParserType.TEXT, List.of(new ParserSearchToken()),
			QueryParserType.KNN, List.of(new ParserSearchToken())
		);

	@Test
	void should_obscure_only_when_the_request_asks_without_knn() {
		var asking = QueryParserRequest.newBuilder()
			.setIncludeObscured(true)
			.build();
		var notAsking = QueryParserRequest.newBuilder().build();

		// the flag decides for a text search
		assertTrue(SearcherService.mayObscure(asking, TEXT_ONLY));
		assertFalse(SearcherService.mayObscure(notAsking, TEXT_ONLY));

		// a request with a knn token stays filtered, even with the flag
		assertFalse(SearcherService.mayObscure(asking, TEXT_AND_KNN));

		// a request without tokens, as the one that lists every document
		assertTrue(SearcherService.mayObscure(asking, Map.of()));
	}

	@Test
	void should_filter_when_the_mode_is_disabled() {
		assertEquals(
			ParserContext.AclMode.FILTER,
			SearcherService.getAclMode(
				ResultAclConfig.SearchSettings.DISABLED, List.of(ROLE_AUDITOR)));
	}

	@Test
	void should_obscure_for_a_user_who_is_not_a_super_user() {
		assertEquals(
			ParserContext.AclMode.OBSCURE,
			SearcherService.getAclMode(ENABLED, List.of(ROLE_SALES)));

		// an anonymous user has no roles
		assertEquals(
			ParserContext.AclMode.OBSCURE,
			SearcherService.getAclMode(ENABLED, List.of()));
	}

	@Test
	void should_apply_no_acl_to_a_super_user() {
		assertEquals(
			ParserContext.AclMode.NONE,
			SearcherService.getAclMode(ENABLED, List.of(ROLE_SALES, ROLE_AUDITOR)));
	}

	@Test
	void should_send_the_visible_fields_of_the_bucket_by_path() {
		// setup — the web doc type with title, url and content, and a field
		// of a doc type the bucket does not search
		var web = docType("web");
		var title = field(1L, "title", web);
		var url = field(2L, "url", web);
		var content = field(3L, "content", web);
		web.setDocTypeFields(Set.of(title, url, content));

		var settings = new ResultAclConfig.SearchSettings(
			true, Set.of(ROLE_AUDITOR), Set.of(title.getId(), content.getId(), 99L));

		// action
		var resultAcl = SearcherService.toResultAcl(
			new TenantWithBucket("1", bucketSearching(web)), settings);

		// assertion — sorted paths of the visible fields the bucket knows
		assertEquals(
			AclQueryParser.AUTHORIZED_QUERY_NAME,
			resultAcl.getAuthorizedQueryName());
		assertEquals(
			List.of("web.content", "web.title"),
			resultAcl.getVisibleFieldsList());
	}

	private static Bucket bucketSearching(DocType docType) {
		var dataIndex = new DataIndex();
		dataIndex.setName("result-acl-test");
		dataIndex.setDocTypes(Set.of(docType));

		var datasource = new Datasource();
		datasource.setDataIndex(dataIndex);
		datasource.setPluginDriver(new PluginDriver());

		var bucket = new Bucket();
		bucket.setDatasources(Set.of(datasource));

		return bucket;
	}

	private static DocType docType(String name) {
		var docType = new DocType();
		docType.setName(name);

		return docType;
	}

	private static DocTypeField field(Long id, String fieldName, DocType docType) {
		var field = new DocTypeField();
		field.setId(id);
		field.setFieldName(fieldName);
		field.setFieldType(FieldType.TEXT);
		field.setDocType(docType);

		return field;
	}

}
