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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "bucket")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NamedQueries({
	@NamedQuery(
		name = Bucket.FETCH_ANNOTATORS_NAMED_QUERY,
		query =
			"from Bucket b " +
				"join fetch b." + Bucket_.TENANT_BINDING  + " tb " +
				"join fetch b." + Bucket_.DATASOURCES + " ds " +
				"join fetch ds." + Datasource_.DATA_INDEX + " di " +
				"join fetch b." + Bucket_.QUERY_ANALYSIS + " qa " +
				"join fetch qa." + QueryAnalysis_.RULES + " qar " +
				"join fetch qa." + QueryAnalysis_.ANNOTATORS + " qaa " +
				"join fetch qaa." + Annotator_.DOC_TYPE_FIELD + " dtf " +
				"left join fetch dtf." + DocTypeField_.PARENT_DOC_TYPE_FIELD + " pdtf " +
				"left join fetch dtf." + DocTypeField_.SUB_DOC_TYPE_FIELDS + " sdtf " +
				"join fetch qa." + QueryAnalysis_.ANNOTATORS + " qaa2 " +
			"where tb." + TenantBinding_.VIRTUAL_HOST + " = :virtualHost " +
			"and (" +
				"qaa." + Annotator_.TYPE + " in " + Annotator.DOCUMENT_TYPE_SET +
				"or qaa2." + Annotator_.TYPE + " not in " + Annotator.DOCUMENT_TYPE_SET +
				" )"
	),
	@NamedQuery(
		name = Bucket.CURRENT_NAMED_QUERY,
		query =
			"select b " +
			"from Bucket b join b." + Bucket_.TENANT_BINDING + " tb " +
			"where tb." + TenantBinding_.VIRTUAL_HOST + " = :virtualHost "
	)
})
public class Bucket extends K9Entity {

	public static final String FETCH_ANNOTATORS_NAMED_QUERY = "Bucket.fetchAnnotators";

	public static final String CURRENT_NAMED_QUERY = "Bucket.current";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "refresh_on_suggestion_category", nullable = false)
	private Boolean refreshOnSuggestionCategory = false;

	@Column(name = "refresh_on_tab", nullable = false)
	private Boolean refreshOnTab = false;

	@Column(name = "refresh_on_date", nullable = false)
	private Boolean refreshOnDate = false;

	@Column(name = "refresh_on_query", nullable = false)
	private Boolean refreshOnQuery = false;

	@ManyToMany(mappedBy = "buckets", cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@ToString.Exclude
	@JsonIgnore
	private Set<Datasource> datasources = new LinkedHashSet<>();

	@OneToMany(mappedBy = "bucket", cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@ToString.Exclude
	@JsonIgnore
	private Set<SuggestionCategory> suggestionCategories
		= new LinkedHashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "query_analysis_id")
	@ToString.Exclude
	private QueryAnalysis queryAnalysis;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "search_config_id")
	@ToString.Exclude
	private SearchConfig searchConfig;

	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "buckets_tabs",
		joinColumns = @JoinColumn(name = "buckets_id"),
		inverseJoinColumns = @JoinColumn(name = "tabs_id"))
	@ToString.Exclude
	@JsonIgnore
	private List<Tab> tabs = new LinkedList<>();

	@OneToOne(mappedBy = "bucket")
	@JsonIgnore
	private TenantBinding tenantBinding;

	@ToString.Exclude
	@ManyToMany(
		cascade = {
			javax.persistence.CascadeType.PERSIST,
			javax.persistence.CascadeType.MERGE,
			javax.persistence.CascadeType.REFRESH,
			javax.persistence.CascadeType.DETACH
		}
	)
	@JoinTable(name = "bucket_language",
		joinColumns = @JoinColumn(name = "bucket_id"),
		inverseJoinColumns = @JoinColumn(name = "language_id"))
	@JsonIgnore
	private Set<Language> availableLanguages = new LinkedHashSet<>();

	@OneToOne
	@JoinColumn(name = "language_id")
	@JsonIgnore
	@ToString.Exclude
	private Language defaultLanguage;

	@Transient
	private boolean enabled = false;

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
		suggestionCategory.setBucket(this);

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
				suggestionCategory.setBucket(null);
				return true;
			}
		}

		return false;

	}

	public boolean addLanguage(
		Collection<Language> languages, Language language) {

		if (language == null || languages.contains(language)) {
			return false;
		}

		languages.add(language);

		return true;

	}

	public boolean removeLanguage(
		Collection<Language> languages, long languageId) {

		Iterator<Language> iterator = languages.iterator();

		while (iterator.hasNext()) {
			Language language = iterator.next();
			if (language.getId() == languageId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

	@PostLoad
	void postLoad() {
		this.enabled = tenantBinding != null;
	}

}