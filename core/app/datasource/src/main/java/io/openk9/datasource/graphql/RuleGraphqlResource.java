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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.Rule;
import io.openk9.datasource.model.dto.RuleDTO;
import io.openk9.datasource.service.RuleService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class RuleGraphqlResource {

	@Query
	public Uni<Connection<Rule>> getRules(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return ruleService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Rule> getRule(@Id long id) {
		return ruleService.findById(id);
	}

	public Uni<Response<Rule>> patchRule(@Id long id, RuleDTO ruleDTO) {
		return ruleService.getValidator().patch(id, ruleDTO);
	}

	public Uni<Response<Rule>> updateRule(@Id long id, RuleDTO ruleDTO) {
		return ruleService.getValidator().update(id, ruleDTO);
	}

	public Uni<Response<Rule>> createRule(RuleDTO ruleDTO) {
		return ruleService.getValidator().create(ruleDTO);
	}

	@Mutation
	public Uni<Response<Rule>> rule(
		@Id Long id, RuleDTO ruleDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createRule(ruleDTO);
		} else {
			return patch
				? patchRule(id, ruleDTO)
				: updateRule(id, ruleDTO);
		}

	}

	@Mutation
	public Uni<Rule> deleteRule(@Id long ruleId) {
		return ruleService.deleteById(ruleId);
	}

	@Subscription
	public Multi<Rule> ruleCreated() {
		return ruleService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Rule> ruleDeleted() {
		return ruleService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Rule> ruleUpdated() {
		return ruleService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	RuleService ruleService;

}