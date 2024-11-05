package io.openk9.datasource.web;

import io.openk9.auth.tenant.TenantRegistry;
import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.service.SchedulerService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.nullable;

@QuarkusTest
@TestHTTPEndpoint(TriggerWithDateResource.class)
public class TriggerWithDateResourceTest {

	public static final String TESTING_DATE = "2022-03-10T14:32:06.247Z";
	private static final String TESTING_DATASOURCE_ID = "5";
	@InjectMock
	TenantRegistry tenantRegistry;

	@InjectMock
	SchedulerService schedulerService;

	@InjectMock
	SchedulerInitializer schedulerInitializer;

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void should_ingest_date_payload() {

		BDDMockito.given(schedulerService.getStatusByDatasources(notNull()))
				.willReturn( Uni.createFrom().item(() -> {
					var datasourceJobStatuses =
						new ArrayList<SchedulerService.DatasourceJobStatus>();

					datasourceJobStatuses.add(
						new SchedulerService.DatasourceJobStatus(
							0l, SchedulerService.JobStatus.ON_SCHEDULING));

					return datasourceJobStatuses;
				}));

		var date = OffsetDateTime.parse(TESTING_DATE);

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{\n" +
				"  \"datasourceId\": " + TESTING_DATASOURCE_ID + ",\n" +
				"  \"reindex\": true,\n" +
				"  \"startIngestionDate\": \"" + TESTING_DATE + "\"\n" +
				"}")
			.post()
			.then()
			.statusCode(200);

		BDDMockito.then(schedulerInitializer).should()
			.triggerJobs(nullable(String.class), argThat(dto ->
				dto.getStartIngestionDate().equals(date) && dto.getDatasourceIds().size() == 1
					&& dto.getDatasourceIds().getFirst().equals(Long.parseLong(TESTING_DATASOURCE_ID))));
	}
}
