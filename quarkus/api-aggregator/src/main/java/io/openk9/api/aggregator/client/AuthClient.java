package io.openk9.api.aggregator.client;

import io.openk9.api.aggregator.api.AuthHttp;
import io.openk9.api.aggregator.service.AuthorizationClientHeadersFactoryImpl;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "keycloak-client")
@RegisterClientHeaders(AuthorizationClientHeadersFactoryImpl.class)
public interface AuthClient extends AuthHttp {
}
