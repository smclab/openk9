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
	public Class<? extends K9Entity> getType() {
		return SuggestionCategoryField.class;
	}

}
