package io.openk9.datasource.client.plugindriver;

import io.openk9.datasource.client.plugindriver.dto.InvokeDataParserDTO;
import io.openk9.datasource.client.plugindriver.dto.PluginDriverDTO;
import io.openk9.datasource.client.plugindriver.dto.PluginDriverDTOList;
import io.openk9.datasource.client.plugindriver.dto.SchedulerEnabledDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collection;

@Path("/v1/plugin-driver")
@RegisterRestClient(configKey = "plugin-driver")
public interface PluginDriverClient {

	@POST
	@Path("/invoke-data-parser/")
	Uni<Void> invokeDataParser(InvokeDataParserDTO invokeDataParserDTO);

	@GET
	@Path("/scheduler-enabled/{serviceDriverName}")
	Uni<SchedulerEnabledDTO> schedulerEnabled(@PathParam("serviceDriverName") String serviceDriverName);

	@GET
	@Path("/{serviceDriverName}")
	Uni<PluginDriverDTO> getPluginDriver(@PathParam("serviceDriverName") String serviceDriverName);

	@POST
	@Path("/")
	Uni<PluginDriverDTOList> getPluginDriverList(
		Collection<String> serviceDriverNames);

	@GET
	@Path("/")
	Uni<PluginDriverDTOList> getPluginDriverList();
	
}
