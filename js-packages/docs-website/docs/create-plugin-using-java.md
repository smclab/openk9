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

- `AsyncExampleNerEnrichProcessor.java` contains java code to define enrich processor which communicate in asynchronous
way with external service. It defines destination name, which is queue name where data are sent,
to be analyzed by the external service that performs the enrichment on the data.

When the enrichment activity is finished the external service sends the data to

```java
package io.openk9.plugins.example.enrichprocessor;

import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = EnrichProcessor.class)
public class AsyncExampleNerEnrichProcessor implements AsyncEnrichProcessor {
    @Override
    public String destinationName() {
        return "io.openk9.ner";
    }

    @Override
    public String name() {
        return AsyncExampleNerEnrichProcessor.class.getName();
    }

    @Override
    public boolean validate(IngestionPayload ingestionPayload) {

        String rawContent = ingestionPayload.getRawContent();
        return rawContent.length() > 0;
    }
}
```

- `ExampleSyncEnrichProcessor.java` contains java code to define enrich processor which communicate in synchronous
way with external service


```java
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

package io.openk9.plugins.example.enrichprocessor;

import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.EnrichItem;
import io.openk9.model.DatasourceContext;
import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.api.SyncEnrichProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component(immediate = true, service = EnrichProcessor.class)
public class ExampleSyncEnrichProcessor implements SyncEnrichProcessor {

	@interface Config {
		String url() default "http://example-parser/";
		String path() default "/predict";
		int method() default HttpHandler.POST;
		String[] headers() default "Content-Type:application/json";
	}

	@Activate
	public void activate(Config config) {
		_config = config;
		_httpClient = _httpClientFactory.getHttpClient(_config.url());
	}

	@Modified
	public void modified(Config config) {
		activate(config);
	}

	@Override
	public Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		EnrichItem enrichItem, PluginDriverDTO pluginDriverDTO) {

		Map<String, Object> headers = Arrays
			.stream(_config.headers())
			.map(s -> s.split(":"))
			.collect(Collectors.toMap(e -> e[0], e -> e[1]));

		String jsonConfig = enrichItem.getJsonConfig();

		ObjectNode datasourceConfiguration =
			_jsonFactory.fromJsonToJsonNode(jsonConfig).toObjectNode();

		JsonNode rawContentNode = objectNode.get("rawContent");

		JsonNode confidenceNode = datasourceConfiguration.get("confidence");

		ObjectNode request = _jsonFactory.createObjectNode();

		request.put("confidence", confidenceNode);

		request.put("content", rawContentNode);

		return Mono.from(
			_httpClient.request(
				_config.method(),
				_config.path(),
				request.toString(),
				headers))
			.map(_jsonFactory::fromJsonToJsonNode)
			.map(JsonNode::toObjectNode)
			.map(objectNode::merge);

	}

	@Override
	public String name() {
		return SpacesTypeEnrichProcessor.class.getName();
	}

	private HttpClient _httpClient;

	private Config _config;

	@Reference
	private HttpClientFactory _httpClientFactory;

	@Reference
	private JsonFactory _jsonFactory;

}
```
