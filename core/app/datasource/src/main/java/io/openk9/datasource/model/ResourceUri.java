package io.openk9.datasource.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@RequiredArgsConstructor
public class ResourceUri {

	@Column(name = "baseUri")
	private String baseUri;

	@Column(name = "path")
	private String path;
}
