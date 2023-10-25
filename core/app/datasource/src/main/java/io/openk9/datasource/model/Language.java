package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "language")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Language extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "value", length = 4096)
	private String value;
}
