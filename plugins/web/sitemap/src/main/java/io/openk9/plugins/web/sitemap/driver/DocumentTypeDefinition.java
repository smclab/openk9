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

package io.openk9.plugins.web.sitemap.driver;

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
					)
			);
	}

	@Reference(
		target = "(component.name=io.openk9.plugins.web.sitemap.driver.SitemapWebPluginDriver)"
	)
	private PluginDriver _pluginDriver;

}
