package io.openk9.datasource.web;


import io.openk9.datasource.mapper.BucketResourceMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tab_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.sql.TransactionInvoker;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;

@ApplicationScoped
@Path("/buckets")
public class BucketResource {

	@Context
	HttpServerRequest request;

	@Path("/current/templates")
	@GET
	public Uni<List<TemplateResponseDto>> getTemplates() {
		return getDocTypeTemplateList(request.host());
	}

	@Path("/current/tabs")
	@GET
	public Uni<List<TabResponseDto>> getTabs() {
		return getTabList(request.host());
	}

	@Path("/current/suggestionCategories")
	@GET
	public Uni<List<SuggestionCategory>> getSuggestionCategories() {
		return getSuggestionCategoryList(request.host());
	}

	private Uni<List<TemplateResponseDto>> getDocTypeTemplateList(String virtualhost) {
		return transactionInvoker.withTransaction(session -> {

			CriteriaBuilder cb = transactionInvoker.getCriteriaBuilder();

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
				.setCacheable(true)
				.getResultList()
				.map(mapper::toTemplateResponseDtoList);

		});

	}

	private Uni<List<TabResponseDto>> getTabList(String virtualhost) {
		return transactionInvoker.withTransaction(session -> {

			CriteriaBuilder cb = transactionInvoker.getCriteriaBuilder();

			CriteriaQuery<Tab> query = cb.createQuery(Tab.class);

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingJoin =
				from.join(Bucket_.tenantBinding);

			Join<Bucket, Tab> fetch = from.join(Bucket_.tabs);

			fetch.fetch(Tab_.tokenTabs, JoinType.LEFT);

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

			query.orderBy(cb.desc(fetch.get(Tab_.priority)));

			return session
				.createQuery(query)
				.setCacheable(true)
				.getResultList()
				.map(mapper::toTabResponseDtoList);

		});

	}

	private Uni<List<SuggestionCategory>> getSuggestionCategoryList(String virtualhost) {
		return transactionInvoker.withTransaction(session -> {

			CriteriaBuilder cb = transactionInvoker.getCriteriaBuilder();

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
				.setCacheable(true)
				.getResultList();
		});

	}
	public record TabResponseDto(String label, List<TokenTabResponseDto> tokens) {}

	public record TokenTabResponseDto(
		String tokenType, String keywordKey, boolean filter, List<String> values) {}

	public record TemplateResponseDto(String name, Long id) {}

	@Inject
	BucketResourceMapper mapper;

	@Inject
	TransactionInvoker transactionInvoker;

}
