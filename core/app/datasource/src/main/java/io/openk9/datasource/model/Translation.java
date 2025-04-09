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

import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.NaturalId;

import java.util.Objects;

@Entity
@Table(
	indexes = {
		@Index(
			name = "idx_translation_key",
			columnList = "language, class_name, class_pk, key",
			unique = true
		),
		@Index(
			name = "idx_translation_entities",
			columnList = "class_name, class_pk"
		)
	}
)
@Getter
@Setter
public class Translation extends K9Entity {

	@Embedded
	@NaturalId
	private TranslationKey pk;

	@Nationalized
	private String value;

	@Transient
	public String getLanguage() {
		return pk.getLanguage();
	}

	@Transient
	public String getClassName() {
		return pk.getClassName();
	}

	@Transient
	public Long getClassPK() {
		return pk.getClassPK();
	}

	@Transient
	public String getKey() {
		return pk.getKey();
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
		Translation that = (Translation) o;
		return Objects.equals(pk, that.pk);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, pk);
	}
}
