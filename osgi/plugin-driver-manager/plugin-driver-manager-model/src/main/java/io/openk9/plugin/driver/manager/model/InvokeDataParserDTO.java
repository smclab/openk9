package io.openk9.plugin.driver.manager.model;

import io.openk9.model.Datasource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class InvokeDataParserDTO {
	private String serviceDriverName;
	private Datasource datasource;
	private Date fromDate;
	private Date toDate;
}
