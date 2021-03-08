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

package io.openk9.search.client.internal;

import io.openk9.osgi.util.AutoCloseables;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.client.internal.configuration.ElasticSearchConfiguration;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Component(immediate = true, service = ElasticSearchActivator.class)
public class ElasticSearchActivator {

	@Activate
	public void activate(BundleContext bundleContext)
		throws IOException {

		RestClientBuilder builder = RestClient.builder(
			Arrays
				.stream(_elasticSearchConfiguration.hosts())
				.map(e -> e.split(":"))
				.map(e -> new HttpHost(e[0], Integer.parseInt(e[1])))
				.toArray(HttpHost[]::new)
		);

		RestHighLevelClient restHighLevelClient =
			new RestHighLevelClient(builder);

		RestHighLevelClientProvider restHighLevelClientProvider =
			new RestHighLevelClientProviderImpl(restHighLevelClient);

		ServiceRegistration<RestHighLevelClientProvider>
			serviceRegistration =
			bundleContext.registerService(
				RestHighLevelClientProvider.class,
				restHighLevelClientProvider, null);

		_registrationList.add(
			AutoCloseables.mergeAutoCloseableToSafe(
				serviceRegistration::unregister, restHighLevelClient));

	}

	@Modified
	public void modified(
			BundleContext bundleContext)
		throws IOException {

		deactivate();

		activate(bundleContext);
	}


	@Deactivate
	public void deactivate() {
		Iterator<AutoCloseables.AutoCloseableSafe> iterator =
			_registrationList.iterator();

		while (iterator.hasNext()) {
			iterator.next().close();
			iterator.remove();
		}

	}

	private final List<AutoCloseables.AutoCloseableSafe> _registrationList =
		new ArrayList<>();

	@Reference
	private ElasticSearchConfiguration _elasticSearchConfiguration;

}
