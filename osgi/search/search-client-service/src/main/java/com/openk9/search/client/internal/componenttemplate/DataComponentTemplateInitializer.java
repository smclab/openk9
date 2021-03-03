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
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = ComponentTemplate.class)
public class DataComponentTemplateInitializer extends
	BaseComponentTemplateInitializer implements ComponentTemplate {

	@Override
	public String componentTemplatePath() {
		return "component/base-data-component-template.json";
	}

	@Override
	public String componentTemplateName() {
		return "data";
	}

	@Activate
	public void activate(BundleContext bundleContext) {
		super.activate(bundleContext);
	}

	@Override
	@Reference
	public void setComponentTemplateService(
		ComponentTemplateService componentTemplateService) {
		super.setComponentTemplateService(componentTemplateService);
	}

}
