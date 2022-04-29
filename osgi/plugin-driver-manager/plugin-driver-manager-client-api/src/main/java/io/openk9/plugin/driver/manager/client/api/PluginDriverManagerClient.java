package io.openk9.plugin.driver.manager.client.api;

import io.openk9.auth.api.UserInfo;
import io.openk9.model.Datasource;
import io.openk9.plugin.driver.manager.model.PluginDriverContextDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SchedulerEnabledDTO;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Date;

public interface PluginDriverManagerClient {

	Mono<Void> invokeDataParser(
		String serviceDriverName, Datasource datasource, Date fromDate,
		Date toDate);

	Mono<SchedulerEnabledDTO> schedulerEnabled(String serviceDriverName);

	Mono<PluginDriverDTO> getPluginDriver(String serviceDriverName);

	Mono<PluginDriverDTOList> getPluginDriverList(
		Collection<String> serviceDriverNames);

	Mono<PluginDriverContextDTO> getPluginDriverContextDTO(
		Collection<String> serviceDriverNames);

	Mono<PluginDriverDTOList> getPluginDriverList();

}
