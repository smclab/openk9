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
import io.openk9.datasource.model.util.Fuzziness;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "annotator")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Annotator extends K9Entity {
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "description", length = 4096)
	private String description;
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private AnnotatorType type;
	@Enumerated(EnumType.STRING)
	@Column(name = "fuziness")
	private Fuzziness fuziness;
	@Column(name = "size")
	private Integer size;
	@OneToOne(
		fetch = FetchType.LAZY
	)
	@JoinColumn(name = "doc_type_field_id")
	@JsonIgnore
	@ToString.Exclude
	private DocTypeField docTypeField;
	@Column(name = "field_name", nullable = false)
	private String fieldName;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		Annotator annotator = (Annotator) o;
		return id != null && Objects.equals(id, annotator.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public static final String DOCUMENT_TYPE_SET =
		"('AGGREGATOR', 'AUTOCOMPLETE', 'AUTOCORRECT', 'KEYWORD_AUTOCOMPLETE')";

}