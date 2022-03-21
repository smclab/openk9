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
