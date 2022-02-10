---
id: create-plugin-using-java
title: Create a plugin using Java
---

In OpenK9 you can create custom plugin using Java.

## Prerequisites

Prerequisites are described in [`OSGi project requirements`](/docs/osgi-requirements).

### Create Plugin

A complete example is present at the link https://github.com/smclab/openk9-example-java-datasource

To create a new plugin with Java you need to have a gradle project with this structure:

```
example-plugin/
├── bnd.bnd
├── build.gradle
└── src
    └── main
        └── java
            └── com
                └── openk9
                    └── plugins
                        └── example
                            └── driver
                                ├── DocumentTypeDefinition.java
                                └── ExamplePluginDriver.java
```

Your project needs to contains following files:

- `bnd.bnd` identifies plugin as bundle
```aidl
Bundle-Name: [OpenK9 - Plugin] Example
Bundle-SymbolicName: io.openk9.plugins.example

Bundle-Version: 0.0.1

-noimportjava: true
```
- `build.gradle` defines java dependencies and adds bndtools plugin
```aidl
buildscript {
	repositories {
		maven {
			url "https://plugins.gradle.org/m2/"
		}
	}
	dependencies {
		classpath "biz.aQute.bnd:biz.aQute.bnd.gradle:5.2.0"
	}
}

apply plugin: 'java'
apply plugin: 'biz.aQute.bnd.builder'

sourceCompatibility = 11
targetCompatibility = 11

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	compile 'io.openk9:io.openk9.release.api:OPENK9_VERSION'
}
```

### Driver Definition

- `ExamplePluginDriver.java` contains java code to enable plugin inside OpenK9.


```java
package io.openk9.plugins.exampledatasource.driver;

import io.openk9.datasource.model.Datasource;
import io.openk9.ingestion.driver.manager.api.PluginDriver;
import io.openk9.ingestion.logic.api.IngestionLogic;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component(
	immediate = true,
	service = PluginDriver.class
)
public class ExamplePluginDriver implements PluginDriver {

	@interface Config {
		boolean schedulerEnabled() default true;
	}

	@Activate
	public void activate(Config config) {
		_config = config;
	}

	@Modified
	public void modified(Config config) {
		_config = config;
	}

	@Override
	public String getName() {
		return "example";
	}

	@Override
	public Publisher<Void> invokeDataParser(
		Datasource datasource, Date fromDate, Date toDate) {

		return Mono.fromRunnable(() -> {

			for (int i = 0; i < 5; i++) {
				String id = "example-" + i;
				JsonNode data = _jsonFactory.createObjectNode()
								.put("title", "Item " + i)
								.put("description", "This is a test, you are looking for item number " + i);

				_ingestionLogicSender.send(
					IngestionPayload
						.builder()
						.datasourceId(datasource.getDatasourceId())
						.rawContent(id)
						.contentId(id)
						.tenantId(datasource.getTenantId())
						.datasourcePayload(
							_jsonFactory
								.createObjectNode()
								.set(getName(), data.toObjectNode())
								.toMap()
						)
						.parsingDate(toDate.getTime())
						.type(new String[] {getName()})
						.build()
				);
			}

		});

	}

	@Override
	public boolean schedulerEnabled() {
		return _config.schedulerEnabled();
	}

	@Reference
	private IngestionLogic _ingestionLogicSender;

	@Reference
	private JsonFactory _jsonFactory;

	private Config _config;

}
```


### Document Type Definition

- `DocumentTypeDefinition.java` contains java code to define indexed document types, fields and search keywords

```java
package io.openk9.plugins.exampledatasource.driver;

import io.openk9.ingestion.driver.manager.api.DocumentType;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactory;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryRegistry;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryRegistryAware;
import io.openk9.ingestion.driver.manager.api.PluginDriver;
import io.openk9.ingestion.driver.manager.api.SearchKeyword;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.search.client.api.mapping.Field;
import io.openk9.search.client.api.mapping.FieldType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
						.name(pluginDriverName)
						.searchKeywords(
							List.of(
								SearchKeyword.text("title", pluginDriverName),
								SearchKeyword.text("description", pluginDriverName)
							)
						)
						.sourceFields(
							List.of(
								Field.of("title", FieldType.TEXT),
								Field.of("description", FieldType.TEXT)
							)
						)
						.build())
			);
	}

	@Reference(
		target = "(component.name=io.openk9.plugins.exampledatasource.driver.ExamplePluginDriver)"
	)
	private PluginDriver _pluginDriver;

}
```

### Enrich Processor Definition

Enrich Processor are components used to perform some enrichment activity on data during ingestion: These activities
can be customized based on your needs.

Enrich processor normally use external services, like Machine Learning models or Parsers, to extract information from data
and enrich it with these information. Two different type of Enrich Processor can be developed, based on communication with
these services.

- `AsyncEnrichProcessor.java` contains java code to define enrich processor which communicate in synchronous
way with external service

- `SyncEnrichProcessor.java` contains java code to define enrich processor which communicate in asynchronous
way with external service, then using a queue to send data e get response
