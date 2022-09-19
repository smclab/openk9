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

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "query_analysis")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class QueryAnalysis extends K9Entity {
	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "query_analysis_rules",
		joinColumns = @JoinColumn(name = "query_analysis_id"),
		inverseJoinColumns = @JoinColumn(name = "rules_id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<Rule> rules = new LinkedHashSet<>();


	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "query_analysis_annotators",
		joinColumns = @JoinColumn(name = "query_analysis_id"),
		inverseJoinColumns = @JoinColumn(name = "annotators_id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<Annotator> annotators = new LinkedHashSet<>();

	@ToString.Exclude
	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "query_analysis_stopWords",
		joinColumns = @JoinColumn(name = "queryAnalysis_id"),
		inverseJoinColumns = @JoinColumn(name = "stopWords_id"))
	private Set<StopWord> stopWords = new LinkedHashSet<>();

	public boolean removeRule(
		Set<Rule> rules, long ruleId) {

		Iterator<Rule> iterator = rules.iterator();

		while (iterator.hasNext()) {
			Rule rule = iterator.next();
			if (rule.getId() == ruleId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

}