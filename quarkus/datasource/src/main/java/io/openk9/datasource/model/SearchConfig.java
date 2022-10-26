package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "search_config")
public class SearchConfig extends K9Entity {

	@Column(name = "min_score")
	private Float minScore;

	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "search_config_query_parser_config",
		joinColumns = @JoinColumn(name = "search_config_id"),
		inverseJoinColumns = @JoinColumn(name = "query_parser_configs_id"))
	private Set<QueryParserConfig> queryParserConfigs = new LinkedHashSet<>();

	public Set<QueryParserConfig> getQueryParserConfigs() {
		return queryParserConfigs;
	}

	public void setQueryParserConfigs(
		Set<QueryParserConfig> queryParserConfigs) {
		this.queryParserConfigs = queryParserConfigs;
	}

	public Float getMinScore() {
		return minScore;
	}

	public void setMinScore(Float minScore) {
		this.minScore = minScore;
	}
}