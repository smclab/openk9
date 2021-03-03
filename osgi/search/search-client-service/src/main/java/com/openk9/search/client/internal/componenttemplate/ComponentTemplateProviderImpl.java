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

package com.openk9.search.client.internal.componenttemplate;

import com.openk9.search.client.api.componenttemplate.ComponentTemplate;
import com.openk9.search.client.api.componenttemplate.ComponentTemplateProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = ComponentTemplateProvider.class
)
public class ComponentTemplateProviderImpl
	implements ComponentTemplateProvider {

	@Override
	public List<ComponentTemplate> getComponentTemplates(
		String componentTemplateName) {

		return _componentTemplatesMap.getOrDefault(
			componentTemplateName,
			Collections.emptyList()
		);
	}

	@Override
	public List<String> getComponentTemplateNames(
		String componentTemplateName) {

		return getComponentTemplates(componentTemplateName)
			.stream()
			.map(ComponentTemplate::componentTemplateName)
			.collect(Collectors.toList());
	}

	@Reference(
		service = ComponentTemplate.class,
		policyOption = ReferencePolicyOption.GREEDY,
		policy = ReferencePolicy.DYNAMIC,
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		bind = "addComponentTemplate",
		unbind = "removeComponentTemplate"
	)
	private void addComponentTemplate(ComponentTemplate componentTemplate) {
		List<ComponentTemplate> componentTemplates =
			_componentTemplatesMap.computeIfAbsent(
				componentTemplate.componentTemplateName(),
				k -> new ArrayList<>());

		componentTemplates.add(componentTemplate);

	}

	private void removeComponentTemplate(ComponentTemplate componentTemplate) {

		List<ComponentTemplate> componentTemplates = _componentTemplatesMap.get(
			componentTemplate.componentTemplateName());

		if (componentTemplates != null) {
			componentTemplates.remove(componentTemplate);
		}

	}

	private final Map<String, List<ComponentTemplate>> _componentTemplatesMap =
		new HashMap<>();

}
