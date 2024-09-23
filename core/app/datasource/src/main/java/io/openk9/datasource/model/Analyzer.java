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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
@Table(name = "analyzer")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Analyzer extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "type")
	private String type;

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	}
	)
	@JoinTable(name = "analyzer_token_filter",
		joinColumns = @JoinColumn(name = "analyzer", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "token_filter", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<TokenFilter> tokenFilters = new LinkedHashSet<>();

	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	}
	)
	@JoinTable(name = "analyzer_char_filter",
		joinColumns = @JoinColumn(name = "analyzer", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "char_filter", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<CharFilter> charFilters = new LinkedHashSet<>();

	@ToString.Exclude
	@ManyToOne(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.REFRESH,
		jakarta.persistence.CascadeType.DETACH
	}
	)
	@JoinColumn(name = "tokenizer")
	@JsonIgnore
	private Tokenizer tokenizer;

	@Lob
	@Column(name="json_config")
	private String jsonConfig;

	public boolean removeTokenFilter(
		Collection<TokenFilter> tokenFilters, long tokenFilterId) {

		Iterator<TokenFilter> iterator = tokenFilters.iterator();

		while (iterator.hasNext()) {
			TokenFilter tokenFilter = iterator.next();
			if (tokenFilter.getId() == tokenFilterId) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	public boolean removeCharFilter(
		Collection<CharFilter> charFilters, long charFilterId) {

		Iterator<CharFilter> iterator = charFilters.iterator();

		while (iterator.hasNext()) {
			CharFilter charFilter = iterator.next();
			if (charFilter.getId() == charFilterId) {
				iterator.remove();
				return true;
			}
		}

		return false;
	}

}
