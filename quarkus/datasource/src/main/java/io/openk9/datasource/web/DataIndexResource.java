package io.openk9.datasource.web;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.processor.indexwriter.IndexerEvents;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@CircuitBreaker
@Path("/v1/data-index")
public class DataIndexResource {

	@Path("/generate-doc-types")
	@POST
	public Uni<Void> autoGenerateDocTypes(AutoGenerateDocTypesRequest request) {

		return sf.withTransaction(session -> {

			CriteriaBuilder cb = sf.getCriteriaBuilder();

			CriteriaQuery<DataIndex> query = cb.createQuery(DataIndex.class);

			Root<Datasource> from = query.from(Datasource.class);

			query.select(from.get(Datasource_.dataIndex));

			query.where(from.get(Datasource_.id).in(request.getDatasourceId()));

			return session
				.createQuery(query)
				.getSingleResult()
				.onItem()
				.transformToUni(dataIndex -> indexerEvents.generateDocTypeFields(dataIndex));

		});

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AutoGenerateDocTypesRequest {
		private long datasourceId;
	}

	@Inject
	Mutiny.SessionFactory sf;

	@Inject
	IndexerEvents indexerEvents;

}
