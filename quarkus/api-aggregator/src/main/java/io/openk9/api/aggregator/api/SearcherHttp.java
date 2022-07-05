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

package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.KeywordType;
import io.openk9.api.aggregator.client.dto.QueryAnalysisRequestDTO;
import io.openk9.api.aggregator.client.dto.QueryAnalysisResponseDTO;
import io.openk9.api.aggregator.client.dto.SearchRequestDTO;
import io.openk9.api.aggregator.client.dto.SearcherResponseDTO;
import io.openk9.api.aggregator.client.dto.SearcherSuggestionCategoryDTO;
import io.openk9.api.aggregator.client.dto.SuggestionsResponseDTO;
import io.openk9.api.aggregator.client.dto.SupportedDatasourcesResponseDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SearcherHttp {

	@PermitAll
	@POST
	@Path("/v1/search")
	Uni<SearcherResponseDTO> search(SearchRequestDTO searchToken);

	@PermitAll
	@POST
	@Path("/v1/search/{datasourceId}")
	Uni<SearcherResponseDTO> search(
		@PathParam("datasourceId") long datasourceId,
		SearchRequestDTO searchToken);

	@PermitAll
	@POST
	@Path("/v1/autocomplete")
	Uni<JsonObject> autocomplete(SearchRequestDTO searchToken);

	@PermitAll
	@POST
	@Path("/v1/suggestions")
	Uni<SuggestionsResponseDTO> suggestions(SearchRequestDTO searchToken);

	@PermitAll
	@POST
	@Path("/v1/query-analysis")
	Uni<QueryAnalysisResponseDTO> queryAnalysis(
		QueryAnalysisRequestDTO queryAnalysisRequest);

	@PermitAll
	@GET
	@Path("/v1/document-types")
	Uni<Map<String, Collection<String>>> documentTypes();

	@PermitAll
	@GET
	@Path("/v2/document-types")
	Uni<Map<String, Map<String, Collection<String>>>> v2DocumentTypes();

	@PermitAll
	@GET
	@Path("/v2/document-types/{keywordType}")
	Uni<Map<String, Map<String, Collection<String>>>> v2DocumentTypes(
		@PathParam("keywordType")KeywordType keywordType);

	@PermitAll
	@GET
	@Path("/v1/entity/name")
	Uni<SearcherResponseDTO> entityName();

	@PermitAll
	@POST
	@Path("/v1/entity")
	Uni<SearcherResponseDTO> postEntity(JsonObject jsonObject);

	@PermitAll
	@GET
	@Path("/v1/entity")
	Uni<SearcherResponseDTO> getEntity(JsonObject jsonObject);

	@PermitAll
	@GET
	@Path("/v1/driver-service-names")
	Uni<List<String>> driverServiceNames(JsonObject jsonObject);

	@PermitAll
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/resources/{datasourceId}/{documentId}/{resourceId}")
	Uni<Response> downloadResources(
		@PathParam("datasourceId") long datasourceId,
		@PathParam("documentId") String documentId,
		@PathParam("resourceId") String resourceId
	);

	@PermitAll
	@GET
	@Path("/suggestion-categories")
	Uni<List<SearcherSuggestionCategoryDTO>> suggestionCategories();

	@PermitAll
	@GET
	@Path("/suggestion-categories/{categoryId}")
	Uni<SearcherSuggestionCategoryDTO> suggestionCategories(@PathParam("categoryId") long categoryId);

	@PermitAll
	@GET
	@Path("/v1/supported-datasources")
	Uni<SupportedDatasourcesResponseDTO> supportedDatasources();

}
