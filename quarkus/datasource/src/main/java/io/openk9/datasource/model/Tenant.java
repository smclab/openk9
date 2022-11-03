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
import io.quarkus.resteasy.reactive.jackson.SecureField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class Tenant extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@ManyToMany(mappedBy = "tenants", cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@ToString.Exclude
	@JsonIgnore
	private Set<Datasource> datasources = new LinkedHashSet<>();

	@Column(name = "virtual_host", nullable = false, unique = true)
	private String virtualHost;

	@Column(name = "client_id")
	@SecureField(rolesAllowed = "admin")
	private String clientId;

	@Column(name = "client_secret")
	@SecureField(rolesAllowed = "admin")
	private String clientSecret;

	@Column(name = "realm_name")
	@SecureField(rolesAllowed = "admin")
	private String realmName;

	@OneToMany(mappedBy = "tenant", cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@ToString.Exclude
	@JsonIgnore
	private Set<SuggestionCategory> suggestionCategories
		= new LinkedHashSet<>();

	@ManyToOne(
		fetch = FetchType.LAZY
	)
	@JoinColumn(name = "query_analysis_id")
	@ToString.Exclude
	private QueryAnalysis queryAnalysis;

	@ManyToOne(
		cascade = CascadeType.ALL,
		fetch = FetchType.LAZY
	)
	@JoinColumn(name = "search_config_id")
	private SearchConfig searchConfig;

	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "tenants_tabs",
		joinColumns = @JoinColumn(name = "tenants_id"),
		inverseJoinColumns = @JoinColumn(name = "tabs_id"))
	@ToString.Exclude
	@JsonIgnore
	private List<Tab> tabs = new LinkedList<>();

	public boolean removeTab(
		Collection<Tab> tabs, long tabId) {

		Iterator<Tab> iterator = tabs.iterator();

		while (iterator.hasNext()) {
			Tab tab = iterator.next();
			if (tab.getId() == tabId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

	public boolean addSuggestionCategory(
		Collection<SuggestionCategory> suggestionCategories,
		SuggestionCategory suggestionCategory) {

		if (suggestionCategory == null || suggestionCategories.contains(suggestionCategory)) {
			return false;
		}

		suggestionCategories.add(suggestionCategory);
		suggestionCategory.setTenant(this);

		return true;

	}

	public boolean removeSuggestionCategory(
		Collection<SuggestionCategory> suggestionCategories,
		long suggestionCategoryId) {
		Iterator<SuggestionCategory> iterator = suggestionCategories.iterator();

		while (iterator.hasNext()) {
			SuggestionCategory suggestionCategory = iterator.next();
			if (suggestionCategory.getId() == suggestionCategoryId) {
				iterator.remove();
				suggestionCategory.setTenant(null);
				return true;
			}
		}

		return false;

	}

}