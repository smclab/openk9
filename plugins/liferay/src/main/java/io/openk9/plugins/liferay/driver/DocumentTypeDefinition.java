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

package io.openk9.plugins.liferay.driver;

import io.openk9.common.api.constant.Strings;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.search.client.api.mapping.Field;
import io.openk9.search.client.api.mapping.FieldType;
import io.openk9.ingestion.driver.manager.api.DocumentType;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactory;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryRegistry;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryRegistryAware;
import io.openk9.ingestion.driver.manager.api.PluginDriver;
import io.openk9.ingestion.driver.manager.api.SearchKeyword;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;

@Component(
	immediate = true,
	service = DocumentTypeFactoryRegistryAware.class
)
public class DocumentTypeDefinition implements
	DocumentTypeFactoryRegistryAware {

	@Override
	public AutoCloseables.AutoCloseableSafe apply(
		DocumentTypeFactoryRegistry documentTypeFactoryRegistry) {

		String pluginDriverName = _pluginDriver.getName();

		return documentTypeFactoryRegistry
			.register(
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, true,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("user")
						.searchKeywords(
							List.of(
								SearchKeyword.number("userId", "user"),
								SearchKeyword.boostText("screenName", "user", 5),
								SearchKeyword.boostText("emailAddress", "user", 5),
								SearchKeyword.number("employeeNumber", "user"),
								SearchKeyword.text("jobTitle", "user"),
								SearchKeyword.text("jobClass", "user"),
								SearchKeyword.number("male", "user"),
								SearchKeyword.text("twitterSn", "user"),
								SearchKeyword.text("skypeSn", "user"),
								SearchKeyword.text("facebookSn", "user"),
								SearchKeyword.boostText("firstName", "user", 5),
								SearchKeyword.boostText("middleName", "user", 5),
								SearchKeyword.boostText("lastName", "user", 5),
								SearchKeyword.number("birthday", "user")
							)
						)
						.sourceFields(
							List.of(
								Field.of("userId", FieldType.LONG),
								Field.of("screenName", FieldType.TEXT),
								Field.of(
									"emailAddress", FieldType.TEXT,
									Collections.singletonMap("analyzer", "email")),
								Field.of("employeeNumber", FieldType.TEXT),
								Field.of("jobTitle", FieldType.TEXT),
								Field.of("jobClass", FieldType.TEXT),
								Field.of("male", FieldType.BOOLEAN),
								Field.of("twitterSn", FieldType.TEXT),
								Field.of("skypeSn", FieldType.TEXT),
								Field.of("facebookSn", FieldType.TEXT),
								Field.of("firstName", FieldType.TEXT),
								Field.of("middleName", FieldType.TEXT),
								Field.of("lastName", FieldType.TEXT),
								Field.of("birthday", FieldType.LONG)
							)
						)
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, false,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("calendar")
						.searchKeywords(
							List.of(
								SearchKeyword.number("calendarBookingId", "calendar"),
								SearchKeyword.text("description", "calendar"),
								SearchKeyword.text("location", "calendar"),
								SearchKeyword.text("title", "calendar"),
								SearchKeyword.text("titleCurrentValue", "calendar"),
								SearchKeyword.number("startTime", "calendar"),
								SearchKeyword.number("endTime", "calendar"),
								SearchKeyword.number("allDay", "calendar")
							)
						)
						.sourceFields(
							List.of(
								Field.of("calendarBookingId", FieldType.LONG),
								Field.of("description", FieldType.TEXT),
								Field.of("location", FieldType.TEXT),
								Field.of("title", FieldType.TEXT),
								Field.of("titleCurrentValue", FieldType.TEXT),
								Field.of("startTime", FieldType.DATE),
								Field.of("endTime", FieldType.DATE),
								Field.of("allDay", FieldType.BOOLEAN)
							)
						)
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, true,
					DocumentType
						.builder()
						.searchKeywords(
							List.of(
								SearchKeyword.text("path", "file")
							)
						)
						.name("file")
						.icon(Strings.BLANK)
						.sourceFields(
							List.of(
								Field.of("lastModifiedDate", FieldType.DATE),
								Field.of("path", FieldType.TEXT)
							)
						)
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, false,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("document")
						.searchKeywords(
							List.of(
								SearchKeyword.text("content", "document"),
								SearchKeyword.text("title", "document")
							)
						)
						.sourceFields(
							List.of(
								Field.of("previewURLs", FieldType.TEXT),
								Field.of("previewUrl", FieldType.TEXT),
								Field.of("content", FieldType.TEXT),
								Field.of("contentType", FieldType.TEXT),
								Field.of("title", FieldType.TEXT),
								Field.of("URL", FieldType.TEXT)
							)
						)
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, false,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("office-word")
						.searchKeywords(List.of())
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, false,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("office-excel")
						.searchKeywords(List.of())
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, false,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("office-powerpoint")
						.searchKeywords(List.of())
						.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, false,
					DocumentType
						.builder()
						.icon(Strings.BLANK)
						.name("acl")
						.searchKeywords(List.of())
						.sourceFields(
							List.of(
								Field.of(
									"allow", Field.of(
										"roles", 
										FieldType.KEYWORD
										)
									)
								)
						)
						.build()
				)
			);
	}

	@Reference(
		target = "(component.name=io.openk9.plugins.liferay.driver.LiferayPluginDriver)"
	)
	private PluginDriver _pluginDriver;

}
