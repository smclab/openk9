package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "token_tab")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TokenTab extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "token_type", nullable = false)
	private TokenType tokenType;

	@Column(name = "value")
	private String value;

	@Column(name ="filter", nullable = false)
	private Boolean filter;

	@OneToOne(
		fetch = FetchType.LAZY
	)
	@JoinColumn(name = "doc_type_field_id")
	@JsonIgnore
	@ToString.Exclude
	private DocTypeField docTypeField;

	@ElementCollection
	@CollectionTable(
		name = "token_tab_extra_params",
		joinColumns = @JoinColumn(name = "token_tab_id")

	)
	@MapKeyColumn(name = "key")
	@Column(name = "value")
	@JsonIgnore
	private Map<String, String> extraParams = new HashMap<>();

	public enum TokenType {
		DATE, DOCTYPE, TEXT, ENTITY, AUTOCOMPLETE, FILTER
	}

	public void addExtraParam(String key, String value) {
		extraParams.put(key, value);
	}

	public void removeExtraParam(String key) {
		extraParams.remove(key);
	}

	public static Set<ExtraParam> getExtraParamsSet(Map<String, String> extraParams) {
		return extraParams
			.entrySet()
			.stream().map(e -> new ExtraParam(e.getKey(), e.getValue()))
			.collect(Collectors.toSet());
	}

	public record ExtraParam(String key, String value) {}
}
