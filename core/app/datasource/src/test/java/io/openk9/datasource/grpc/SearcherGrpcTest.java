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

import com.google.protobuf.Struct;
import io.grpc.StatusRuntimeException;
import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.projection.BucketLargeLanguageModel;
import io.openk9.datasource.service.LargeLanguageModelService;
import io.openk9.searcher.grpc.GetLLMConfigurationsRequest;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;

@QuarkusTest
public class SearcherGrpcTest {

	private static final String BLANK_STRING = "";
	private static final String VIRTUAL_HOST = BLANK_STRING;
	private static final String LLM_API_KEY = BLANK_STRING;
	private static final String LLM_API_URL = BLANK_STRING;
	private static final String LLM_JSON_CONFIG = "{testField: \"test\"}";
	private static final Struct STRUCT_JSON_CONFIG = StructUtils.fromJson(LLM_JSON_CONFIG);
	private static final String SCHEMA_NAME = BLANK_STRING;
	private static final Bucket BUCKET = new Bucket();
	private static final Bucket BUCKET_RETRIEVE_TYPE_NULL = new Bucket();
	private static final LargeLanguageModel LARGE_LANGUAGE_MODEL = new LargeLanguageModel();

	static {
		BUCKET.setRetrieveType(Bucket.RetrieveType.HYBRID);
		LARGE_LANGUAGE_MODEL.setApiKey(LLM_API_KEY);
		LARGE_LANGUAGE_MODEL.setApiUrl(LLM_API_URL);
		LARGE_LANGUAGE_MODEL.setJsonConfig(LLM_JSON_CONFIG);
	}

	@GrpcClient
	Searcher searcher;

	@InjectMock
	LargeLanguageModelService largeLanguageModelService;
	@InjectMock
	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@Test
	@RunOnVertxContext
	void should_get_llm_configurations(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().item(
				new BucketLargeLanguageModel(BUCKET, LARGE_LANGUAGE_MODEL)));

		asserter.assertThat(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
			response -> {
				BDDMockito.then(largeLanguageModelService)
					.should(times(1))
					.fetchCurrentLLMAndBucket(anyString());

				Assertions.assertEquals(LLM_API_KEY, response.getApiKey());
				Assertions.assertEquals(LLM_API_URL, response.getApiUrl());
				Assertions.assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
				Assertions.assertEquals(
					Bucket.RetrieveType.HYBRID.name(), response.getRetrieveType());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_get_llm_configurations_retrieve_type_null(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().item(
				new BucketLargeLanguageModel(BUCKET_RETRIEVE_TYPE_NULL, LARGE_LANGUAGE_MODEL)));

		asserter.assertThat(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
			response -> {
				BDDMockito.then(largeLanguageModelService)
					.should(times(1))
					.fetchCurrentLLMAndBucket(anyString());

				Assertions.assertEquals(LLM_API_KEY, response.getApiKey());
				Assertions.assertEquals(LLM_API_URL, response.getApiUrl());
				Assertions.assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
				Assertions.assertEquals(BLANK_STRING, response.getRetrieveType());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_fail_on_get_llm_configurations(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().failure(InternalServiceMockException::new));

		asserter.assertFailedWith(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
				SearcherGrpcTest::failureAssertions
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
}
