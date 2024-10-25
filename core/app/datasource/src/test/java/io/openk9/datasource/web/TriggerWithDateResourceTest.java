package io.openk9.datasource.web;

import io.openk9.datasource.web.dto.TriggerWithDateResourceDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(TriggerWithDateResource.class)
public class TriggerWithDateResourceTest {


	@Test
	void should_ingest_date_payload() {

		given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.body(TriggerWithDateResourceDTO.builder()
				.startIngestionDate(OffsetDateTime.now())
				.reindex(false)
				.build())
			.post()
			.then()
			.statusCode(200);
	}
}
