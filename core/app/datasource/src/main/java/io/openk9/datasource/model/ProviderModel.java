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

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.Objects;

@Embeddable
@Data
public class ProviderModel {

	private static final String EMPTY_STRING = "";

	private String provider;
	private String model;

	public ProviderModel() {
		this.model = EMPTY_STRING;
		this.provider = EMPTY_STRING;
	}

	public ProviderModel(String model, String provider) {
		this.model = Objects.requireNonNullElse(model, EMPTY_STRING);
		this.provider = Objects.requireNonNullElse(provider, EMPTY_STRING);
	}

	public void setModel(String model) {
		this.model = Objects.requireNonNullElse(model, EMPTY_STRING);
	}

	public void setProvider(String provider) {
		this.provider = Objects.requireNonNullElse(provider, EMPTY_STRING);
	}
}
