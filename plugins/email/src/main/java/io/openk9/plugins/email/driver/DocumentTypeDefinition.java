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

package io.openk9.plugins.email.driver;

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
						.name(pluginDriverName)
						.searchKeywords(
							List.of(
								SearchKeyword.text("cc", pluginDriverName),
								SearchKeyword.text("subject", pluginDriverName),
								SearchKeyword.text("from", pluginDriverName),
								SearchKeyword.text("to", pluginDriverName),
								SearchKeyword.text("body", pluginDriverName)
							)
						)
						.sourceFields(
							List.of(
								Field.of(
									"cc", FieldType.TEXT,
									Collections.singletonMap("analyzer", "email")),

								Field.of("subject", FieldType.TEXT),
								Field.of(
									"from", FieldType.TEXT,
									Collections.singletonMap("analyzer", "email")),
								Field.of("body", FieldType.TEXT),
								Field.of("date", FieldType.DATE),
								Field.of(
									"to", FieldType.TEXT,
									Collections.singletonMap("analyzer", "email")),
								Field.of(
									"htmlBody", FieldType.TEXT,
									Collections.singletonMap("index", false))
							)
						)
						.build()
				)
			);
	}

	@Reference(
		target = "(component.name=io.openk9.plugins.email.driver.EmailPluginDriver)"
	)
	private PluginDriver _pluginDriver;

}
