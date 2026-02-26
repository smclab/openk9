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

package io.quarkus.hibernate.reactive.deployment;

import java.util.function.BooleanSupplier;

import io.quarkus.hibernate.orm.deployment.HibernateOrmConfig;

/**
 * Supplier that can be used to only run build steps
 * if the Hibernate ORM extension is enabled.
 */
// TODO Ideally we'd rely on separate configuration for Hibernate Reactive,
//  both in general and specifically to enable/disable the extension.
//  But we would first need to split common code to a separate artifact,
//  and that's a lot of work that will conflict with other ongoing changes,
//  so we better wait.
//  See also https://github.com/quarkusio/quarkus/issues/13425
public class HibernateReactiveEnabled implements BooleanSupplier {

    private final HibernateOrmConfig config;

    HibernateReactiveEnabled(HibernateOrmConfig config) {
        this.config = config;
    }

    @Override
    public boolean getAsBoolean() {
        return config.enabled();
    }

}
