--liquibase formatted sql

-- changeset openk9:init-tenant_binding

INSERT INTO "tenant_binding" (
	id,
	virtual_host
) VALUES (
	1,
	'test.openk9.local'
);

-- in order to test the
INSERT INTO bucket (
    id,
    name,
    description
) VALUES (
    2,
    'Sample Bucket',
    'This is a sample bucket description.'
);

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
