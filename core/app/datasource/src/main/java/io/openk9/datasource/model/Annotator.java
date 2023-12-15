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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

	@ElementCollection
	@CollectionTable(
		name = "annotator_extra_params",
		joinColumns = @JoinColumn(name = "annotator_id")
	)
	@MapKeyColumn(name = "key")
	@Column(name = "value")
	@JsonIgnore
	private Map<String, String> extraParams = new HashMap<>();

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

	public void addExtraParam(String key, String value) {
		extraParams.put(key, value);
	}

	public void removeExtraParam(String key) {
		extraParams.remove(key);
	}

	public static Set<Annotator.AnnotatorExtraParam> getExtraParamsSet(
		Map<String, String> extraParams) {
		return extraParams
			.entrySet()
			.stream().map(e -> new Annotator.AnnotatorExtraParam(e.getKey(), e.getValue()))
			.collect(Collectors.toSet());
	}

	public record AnnotatorExtraParam(String key, String value) {}

}