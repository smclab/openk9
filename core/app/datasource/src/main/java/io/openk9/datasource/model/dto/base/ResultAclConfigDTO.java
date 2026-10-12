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

package io.openk9.datasource.model.dto.base;

import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.smallrye.graphql.api.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ResultAclConfigDTO extends K9EntityDTO {

	@Nullable
	@Description("""
		Whether the ACLs decide what is visible of each result instead of
		filtering the query. False when omitted on create or update; left
		unchanged when omitted on patch.
	""")
	private Boolean enabled;

	@Nullable
	@Description("""
		Roles whose holders read every result in full, compared with the
		roles of the JWT. Each role must be non-blank and on a single line.
	""")
	private List<@NotBlank @Pattern(regexp = "[^\\r\\n]+") String> superUserRoles;

	@Nullable
	@Description("""
		IDs of the DocTypeFields still visible on a result the user is not
		authorized to read. Excluded DocTypeFields are rejected.
	""")
	private Set<Long> visibleFieldIds;

}
