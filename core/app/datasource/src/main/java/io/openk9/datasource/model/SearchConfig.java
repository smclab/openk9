package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.AllArgsConstructor;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Cacheable
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
		cascade = javax.persistence.CascadeType.ALL,
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



}