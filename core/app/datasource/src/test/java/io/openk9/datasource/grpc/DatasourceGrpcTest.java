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

package io.openk9.datasource.grpc;

import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DatasourceGrpcTest {

	@GrpcClient
	Datasource datasource;

	@Test
	@RunOnVertxContext
	void initTenantSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasource.initTenant(InitTenantRequest.newBuilder()
				.setSchemaName("mew")
				.build()
			),
			response -> Assertions.assertEquals("SUCCESS", response.getStatus())
		);

	}

	@Test
	@RunOnVertxContext
	void initTenantFailure(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> datasource.initTenant(InitTenantRequest.newBuilder()
				.setSchemaName("mew")
				.build()
			),
			throwable -> Assertions.assertInstanceOf(StatusRuntimeException.class, throwable)
		);

	}

	@Test
	@RunOnVertxContext
	void createEnrichItemSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasource.createEnrichItem(CreateEnrichItemRequest.newBuilder()
				.build()),
			response -> Assertions.assertEquals("SUCCESS", response.getStatus())
		);

	}

	@Test
	@RunOnVertxContext
	void createEnrichItemFailuer(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> datasource.createEnrichItem(CreateEnrichItemRequest.newBuilder().build()),
			throwable -> Assertions.assertInstanceOf(StatusRuntimeException.class, throwable)
		);

	}

	@Test
	@RunOnVertxContext
	void createPluginDriverSuccess(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasource.createPluginDriver(CreatePluginDriverRequest.newBuilder().build()),
			response -> Assertions.assertEquals("SUCCESS", response.getStatus())
		);

	}

	@Test
	@RunOnVertxContext
	void createPluginDriverFailure(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> datasource.createPluginDriver(CreatePluginDriverRequest.newBuilder().build()),
			throwable -> Assertions.assertInstanceOf(StatusRuntimeException.class, throwable)
		);

	}

}
