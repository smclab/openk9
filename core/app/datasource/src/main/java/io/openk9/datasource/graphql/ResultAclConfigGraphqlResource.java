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

import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.web.Response;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.model.dto.base.ResultAclConfigDTO;
import io.openk9.datasource.service.ResultAclConfigService;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class ResultAclConfigGraphqlResource {

	@Inject
	ResultAclConfigService resultAclConfigService;

	/**
	 * Returns the result ACL configuration of the tenant.
	 *
	 * @return the configuration, or {@code null} when the tenant has none
	 */
	@Description("""
		Retrieves the result ACL configuration of the tenant.

		Returns null when the tenant has no configuration, which is equivalent
		to a configuration with the mode disabled.
		""")
	@Query
	public Uni<ResultAclConfig> getResultAclConfig() {
		return resultAclConfigService.findCurrent();
	}

	/**
	 * Resolves the roles whose holders read every result in full.
	 *
	 * @param resultAclConfig the configuration
	 * @return the super-user roles, empty when none is configured
	 */
	@Description("Roles whose holders read every result in full")
	public List<String> superUserRoles(@Source ResultAclConfig resultAclConfig) {
		return ResultAclConfig.toRoleList(resultAclConfig.getSuperUserRoles());
	}

	/**
	 * Resolves the fields still visible on an obscured result.
	 *
	 * @param resultAclConfig the configuration
	 * @return the visible fields
	 */
	@Description("DocTypeFields still visible on a result the user cannot read")
	public Uni<Set<DocTypeField>> visibleFields(
		@Source ResultAclConfig resultAclConfig) {

		return resultAclConfigService.getVisibleFields(resultAclConfig.getId());
	}

	/**
	 * Creates the result ACL configuration of the tenant, or updates it when
	 * it already exists.
	 *
	 * @param resultAclConfigDTO the configuration data; its name is ignored
	 * @param patch              true to update only the non-null values
	 * @return the saved configuration, or the validation errors
	 */
	@Description("""
		Creates or updates the result ACL configuration of the tenant.

		A tenant has a single configuration: the mutation creates it when it
		does not exist yet and updates it otherwise. The name is fixed and any
		value sent is ignored.

		Arguments:
		- `resultAclConfigDTO` (ResultAclConfigDTO!): The configuration data.
		- `patch` (Boolean): If true, updates only the values that are sent.
		  If false, replaces the whole configuration. Defaults to false.

		Returns:
		- A Response containing the saved configuration or the field
		  validation errors.
		""")
	@Mutation
	public Uni<Response<ResultAclConfig>> resultAclConfig(
		ResultAclConfigDTO resultAclConfigDTO,
		@DefaultValue("false") boolean patch) {

		resultAclConfigDTO.setName(ResultAclConfig.DEFAULT_NAME);

		var validator = resultAclConfigService.getValidator();

		return resultAclConfigService.findCurrent()
			.flatMap(current -> {
				if (current == null) {
					return validator.create(resultAclConfigDTO);
				}

				return patch
					? validator.patch(current.getId(), resultAclConfigDTO)
					: validator.update(current.getId(), resultAclConfigDTO);
			});
	}

}
