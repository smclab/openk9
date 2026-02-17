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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class TranslationKey implements Serializable {

	@Column(name = "translation_language")
	private String language;
	@Column(name = "class_name")
	private String className;
	@Column(name = "class_pk")
	private Long classPK;
	@Column(name = "translation_key")
	private String key;

	public TranslationKey() {
	}

	public TranslationKey(String language, String className, Long classPK, String key) {
		this.language = language;
		this.className = className;
		this.classPK = classPK;
		this.key = key;
	}

	@Override
	public String toString() {
		return key + "." + language;
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
		TranslationKey that = (TranslationKey) o;
		return Objects.equals(language, that.language)
			&& Objects.equals(className, that.className)
			&& Objects.equals(classPK, that.classPK)
			&& Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(language, className, classPK, key);
	}

}
