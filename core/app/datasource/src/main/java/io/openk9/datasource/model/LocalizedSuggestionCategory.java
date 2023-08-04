package io.openk9.datasource.model;

import io.openk9.datasource.model.util.LocalizedEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.experimental.Delegate;

import java.util.Map;

@Data
public class LocalizedSuggestionCategory
	extends SuggestionCategory
	implements LocalizedEntity<SuggestionCategory> {

	@Delegate(excludes = PanacheEntityBase.class)
	private final SuggestionCategory wrappee;
	private final Map<String, String> translationMap;

	@Override
	public Class<SuggestionCategory> getWrappeeClass() {
		return SuggestionCategory.class;
	}

	@Override
	public Map<String, String> getTranslationMap() {
		return translationMap;
	}

}
