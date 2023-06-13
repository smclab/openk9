package io.openk9.datasource.model.util;

import io.openk9.datasource.model.ScheduleId;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.UUID;

@Converter
public class ScheduleIdConverter implements AttributeConverter<ScheduleId, String> {

	@Override
	public String convertToDatabaseColumn(ScheduleId attribute) {
		return attribute.getValue();
	}

	@Override
	public ScheduleId convertToEntityAttribute(String dbData) {
		return new ScheduleId(UUID.fromString(dbData));
	}
}
