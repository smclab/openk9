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

package io.openk9.plugins.demanio.driver;

import io.openk9.common.api.constant.Strings;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactory;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactoryRegistry;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactoryRegistryAware;
import io.openk9.plugin.driver.manager.api.Field;
import io.openk9.plugin.driver.manager.api.FieldType;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.SearchKeyword;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;

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
									.name("web")
									.searchKeywords(
											List.of(
													SearchKeyword.boostText("title", "web", 10.0f),
													SearchKeyword.text("content", "web")
											)
									)
									.sourceFields(
											List.of(
													Field.of("title", FieldType.TEXT,
															Map.of("analyzer", "standard_lowercase_italian_stop_words_filter")),
													Field.of("content", FieldType.TEXT,
															Map.of("analyzer", "standard_lowercase_italian_stop_words_filter")),
													Field.of("url", FieldType.TEXT),
													Field.of("favicon", FieldType.TEXT)
											)
									)
									.build()
					),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
						pluginDriverName, false,
						DocumentType
								.builder()
								.icon(Strings.BLANK)
								.name("notizie")
								.searchKeywords(
										List.of(
												SearchKeyword.text("topics.keyword", "notizie"),
												SearchKeyword.boostText("topics.keyword", "notizie", 50.0f),
												SearchKeyword.autocompleteReference("notizie.topics.searchasyou"
														, "notizie.topics"),
												SearchKeyword.text("pubDate", "notizie"),
												SearchKeyword.date("pubDate.sortable", "notizie"),
												SearchKeyword.boostText("pubDate.keyword", "notizie", 50.0f)
										)
								)
								.sourceFields(
										List.of(
												Field.of(
														"topics", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"pubDate", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"sortable", Map.of(
																				"type", FieldType.DATE.getType(),
																				"locale", "it_IT",
																				"format", "[dd MMMM yyyy]",
																				"ignore_malformed", true
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												)
										)
								)
								.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
						pluginDriverName, false,
						DocumentType
								.builder()
								.icon(Strings.BLANK)
								.name("demanio")
								.searchKeywords(
										List.of(
												SearchKeyword.text("category.keyword", "demanio"),
												SearchKeyword.boostText("category.keyword", "demanio", 50.0f),
												SearchKeyword.autocompleteReference("demanio.category.searchasyou"
														, "demanio.category"),
												SearchKeyword.text("topic.keyword", "demanio"),
												SearchKeyword.boostText("topic.keyword", "demanio", 50.0f),
												SearchKeyword.autocompleteReference("demanio.topic.searchasyou"
														, "demanio.topic")
										)
								)
								.sourceFields(
										List.of(
												Field.of(
														"category", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"topic", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												)
										)
								)
								.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
						pluginDriverName, false,
						DocumentType
								.builder()
								.icon(Strings.BLANK)
								.name("gare")
								.searchKeywords(
										List.of(
												SearchKeyword.text("descrizione", "gare"),
												SearchKeyword.text("regione", "gare"),
												SearchKeyword.text("provincia", "gare"),
												SearchKeyword.text("comune", "gare"),
												SearchKeyword.text("stazione", "gare"),
												SearchKeyword.text("tipologia", "gare"),
												SearchKeyword.text("oggettoGara", "gare"),
												SearchKeyword.text("status", "gare"),
												SearchKeyword.text("importo", "gare"),
												SearchKeyword.text("criterio", "gare"),
												SearchKeyword.text("nominativo", "gare"),
												SearchKeyword.text("email", "gare"),
												SearchKeyword.boostText("stazione.keyword", "gare", 50.0f),
												SearchKeyword.boostText("regione.keyword", "gare", 50.0f),
												SearchKeyword.boostText("provincia.keyword", "gare", 50.0f),
												SearchKeyword.boostText("tipologia.keyword", "gare", 50.0f),
												SearchKeyword.boostText("oggettoGara.keyword", "gare", 50.0f),
												SearchKeyword.boostText("status.keyword", "gare", 50.0f),
												SearchKeyword.boostText("nominativo.keyword", "gare", 50.0f),
												SearchKeyword.boostText("criterio.keyword", "gare", 50.0f),
												SearchKeyword.autocompleteReference("gare.criterio.searchasyou"
														, "gare.criterio"),
												SearchKeyword.autocompleteReference("gare.tipologia.searchasyou"
														, "gare.tipologia"),
												SearchKeyword.autocompleteReference("gare.oggettoGara.searchasyou"
														, "gare.oggettoGara"),
												SearchKeyword.autocompleteReference("gare.status.searchasyou"
														, "gare.status"),
												SearchKeyword.autocompleteReference("gare.nominativo.searchasyou"
														, "gare.nominativo"),
												SearchKeyword.boostText("datapubblicazione.keyword", "gare", 50.0f),
												SearchKeyword.date("datapubblicazione.sortable", "gare")
										)
								)
								.sourceFields(
										List.of(
												Field.of("descrizione", FieldType.TEXT),
												Field.of(
														"regione", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"provincia", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of("comune", FieldType.TEXT),
												Field.of(
														"stazione", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of("importo", FieldType.TEXT),
												Field.of("email", FieldType.TEXT),
												Field.of(
														"criterio", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"nominativo", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"tipologia", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"status", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"oggettoGara", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"searchasyou", Map.of(
																				"type", FieldType.SEARCH_AS_YOU_TYPE.getType()
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												),
												Field.of(
														"datapubblicazione", FieldType.TEXT, Map.of(
																"fields", Map.of(
																		"sortable", Map.of(
																				"type", FieldType.DATE.getType(),
																				"locale", "it_IT",
																				"format", "dd MMMM yyyy",
																				"ignore_malformed", true
																		),
																		"keyword", Map.of(
																				"type", FieldType.KEYWORD.getType()
																		)
																)
														)
												)
										)
								)
								.build()
				),
				DocumentTypeFactory.DefaultDocumentTypeFactory.of(
					pluginDriverName, true,
					DocumentType
						.builder()
						.name("file")
						.icon(Strings.BLANK)
						.searchKeywords(
							List.of(
								SearchKeyword.text("path", "file")
							)
						)
						.sourceFields(
							List.of(
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
								SearchKeyword.boostText("title", "document", 10.0f),
								SearchKeyword.text("url", "document")
							)
						)
						.sourceFields(
							List.of(
								Field.of("title", FieldType.TEXT,
										Map.of("analyzer", "standard_lowercase_italian_stop_words_filter")),
								Field.of("content", FieldType.TEXT,
										Map.of("analyzer", "standard_lowercase_italian_stop_words_filter")),
								Field.of("contentType", FieldType.TEXT),
								Field.of("url", FieldType.TEXT)
							)
						)
						.build()
				)
			);
	}

	@Reference(
		target = "(component.name=io.openk9.plugins.demanio.driver.DemanioPluginDriver)"
	)
	private PluginDriver _pluginDriver;

}
