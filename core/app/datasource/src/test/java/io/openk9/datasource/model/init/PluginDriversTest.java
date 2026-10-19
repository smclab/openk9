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

package io.openk9.datasource.model.init;

import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;

import io.vertx.core.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PluginDriversTest {

	// The crawler and sitemap presets share the same connector host, so
	// they must be served under distinct base paths: the core fetches
	// the form as baseUri + "/form" and needs a different one for each
	// crawling mode.
	@Test
	void crawlerAndSitemapPresets_exposeDistinctBasePaths() {
		// crawler preset resolves to the urls mode
		var crawlerBaseUri = baseUri(Preset.CRAWLER);
		Assertions.assertTrue(
			crawlerBaseUri.endsWith(":5000/urls"),
			"Crawler base uri must end with :5000/urls, got: " + crawlerBaseUri
		);

		// sitemap preset resolves to the sitemap mode
		var sitemapBaseUri = baseUri(Preset.SITEMAP);
		Assertions.assertTrue(
			sitemapBaseUri.endsWith(":5000/sitemap"),
			"Sitemap base uri must end with :5000/sitemap, got: " + sitemapBaseUri
		);
	}

	private static String baseUri(Preset preset) {
		var dto = PluginDrivers.getPluginDriverDTO(preset);

		return Json
			.decodeValue(dto.getJsonConfig(), HttpPluginDriverInfo.class)
			.getBaseUri();
	}

}
