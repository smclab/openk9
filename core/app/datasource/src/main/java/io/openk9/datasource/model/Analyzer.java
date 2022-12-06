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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "analyzer")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class Analyzer extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "type")
	private String type;

	@ManyToMany(cascade = {
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinTable(name = "analyzer_token_filter",
		joinColumns = @JoinColumn(name = "analyzer", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "token_filter", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<TokenFilter> tokenFilters = new LinkedHashSet<>();

	@ManyToMany(cascade = {
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinTable(name = "analyzer_char_filter",
		joinColumns = @JoinColumn(name = "analyzer", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "char_filter", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<CharFilter> charFilters = new LinkedHashSet<>();

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.REFRESH,
		javax.persistence.CascadeType.DETACH})
	@JoinColumn(name = "tokenizer")
	@JsonIgnore
	private Tokenizer tokenizer;

	@Lob
	@Column(name="json_config")
	private String jsonConfig;

	public boolean removeTokenFilter(
		Collection<TokenFilter> tokenFilters, long tokenFilterId) {

		Iterator<TokenFilter> iterator = tokenFilters.iterator();

		while (iterator.hasNext()) {
			TokenFilter tokenFilter = iterator.next();
			if (tokenFilter.getId() == tokenFilterId) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	public boolean removeCharFilter(
		Collection<CharFilter> charFilters, long charFilterId) {

		Iterator<CharFilter> iterator = charFilters.iterator();

		while (iterator.hasNext()) {
			CharFilter charFilter = iterator.next();
			if (charFilter.getId() == charFilterId) {
				iterator.remove();
				return true;
			}
		}

		return false;
	}

}
