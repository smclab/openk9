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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

	@Column(name = "name", unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "token_type")
	private String tokenType;

	@Column(name = "keyword_key")
	private String keywordKey;

	@Column(name = "value")
	private  String value;

	@Column(name ="filter")
	private Boolean filter;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = javax.persistence.CascadeType.ALL, optional = false)
	@JoinColumn(name = "tab_id", nullable = false)
	@JsonIgnore
	private Tab tab;
}
