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

package io.openk9.plugins.applications.driver;

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
import org.osgi.service.component.annotations.ReferencePolicyOption;

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
						.name(pluginDriverName)
						.searchKeywords(
							List.of(
								SearchKeyword.text("title", pluginDriverName),
								SearchKeyword.boostText("applicationName", pluginDriverName, 10.0f),
								SearchKeyword.text("URL", pluginDriverName),
								SearchKeyword.text("description", pluginDriverName)
							)
						)
						.sourceFields(
							List.of(
								Field.of("title", FieldType.TEXT),
								Field.of("applicationName", FieldType.TEXT),
								Field.of("URL", FieldType.TEXT),
								Field.of("description", FieldType.TEXT),
								Field.of(
									"icon",
									FieldType.TEXT,
									Map.of("index", false)
								)
							)
						)
						.build())
			);
	}

	@Reference(
		target = "(component.name=io.openk9.plugins.applications.driver.ApplicationPluginDriver)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private PluginDriver _pluginDriver;

}
