package io.openk9.auth.keycloak;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class AuthBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of("auth.topic", Exchange.Type.topic);
	}

	@Override
	public String getRoutingKey() {
		return "KK.EVENT.CLIENT.#";
	}

	@Override
	public String getQueue() {
		return "keycloak-events";
	}

}
