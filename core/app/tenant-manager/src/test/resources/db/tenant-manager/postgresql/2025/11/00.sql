--
-- Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
--

-- an initialization sql script, used to verify the execution of the CustomChange ...

--liquibase formatted sql

-- changeset openk9:insert-some-legacy-tenant

INSERT INTO "tenant" (
	id,
	create_date,
	modified_date,
	schema_name,
	liquibase_schema_name,
	virtual_host,
	client_id,
	realm_name
) VALUES (
	1,
	'2024-11-16:00:00',
	'2024-11-16:00:00',
	'shiny-charmender',
	'shiny-charmender-liquibase',
	'shiny-charmender.openk9.local',
	'shiny-charmender',
	'shiny-charmender'
);

INSERT INTO "tenant" (
	id,
	create_date,
	modified_date,
	schema_name,
	liquibase_schema_name,
	virtual_host,
	client_id,
	realm_name
) VALUES (
	2,
	'2025-11-16:00:00',
	'2025-11-16:00:00',
	'shiny-pikachu',
	'shiny-pikachu-liquibase',
	'shiny-pikachu.openk9.local',
	'shiny-pikachu',
	'shiny-pikachu'
);

