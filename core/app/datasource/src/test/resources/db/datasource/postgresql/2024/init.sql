--liquibase formatted sql

-- changeset openk9:sql1

INSERT INTO "tenant_binding" ("id", "create_date", "modified_date", "virtual_host", "tenant_binding_bucket_id", "embedding_model_id", "large_language_model_id") VALUES
(1,	'2023-12-15 11:43:08.946',	'2024-09-30 14:04:11.407626', 'test.openk9.local',	NULL,	NULL,	NULL);

INSERT INTO "enrich_item" ("id", "create_date", "modified_date", "description", "json_config", "name", "service_name", "type", "script", "behavior_merge_type", "json_path", "behavior_on_error", "request_timeout") VALUES
(2,	'2023-12-18 10:06:34.039058',	'2024-09-17 16:20:23.496989',	'',	'{}',	'unreachableservicefail',	'http://localhost:8080',	'HTTP_ASYNC',	'',	'MERGE',	'$',	'FAIL',	1000),
(3,	'2024-10-15 12:22:43.602733',	'2024-10-15 12:39:54.876733',	'',	'{}',	'skip all',	'none',	'GROOVY_SCRIPT',	'[
  "_openk9SkipDocument": true
]',	'MERGE',	'$',	'SKIP',	10000),
(4,	'2023-12-28 14:56:17.194704',	'2024-08-07 10:43:53.409461',	'',	'{}',	'sleepy enrich-item',	'sleepy',	'GROOVY_SCRIPT',	'sleep(5000)
test',	'MERGE',	'$',	'FAIL',	10000);

ALTER SEQUENCE hibernate_sequence RESTART WITH 100;
