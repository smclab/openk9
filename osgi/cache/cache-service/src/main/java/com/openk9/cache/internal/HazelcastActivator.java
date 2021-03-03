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

package com.openk9.cache.internal;

import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.osgi.HazelcastOSGiInstance;
import com.hazelcast.osgi.impl.HazelcastInternalOSGiService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

@Component(
	immediate = true,
	service = HazelcastActivator.class
)
public class HazelcastActivator {

	@interface Config {
		String xmlFileConfig() default "/opt/hazelcast/hazelcast.xml";
	}

	@Activate
	public void activate(Config componentConfig) {

		try {

			com.hazelcast.config.Config config =
				new FileSystemXmlConfig(componentConfig.xmlFileConfig());

			_hazelcastOSGiInstance =
				_hazelcastInternalOSGiService.newHazelcastInstance(config);
		}
		catch (FileNotFoundException e) {

			_log.error(e.getMessage());
			_log.error("fallback initialize default HazelcastInstance...");

			_hazelcastOSGiInstance =
				_hazelcastInternalOSGiService.newHazelcastInstance();

		}

	}

	@Modified
	public void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	public void deactivate() {
		_hazelcastInternalOSGiService.shutdownHazelcastInstance(
			_hazelcastOSGiInstance);
	}

	private HazelcastOSGiInstance _hazelcastOSGiInstance;

	@Reference
	private HazelcastInternalOSGiService _hazelcastInternalOSGiService;

	private static final Logger _log =
		LoggerFactory.getLogger(HazelcastActivator.class.getName());

}
