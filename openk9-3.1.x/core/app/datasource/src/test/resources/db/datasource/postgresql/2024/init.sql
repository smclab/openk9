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

--liquibase formatted sql

-- changeset openk9:init-tenant_binding

INSERT INTO "tenant_binding" (
	id,
	virtual_host
) VALUES (
	1,
	'test.openk9.local'
);

-- test for changeSet with id 1739804793-2
INSERT INTO bucket (
    id,
    name,
    description
) VALUES (
    2,
    'Sample Bucket',
    'This is a sample bucket description.'
);

-- test for changeSet with id 1739804793-2
INSERT INTO suggestion_category (
	id,
    name,
    description,
    priority,
    multi_select,
    bucket_id
) VALUES (
	3,
    'Sample Suggestion category',
    'This is a sample suggestion category description',
    1,
    true,
    2
);

ALTER SEQUENCE hibernate_sequence RESTART WITH 10000;
