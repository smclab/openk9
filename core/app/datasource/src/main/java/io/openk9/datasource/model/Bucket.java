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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.eclipse.microprofile.graphql.Ignore;

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
			"join fetch b.tenantBinding tb " +
			"join fetch b.datasources ds " +
			"join fetch ds.dataIndex di " +
			"left join fetch ds.pluginDriver pr " +
			"left join fetch pr.aclMappings am " +
			"left join fetch am.docTypeField amdtf " +
			"join fetch b.queryAnalysis qa " +
			"join fetch qa.rules qar " +
			"join fetch qa.annotators qaa " +
			"left join fetch qaa.extraParams extra " +
			"left join fetch qaa.docTypeField dtf " +
			"left join fetch dtf.parentDocTypeField pdtf " +
			"left join fetch dtf.subDocTypeFields sdtf " +
			"where tb.virtualHost = :virtualHost " +
			"and (" +
			"(dtf is not null and qaa.type in ('AGGREGATOR', 'AUTOCOMPLETE', 'AUTOCORRECT', 'KEYWORD_AUTOCOMPLETE')) " +
			"or (dtf is null and qaa.type not in ('AGGREGATOR', 'AUTOCOMPLETE', 'AUTOCORRECT', 'KEYWORD_AUTOCOMPLETE')) " +
			" )"
	),
	@NamedQuery(
		name = Bucket.CURRENT_NAMED_QUERY,
		query =
			"select b " +
			"from Bucket b join b.tenantBinding tb " +
			"where tb.virtualHost = :virtualHost "
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
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	}
	)
	@ToString.Exclude
	@JsonIgnore
	private Set<Datasource> datasources = new LinkedHashSet<>();

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	})
	@JoinTable(name = "buckets_suggestion_categories",
		joinColumns = @JoinColumn(name = "bucket_id"),
		inverseJoinColumns = @JoinColumn(name = "suggestion_category_id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<SuggestionCategory> suggestionCategories
		= new LinkedHashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "query_analysis_id")
	@ToString.Exclude
	@Ignore
	private QueryAnalysis queryAnalysis;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "search_config_id")
	@ToString.Exclude
	@Ignore
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

	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "buckets_sortings",
		joinColumns = @JoinColumn(name = "buckets_id"),
		inverseJoinColumns = @JoinColumn(name = "sortings_id"))
	@ToString.Exclude
	@JsonIgnore
	private List<Sorting> sortings = new LinkedList<>();

	@OneToOne(mappedBy = "bucket")
	@JsonIgnore
	private TenantBinding tenantBinding;

	@ToString.Exclude
	@ManyToMany(
		cascade = {
			jakarta.persistence.CascadeType.PERSIST,
			jakarta.persistence.CascadeType.MERGE,
			jakarta.persistence.CascadeType.REFRESH,
			jakarta.persistence.CascadeType.DETACH
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

	@Enumerated(EnumType.STRING)
	@Column(name = "retrieve_type", nullable = false)
	private RetrieveType retrieveType;

	@Transient
	private boolean enabled = false;

	@OneToOne
	@JoinColumn(name = "rag_configuration_chat_id")
	@JsonIgnore
	private RAGConfiguration ragConfigurationChat;

	@OneToOne
	@JoinColumn(name = "rag_configuration_chat_tool_id")
	@JsonIgnore
	private RAGConfiguration ragConfigurationChatTool;

	@OneToOne
	@JoinColumn(name = "rag_configuration_search_id")
	@JsonIgnore
	private RAGConfiguration ragConfigurationSearch;

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

	public boolean addSuggestionCategory(
		Collection<SuggestionCategory> suggestionCategories,
		SuggestionCategory suggestionCategory) {

		if (suggestionCategory == null || suggestionCategories.contains(suggestionCategory)) {
			return false;
		}

		suggestionCategories.add(suggestionCategory);

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

	public enum RetrieveType {
		KNN,
		HYBRID,
		MATCH
	}

}