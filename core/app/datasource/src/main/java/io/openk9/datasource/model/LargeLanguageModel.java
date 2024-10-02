/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "large_language_model")
@NamedQuery(
	name = LargeLanguageModel.FETCH_CURRENT,
	query = "select llm from LargeLanguageModel llm join llm.tenantBinding t where t is not null"
)
@Getter
@Setter
public class LargeLanguageModel extends K9Entity {

	public static final String FETCH_CURRENT = "LargeLanguageModel#fetchCurrent";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "api_url")
	private String apiUrl;

	@Column(name = "api_key")
	private String apiKey;

	@Lob
	@JdbcTypeCode(Types.LONGNVARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;

	@OneToOne(mappedBy = "largeLanguageModel")
	@JsonIgnore
	private TenantBinding tenantBinding;

	@Transient
	private boolean enabled = false;

	@PostLoad
	void postLoad() {
		this.enabled = tenantBinding != null;
	}


}
