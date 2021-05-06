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

package io.openk9.plugin.driver.manager.service;

import io.openk9.plugin.driver.manager.api.DocumentType;
import io.openk9.plugin.driver.manager.api.DocumentTypeFactory;
import io.openk9.plugin.driver.manager.api.DocumentTypeProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component(
	immediate = true,
	service = DocumentTypeProvider.class
)
public class DocumentTypeProviderImpl implements DocumentTypeProvider {

	@Override
	public DocumentType getDefaultDocumentType(String pluginDriverName) {
		return _defaultDocumentTypeMap.get(pluginDriverName);
	}

	@Override
	public List<DocumentType> getDocumentTypeList(String pluginDriverName) {
		return new ArrayList<>(
			_documentTypeMap.getOrDefault(
				pluginDriverName, Collections.emptyList()));
	}

	@Override
	public Map<String, List<DocumentType>> getDocumentTypeMap() {
		return new HashMap<>(_documentTypeMap);
	}

	@Reference(
		service = DocumentTypeFactory.class,
		bind = "addDocumentTypeFactory",
		unbind = "removeDocumentTypeFactory",
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.GREEDY,
		cardinality = ReferenceCardinality.MULTIPLE
	)
	public void addDocumentTypeFactory(
		DocumentTypeFactory documentTypeFactory,
		Map<String, Object> props) {

		Object pluginDriverNameObj =
			props.get(DocumentTypeFactory.PLUGIN_DRIVER_NAME);

		if (pluginDriverNameObj == null) {

			_log.warn(
				"DocumentTypeFactory must have "
				+ DocumentTypeFactory.PLUGIN_DRIVER_NAME
				+ " property. component: " + documentTypeFactory);

			return;
		}

		DocumentType newValue = documentTypeFactory.getDocumentType();

		if (Objects.isNull(newValue)) {

			_log.warn(
				"DocumentType is null for DocumentTypeFactory: "
				+ documentTypeFactory);

			return;
		}

		String pluginDriverName =(String)pluginDriverNameObj;

		Object defaultObj = props.get(DocumentTypeFactory.DEFAULT);

		boolean isDefault =
			defaultObj != null
			&& (defaultObj instanceof String
				? Boolean.parseBoolean((String)defaultObj)
				: (defaultObj instanceof Boolean ? (Boolean) defaultObj : false)
			);

		if (isDefault) {

			DocumentType previousValue = _defaultDocumentTypeMap.put(
				pluginDriverName, newValue);

			_actionMap.put(
				documentTypeFactory,
				() -> _defaultDocumentTypeMap.remove(pluginDriverName));

			if (previousValue != null) {
				_log.warn(
					"DocumentTypeFactory previous: "
					+ previousValue + " new: " + newValue);
			}
		}

		List<DocumentType> documentTypes =
			_documentTypeMap.computeIfAbsent(
				pluginDriverName, s -> new CopyOnWriteArrayList<>());

		documentTypes.add(newValue);

		Action action = _actionMap.get(documentTypeFactory);

		Action newAction = () -> documentTypes.remove(newValue);

		if (action != null) {
			_actionMap.put(
				documentTypeFactory, () -> {action.exec(); newAction.exec();});
		}
		else {
			_actionMap.put(documentTypeFactory, newAction);
		}

	}

	public void removeDocumentTypeFactory(
		DocumentTypeFactory documentTypeFactory) {

		Action action = _actionMap.get(documentTypeFactory);

		if (action != null) {
			action.exec();
		}

	}

	private final Map<String, List<DocumentType>> _documentTypeMap =
		new ConcurrentHashMap<>();

	private final Map<String, DocumentType> _defaultDocumentTypeMap =
		new ConcurrentHashMap<>();

	private final Map<DocumentTypeFactory, Action> _actionMap =
		Collections.synchronizedMap(new IdentityHashMap<>());

	interface Action {
		void exec();
	}

	private static final Logger _log = LoggerFactory.getLogger(
		DocumentTypeProviderImpl.class);

}
