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

package io.quarkus.hibernate.reactive.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * This was the only way I found to get our hands on the settings used during metadata building.
 * Feel free to use some other solution if you find one.
 */
public class SettingsSpyingIdentifierGenerator implements IdentifierGenerator {
    public static final List<Map<String, Object>> collectedSettings = new ArrayList<>();

    @Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry)
		throws MappingException {
		collectedSettings.add(new HashMap<>(serviceRegistry
			.getService(ConfigurationService.class)
			.getSettings()));
    }

    @Override
	public Serializable generate(SharedSessionContractImplementor session, Object object)
		throws HibernateException {
        throw new IllegalStateException("This should not be called");
    }
}
