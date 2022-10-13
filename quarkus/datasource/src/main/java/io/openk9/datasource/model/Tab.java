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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "tab")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class Tab extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name="description", length = 4096)
	private String description;

	@Column(name = "priority")
	private Integer priority;

	@OneToMany(
		mappedBy = "tab",
		cascade = javax.persistence.CascadeType.ALL,
		fetch = FetchType.EAGER
	)
	@ToString.Exclude
	@JsonIgnore
	private List<TokenTab> tokenTabs = new LinkedList<>();

	public boolean addTokenTab(
		Collection<TokenTab> tokenTabs, TokenTab tokenTab) {
		if (tokenTabs.add(tokenTab)) {
			tokenTab.setTab(this);
			return true;
		}
		return false;
	}

	public boolean removeTokenTab(
		Collection<TokenTab> tokenTabs, TokenTab tokenTab) {

		if (tokenTabs.remove(tokenTab)) {
			tokenTab.setTab(null);
			return true;
		}

		return false;

	}

	public boolean removeTokenTab(Collection<TokenTab> tokenTabs, long tokenTabId) {

		Iterator<TokenTab> iterator = tokenTabs.iterator();

		while (iterator.hasNext()) {
			TokenTab tokenTab = iterator.next();
			if (tokenTab.getId() == tokenTabId) {
				iterator.remove();
				tokenTab.setTab(null);
				return true;
			}
		}
		return false;

	}


}
