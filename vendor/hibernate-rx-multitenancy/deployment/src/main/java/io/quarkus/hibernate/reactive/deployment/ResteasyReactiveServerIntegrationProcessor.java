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

import jakarta.persistence.PersistenceException;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.resteasy.reactive.server.spi.UnwrappedExceptionBuildItem;

@BuildSteps(onlyIf = HibernateReactiveEnabled.class)
public class ResteasyReactiveServerIntegrationProcessor {

    @BuildStep
    public UnwrappedExceptionBuildItem unwrappedExceptions() {
        return new UnwrappedExceptionBuildItem(PersistenceException.class);
    }
}
