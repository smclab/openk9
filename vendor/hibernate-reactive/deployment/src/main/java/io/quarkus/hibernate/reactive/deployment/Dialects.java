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

package io.quarkus.hibernate.reactive.deployment;

import io.quarkus.datasource.common.runtime.DatabaseKind;
import io.quarkus.hibernate.orm.deployment.spi.DatabaseKindDialectBuildItem;
import io.quarkus.hibernate.orm.runtime.HibernateOrmRuntimeConfig;
import io.quarkus.runtime.configuration.ConfigurationException;

import java.util.List;

/**
 * This used to be the approach before 6bf38240 in the Hibernate ORM extension as well.
 * Align to ORM? TBD
 */
@Deprecated
final class Dialects {

	private Dialects() {
		//utility
	}

	public static String guessDialect(
		String persistenceUnitName, String resolvedDbKind,
		List<DatabaseKindDialectBuildItem> dbKindDialectBuildItems) {
		for (DatabaseKindDialectBuildItem item : dbKindDialectBuildItems) {
			if (DatabaseKind.is(resolvedDbKind, item.getDbKind())) {
				return item.getDialect();
			}
		}

		String error =
			"The Hibernate ORM extension could not guess the dialect from the database kind '" +
			resolvedDbKind
			+ "'. Add an explicit '" + HibernateOrmRuntimeConfig.puPropertyKey(
				persistenceUnitName,
				"dialect"
			)
			+ "' property.";
		throw new ConfigurationException(error);
	}

}
