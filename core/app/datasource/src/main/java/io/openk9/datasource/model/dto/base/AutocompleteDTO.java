/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import java.util.Set;

import io.smallrye.graphql.api.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;
import org.opensearch.client.opensearch._types.query_dsl.Operator;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AutocompleteDTO extends K9EntityDTO{

	@Nullable
	@Description("""
		Maximum number of results to return when no explicit limit is specified in the query.
	""")
	private Integer fallbackResultSize;
	@Description("""
		The field list used in the autocomplete query,
		must not be empty and all the docTypeField must be of type search_as_you_type.
	""")
	private Set<Long> fieldIds;
	@Nullable
	@Description("""
		Edit distance allowed for fuzzy matching (e.g., "0", "1", "2", or "AUTO")
	""")
	private String fuzziness;
	@Nullable
	@Description("""
		Minimum number of optional clauses that must match for a document to be returned
	""")
	private String minimumShouldMatch;
	@Nullable
	@Description("""
		Boolean operator to combine query terms (AND or OR)
	""")
	private Operator operator;

}
