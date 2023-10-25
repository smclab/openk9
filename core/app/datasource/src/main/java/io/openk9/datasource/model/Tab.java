package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tab")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Tab extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name="description", length = 4096)
	private String description;

	@Column(name = "priority", nullable = false)
	private Integer priority;

	@ManyToMany(cascade = {
		javax.persistence.CascadeType.PERSIST,
		javax.persistence.CascadeType.MERGE,
		javax.persistence.CascadeType.DETACH,
		javax.persistence.CascadeType.REFRESH})
	@JoinTable(name = "tab_token_tab",
		joinColumns = @JoinColumn(name = "tab_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "token_tab_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	private Set<TokenTab> tokenTabs = new LinkedHashSet<>();

	public boolean removeTokenTab(
		Collection<TokenTab> tokenTabs, long tokenTabId) {

		Iterator<TokenTab> iterator = tokenTabs.iterator();

		while (iterator.hasNext()) {
			TokenTab tokenTab = iterator.next();
			if (tokenTab.getId() == tokenTabId) {
				iterator.remove();
				return true;
			}
		}

		return false;

	}

}
