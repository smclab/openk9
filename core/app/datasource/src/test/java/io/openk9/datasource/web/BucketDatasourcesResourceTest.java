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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestHTTPEndpoint(BucketResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BucketDatasourcesResourceTest {

	private static final String PREFIX = "BucketDatasourcesResourceTest - ";
	private static final String BUCKET_NAME = PREFIX + "Bucket";
	private static final String DATASOURCE_ONE_NAME = PREFIX + "Datasource 1";
	private static final String DATASOURCE_TWO_NAME = PREFIX + "Datasource 2";
	private static final String DATASOURCE_UNLINKED_NAME = PREFIX + "Datasource unlinked";

	// virtual host seeded on the singleton TenantBinding (see init.sql)
	private static final String VIRTUAL_HOST = "test.openk9.local";

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	@Order(1)
	void setup() {
		// 1. Create three datasources: two to link, one to leave unlinked
		createDatasource(DATASOURCE_ONE_NAME);
		createDatasource(DATASOURCE_TWO_NAME);
		createDatasource(DATASOURCE_UNLINKED_NAME);

		// 2. Create the bucket and link only the first two datasources
		EntitiesUtils.createBucket(BUCKET_NAME, bucketService, sessionFactory);

		var bucketId = getBucket().getId();
		addDatasource(bucketId, DATASOURCE_ONE_NAME);
		addDatasource(bucketId, DATASOURCE_TWO_NAME);

		// 3. Bind the bucket to the tenant so it becomes the active bucket
		//    resolved by virtual host test.openk9.local
		bucketService.enableTenant(bucketId)
			.await()
			.indefinitely();
	}

	@Test
	@Order(2)
	void should_return_only_active_bucket_datasources() {
		// call the endpoint resolving the active bucket by the Host header
		given()
			.header("Host", VIRTUAL_HOST)
			.accept(ContentType.JSON)
			.when()
			.get("current/datasources")
			.then()
			.statusCode(200)
			// only the two linked datasources are returned, with id and name;
			// the unlinked datasource is absent
			.body("size()", is(2))
			.body("name", containsInAnyOrder(
				DATASOURCE_ONE_NAME, DATASOURCE_TWO_NAME))
			.body("id", everyItem(notNullValue()));
	}

	@Test
	@Order(3)
	void tearDown() {
		var bucketId = getBucket().getId();

		// unlink the datasources before deleting them (FK on the join table)
		bucketService.removeDatasource(bucketId, getDatasourceId(DATASOURCE_ONE_NAME))
			.await()
			.indefinitely();
		bucketService.removeDatasource(bucketId, getDatasourceId(DATASOURCE_TWO_NAME))
			.await()
			.indefinitely();

		// delete the datasources (the bucket still holds the tenant binding,
		// so it is left in place and reused by other tests)
		EntitiesUtils.removeEntity(
			DATASOURCE_ONE_NAME, datasourceService, sessionFactory);
		EntitiesUtils.removeEntity(
			DATASOURCE_TWO_NAME, datasourceService, sessionFactory);
		EntitiesUtils.removeEntity(
			DATASOURCE_UNLINKED_NAME, datasourceService, sessionFactory);
	}

	private void createDatasource(String name) {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(name)
			.schedulable(false)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.reindexable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.build();

		EntitiesUtils.createEntity(dto, datasourceService, sessionFactory);
	}

	private void addDatasource(long bucketId, String datasourceName) {
		bucketService.addDatasource(bucketId, getDatasourceId(datasourceName))
			.await()
			.indefinitely();
	}

	private Bucket getBucket() {
		return EntitiesUtils.getEntity(BUCKET_NAME, bucketService, sessionFactory);
	}

	private long getDatasourceId(String name) {
		return EntitiesUtils
			.getEntity(name, datasourceService, sessionFactory)
			.getId();
	}

}
