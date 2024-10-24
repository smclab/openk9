package io.openk9.datasource.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerResourceDTO {
	private List<Long> datasourceIds;
}
