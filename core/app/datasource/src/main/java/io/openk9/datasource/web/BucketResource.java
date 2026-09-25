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

package io.openk9.datasource.web;


import java.util.List;
import java.util.stream.Stream;

import io.openk9.datasource.web.dto.DatasourceResponseDTO;
import io.openk9.datasource.web.dto.DocTypeFieldResponseDTO;
import io.openk9.datasource.web.dto.SortingResponseDTO;
import io.openk9.datasource.web.dto.TabResponseDTO;
import io.openk9.datasource.web.dto.TemplateResponseDTO;
import io.openk9.datasource.web.dto.openapi.BucketDtoExamples;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.openk9.datasource.mapper.BucketResourceMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.LocalizedSuggestionCategory;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Sorting_;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.TranslationService;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.hibernate.reactive.mutiny.Mutiny;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;


@ApplicationScoped
@Path("/buckets")
public class BucketResource {

	@Inject
	RoutingContext routingContext;

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of Templates returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					example = BucketDtoExamples.TEMPLATES_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/templates")
	@GET
	public Uni<List<TemplateResponseDTO>> getTemplates() {
		return cache.getAsync(
			cacheKey("getTemplates"),
			key -> getDocTypeTemplateList()
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of Tabs returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					example = BucketDtoExamples.TABS_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/tabs")
	@GET
	public Uni<List<TabResponseDTO>> getTabs(
			@Parameter(description = "If return translations")
			@QueryParam("translated") @DefaultValue("true") boolean translated) {

		return cache.getAsync(
			cacheKey("getTabs", translated),
			key -> bucketService.getTabList(translated)
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of Suggestion Categories returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					example = BucketDtoExamples.SUGGESTION_CATEGORIES_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/suggestionCategories")
	@GET
	public Uni<List<? extends SuggestionCategory>> getSuggestionCategories(
			@Parameter(description = "If return translations")
			@QueryParam("translated") @DefaultValue("true") boolean translated) {

		return cache.getAsync(
			cacheKey("getSuggestionCategories", translated),
			key -> getSuggestionCategoryList(translated)
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of Sortable DocTypeFields returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					example = BucketDtoExamples.SORTABLE_DOCTYPE_FIELDS_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/doc-type-fields-sortable")
	@GET
	public Uni<List<DocTypeFieldResponseDTO>> getDocTypeFieldsSortable(
			@Parameter(description = "If return translations")
			@QueryParam("translated") @DefaultValue("true") boolean translated){
		return cache.getAsync(
			cacheKey("getDocTypeFieldsSortable", translated),
			key -> getDocTypeFieldsSortableList(translated)
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of Sortings returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					example = BucketDtoExamples.SORTING_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/sortings")
	@GET
	public Uni<List<SortingResponseDTO>> getSortings(
			@Parameter(description = "If return translations")
			@QueryParam("translated") @DefaultValue("true") boolean translated){
		return cache.getAsync(
			cacheKey("getSortings", translated),
			key -> getSortingList(translated)
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of Datasources returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					example = BucketDtoExamples.DATASOURCES_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/datasources")
	@GET
	public Uni<List<DatasourceResponseDTO>> getDatasources() {
		return cache.getAsync(
			cacheKey("getDatasources"),
			key -> getDatasourceList()
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Default Language returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = Response.class),
					example = BucketDtoExamples.DEFAULT_LANGUAGE_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/defaultLanguage")
	@GET
	public Uni<Language> getDefaultLanguage(){
		return cache.getAsync(
			cacheKey("getDefaultLanguage"),
			key -> findDefaultLanguage()
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of available Languages returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = Response.class),
					example = BucketDtoExamples.AVAILABLE_LANGUAGES_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current/availableLanguage")
	@GET
	public Uni<List<Language>> getAvailableLanguage(){
		return cache.getAsync(
			cacheKey("getAvailableLanguage"),
			key -> getAvailableLanguageList()
		);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Current Bucket configuration returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = Response.class),
					example = BucketDtoExamples.CURRENT_BUCKET_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/current")
	@GET
	public Uni<CurrentBucket> getCurrentBucket() {
		return cache.getAsync(
			cacheKey("getCurrentBucket"),
			key -> _getCurrentBucket()
		);
	}

	/**
	 * The current bucket is the one of the tenant the request is routed to,
	 * so the tenant scopes every cached response.
	 */
	private CompositeCacheKey cacheKey(Object... keyElements) {
		var elements = new Object[keyElements.length + 1];
		elements[0] = String.valueOf((Object) routingContext.get("_tenantId"));
		System.arraycopy(keyElements, 0, elements, 1, keyElements.length);

		return new CompositeCacheKey(elements);
	}

	private Uni<CurrentBucket> _getCurrentBucket() {
		return bucketService
			.getCurrentBucket()
			.map(mapper::toCurrentBucket);
	}

	private Uni<List<DocTypeFieldResponseDTO>> getDocTypeFieldsSortableList(boolean translated) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Tuple> query = cb.createTupleQuery();

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<DocType, DocTypeField> parentDocTypeFieldJoin =
				from.join(Bucket_.datasources)
					.join(Datasource_.dataIndex)
					.join(DataIndex_.docTypes)
					.join(DocType_.docTypeFields);

			SetJoin<DocTypeField, DocTypeField> subDocTypeFieldJoin =
				parentDocTypeFieldJoin.join(DocTypeField_.subDocTypeFields, JoinType.LEFT);

			query.multiselect(parentDocTypeFieldJoin, subDocTypeFieldJoin);

			query.where(
				cb.or(
					cb.isTrue(
						parentDocTypeFieldJoin.get(DocTypeField_.sortable)
					),
					cb.isTrue(
						subDocTypeFieldJoin.get(DocTypeField_.sortable)
					)
				)
			);

			return session
				.createQuery(query)
				.getResultList()
				.map(tList ->
					tList
						.stream()
						.flatMap(t -> {

							Stream.Builder<DocTypeField> builder =
								Stream.builder();

							DocTypeField docTypeField1 =
								t.get(0, DocTypeField.class);

							if (docTypeField1 != null && docTypeField1.isSortable()) {
								builder.add(docTypeField1);
							}

							DocTypeField docTypeField2 =
								t.get(1, DocTypeField.class);

							if (docTypeField2 != null && docTypeField2.isSortable()) {
								builder.add(docTypeField2);
							}

							return builder.build();
						}))
				.chain(docTypeFields -> {
					List<DocTypeField> docTypeFieldList = docTypeFields.distinct().toList();
					if (translated) {
						return translationService
							.getTranslationMaps(
								DocTypeField.class,
								docTypeFieldList.stream()
									.map(K9Entity::getId)
									.toList())
							.map(maps -> mapper.toDocTypeFieldResponseDtoList(docTypeFieldList, maps));
					}
					else {
						return Uni
							.createFrom()
							.item(mapper.toDocTypeFieldResponseDtoList(docTypeFieldList));
					}
				});
		});

	}

	private Uni<List<SortingResponseDTO>> getSortingList(boolean translated) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Sorting> query = cb.createQuery(Sorting.class);

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<Bucket, Sorting> sortingsJoin = from.join(Bucket_.sortings);

			sortingsJoin.fetch(Sorting_.docTypeField, JoinType.LEFT);

			query.select(sortingsJoin);


			query.orderBy(cb.desc(sortingsJoin.get(Sorting_.priority)));

			query.distinct(true);

			return session
				.createQuery(query)
				.getResultList()
				.chain(sortings -> {
					if (translated) {
						return translationService
							.getTranslationMaps(
								Sorting.class,
								sortings.stream()
									.map(K9Entity::getId)
									.toList())
							.map(maps -> mapper.toSortingResponseDtoList(sortings, maps));
					}
					else {
						return Uni
							.createFrom()
							.item(mapper.toSortingResponseDtoList(sortings));
					}
				});
		});

	}

	private Uni<List<DatasourceResponseDTO>> getDatasourceList() {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Datasource> query = cb.createQuery(Datasource.class);

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<Bucket, Datasource> datasourcesJoin =
				from.join(Bucket_.datasources);

			query.select(datasourcesJoin);


			query.orderBy(cb.asc(datasourcesJoin.get(Datasource_.name)));

			query.distinct(true);

			return session
				.createQuery(query)
				.getResultList()
				.map(mapper::toDatasourceResponseDtoList);
		});

	}

	private Uni<List<TemplateResponseDTO>> getDocTypeTemplateList() {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<DocTypeTemplate> query = cb.createQuery(DocTypeTemplate.class);

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<DocType, DocTypeTemplate> fetch =
				from.join(Bucket_.datasources)
					.join(Datasource_.dataIndex)
					.join(DataIndex_.docTypes)
					.join(DocType_.docTypeTemplate);

			query.select(fetch);


			return session
				.createQuery(query)
				.getResultList()
				.map(mapper::toTemplateResponseDtoList);

		});

	}

	private Uni<List<? extends SuggestionCategory>> getSuggestionCategoryList(
		boolean translated) {

		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<SuggestionCategory> query = cb.createQuery(SuggestionCategory.class);

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<Bucket, SuggestionCategory> fetch = from.join(Bucket_.suggestionCategories);

			query.select(fetch);


			query.orderBy(cb.desc(fetch.get(SuggestionCategory_.priority)));

			return session
				.createQuery(query)
				.getResultList()
				.chain(categories -> {
						if (translated) {
							return translationService
								.getLocalizedEntities(
									SuggestionCategory.class,
									categories,
									LocalizedSuggestionCategory::new);
						}
						else {
							return Uni
								.createFrom()
								.item(categories);
						}
					}
				);
		});

	}

	private Uni<Language> findDefaultLanguage() {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Language> query = cb.createQuery(Language.class);

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<Bucket, Language> fetch = from.join(Bucket_.defaultLanguage);

			query.select(fetch);


			return session
				.createQuery(query)
				.getSingleResultOrNull();
		});

	}

	private Uni<List<Language>> getAvailableLanguageList() {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Language> query = cb.createQuery(Language.class);

			Root<Bucket> from = query.from(Bucket.class);

			// the inner join keeps only the current bucket, the one bound to
			// the tenant
			from.join(Bucket_.tenantBinding);

			Join<Bucket, Language> fetch = from.join(Bucket_.availableLanguages);

			query.select(fetch);


			return session
				.createQuery(query)
				.getResultList();
		});

	}


	@Inject
	BucketResourceMapper mapper;

	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	TranslationService translationService;
	@Inject
	BucketService bucketService;
	@Inject
	@CacheName("bucket-resource")
	Cache cache;

	public record CurrentBucket(
		boolean refreshOnSuggestionCategory,
		boolean refreshOnTab,
		boolean refreshOnDate,
		boolean refreshOnQuery,
		Bucket.RetrieveType retrieveType
	)
	{}

}
