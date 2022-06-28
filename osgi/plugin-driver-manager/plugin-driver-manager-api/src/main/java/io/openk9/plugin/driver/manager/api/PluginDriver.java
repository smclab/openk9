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

package io.openk9.plugin.driver.manager.api;

import io.openk9.model.Datasource;
import org.reactivestreams.Publisher;

import java.util.Date;

public interface PluginDriver {

	String getName();

	Publisher<Void> invokeDataParser(
		Datasource datasource, Date fromDate, Date toDate, String scheduleId);

	boolean schedulerEnabled();

	default String getDriverServiceName() {
		return this.getClass().getName();
	}

}