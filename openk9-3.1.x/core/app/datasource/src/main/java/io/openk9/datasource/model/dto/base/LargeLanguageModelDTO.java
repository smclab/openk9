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

import io.openk9.datasource.validation.json.Json;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;


@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class LargeLanguageModelDTO extends K9EntityDTO {

	@NotNull
	@Description("It is the API url of the model that you want to use.")
	private String apiUrl;
	@Description("It is the API key that you have to provide in order to make the authentication.")
	private String apiKey;
	@Json
	@Description("It is a JSON that can be used to add additional configurations to the LargeLanguageModel.")
	private String jsonConfig;
	@NotNull
	private ProviderModelDTO providerModel;
	@Description("It is the context window size.")
	private Integer contextWindow;
	@Description("It indicates whether the LargeLanguageModel retrieves citations.")
	private Boolean retrieveCitations;

}
