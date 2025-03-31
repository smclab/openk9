package io.openk9.datasource.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;


@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class TriggerWithDateResourceDTO extends TriggerResourceDTO{
	private boolean reindex;
	private OffsetDateTime startIngestionDate;
}
