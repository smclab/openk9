--liquibase formatted sql

-- changeset openk9:init-tenant_binding

INSERT INTO "tenant_binding" ("id", "create_date", "modified_date", "virtual_host", "tenant_binding_bucket_id", "embedding_model_id", "large_language_model_id") VALUES
(1,	'2024-11-26 00:00:00.000',	'2024-11-26 00:00:00.000', 'test.openk9.local',	NULL,	NULL,	NULL);

ALTER SEQUENCE hibernate_sequence RESTART WITH 100;
