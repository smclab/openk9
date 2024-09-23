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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "tab")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Tab extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name="description", length = 4096)
	private String description;

	@Column(name = "priority", nullable = false)
	private Integer priority;

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.DETACH,
		jakarta.persistence.CascadeType.REFRESH
	}
	)
	@JoinTable(name = "tab_token_tab",
		joinColumns = @JoinColumn(name = "tab_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "token_tab_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<TokenTab> tokenTabs = new LinkedHashSet<>();

	public boolean removeTokenTab(
		Collection<TokenTab> tokenTabs, long tokenTabId) {

		Iterator<TokenTab> iterator = tokenTabs.iterator();

		while (iterator.hasNext()) {
			TokenTab tokenTab = iterator.next();
			if (tokenTab.getId() == tokenTabId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.DETACH,
		jakarta.persistence.CascadeType.REFRESH
	}
	)
	@JoinTable(name = "tab_sorting",
		joinColumns = @JoinColumn(name = "tab_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "sorting_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<Sorting> sortings = new LinkedHashSet<>();

	public boolean removeSorting(
		Collection<Sorting> sortings, long sortingId) {

		Iterator<Sorting> iterator = sortings.iterator();

		while (iterator.hasNext()) {
			Sorting sorting = iterator.next();
			if (sorting.getId() == sortingId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

}
