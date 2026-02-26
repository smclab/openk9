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
import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "search_config")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class SearchConfig extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;
	@Column(name = "min_score")
	private Float minScore;
	@Column(name = "min_score_suggestions", nullable = false)
	private boolean minScoreSuggestions = false;
	@Column(name = "min_score_search", nullable = false)
	private boolean minScoreSearch = false;

	@OneToMany(
		mappedBy = "searchConfig",
		cascade = jakarta.persistence.CascadeType.ALL,
		fetch = FetchType.LAZY
	)
	@ToString.Exclude
	@JsonIgnore
	private Set<QueryParserConfig> queryParserConfigs = new LinkedHashSet<>();

	public boolean addQueryParserConfig(
		Collection<QueryParserConfig> queryParserConfigs, QueryParserConfig queryParserConfig) {
		if (queryParserConfigs.add(queryParserConfig)) {
			queryParserConfig.setSearchConfig(this);
			return true;
		}
		return false;
	}

	public boolean removeQueryParserConfig(
		Collection<QueryParserConfig> queryParserConfigs, QueryParserConfig queryParserConfig) {

		if (queryParserConfigs.remove(queryParserConfig)) {
			queryParserConfig.setSearchConfig(null);
			return true;
		}

		return false;

	}

	public boolean removeQueryParserConfig(Collection<QueryParserConfig> queryParserConfigs, long queryParserConfigId) {

		Iterator<QueryParserConfig> iterator = queryParserConfigs.iterator();

		while (iterator.hasNext()) {
			QueryParserConfig queryParserConfig = iterator.next();
			if (queryParserConfig.getId() == queryParserConfigId) {
				iterator.remove();
				queryParserConfig.setSearchConfig(null);
				return true;
			}
		}
		return false;
	}

	public void removeAllQueryParserConfig(Collection<QueryParserConfig> queryParserConfigs) {

		queryParserConfigs.stream()
			.forEach(queryParserConfig -> queryParserConfig.setSearchConfig(null));

		queryParserConfigs.clear();
	}

}