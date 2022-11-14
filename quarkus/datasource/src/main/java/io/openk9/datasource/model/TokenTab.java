package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(name = "token_tab", uniqueConstraints = {
	@UniqueConstraint(
		name = "uc_tokentab_name_tab_id",
		columnNames = {"name", "tab_id"}
	)
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class TokenTab extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "token_type", nullable = false)
	private String tokenType;

	@Column(name = "keyword_key", nullable = false)
	private String keywordKey;

	@Column(name = "value")
	private  String value;

	@Column(name ="filter", nullable = false)
	private Boolean filter;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL)
	@JoinColumn(name = "tab_id")
	@JsonIgnore
	private Tab tab;

	@OneToOne(
		fetch = FetchType.LAZY
	)
	@JoinColumn(name = "doc_type_field_id")
	@JsonIgnore
	@ToString.Exclude
	private DocTypeField docTypeField;
}
