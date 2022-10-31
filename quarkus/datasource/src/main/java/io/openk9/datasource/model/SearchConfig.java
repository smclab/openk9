package io.openk9.datasource.model;

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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@AllArgsConstructor(staticName = "of")
@Cacheable
public class SearchConfig extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;
	@Column(name = "min_score")
	private Float minScore;
	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "search_config_query_parser_config",
		joinColumns = @JoinColumn(name = "search_config_id"),
		inverseJoinColumns = @JoinColumn(name = "query_parser_configs_id"))
	@ToString.Exclude
	private Set<QueryParserConfig> queryParserConfigs = new LinkedHashSet<>();

	public boolean removeQueryParserConfig(
		Collection<QueryParserConfig> queryParserConfigs, long queryParserConfigId) {

		Iterator<QueryParserConfig> iterator = queryParserConfigs.iterator();

		while (iterator.hasNext()) {
			QueryParserConfig queryParserConfig = iterator.next();
			if (queryParserConfig.getId() == queryParserConfigId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

}