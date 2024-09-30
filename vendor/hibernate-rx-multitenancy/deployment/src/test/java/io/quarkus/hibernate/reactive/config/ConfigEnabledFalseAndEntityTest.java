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

import io.quarkus.arc.Arc;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigEnabledFalseAndEntityTest {

	@RegisterExtension
	static final QuarkusUnitTest config = new QuarkusUnitTest()
		.withApplicationRoot(jar -> jar.addClass(MyEntity.class))
		.withConfigurationResource("application.properties")
		// This should disable Hibernate Reactive even if there is an entity
		.overrideConfigKey("quarkus.hibernate-orm.enabled", "false");

	@Test
	public void entityManagerFactory() {
		// The bean is not defined during static init, so it's null.
		assertThat(Arc.container().instance(EntityManagerFactory.class).get())
			.isNull();
	}

	@Test
	public void sessionFactory() {
		// The bean is not defined during static init, so it's null.
		assertThat(Arc.container().instance(SessionFactory.class).get())
			.isNull();
	}

	@Test
	public void mutinySessionFactory() {
		// The bean is not defined during static init, so it's null.
		assertThat(Arc.container().instance(Mutiny.SessionFactory.class).get())
			.isNull();
	}

	@Test
	@ActivateRequestContext
	public void mutinySession() {
		// The bean is not defined during static init, so it's null.
		assertThat(Arc.container().instance(Mutiny.Session.class).get())
			.isNull();
	}

}
