package io.openk9.datasource.model.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.TranslationKey;

import java.util.Map;

public interface LocalizedEntity<T extends K9Entity> {

	@JsonIgnore
	Class<T> getWrappeeClass();

	Map<String, String> getTranslationMap();
}
