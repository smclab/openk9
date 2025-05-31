\connect "tenantmanager";

DO $$
BEGIN
    -- Check if the foreign key constraint exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'databasechangelog'
    ) THEN

        CREATE TABLE IF NOT EXISTS "public"."background_process" (
            "create_date" timestamp,
            "message" text,
            "modified_date" timestamp,
            "status" character varying(255) NOT NULL,
            "id" bigint NOT NULL,
            "process_id" uuid NOT NULL,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "background_process_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."databasechangelog" (
            "id" character varying(255) NOT NULL,
            "author" character varying(255) NOT NULL,
            "filename" character varying(255) NOT NULL,
            "dateexecuted" timestamp NOT NULL,
            "orderexecuted" integer NOT NULL,
            "exectype" character varying(10) NOT NULL,
            "md5sum" character varying(35),
            "description" character varying(255),
            "comments" character varying(255),
            "tag" character varying(255),
            "liquibase" character varying(20),
            "contexts" character varying(255),
            "labels" character varying(255),
            "deployment_id" character varying(10)
        ) WITH (oids = false);

        INSERT INTO "databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES
        ('1669809781403-1',	'openk9',	'db/tenant-manager/2022/11/30-01-changelog.xml',	'2025-04-27 17:14:36.93923',	1,	'EXECUTED',	'8:0f00f195ccd175b7610582d78eb830e6',	'createTable tableName=background_process',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1669809781403-2',	'openk9',	'db/tenant-manager/2022/11/30-01-changelog.xml',	'2025-04-27 17:14:36.950323',	2,	'EXECUTED',	'8:13da9bde8f79ff81d7861976e680790f',	'createTable tableName=tenant',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1669809781403-3',	'openk9',	'db/tenant-manager/2022/11/30-01-changelog.xml',	'2025-04-27 17:14:36.957091',	3,	'EXECUTED',	'8:1c7d83bc33acae2f59e6b8b7a3a43146',	'addUniqueConstraint constraintName=uk_db8nx111rjup8geous5f1gies, tableName=background_process',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1669809781403-4',	'openk9',	'db/tenant-manager/2022/11/30-01-changelog.xml',	'2025-04-27 17:14:36.961921',	4,	'EXECUTED',	'8:2d4c386fe7e92b2f528c635b32c29d8a',	'addUniqueConstraint constraintName=uk_ww2yffcngi9y67p35ifew6sg, tableName=tenant',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1669809781403-5',	'openk9',	'db/tenant-manager/2022/11/30-01-changelog.xml',	'2025-04-27 17:14:36.965164',	5,	'EXECUTED',	'8:9ceb883b9596bb0ffbea0d025c1339ff',	'createSequence sequenceName=hibernate_sequence',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1669809781403-6',	'openk9',	'db/tenant-manager/2022/11/30-01-changelog.xml',	'2025-04-27 17:14:36.969469',	6,	'EXECUTED',	'8:61b51b87dd4fa2c1f5bfece98eee57d3',	'addForeignKeyConstraint baseTableName=background_process, constraintName=fkct0m5s7sbp93ojisdindq56o6, referencedTableName=tenant',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1670942167821-1',	'openk9',	'db/tenant-manager/2022/12/13-01-changelog.xml',	'2025-04-27 17:14:36.980138',	7,	'EXECUTED',	'8:e9ba1037e169abc511cd00c43d67f53e',	'delete tableName=background_process; dropColumn columnName=id, tableName=background_process; addColumn tableName=background_process; addColumn tableName=background_process; dropColumn tableName=background_process; addColumn tableName=background_pr...',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910'),
        ('1673450209846-1',	'openk9',	'db/tenant-manager/2023/01/11-01-changelog.xml',	'2025-04-27 17:14:36.984285',	8,	'EXECUTED',	'8:5be6b201f766f8b115ca207d0be71d4c',	'addColumn tableName=tenant',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5774076910') ON CONFLICT DO NOTHING;

        CREATE TABLE IF NOT EXISTS "public"."databasechangeloglock" (
            "id" integer NOT NULL,
            "locked" boolean NOT NULL,
            "lockgranted" timestamp,
            "lockedby" character varying(255),
            CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        INSERT INTO "databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
        (1,	'0',	NULL,	NULL) ON CONFLICT DO NOTHING;

        CREATE TABLE IF NOT EXISTS "public"."tenant" (
            "id" bigint NOT NULL,
            "client_id" character varying(255) NOT NULL,
            "client_secret" character varying(255),
            "create_date" timestamp,
            "modified_date" timestamp,
            "realm_name" character varying(255) NOT NULL,
            "schema_name" character varying(255) NOT NULL,
            "virtual_host" character varying(255) NOT NULL,
            "liquibase_schema_name" character varying(255) NOT NULL,
            CONSTRAINT "tenant_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX IF NOT EXISTS uk_ww2yffcngi9y67p35ifew6sg ON public.tenant USING btree (virtual_host);

        INSERT INTO "tenant" ("id", "client_id", "client_secret", "create_date", "modified_date", "realm_name", "schema_name", "virtual_host", "liquibase_schema_name") VALUES
        (1,	'openk9',	NULL,	NULL,	NULL,	'openk9',	'openk9',	'demo.openk9.localhost',	'openk9_liquibase') ON CONFLICT DO NOTHING;

        END IF;
END $$;