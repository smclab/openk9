package io.openk9.datasource.client.plugindriver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class PluginDriverDTOList {
	private List<PluginDriverDTO> pluginDriverDTOList;
}
