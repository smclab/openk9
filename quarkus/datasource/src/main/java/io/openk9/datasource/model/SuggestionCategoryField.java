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

import io.openk9.datasource.listener.K9EntityListener;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(name = "SuggestionCategoryField", uniqueConstraints = {
	@UniqueConstraint(name = "uc_suggestioncategoryfield", columnNames = {
		"fieldName", "tenantId"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor(staticName = "of")
@EntityListeners(K9EntityListener.class)
@Cacheable
public class SuggestionCategoryField extends PanacheEntityBase implements K9Entity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long suggestionCategoryFieldId;
	@Column(nullable = false)
	private Long tenantId;
	@Column(nullable = false)
	private Long categoryId;
	@Column(unique = true)
	private String fieldName;
	private String name;
	@Column(columnDefinition = "boolean default true")
	private boolean enabled;

	public static PanacheQuery<SuggestionCategoryField> findAll() {
		return SuggestionCategoryField.find("enabled",true);
	}

	public static PanacheQuery<SuggestionCategoryField> findAll(Sort sort) {
		return SuggestionCategoryField.find("enabled", sort, true);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		SuggestionCategoryField that = (SuggestionCategoryField) o;
		return suggestionCategoryFieldId != null &&
			   Objects.equals(
				   suggestionCategoryFieldId,
				   that.suggestionCategoryFieldId);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String getPrimaryKey() {
		return suggestionCategoryFieldId.toString();
	}

	@Override
	public Class<? extends K9Entity> getType() {
		return SuggestionCategoryField.class;
	}

}
