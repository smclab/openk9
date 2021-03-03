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

import org.elasticsearch.cluster.metadata.ComponentTemplate;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

abstract class BaseComponentTemplateInitializer {

	public abstract String componentTemplatePath();

	public abstract String componentTemplateName();

	protected void activate(BundleContext bundleContext) {

		Bundle bundle = bundleContext.getBundle();

		String mappingPosition = componentTemplatePath();

		URL resource = bundleContext
			.getBundle()
			.getResource(mappingPosition);

		if (resource == null) {
			throw new RuntimeException(
				mappingPosition + " not found in bundle: "
				+ bundle.getSymbolicName());
		}

		try (InputStream is = resource.openStream()) {

			XContentParser parser = XContentType
				.JSON.xContent()
				.createParser(
					NamedXContentRegistry.EMPTY,
					DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
					is);

			_componentTemplateService.createOrUpdateComponentTemplate(
				componentTemplateName(),
				ComponentTemplate.parse(parser)
			);

		}
		catch (IOException exception) {
			throw new RuntimeException(exception);
		}

	}

	protected void setComponentTemplateService(
		ComponentTemplateService componentTemplateService) {
		_componentTemplateService = componentTemplateService;
	}

	private ComponentTemplateService _componentTemplateService;

}
