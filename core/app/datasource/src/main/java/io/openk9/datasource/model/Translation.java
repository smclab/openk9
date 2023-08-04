package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
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
