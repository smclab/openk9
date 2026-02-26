/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.LocalizedEntity;
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
	@Delegate
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
