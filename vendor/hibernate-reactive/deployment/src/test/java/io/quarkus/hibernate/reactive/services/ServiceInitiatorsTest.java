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

package io.quarkus.hibernate.reactive.services;

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.reactive.provider.impl.ReactiveServiceInitiators;
import org.hibernate.service.StandardServiceInitiators;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ServiceInitiatorsTest {

	private static final Map<String, String> HR_SERVICES =
		toServicesMap(ReactiveServiceInitiators.LIST);
	private static final Map<String, String> ORM_SERVICES =
		toServicesMap(StandardServiceInitiators.LIST);
	private static final Map<String, String> QUARKUS_HR_SERVICES = toServicesMap(
		ReactiveServiceInitiators.LIST);

	// These services are NOT provided by the Hibernate Reactive default initiators, and that should be fine:
	private static final Set<String> HR_INTENTIONALLY_OMITTED = Set
		.of("org.hibernate.engine.transaction.jta.platform.spi.JtaPlatformResolver");

	@Test
	public void serviceInitiatorsAreUnique() {
		Assertions.assertEquals(HR_SERVICES.size(), ReactiveServiceInitiators.LIST.size());
		Assertions.assertEquals(ORM_SERVICES.size(), StandardServiceInitiators.LIST.size());
		Assertions.assertEquals(ORM_SERVICES.size(), StandardServiceInitiators.LIST.size());
	}

	private static Map<String, String> toServicesMap(List<StandardServiceInitiator<?>> list) {
		TreeMap<String, String> rolesToImplMap = new TreeMap<>();
		for (StandardServiceInitiator<?> initiator : list) {
			final String serviceRole = initiator.getServiceInitiated().getName();
			rolesToImplMap.put(serviceRole, initiator.getClass().getName());
		}
		return Collections.unmodifiableMap(rolesToImplMap);
	}

}
