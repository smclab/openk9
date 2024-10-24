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

package io.openk9.tenantmanager.resource;

import com.google.protobuf.Empty;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.ApplyResponse;
import io.openk9.datasource.grpc.CreatePluginDriverResponse;
import io.openk9.datasource.grpc.Datasource;
import io.openk9.tenantmanager.provisioning.plugindriver.CreateConnectorSaga;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@QuarkusTest
@TestHTTPEndpoint(ProvisioningResource.class)
@TestSecurity(user = "k9-admin", roles = {"admin"})
public class ProvisioningResourceTest {

	@InjectMock
	@GrpcClient("appmanager")
	AppManager appManager;

	@InjectMock
	@GrpcClient("datasource")
	Datasource datasource;

	@Test
	void should_create_connector() {

		givenSagaHappyPath();

		doPostAndExpect2xx(givenValidRequest())
			.and()
			.body("result", is(CreateConnectorSaga.Responses.SUCCESS.name()));

		thenVerifySagaHappyPath();

	}

	@Test
	void should_fail_with_error() {

		givenSagaErrorPath();

		doPostAndExpect2xx(givenValidRequest())
			.and()
			.body("result", is(CreateConnectorSaga.Responses.ERROR.name()));

		thenVerifySagaErrorPath();

	}

	@Test
	void should_compensate() {

		givenSagaCompensatePath();

		doPostAndExpect2xx(givenValidRequest())
			.and()
			.body("result", is(CreateConnectorSaga.Responses.COMPENSATION.name()));

		thenVerifySagaCompensatePath();

	}

	@Test
	void should_not_compensate() {

		givenSagaCompensateErrorPath();

		doPostAndExpect2xx(givenValidRequest())
			.and()
			.body("result", is(CreateConnectorSaga.Responses.COMPENSATION_ERROR.name()));

		thenVerifySagaCompensatePath();

	}

	@Test
	void should_not_start_saga() {

		givenSagaHappyPath();

		doPostAndExpect4xx(givenInvalidRequest());

		thenVerifySagaNotStarted();

	}

	private static RequestSpecification givenValidRequest() {
		return given()
			.contentType(ContentType.JSON)
			.body(Constants.VALID_JSON_BODY);
	}

	private static RequestSpecification givenInvalidRequest() {
		return given()
			.contentType(ContentType.JSON)
			.body(Constants.INVALID_JSON_BODY);
	}


	private ValidatableResponse doPostAndExpect2xx(RequestSpecification requestSpecification) {
		return requestSpecification
			.when()
			.post(Constants.CREATE_CONNECTOR_PATH)
			.then()
			.statusCode(200);
	}

	private ValidatableResponse doPostAndExpect4xx(RequestSpecification requestSpecification) {
		return requestSpecification
			.when()
			.post(Constants.CREATE_CONNECTOR_PATH)
			.then()
			.statusCode(400);
	}

	private void givenSagaHappyPath() {
		applyResourceSuccess();
		createPluginDriverSuccess();
	}

	private void givenSagaErrorPath() {
		BDDMockito.given(appManager.applyResource(any()))
			.willReturn(Uni.createFrom().failure(RuntimeException::new));
	}

	private void givenSagaCompensatePath() {
		applyResourceSuccess();
		createPluginDriverFailure();
		deleteResourceSuccess();
	}

	private void givenSagaCompensateErrorPath() {
		applyResourceSuccess();
		createPluginDriverFailure();
		deleteResourceFailure();
	}

	private void thenVerifySagaHappyPath() {
		applyResourceVerify();
		createPluginDriverVerify();
	}

	private void thenVerifySagaErrorPath() {
		applyResourceVerify();
		BDDMockito.then(datasource).shouldHaveNoInteractions();
	}

	private void thenVerifySagaCompensatePath() {
		applyResourceVerify();
		createPluginDriverVerify();
		deleteResourceVerify();
	}

	private void thenVerifySagaNotStarted() {
		BDDMockito.then(appManager).shouldHaveNoInteractions();
		BDDMockito.then(datasource).shouldHaveNoInteractions();
	}

	private void applyResourceSuccess() {
		BDDMockito.given(appManager.applyResource(any()))
			.willReturn(Uni.createFrom().item(ApplyResponse.newBuilder()
				.setStatus("done")
				.build()));
	}

	private void deleteResourceSuccess() {
		BDDMockito.given(appManager.deleteResource(any()))
			.willReturn(Uni.createFrom().item(Empty.newBuilder().build()));
	}

	private void deleteResourceFailure() {
		BDDMockito.given(appManager.deleteResource(any()))
			.willReturn(Uni.createFrom().failure(RuntimeException::new));
	}

	private void createPluginDriverSuccess() {
		BDDMockito.given(datasource.createPresetPluginDriver(any()))
			.willReturn(Uni.createFrom().item(CreatePluginDriverResponse.newBuilder()
				.setPluginDriverId(1001L)
				.build()));
	}

	private void createPluginDriverFailure() {
		BDDMockito.given(datasource.createPresetPluginDriver(any()))
			.willReturn(Uni.createFrom().failure(RuntimeException::new));
	}

	private void createPluginDriverVerify() {
		BDDMockito.then(datasource)
			.should(times(1))
			.createPresetPluginDriver(eq(Constants.CREATE_PRESET_REQUEST));
	}

	private void applyResourceVerify() {
		BDDMockito.then(appManager)
			.should(times(1))
			.applyResource(eq(Constants.APP_MANIFEST));
	}

	private void deleteResourceVerify() {
		BDDMockito.then(appManager)
			.should(times(1))
			.deleteResource(eq(Constants.APP_MANIFEST));
	}


}