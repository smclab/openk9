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

package io.openk9.plugin.driver.manager.search.service;

import io.openk9.index.writer.mappings.publisher.api.IndexWriterEventPublisher;
import io.openk9.index.writer.model.IndexTemplateDTO;
import io.openk9.json.api.JsonFactory;
import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactoryCustomizer;
import io.openk9.plugin.driver.manager.api.Field;
import io.openk9.plugin.driver.manager.api.FieldType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.HashMap;
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

	@Override
	public Mono<Void> apply(
		Map.Entry<String, List<DocumentType>> entry) {

		List<Field> collect = entry.getValue()
			.stream()
			.flatMap(documentType -> {

				List<Field> sourceFields =
					documentType.getSourceFields();

				if (sourceFields == null) {
					return Stream.empty();
				}

				if (sourceFields.size() == 1) {
					Field field = sourceFields.get(0);
					if (field instanceof Field.FieldMappings) {
						return Stream.of(field);
					}
				}

				return sourceFields
					.stream()
					.map(child -> Field.of(
						documentType.getName(), child));
			})
			.collect(Collectors.toList());

		if (collect.stream().allMatch(f -> f instanceof Field.FieldMappings)) {

			Map<String, Object> accumulator = new HashMap<>();

			for (Field field : collect) {
				deepMerge(accumulator, field.getExtra());
			}

			String pluginDriverName = entry.getKey();

			return _indexWriterEventPublisher.publishCreateIndexTemplate(
				IndexTemplateDTO.of(
					pluginDriverName + "_template",
					null,
					List.of(
						"*-" + pluginDriverName + "-data"),
					_jsonFactory.toJson(accumulator),
					List.of("data"),
					10
				)
			);

		}

		Map<String, Object> objectNode = new HashMap<>();

		for (Field parent : collect) {

			Map<String, Object> fieldNode = _createFieldNode(parent);

			Map<String, Object> parentNodeWithName = fieldNode;

			Field child = parent.getChild();

			while (child != Field.FieldObj.NIL) {

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

		String pluginDriverName = entry.getKey();

		return _indexWriterEventPublisher.publishCreateIndexTemplate(
			IndexTemplateDTO.of(
				pluginDriverName + "_template",
				null,
				List.of(
					"*-" + pluginDriverName + "-data"),
				_jsonFactory.toJson(Map.of("properties", objectNode)),
				List.of("data"),
				10
			)
		);

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

	private static Map deepMerge(Map original, Map newMap) {
		for (Object key : newMap.keySet()) {
			if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
				Map originalChild = (Map) original.get(key);
				Map newChild = (Map) newMap.get(key);
				original.put(key, deepMerge(originalChild, newChild));
			} else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
				List originalChild = (List) original.get(key);
				List newChild = (List) newMap.get(key);
				for (Object each : newChild) {
					if (!originalChild.contains(each)) {
						originalChild.add(each);
					}
				}
			} else {
				original.put(key, newMap.get(key));
			}
		}
		return original;
	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private IndexWriterEventPublisher _indexWriterEventPublisher;

}
