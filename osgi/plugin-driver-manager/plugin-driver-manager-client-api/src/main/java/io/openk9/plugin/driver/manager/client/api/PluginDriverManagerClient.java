package io.openk9.plugin.driver.manager.client.api;

import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface PluginDriverManagerClient {

	Mono<PluginDriverDTO> getPluginDriver(String serviceDriverName);

	Mono<PluginDriverDTOList> getPluginDriverList(
		Collection<String> serviceDriverNames);

	Mono<PluginDriverDTOList> getPluginDriverList();

}
