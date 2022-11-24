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

package io.openk9.datasource.service.util;

import io.quarkus.hibernate.reactive.panache.common.runtime.AbstractJpaOperations;
import io.quarkus.hibernate.reactive.panache.common.runtime.CommonPanacheQueryImpl;
import io.quarkus.hibernate.reactive.panache.runtime.PanacheQueryImpl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.util.List;

@Dependent
public class JpaOperationsFactory {

	@Produces
	public <T> AbstractJpaOperations<PanacheQueryImpl<T>> createAbstractJpaOperations() {

		return new AbstractJpaOperations<>() {
			@Override
			protected PanacheQueryImpl<T> createPanacheQuery(
				Uni<Mutiny.Session> session, String query, String orderBy,
				Object paramsArrayOrMap) {
				return new PanacheQueryImpl<>(new CommonPanacheQueryImpl<>(session, query, orderBy, paramsArrayOrMap)) {};
			}

			@Override
			protected Uni<List<?>> list(PanacheQueryImpl<T> query) {
				return (Uni)query.list();
			}

			@Override
			protected Multi<?> stream(PanacheQueryImpl<T> query) {
				return query.stream();
			}
		};
	}

}
