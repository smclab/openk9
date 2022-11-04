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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "query_analysis")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class QueryAnalysis extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;
	@Column(name = "stopWords")
	@Lob
	private String stopWords;

	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "query_analysis_rules",
		joinColumns = @JoinColumn(name = "query_analysis_id"),
		inverseJoinColumns = @JoinColumn(name = "rules_id"))
	@ToString.Exclude
	@JsonIgnore
	private List<Rule> rules = new LinkedList<>();


	@ManyToMany(cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
		CascadeType.DETACH})
	@JoinTable(name = "query_analysis_annotators",
		joinColumns = @JoinColumn(name = "query_analysis_id"),
		inverseJoinColumns = @JoinColumn(name = "annotators_id"))
	@ToString.Exclude
	@JsonIgnore
	private List<Annotator> annotators = new LinkedList<>();


	public List<String> getStopWordsList() {
		List<String> stopWordsList = new LinkedList<>();
		if (stopWords != null) {
			String[] split = stopWords.split(",");
			for (String s : split) {
				stopWordsList.add(s.strip());
			}
		}
		return stopWordsList;
	}

	public boolean removeRule(
		Collection<Rule> rules, long ruleId) {

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

    public boolean removeAnnotators(
			Collection<Annotator> annotators, long annotatorId) {

			Iterator<Annotator> iterator = annotators.iterator();

			while (iterator.hasNext()) {
				Annotator annotator = iterator.next();
				if (annotator.getId() == annotatorId) {
					iterator.remove();
					return true;
				}
			}

			return false;


		}
}