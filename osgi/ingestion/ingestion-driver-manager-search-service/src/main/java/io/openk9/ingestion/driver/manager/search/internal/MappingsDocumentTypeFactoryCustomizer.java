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

package io.openk9.ingestion.driver.manager.search.internal;

import io.openk9.ingestion.driver.manager.api.DocumentTypeFactory.DefaultDocumentTypeFactory;
import io.openk9.ingestion.driver.manager.api.DocumentTypeFactoryCustomizer;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.search.client.api.componenttemplate.ComponentTemplateProvider;
import io.openk9.search.client.api.indextemplate.IndexTemplateService;
import io.openk9.search.client.api.mapping.Field;
import io.openk9.search.client.api.mapping.FieldType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = DocumentTypeFactoryCustomizer.class
)
public class MappingsDocumentTypeFactoryCustomizer
	implements DocumentTypeFactoryCustomizer {

	@Activate
	public void activate() {

		_many = Sinks.many().unicast().onBackpressureBuffer();

		_disposable = _many
			.asFlux()
			.log()
			.bufferUntilChanged(DefaultDocumentTypeFactory::getPluginDriverName)
			.subscribe(list -> {

				List<Field> collect = list
					.stream()
					.map(DefaultDocumentTypeFactory::getDocumentType)
					.flatMap(documentType -> {

						List<Field> sourceFields =
							documentType.getSourceFields();

						if (sourceFields == null) {
							return Stream.empty();
						}

						return sourceFields
							.stream()
							.map(child -> Field.of(
								documentType.getName(), child));
					})
					.collect(Collectors.toList());

				Map<String, Object> objectNode = new HashMap<>();

				for (Field parent : collect) {

					Map<String, Object> fieldNode = _createFieldNode(parent);

					Map<String, Object> parentNodeWithName = fieldNode;

					Field child = parent.getChild();

					while (child != Field.NIL) {

						Map<String, Object> parentNode =(Map<String, Object>)
							parentNodeWithName.get(parent.getName());

						Map<String, Object> childNode = _createFieldNode(child);

						parentNode.put("properties", childNode);

						parentNodeWithName = childNode;

						parent = child;

						child = child.getChild();

					}

					objectNode = _merge(objectNode, fieldNode);

				}

				String pluginDriverName = list.get(0).getPluginDriverName();

				_indexTemplateService.createOrUpdateIndexTemplate(
					pluginDriverName + "_template",
					null,
					List.of(
						"*-" + pluginDriverName + "-data"),
					_jsonFactory
						.toJson(Map.of("properties", objectNode)),
					_componentTemplateProvider.getComponentTemplateNames(
						"data"),
					10
				);

			});


	}

	private Map<String, Object> _merge(
		Map<String, Object> objectNode, Map<String, Object> fieldNode) {

		return Stream.concat(
			objectNode.entrySet().stream(),
			fieldNode.entrySet().stream()
		)
			.collect(Collectors.toMap(
				Map.Entry::getKey, Map.Entry::getValue,
				(o, o2) -> _merge(
					(Map<String, Object>)o, (Map<String, Object>)o2)));
	}

	private Map<String, Object> _createFieldNode(Field field) {

		FieldType fieldType = field.getFieldType();

		Map<String, Object> fieldNode = new HashMap<>();

		if (fieldType != FieldType.NULL) {
			fieldNode.put("type", fieldType.getType());
		}

		for (Map.Entry<String, Object> entry :
			field.getExtra().entrySet()) {

			fieldNode.put(entry.getKey(), entry.getValue());

		}

		Map<String, Object> result = new HashMap<>();

		result.put(field.getName(), fieldNode);

		return result;
	}

	@Deactivate
	public void deactivate() {
		_disposable.dispose();
		_many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		_many = null;
	}


	@Override
	public void accept(
		DefaultDocumentTypeFactory defaultDocumentTypeFactory) {

		_many.emitNext(
			defaultDocumentTypeFactory, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	public static void merge(JsonNode mainNode, JsonNode updateNode) {

		Iterator<String> fieldNames = updateNode.fieldNames();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			JsonNode jsonNode = mainNode.get(fieldName);
			// if field exists and is an embedded object
			if (jsonNode != null && jsonNode.isObject()) {
				merge(jsonNode, updateNode.get(fieldName));
			}
			else {
				if (mainNode instanceof ObjectNode) {
					// Overwrite field
					JsonNode value = updateNode.get(fieldName);
					((ObjectNode) mainNode).put(fieldName, value);
				}
			}

		}

	}

	private Sinks.Many<DefaultDocumentTypeFactory> _many;

	private Disposable _disposable;

	@Reference
	private IndexTemplateService _indexTemplateService;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private ComponentTemplateProvider _componentTemplateProvider;

}
