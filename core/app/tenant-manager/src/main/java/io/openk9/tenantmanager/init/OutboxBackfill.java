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

package io.openk9.tenantmanager.init;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.jboss.logging.Logger;

public class OutboxBackfill implements CustomTaskChange {

	@Override
	public void execute(Database database) throws CustomChangeException {
		log.infof(" --- I'm doing something to the database --- ");
	}

	@Override
	public String getConfirmationMessage() {
		return "OutboxEvent table backfilled for Tenants that already exist.";
	}

	@Override
	public void setUp() throws SetupException {
		// nothing to do during setUp
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// nothing to open
	}

	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}

	private static final Logger log = Logger.getLogger(OutboxBackfill.class);
}
