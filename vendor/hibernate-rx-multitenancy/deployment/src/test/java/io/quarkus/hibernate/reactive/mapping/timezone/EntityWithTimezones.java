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

package io.quarkus.hibernate.reactive.mapping.timezone;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

@Entity
public class EntityWithTimezones {

	public ZonedDateTime zonedDateTime;
	public OffsetDateTime offsetDateTime;
	public OffsetTime offsetTime;
	@Id
	@GeneratedValue
	Long id;

	public EntityWithTimezones() {
	}

	public EntityWithTimezones(
		ZonedDateTime zonedDateTime,
		OffsetDateTime offsetDateTime,
		OffsetTime offsetTime) {
		this.zonedDateTime = zonedDateTime;
		this.offsetDateTime = offsetDateTime;
		this.offsetTime = offsetTime;
	}

}
