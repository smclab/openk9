package io.openk9.datasource.web;


import io.openk9.datasource.mapper.BucketResourceMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.LocalizedSuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tab_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.TokenTab_;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.TranslationService;
import io.openk9.datasource.web.dto.PartialDocTypeFieldDTO;
import io.openk9.datasource.web.dto.TabResponseDTO;
import io.openk9.datasource.web.dto.TemplateResponseDTO;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@ApplicationScoped
@Path("/buckets")
public class BucketResource {

	@Context
	HttpServerRequest request;

	@Path("/current/templates")
	@GET
	public Uni<List<TemplateResponseDTO>> getTemplates() {
		return cache
			.get(
				request.host() + "#getTemplates",
				k -> getDocTypeTemplateList(request.host()))
			.flatMap(Function.identity());
	}

	@Path("/current/tabs")
	@GET
	public Uni<List<TabResponseDTO>> getTabs(
			@QueryParam("translated") @DefaultValue("false") boolean translated) {

		return cache
			.get(
				request.host() + "#getTabs" + translated,
				k -> getTabList(request.host(), translated))
			.flatMap(Function.identity());
	}

	@Path("/current/suggestionCategories")
	@GET
	public Uni<List<? extends SuggestionCategory>> getSuggestionCategories(
			@QueryParam("translated") @DefaultValue("false") boolean translated) {

		return cache
			.get(
				request.host() + "#getSuggestionCategories" + translated,
				k -> getSuggestionCategoryList(request.host(), translated))
			.flatMap(Function.identity());
	}

	@Path("/current/doc-type-fields-sortable")
	@GET
	public Uni<List<PartialDocTypeFieldDTO>> getDocTypeFieldsSortable(){
		return cache
			.get(
				request.host() + "#getDocTypeFieldsSortable",
				k -> getDocTypeFieldsSortableList(request.host()))
			.flatMap(Function.identity());
	}

	@Path("/current/defaultLanguage")
	@GET
	public Uni<Language> getDefaultLanguage(){
		return cache
			.get(
				request.host() + "#getDefaultLanguage",
				k -> getDefaultLanguage(request.host()))
			.flatMap(Function.identity());
	}

	@Path("/current/availableLanguage")
	@GET
	public Uni<List<Language>> getAvailableLanguage(){
		return cache
			.get(
				request.host() + "#getAvailableLanguage",
				k -> getAvailableLanguageList(request.host()))
			.flatMap(Function.identity());
	}

	@Path("/current")
	@GET
	public Uni<BucketResponse> getCurrentBucket() {
		return cache
			.get(
				request.host() + "#getCurrentBucket",
				k -> getBucket(request.host()))
			.flatMap(Function.identity());
	}

	private Uni<BucketResponse> getBucket(String host) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Boolean> query = cb.createQuery(Boolean.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingFetch =
				from.join(Bucket_.tenantBinding);

			tenantBindingFetch.on(
				cb.equal(
					tenantBindingFetch.get(TenantBinding_.virtualHost),
					host
				)
			);

			query.select(from.get(Bucket_.handleDynamicFilters));

			return session.createQuery(query)
				.getSingleResult()
				.map(BucketResponse::new);

		});
	}

	private Uni<List<PartialDocTypeFieldDTO>> getDocTypeFieldsSortableList(String virtualhost) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Tuple> query = cb.createTupleQuery();

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingFetch =
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
				cb.and(
					cb.equal(
						tenantBindingFetch.get(
							TenantBinding_.virtualHost),
						virtualhost
					),
					cb.or(
						cb.isTrue(
							parentDocTypeFieldJoin.get(DocTypeField_.sortable)
						),
						cb.isTrue(
							subDocTypeFieldJoin.get(DocTypeField_.sortable)
						)
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
						})
						.distinct()
						.map(PartialDocTypeFieldDTO::of)
						.toList()
				);
		});

	}

	private Uni<List<TemplateResponseDTO>> getDocTypeTemplateList(String virtualhost) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<DocTypeTemplate> query = cb.createQuery(DocTypeTemplate.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingJoin =
				from.join(Bucket_.tenantBinding);

			Join<DocType, DocTypeTemplate> fetch =
				from.join(Bucket_.datasources)
					.join(Datasource_.dataIndex)
					.join(DataIndex_.docTypes)
					.join(DocType_.docTypeTemplate);

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

			return session
				.createQuery(query)
				.getResultList()
				.map(mapper::toTemplateResponseDtoList);

		});

	}

	private Uni<List<TabResponseDTO>> getTabList(String virtualhost, boolean translated) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Tab> query = cb.createQuery(Tab.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingJoin =
				from.join(Bucket_.tenantBinding);

			Join<Bucket, Tab> tabsJoin = from.join(Bucket_.tabs);

			Fetch<Tab, TokenTab> tokenTabFetch = tabsJoin.fetch(Tab_.tokenTabs, JoinType.LEFT);

			tokenTabFetch.fetch(TokenTab_.docTypeField, JoinType.LEFT);

			query.select(tabsJoin);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

			query.orderBy(cb.desc(tabsJoin.get(Tab_.priority)));

			query.distinct(true);

			return session
				.createQuery(query)
				.getResultList()
				.chain(tabs -> {
					if (translated) {
						return translationService
							.getTranslationMaps(
								Tab.class,
								tabs.stream()
									.map(K9Entity::getId)
									.toList())
							.map(maps -> mapper.toTabResponseDtoList(tabs, maps));
					}
					else {
						return Uni
							.createFrom()
							.item(mapper.toTabResponseDtoList(tabs));
					}
				});
		});

	}

	private Uni<List<? extends SuggestionCategory>> getSuggestionCategoryList(String virtualhost, boolean translated) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<SuggestionCategory> query = cb.createQuery(SuggestionCategory.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingJoin =
				from.join(Bucket_.tenantBinding);

			Join<Bucket, SuggestionCategory> fetch = from.join(Bucket_.suggestionCategories);

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

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

	private Uni<Language> getDefaultLanguage(String virtualhost) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Language> query = cb.createQuery(Language.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingJoin =
				from.join(Bucket_.tenantBinding);

			Join<Bucket, Language> fetch = from.join(Bucket_.defaultLanguage);

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

			return session
				.createQuery(query)
				.getSingleResult();
		});

	}

	private Uni<List<Language>> getAvailableLanguageList(String virtualhost) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Language> query = cb.createQuery(Language.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingJoin =
				from.join(Bucket_.tenantBinding);

			Join<Bucket, Language> fetch = from.join(Bucket_.availableLanguages);

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

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
	@CacheName("bucket-resource")
	Cache cache;

	public record BucketResponse(boolean handleDynamicFilters) {}

}
