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
