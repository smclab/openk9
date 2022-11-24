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

package io.openk9.entity.manager.config;

import io.quarkus.arc.Unremovable;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Getter
@Setter
@ApplicationScoped
@Unremovable
public class EntityGraphConfig {

	@Inject
	@ConfigProperty(
		name = "openk9.entity.score-threshold",
		defaultValue = "0.8"
	)
	float scoreThreshold;

	@Inject
	@ConfigProperty(
		name = "openk9.entity.unique-entities",
		defaultValue = "date,organization,loc,email,person,document"
	)
	String[] uniqueEntities;

	@Inject
	@ConfigProperty(
		name = "openk9.entity.min-hops",
		defaultValue = "1"
	)
	int minHops;
	@Inject
	@ConfigProperty(
		name = "openk9.entity.max-hops",
		defaultValue = "2"
	)
	int maxHops;

}
