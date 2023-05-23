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
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.SuggestionCategory_;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tab_;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.web.dto.PartialDocTypeFieldDTO;
import io.openk9.datasource.web.dto.TabResponseDTO;
import io.openk9.datasource.web.dto.TemplateResponseDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
@Path("/buckets")
public class BucketResource {

	@Context
	HttpServerRequest request;

	@Path("/current/templates")
	@GET
	public Uni<List<TemplateResponseDTO>> getTemplates() {
		return getDocTypeTemplateList(request.host());
	}

	@Path("/current/tabs")
	@GET
	public Uni<List<TabResponseDTO>> getTabs() {
		return getTabList(request.host());
	}

	@Path("/current/suggestionCategories")
	@GET
	public Uni<List<SuggestionCategory>> getSuggestionCategories() {
		return getSuggestionCategoryList(request.host());
	}

	@Path("/current/doc-type-fields-sortable")
	@GET
	public Uni<List<PartialDocTypeFieldDTO>> getDocTypeFieldsSortable(){
		return getDocTypeFieldsSortableList(request.host());
	}

	@Path("/current")
	@GET
	public Uni<BucketResponse> getCurrentBucket() {
		return getBucket(request.host());
	}

	private Uni<BucketResponse> getBucket(String host) {
		return transactionInvoker.withStatelessTransaction(session -> {

			CriteriaBuilder cb = transactionInvoker.getCriteriaBuilder();

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
		return transactionInvoker.withStatelessTransaction(session -> {

			CriteriaBuilder cb = transactionInvoker.getCriteriaBuilder();

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
				.setCacheable(true)
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

	private Uni<List<TabResponseDTO>> getTabList(String virtualhost) {
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

			query.distinct(true);

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

	@Inject
	BucketResourceMapper mapper;

	@Inject
	TransactionInvoker transactionInvoker;

	public record BucketResponse(boolean handleDynamicFilters) {}

}
