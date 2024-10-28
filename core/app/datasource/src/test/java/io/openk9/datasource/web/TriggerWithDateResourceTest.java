package io.openk9.datasource.web;

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.auth.tenant.TenantRegistry;
import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.service.SchedulerService;
import io.openk9.datasource.web.dto.TriggerWithDateResourceDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.BDDMockito;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;

@QuarkusTest
@TestHTTPEndpoint(TriggerWithDateResource.class)
public class TriggerWithDateResourceTest {

	public static final String SCHEMA_NAME = "bellossom";
	public static final String REALM_NAME = "bellossom";
	@InjectMock
	TenantRegistry tenantRegistry;

	@InjectMock
	SchedulerService schedulerService;

	@InjectMock
	SchedulerInitializer schedulerInitializer;

	@Test
	@TestSecurity(user = "k9-admin", roles = {"k9-admin"})
	void should_ingest_date_payload() {

		BDDMockito.given(tenantRegistry.getTenantByVirtualHost(anyString()))
				.willReturn(Uni.createFrom().item(
					new TenantManager.Tenant(
						"test",
						SCHEMA_NAME,
						"test",
						"test",
						REALM_NAME)));

		BDDMockito.given(schedulerService.getStatusByDatasources(notNull()))
				.willReturn( Uni.createFrom().item(() -> {
					var datasourceJobStatuses =
						new ArrayList<SchedulerService.DatasourceJobStatus>();

					datasourceJobStatuses.add(
						new SchedulerService.DatasourceJobStatus(
							0l, SchedulerService.JobStatus.ON_SCHEDULING));

					return datasourceJobStatuses;
				}));

		BDDMockito.given(schedulerInitializer.performTask(
			SCHEMA_NAME, 0L, false, OffsetDateTime.now()))
				.willReturn(Uni.createFrom().voidItem());

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body("{\n" +
				"  \"datasourceIds\": [\n" +
				"    0\n" +
				"  ],\n" +
				"  \"reindex\": true,\n" +
				"  \"startIngestionDate\": \"2022-03-10T12:15:50-04:00\"\n" +
				"}")
			.post()
			.then()
			.statusCode(200);
	}
}
