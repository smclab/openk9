package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.LocalizedEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Delegate;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalizedSuggestionCategory
	extends SuggestionCategory
	implements LocalizedEntity<SuggestionCategory> {

	@JsonIgnore
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
