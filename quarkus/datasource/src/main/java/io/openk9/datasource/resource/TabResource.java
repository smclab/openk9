package io.openk9.datasource.resource;

import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.Tenant_;
import io.openk9.datasource.model.TokenTab;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.reactive.mutiny.Mutiny;

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
@Path("/tab")
public class TabResource {

	@Context
	HttpServerRequest request;

	@Path("/getTab-by-virtualhost")
	@GET
	public Uni<List<TabResponseDto>> getTemplates() {

		return getTabList("test.openk9.io");

	}

	private Uni<List<TabResponseDto>> getTabList(String virtualhost) {
		return sf.withTransaction(session -> {

			CriteriaBuilder cb = sf.getCriteriaBuilder();

			CriteriaQuery<Tab> query = cb.createQuery(Tab.class);

			Root<Tenant> from = query.from(Tenant.class);

			Join<Tenant, Tab> fetch =
				from.join(Tenant_.tabs);

			query.select(fetch);

			query.where(cb.equal(from.get(Tenant_.virtualHost), virtualhost));

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
					tabResponseDto.setId(tab.getId());
					tabResponseDto.setTokenTabs(tokenTabResponseDtos);
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
		private Long id;
		private List<TokenTabResponseDto> tokenTabs;
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
	Mutiny.SessionFactory sf;

}

