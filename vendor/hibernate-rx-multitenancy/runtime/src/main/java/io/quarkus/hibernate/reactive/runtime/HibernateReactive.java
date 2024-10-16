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

package io.quarkus.hibernate.reactive.runtime;

import org.jboss.logging.Logger;

public class HibernateReactive {

    public static final String DEFAULT_REACTIVE_PERSISTENCE_UNIT_NAME = "default-reactive";

    public static void featureInit(boolean enabled) {
        // Override the JPA persistence unit resolver so to use our custom boot
        // strategy:
        ReactivePersistenceProviderSetup.registerStaticInitPersistenceProvider();

        if (enabled) {
            Logger.getLogger("org.hibernate.quarkus.feature").debug(
                "Hibernate Reactive Features Enabled");
        }
    }

}
