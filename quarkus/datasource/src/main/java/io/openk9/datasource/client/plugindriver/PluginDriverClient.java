package io.openk9.datasource.client.plugindriver;

import io.openk9.datasource.client.plugindriver.dto.InvokeDataParserDTO;
import io.openk9.datasource.client.plugindriver.dto.PluginDriverDTO;
import io.openk9.datasource.client.plugindriver.dto.PluginDriverDTOList;
import io.openk9.datasource.client.plugindriver.dto.SchedulerEnabledDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Collection;

@Path("/v1/plugin-driver")
@RegisterRestClient(configKey = "plugin-driver")
public interface PluginDriverClient {

	@POST
	@Path("/invoke-data-parser/")
	void invokeDataParser(InvokeDataParserDTO invokeDataParserDTO);

	@GET
	@Path("/scheduler-enabled/{serviceDriverName}")
	SchedulerEnabledDTO schedulerEnabled(@PathParam String serviceDriverName);

	@GET
	@Path("/{serviceDriverName}")
	PluginDriverDTO getPluginDriver(@PathParam String serviceDriverName);

	@POST
	@Path("/")
	PluginDriverDTOList getPluginDriverList(
		Collection<String> serviceDriverNames);

	@GET
	@Path("/")
	PluginDriverDTOList getPluginDriverList();
	
}
