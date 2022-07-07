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

package io.openk9.datasource.graphql;

import io.openk9.datasource.model.mapper.K9Entity;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.GraphQLApi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class K9EntityGraphqlResource {

	@Subscription
	public Multi<K9EntityEvent<K9Entity>> entityEvents() {

		Set<BroadcastProcessor<K9EntityEvent<K9Entity>>> collect =
			services.stream().map(BaseK9EntityService::getProcessor).collect(
				Collectors.toSet());

		return Multi
			.createBy()
			.concatenating()
			.streams(collect)
			.map(Function.identity());
	}

	@Inject
	Instance<BaseK9EntityService<K9Entity>> services;

}