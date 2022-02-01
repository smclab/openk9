package io.openk9.datasource.model;

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
public class SuggestionCategory extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long suggestionCategoryId;
	@Column(nullable = false)
	private Long tenantId;
	private Long parentCategoryId;
	private String name;
	@Column(columnDefinition = "boolean default true")
	private boolean enabled;

	public static PanacheQuery<SuggestionCategory> findAll() {
		return SuggestionCategory.find("enabled",true);
	}

	public static PanacheQuery<SuggestionCategory> findAll(Sort sort) {
		return SuggestionCategory.find("enabled", sort, true);
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
		SuggestionCategory that = (SuggestionCategory) o;
		return suggestionCategoryId != null &&
			   Objects.equals(
				   suggestionCategoryId,
				   that.suggestionCategoryId);
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
