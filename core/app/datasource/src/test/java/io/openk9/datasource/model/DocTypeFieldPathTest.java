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

import io.openk9.datasource.model.util.IncompleteDocTypeFieldException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocTypeFieldPathTest {

	@Test
	void should_fail_when_an_ancestor_has_no_field_name() {
		// setup — the acl -> roles -> keyword chain, with the root field left
		// without a name. This is how an ancestor looks while the rows of a
		// result set are still being read: the object exists, but it is empty.
		var defaultDocType = docType(DocType.DEFAULT_NAME);
		var acl = new DocTypeField();
		acl.setFieldType(FieldType.OBJECT);
		acl.setDocType(defaultDocType);

		var roles = subField("roles", FieldType.TEXT, acl, defaultDocType);
		var keyword = subField("keyword", FieldType.KEYWORD, roles, defaultDocType);

		// action + assertion — going on would concatenate the missing name into
		// "null.roles.keyword", a field the index does not have
		assertThrows(IncompleteDocTypeFieldException.class, keyword::getPath);
	}

	@Test
	void should_recompute_the_path_after_a_failed_attempt() {
		// setup — the same chain, with the root field still empty. A failure
		// must leave the cached path unset, so that a later read tries again
		// instead of returning a value built on incomplete data.
		var defaultDocType = docType(DocType.DEFAULT_NAME);
		var acl = new DocTypeField();
		acl.setFieldType(FieldType.OBJECT);
		acl.setDocType(defaultDocType);

		var roles = subField("roles", FieldType.TEXT, acl, defaultDocType);
		var keyword = subField("keyword", FieldType.KEYWORD, roles, defaultDocType);

		// 1. the path cannot be built yet
		assertThrows(IncompleteDocTypeFieldException.class, keyword::getPath);

		// 2. the root field gets its name, as when its own row is read
		acl.setFieldName("acl");

		// 3. the failed attempt must leave nothing behind: the path is built
		// from the complete chain, and stays the same when read again
		assertEquals("acl.roles.keyword", keyword.getPath());
		assertEquals("acl.roles.keyword", keyword.getPath());
	}

	@Test
	void should_prefix_the_doc_type_name_on_a_deep_chain() {
		// setup — allegati -> hash -> keyword in the "asset" docType. Only
		// keyword, the field whose path is asked for, carries its docType:
		// allegati and hash are loaded without one, as when the query does not
		// fetch the whole chain.
		var assetDocType = docType("asset");
		var allegati = rootField("allegati", FieldType.OBJECT, null);
		var hash = subField("hash", FieldType.TEXT, allegati, null);
		var keyword = subField("keyword", FieldType.KEYWORD, hash, assetDocType);

		// action + assertion — the docType name is read once, from keyword, and
		// must travel up to allegati to become the first segment of the path
		assertEquals("asset.allegati.hash.keyword", keyword.getPath());
	}

	@Test
	void should_build_the_path_of_a_sub_field() {
		// setup — one subField under a root field, in both kinds of doc type
		var defaultDocType = docType(DocType.DEFAULT_NAME);
		var contentId = rootField("contentId", FieldType.TEXT, defaultDocType);
		var contentIdKeyword =
			subField("keyword", FieldType.KEYWORD, contentId, defaultDocType);

		var stradaDocType = docType("strada");
		var title = rootField("title", FieldType.TEXT, stradaDocType);
		var titleKeyword =
			subField("keyword", FieldType.KEYWORD, title, stradaDocType);

		// action + assertion
		assertEquals("contentId.keyword", contentIdKeyword.getPath());
		assertEquals("strada.title.keyword", titleKeyword.getPath());
	}

	@Test
	void should_build_the_path_of_a_root_field() {
		// setup — fields with no parent to walk up to, in both kinds of doc type
		var defaultDocType = docType(DocType.DEFAULT_NAME);
		var rawContent = rootField("rawContent", FieldType.TEXT, defaultDocType);
		var documentTypes =
			rootField("documentTypes", FieldType.KEYWORD, defaultDocType);

		var title = rootField("title", FieldType.TEXT, docType("web"));

		// action + assertion — an application doc type gives the first segment
		// of the path, the "default" one gives none
		assertEquals("rawContent", rawContent.getPath());
		assertEquals("documentTypes", documentTypes.getPath());
		assertEquals("web.title", title.getPath());
	}

	private static DocType docType(String name) {
		var docType = new DocType();
		docType.setName(name);
		return docType;
	}

	private static DocTypeField rootField(
		String fieldName, FieldType fieldType, DocType docType) {

		return subField(fieldName, fieldType, null, docType);
	}

	private static DocTypeField subField(
		String fieldName, FieldType fieldType, DocTypeField parent,
		DocType docType) {

		var docTypeField = new DocTypeField();
		docTypeField.setFieldName(fieldName);
		docTypeField.setFieldType(fieldType);
		docTypeField.setParentDocTypeField(parent);
		docTypeField.setDocType(docType);

		return docTypeField;
	}

}
