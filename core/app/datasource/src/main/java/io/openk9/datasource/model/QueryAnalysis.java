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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "query_analysis")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class QueryAnalysis extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;
	@Column(name = "stopWords")
	@Lob
	@JdbcTypeCode(Types.LONGNVARCHAR)
	private String stopWords;

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


	public List<String> getStopWordsList() {
		List<String> stopWordsList = new LinkedList<>();
		if (stopWords != null) {
			String[] split = stopWords.split(" ");
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