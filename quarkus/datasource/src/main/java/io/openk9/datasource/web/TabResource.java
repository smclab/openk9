package io.openk9.datasource.web;

import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Bucket;
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
@Path("/v1/tab")
public class TabResource {

	@Context
	HttpServerRequest request;

	@Path("/get-by-virtualhost")
	@GET
	public Uni<List<TabResponseDto>> getTabs() {

		return getTabList(request.host());

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
						tokenTabResponseDto.setKeywordKey(token.getKeywordKey());
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


	@Inject
	TransactionInvoker transactionInvoker;

}

