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

package io.openk9.bundle.installer.via.config;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

@Component(
	immediate = true,
	configurationPolicy = ConfigurationPolicy.REQUIRE,
	service = BundleInstallerConfiguration.class
)
@Designate(ocd = BundleInstallerConfigurationOCD.class, factory = true)
public class BundleInstallerConfigurationImpl
	implements BundleInstallerConfiguration {

	@Activate
	void activate(BundleInstallerConfigurationOCD config) {
		bundleSymbolicName = config.bundleSymbolicName();
		bundleBase64 = config.bundleBase64();
	}

	@Override
	public String bundleSymbolicName() {
		return bundleSymbolicName;
	}

	@Override
	public String bundleBase64() {
		return bundleBase64;
	}

	private String bundleSymbolicName;
	private String bundleBase64;

}
