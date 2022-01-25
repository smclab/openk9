package io.openk9.datasource.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor(staticName = "of")
public class SuggestionCategoryField extends PanacheEntityBase {
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
}
