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
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.EnrichItemDTO;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.init.PluginDrivers;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.TenantInitializerService;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@QuarkusTest
public class DatasourceGrpcTest {

	private static final String SCHEMA_NAME_VALUE = "mew";
	private static final long BUCKET_ID_VALUE = 345L;
	private static final long ENRICH_ITEM_ID_VALUE = 1234124L;
	private static final long PLUGIN_DRIVER_ID_VALUE = 1231389L;
	private static PluginDriver CREATED_PLUGIN_DRIVER;
	private static PluginDriver UPDATED_PLUGIN_DRIVER;
	private static EnrichItem CREATED_ENRICH_ITEM;
	private static EnrichItem UPDATED_ENRICH_ITEM;

	@GrpcClient
	Datasource datasource;

	@InjectMock
	EnrichItemService enrichItemService;
	@InjectMock
	PluginDriverService pluginDriverService;
	@InjectMock
	TenantInitializerService tenantInitializerService;

	@Test
	@RunOnVertxContext
	void should_init_tenant(UniAsserter asserter) {

		BDDMockito.given(tenantInitializerService.createDefault(eq(SCHEMA_NAME_VALUE)))
			.willReturn(Uni.createFrom().item(BUCKET_ID_VALUE));

		asserter.assertThat(
			() -> datasource.initTenant(InitTenantRequest.newBuilder()
				.setSchemaName(SCHEMA_NAME_VALUE)
				.build()
			),
			response -> {
				BDDMockito.then(tenantInitializerService)
					.should(times(1))
					.createDefault(eq(SCHEMA_NAME_VALUE));

				Assertions.assertEquals(
					BUCKET_ID_VALUE,
					response.getBucketId()
				);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_on_init_tenant(UniAsserter asserter) {

		BDDMockito.given(tenantInitializerService.createDefault(eq(SCHEMA_NAME_VALUE)))
			.willReturn(Uni.createFrom().failure(InternalServiceMockException::new));

		asserter.assertFailedWith(
			() -> datasource.initTenant(InitTenantRequest.newBuilder()
				.setSchemaName(SCHEMA_NAME_VALUE)
				.build()
			),
			DatasourceGrpcTest::failureAssertions
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_enrich_item(UniAsserter asserter) {

		var enrichItem = new EnrichItem();
		enrichItem.setId(ENRICH_ITEM_ID_VALUE);
		enrichItem.setName("enrichItemTest");

		BDDMockito.given(enrichItemService.upsert(eq(SCHEMA_NAME_VALUE), any(EnrichItemDTO.class)))
			.willReturn(Uni.createFrom().item(enrichItem));

		asserter.assertThat(
			() -> datasource.createEnrichItem(CreateEnrichItemRequest.newBuilder()
				.setSchemaName(SCHEMA_NAME_VALUE)
				.build()),
			response -> {
				BDDMockito.then(enrichItemService)
					.should(times(1))
					.upsert(eq(SCHEMA_NAME_VALUE), any(EnrichItemDTO.class));

				Assertions.assertEquals(ENRICH_ITEM_ID_VALUE, response.getEnrichItemId());
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_on_create_enrich_item(UniAsserter asserter) {

		BDDMockito.given(enrichItemService.upsert(eq(SCHEMA_NAME_VALUE), any(EnrichItemDTO.class)))
			.willReturn(Uni.createFrom().failure(InternalServiceMockException::new));

		asserter.assertFailedWith(
			() -> datasource.createEnrichItem(CreateEnrichItemRequest.newBuilder()
				.setSchemaName(SCHEMA_NAME_VALUE)
				.build()),
			DatasourceGrpcTest::failureAssertions
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_plugin_driver(UniAsserter asserter) {

		givenUpsertSuccess();

		asserter.assertThat(
			() -> datasource.createPluginDriver(CreatePluginDriverRequest.newBuilder()
				.setSchemaName(SCHEMA_NAME_VALUE)
				.build()),
			response -> {
				BDDMockito.then(pluginDriverService)
					.should(times(1))
					.upsert(eq(SCHEMA_NAME_VALUE), any(PluginDriverDTO.class));

				Assertions.assertEquals(PLUGIN_DRIVER_ID_VALUE, response.getPluginDriverId());
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_on_create_plugin_driver(UniAsserter asserter) {

		BDDMockito.given(pluginDriverService.upsert(
				eq(SCHEMA_NAME_VALUE),
				any(PluginDriverDTO.class)
			))
			.willReturn(Uni.createFrom().failure(InternalServiceMockException::new));


		asserter.assertFailedWith(
			() -> datasource.createPluginDriver(CreatePluginDriverRequest.newBuilder()
				.setSchemaName(SCHEMA_NAME_VALUE)
				.build()),
			DatasourceGrpcTest::failureAssertions
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_preset_plugin_driver(UniAsserter asserter) {
		givenUpsertSuccess();

		asserter.assertThat(
			() -> datasource.createPresetPluginDriver(
				CreatePresetPluginDriverRequest.newBuilder()
					.setSchemaName(SCHEMA_NAME_VALUE)
					.setPreset(Preset.LIFERAY)
					.build()),
			response -> {
				BDDMockito.then(pluginDriverService)
					.should(times(1))
					.upsert(
						eq(SCHEMA_NAME_VALUE),
						eq(PluginDrivers.getPluginDriverDTO(SCHEMA_NAME_VALUE, Preset.LIFERAY))
					);

				Assertions.assertEquals(PLUGIN_DRIVER_ID_VALUE, response.getPluginDriverId());
			}
		);

	}

	private static void failureAssertions(Throwable throwable) {
		Assertions.assertInstanceOf(StatusRuntimeException.class, throwable);

		var exception = (StatusRuntimeException) throwable;

		Assertions.assertTrue(exception
			.getMessage()
			.contains(InternalServiceMockException.class.getName())
		);
	}

	private static PluginDriver getCreatedPluginDriver() {
		if (CREATED_PLUGIN_DRIVER == null) {
			var pluginDriver = new PluginDriver();
			pluginDriver.setId(PLUGIN_DRIVER_ID_VALUE);
			pluginDriver.setName("openk9-foo-parser");
			pluginDriver.setDescription("created");
			CREATED_PLUGIN_DRIVER = pluginDriver;
		}

		return CREATED_PLUGIN_DRIVER;
	}

	private static PluginDriver getUpdatedPluginDriver() {
		if (UPDATED_PLUGIN_DRIVER == null) {
			var pluginDriver = new PluginDriver();
			pluginDriver.setId(PLUGIN_DRIVER_ID_VALUE);
			pluginDriver.setName("openk9-foo-parser");
			pluginDriver.setDescription("updated");
			UPDATED_PLUGIN_DRIVER = pluginDriver;
		}

		return UPDATED_PLUGIN_DRIVER;
	}

	private static EnrichItem getCreatedEnrichItem() {
		if (CREATED_ENRICH_ITEM == null) {
			var enrichItem = new EnrichItem();
			enrichItem.setId(ENRICH_ITEM_ID_VALUE);
			enrichItem.setName("openk9-translator");
			enrichItem.setDescription("created");
			CREATED_ENRICH_ITEM = enrichItem;
		}

		return CREATED_ENRICH_ITEM;
	}

	private static EnrichItem getUpdatedEnrichItem() {
		if (UPDATED_ENRICH_ITEM == null) {
			var enrichItem = new EnrichItem();
			enrichItem.setId(ENRICH_ITEM_ID_VALUE);
			enrichItem.setName("openk9-translator");
			enrichItem.setDescription("updated");
			UPDATED_ENRICH_ITEM = enrichItem;
		}

		return CREATED_ENRICH_ITEM;
	}

	private void givenUpsertSuccess() {
		BDDMockito.given(pluginDriverService.upsert(
				eq(SCHEMA_NAME_VALUE),
				any(PluginDriverDTO.class)
			))
			.willReturn(Uni.createFrom().item(DatasourceGrpcTest::getCreatedPluginDriver));
	}

}
