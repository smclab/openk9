package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public abstract class BaseModel extends K9Entity {

	@Column(name = "type")
	private String type;

	@Column(name = "model")
	private String model;
}
