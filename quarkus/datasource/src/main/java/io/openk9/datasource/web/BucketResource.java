package io.openk9.datasource.web;


import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.sql.TransactionInvoker;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
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


			return session.createQuery(query).getResultList().map(docTypeTemplates -> {

				List<TemplateResponseDto> responseDtos = new ArrayList<>();

				for (DocTypeTemplate docTypeTemplate : docTypeTemplates) {

					TemplateResponseDto templateResponseDto = new TemplateResponseDto();
					templateResponseDto.setName(docTypeTemplate.getName());
					templateResponseDto.setId(docTypeTemplate.getId());

					responseDtos.add(templateResponseDto);
				}

				return responseDtos;

			});

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

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingJoin.get(TenantBinding_.virtualHost),
					virtualhost
				)
			);

			return session.createQuery(query).getResultList().map(tabs -> {

				List<TabResponseDto> tabResponseDtos = new ArrayList<>(tabs.size());

				for (Tab tab : tabs) {
					List<TokenTab> tokenTabs = tab.getTokenTabs();
					List<TokenTabResponseDto> tokenTabResponseDtos = new ArrayList<>(tokenTabs.size());
					for(TokenTab token : tokenTabs){
						TokenTabResponseDto tokenTabResponseDto = new TokenTabResponseDto();
						tokenTabResponseDto.setKeywordKey(token.getDocTypeField().getFieldName());
						tokenTabResponseDto.setTokenType(token.getTokenType());
						tokenTabResponseDto.setFilter(token.getFilter());
						tokenTabResponseDto.setValues(List.of(token.getValue()));
						tokenTabResponseDtos.add(tokenTabResponseDto);
					}

					TabResponseDto tabResponseDto = new TabResponseDto();
					tabResponseDto.setLabel(tab.getName());
					tabResponseDto.setTokens(tokenTabResponseDtos);
					tabResponseDtos.add(tabResponseDto);
				}
				return tabResponseDtos;
			});
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

			return session.createQuery(query).getResultList();
		});

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TabResponseDto {
		private String label;
		private List<TokenTabResponseDto> tokens;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TokenTabResponseDto {
		private String tokenType;
		private String keywordKey;
		private boolean filter;
		private List<String> values;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TemplateResponseDto {
		private String name;
		private Long id;
	}

	@Inject
	TransactionInvoker transactionInvoker;

}
