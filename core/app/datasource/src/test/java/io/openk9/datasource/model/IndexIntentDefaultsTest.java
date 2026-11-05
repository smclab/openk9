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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IndexIntentDefaultsTest {

	@Test
	void datasourceSettingsNullFallsBackToEmptyObject() {
		var datasource = new Datasource();

		datasource.setSettings("{\"number_of_shards\":2}");
		datasource.setSettings(null);

		assertEquals("{}", datasource.getSettings());
	}

	@Test
	void newDatasourceHasEmptySettings() {
		assertEquals("{}", new Datasource().getSettings());
	}

	@Test
	void dataIndexKnnIndexNullFallsBackToFalse() {
		var dataIndex = new DataIndex();

		dataIndex.setKnnIndex(null);

		assertFalse(dataIndex.getKnnIndex());
	}

}
