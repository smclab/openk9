\connect "openk9";

CREATE SCHEMA IF NOT EXISTS "openk9";

CREATE SCHEMA IF NOT EXISTS "openk9_liquibase";

DO $$
BEGIN
    -- Check if the foreign key constraint exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'openk9_liquibase'
          AND table_name = 'databasechangelog'
    ) THEN

        CREATE TABLE "openk9_liquibase"."databasechangelog" (
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
        )
        WITH (oids = false);

        INSERT INTO "openk9_liquibase"."databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES
        ('1669810381617-1',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.121831',	1,	'EXECUTED',	'9:faa3378b5433ac6a604030202cfc0b20',	'createTable tableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-2',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.135089',	2,	'EXECUTED',	'9:9e851e0b909187452e926498d2d4df93',	'createTable tableName=token_tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-3',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.146023',	3,	'EXECUTED',	'9:f727415e9b53e4362fc752a513a55adb',	'createTable tableName=query_analysis',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-4',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.15698',	4,	'EXECUTED',	'9:4d476a572bfeb94ec0067c20683814fc',	'createTable tableName=doc_type',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-5',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.166093',	5,	'EXECUTED',	'9:29ba0f599107b3910f39210259c1f174',	'createTable tableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-6',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.176537',	6,	'EXECUTED',	'9:7160407447da9f3114aa9b1706cbbb45',	'createTable tableName=suggestion_category',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-7',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.188034',	7,	'EXECUTED',	'9:f3c8a38f5aa872de2c5b5300d9f72e97',	'createTable tableName=search_config',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-8',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.197648',	8,	'EXECUTED',	'9:80a52e266c91e28cb40d1e6279bc8349',	'createTable tableName=tenant_binding',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-9',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.211497',	9,	'EXECUTED',	'9:0827036e549a32c2041a6cb167bf7e91',	'createTable tableName=annotator',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-10',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.222561',	10,	'EXECUTED',	'9:e7d91afb972bd69d2b46f0f6120fdd43',	'createTable tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-11',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.232075',	11,	'EXECUTED',	'9:361c0c48198030e42f1ef8c6b2104484',	'createTable tableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-12',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.242503',	12,	'EXECUTED',	'9:52e46c58f1654b66d771287ee18dcb78',	'createTable tableName=rule',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-13',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.253017',	13,	'EXECUTED',	'9:e366d1f156c38b4a8fb5d9c45a9cccd7',	'createTable tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-14',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.260881',	14,	'EXECUTED',	'9:d71a541d8283e37a7eafe864a0822147',	'createTable tableName=char_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-15',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.271465',	15,	'EXECUTED',	'9:3fd98540d7a285ee8d2354110a990f47',	'createTable tableName=enrich_pipeline',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-16',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.28081',	16,	'EXECUTED',	'9:587c6d0763ed6f516e24b715686ca632',	'createTable tableName=token_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-17',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.292901',	17,	'EXECUTED',	'9:316c8462428db3d80e5d9eaea07170c6',	'createTable tableName=plugin_driver',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-18',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.302754',	18,	'EXECUTED',	'9:f6ecc49b7ead59348be8aee896326fe6',	'createTable tableName=tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-19',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.312368',	19,	'EXECUTED',	'9:588e16888f4f06834a9424afa52f3036',	'createTable tableName=tokenizer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-20',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.320568',	20,	'EXECUTED',	'9:e4b94be0f673f64db0d1cce924801296',	'createTable tableName=doc_type_template',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-21',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.330812',	21,	'EXECUTED',	'9:cbab35979f398905f6b1b236af6816d6',	'createTable tableName=analyzer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-22',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.339067',	22,	'EXECUTED',	'9:130c0575f5847dff9d552d32ff2001ee',	'addUniqueConstraint constraintName=field_name_doc_type_id_parent_doc_type_field_id, tableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-23',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.34533',	23,	'EXECUTED',	'9:48faa3b4389bb1340a177b2dc1b4810d',	'addUniqueConstraint constraintName=uc_tokentab_name_tab_id, tableName=token_tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-24',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.352226',	24,	'EXECUTED',	'9:f0ed0dd64d9dc284644c9dad73d96be1',	'addUniqueConstraint constraintName=uk_712f8fp1ftw1ug66hrmkgphrf, tableName=token_tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-25',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.357464',	25,	'EXECUTED',	'9:e7efee61619065c5df961c7e0af99ee8',	'addUniqueConstraint constraintName=uk_26k3dv00807y6dy0x82xx8mjd, tableName=query_analysis',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-26',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.367689',	26,	'EXECUTED',	'9:fe6ebf007e5c03e262dc5e21873a8ec7',	'addUniqueConstraint constraintName=uk_2l8a7vqh0i6r6tb8cb9j6yf6n, tableName=doc_type',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-27',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.373332',	27,	'EXECUTED',	'9:5343cc41b9d403a5d1b3f6083a6b743f',	'addUniqueConstraint constraintName=uk_30eqiwulrtffhilqri8poy0e9, tableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-28',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.380781',	28,	'EXECUTED',	'9:3abdabdf5dd7aa08f468b7570052ac8b',	'addUniqueConstraint constraintName=uk_4c591r8ch4g8rilsgfvb32kgn, tableName=suggestion_category',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-29',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.387063',	29,	'EXECUTED',	'9:3bf2360e7f1876db50b836afc434303a',	'addUniqueConstraint constraintName=uk_5abkod761uq37sv3j2p4weaf0, tableName=search_config',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-30',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.394118',	30,	'EXECUTED',	'9:631b985689e887964268c8b53c6a4367',	'addUniqueConstraint constraintName=uk_6wrs6ncojuw0wde7a5jw2wjh1, tableName=tenant_binding',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-31',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.40004',	31,	'EXECUTED',	'9:c816a03583ce6d0937d2c9e32c2be6e6',	'addUniqueConstraint constraintName=uk_9318c5hvkjhrqjlq8rd3jkhe9, tableName=annotator',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-32',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.40561',	32,	'EXECUTED',	'9:9d17a06a704301b29f87063154360b18',	'addUniqueConstraint constraintName=uk_c270jcqlmvthpcasvusahxb7h, tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-33',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.412172',	33,	'EXECUTED',	'9:e61c850e7790f1bc450ddce6edd65210',	'addUniqueConstraint constraintName=uk_ecnihy9mdplwt30pbahqcv9fw, tableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('add-column-analyzer-table',	'openk9',	'db/datasource/2022/12/06-01-changelog.xml',	'2025-06-17 11:58:35.667876',	89,	'EXECUTED',	'9:ad4ce609de2f5fbe545aa366c1d81978',	'addColumn tableName=analyzer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-34',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.421906',	34,	'EXECUTED',	'9:e2575886f155fcbf6a4673ad07de6084',	'addUniqueConstraint constraintName=uk_g0aibm7vybna15mqfxis5nnf1, tableName=rule',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-35',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.427634',	35,	'EXECUTED',	'9:be20ffe9ab77e06a213c0d2b085f2c83',	'addUniqueConstraint constraintName=uk_he0xrer6rh4dgaalutt7prhbm, tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-36',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.434098',	36,	'EXECUTED',	'9:f3c77db0cbcf4c43d4f9beea25ab01df',	'addUniqueConstraint constraintName=uk_hgk1crqb2kqnbkad5mr76v4v0, tableName=char_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-37',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.44097',	37,	'EXECUTED',	'9:a625d4382552a5f997ba77243a3c5390',	'addUniqueConstraint constraintName=uk_hgxv753bx3d7mwghpktnxkk8h, tableName=enrich_pipeline',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-38',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.446705',	38,	'EXECUTED',	'9:fc5d64df9d8a6cab666ff4e42ec71310',	'addUniqueConstraint constraintName=uk_mbf4ldxsh6umx4bx0kgcwrmg2, tableName=token_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-39',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.453229',	39,	'EXECUTED',	'9:6123e3e29378cc61053613eb2214fe9a',	'addUniqueConstraint constraintName=uk_o3hjnq90nnkxc3y34jumnhyg9, tableName=plugin_driver',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-40',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.45905',	40,	'EXECUTED',	'9:8c44da57644db955ec15d27079d4212d',	'addUniqueConstraint constraintName=uk_r17opdmucm7ij4aveppoa8wep, tableName=tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-41',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.467222',	41,	'EXECUTED',	'9:5e886775d5cb9577e6d4d84189ecad90',	'addUniqueConstraint constraintName=uk_re72pk1pijmchwmusxdil83sy, tableName=tokenizer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-42',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.472498',	42,	'EXECUTED',	'9:c22821ac62eab9963051d884d064d365',	'addUniqueConstraint constraintName=uk_t7tmy5syhhbjxbfktw4l3ejdf, tableName=doc_type_template',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-43',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.477459',	43,	'EXECUTED',	'9:f040cd31f0cb7e1ed874673e19cf7147',	'addUniqueConstraint constraintName=uk_tb9s0i7disnbabdksial8ic43, tableName=analyzer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-44',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.481116',	44,	'EXECUTED',	'9:65670d9ff694684fd67cc3239c69c523',	'createSequence sequenceName=hibernate_sequence',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-45',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.488052',	45,	'EXECUTED',	'9:25f17ca4ca3e4d75f91407d0f319b58e',	'createTable tableName=analyzer_char_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-46',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.493245',	46,	'EXECUTED',	'9:1312b66cd02f0d7c56889b446eca87af',	'createTable tableName=analyzer_token_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-47',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.496489',	47,	'EXECUTED',	'9:fcb98ff685ef79599d8b5818f04a3309',	'createTable tableName=buckets_tabs',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-48',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.503054',	48,	'EXECUTED',	'9:ce6a5954fc0f2bb1de7d9e3efd5d3c04',	'createTable tableName=data_index_doc_types',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-49',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.508624',	49,	'EXECUTED',	'9:a657b9ecad238512e3bf8a871cb910de',	'createTable tableName=datasource_buckets',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-50',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.513975',	50,	'EXECUTED',	'9:c15604bcba52a4739cb3c5f0a4ab22f6',	'createTable tableName=enrich_pipeline_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-51',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.521091',	51,	'EXECUTED',	'9:9734ca233d8411e4c5c7b870e3b08243',	'createTable tableName=query_analysis_annotators',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-52',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.526596',	52,	'EXECUTED',	'9:39a830da05ff5a653e66787df066100c',	'createTable tableName=query_analysis_rules',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-53',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.535345',	53,	'EXECUTED',	'9:2ba14db6bfe92a666149c7e66b461265',	'createTable tableName=query_parser_config',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-54',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.540069',	54,	'EXECUTED',	'9:638b6215faaff7bce56ac639d904d3df',	'createTable tableName=suggestion_category_doc_type_fields',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-55',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.543784',	55,	'EXECUTED',	'9:ada7083b8d6aba9ad5569e85392883fe',	'addForeignKeyConstraint baseTableName=data_index_doc_types, constraintName=fk1a9m9bg9q7ni7gc9ysg3rq6fp, referencedTableName=doc_type',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-56',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.547567',	56,	'EXECUTED',	'9:402a946ef72ef55c72c95314634a7aed',	'addForeignKeyConstraint baseTableName=doc_type, constraintName=fk1ocwbmim5560h0bysfpunyq46, referencedTableName=doc_type_template',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-57',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.551602',	57,	'EXECUTED',	'9:cf8392d6643364c776e92b153ca8fa1d',	'addForeignKeyConstraint baseTableName=token_tab, constraintName=fk37w5m3swa2cebgar0umrnytyy, referencedTableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-58',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.554924',	58,	'EXECUTED',	'9:fd528c6b016ad12e49c35136af227117',	'addForeignKeyConstraint baseTableName=suggestion_category, constraintName=fk3svyhailyqjc0iofjqpqymm78, referencedTableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-59',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.558066',	59,	'EXECUTED',	'9:e6e1eec921578759e47945798bcc0ba8',	'addForeignKeyConstraint baseTableName=doc_type_field, constraintName=fk44qu7fg8d1tl3bn4d43sge02x, referencedTableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-60',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.561181',	60,	'EXECUTED',	'9:2f135c4423f0dc96113e975f88acd594',	'addForeignKeyConstraint baseTableName=query_analysis_rules, constraintName=fk5412heryog6cetwbsq5enp335, referencedTableName=query_analysis',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-61',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.565414',	61,	'EXECUTED',	'9:a6e2b282a5d6aa8851ec25a32d24db3e',	'addForeignKeyConstraint baseTableName=datasource_buckets, constraintName=fk80gj884mrv2t03t7g36qlrae9, referencedTableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-62',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.569586',	62,	'EXECUTED',	'9:ad6b466d0268a3757c91209e2505dfa4',	'addForeignKeyConstraint baseTableName=analyzer_token_filter, constraintName=fk84vkkc91bhjv3ye2l19iimcl8, referencedTableName=token_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-63',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.573173',	63,	'EXECUTED',	'9:e660484a4f3c95d89899bd84d8b9e2a3',	'addForeignKeyConstraint baseTableName=analyzer_token_filter, constraintName=fk8knl2njy74t871j2eycwu5fql, referencedTableName=analyzer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-64',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.576127',	64,	'EXECUTED',	'9:69eb495ebaa6544c111ededf32cbdfae',	'addForeignKeyConstraint baseTableName=data_index_doc_types, constraintName=fk9tu5iup1pex2vpk80jftsfbey, referencedTableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-65',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.57941',	65,	'EXECUTED',	'9:77cd42dd1e8b6345250a40281676466c',	'addForeignKeyConstraint baseTableName=query_analysis_annotators, constraintName=fka1ext4cmdvvc0pjxgci94xvvn, referencedTableName=query_analysis',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-66',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.583197',	66,	'EXECUTED',	'9:c01495d9cddbffea16bdc03623d2f828',	'addForeignKeyConstraint baseTableName=analyzer, constraintName=fkc7nh3nt723kkehd09dl3jpcmw, referencedTableName=tokenizer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-67',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.586293',	67,	'EXECUTED',	'9:ae177e82d0fc97e9b12d3cc8027e9bf9',	'addForeignKeyConstraint baseTableName=datasource, constraintName=fkewjpbr7f4fkff02y19op5g6a, referencedTableName=enrich_pipeline',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-68',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.589128',	68,	'EXECUTED',	'9:fc2f12e72ef2160a6536379f6971f999',	'addForeignKeyConstraint baseTableName=datasource, constraintName=fkeyxlmk7fbek60xtie38cwu2v2, referencedTableName=plugin_driver',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-69',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.592125',	69,	'EXECUTED',	'9:e26fbcb93e2341e1cf7be49b4dad294a',	'addForeignKeyConstraint baseTableName=annotator, constraintName=fkfiv3bsnm26bb802cm7i1ei4q2, referencedTableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-70',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.594975',	70,	'EXECUTED',	'9:f7eb53860484225af76526d2118eeda8',	'addForeignKeyConstraint baseTableName=analyzer_char_filter, constraintName=fkfjki6k33j86nxxjtpjfsjk0bg, referencedTableName=analyzer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-71',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.598083',	71,	'EXECUTED',	'9:f0bf11db8b03ebb838b2f09dce199949',	'addForeignKeyConstraint baseTableName=doc_type_field, constraintName=fkfjuk779hqxyt5ngdk9asog0bj, referencedTableName=analyzer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-72',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.601227',	72,	'EXECUTED',	'9:56dc94893a6859bb664e9299c1d4e2bd',	'addForeignKeyConstraint baseTableName=bucket, constraintName=fkgj8f2hdk19shy1gwqu3u4ae9x, referencedTableName=query_analysis',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-73',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.605567',	73,	'EXECUTED',	'9:0272b57bae278928b9a071a031b45848',	'addForeignKeyConstraint baseTableName=buckets_tabs, constraintName=fki0729hs5mp7hisvr1xc4kcnio, referencedTableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-74',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.608527',	74,	'EXECUTED',	'9:95073b68955b9f29bb8fbeb5ca7eae64',	'addForeignKeyConstraint baseTableName=tenant_binding, constraintName=fki7bn20nxw6x541wfvvwe62t28, referencedTableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-75',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.611461',	75,	'EXECUTED',	'9:1feb15d3a163b5d141abbfff4aa0f901',	'addForeignKeyConstraint baseTableName=datasource_buckets, constraintName=fki9t38w4aa24jenr7lp4uguxb2, referencedTableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-76',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.614897',	76,	'EXECUTED',	'9:4c1fb7d9be2798bda0f7bbf1053cfd5a',	'addForeignKeyConstraint baseTableName=analyzer_char_filter, constraintName=fki9uan06v9eosrqrj0d2ab839l, referencedTableName=char_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-77',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.618566',	77,	'EXECUTED',	'9:b84434d2d80ffbda6d3bd30aadcc7db3',	'addForeignKeyConstraint baseTableName=token_tab, constraintName=fkiyk7xhe4lx9pbr64vv2yit6jd, referencedTableName=tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-78',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.621418',	78,	'EXECUTED',	'9:2b4aea72ad13ab1d7797d4981b8cda97',	'addForeignKeyConstraint baseTableName=query_analysis_annotators, constraintName=fkj6bkvkacmj53kv5o5k8065hvt, referencedTableName=annotator',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-79',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.624175',	79,	'EXECUTED',	'9:c221858bfeb89916819e14d9ff0ff3f4',	'addForeignKeyConstraint baseTableName=buckets_tabs, constraintName=fkkivu1a4ehytvoy78u74r3vq3l, referencedTableName=tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-80',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.62692',	80,	'EXECUTED',	'9:e644b7022fe8ff59980a613765111036',	'addForeignKeyConstraint baseTableName=suggestion_category_doc_type_fields, constraintName=fklfdkxdh7p8pkjn7qtwa2qeckc, referencedTableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-81',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.629686',	81,	'EXECUTED',	'9:2ae7ad75bc0a2a7fd1f1a921d41f000c',	'addForeignKeyConstraint baseTableName=suggestion_category_doc_type_fields, constraintName=fkm97oc2r4h2fi5lym1g5f8pkwj, referencedTableName=suggestion_category',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-82',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.633322',	82,	'EXECUTED',	'9:637f414959e7cf86eff1ae9657fae946',	'addForeignKeyConstraint baseTableName=query_analysis_rules, constraintName=fkmbqqghon1m8h554uh8hi0qkru, referencedTableName=rule',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-83',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.636523',	83,	'EXECUTED',	'9:1221836823601b5ac9391ed7c28a75a1',	'addForeignKeyConstraint baseTableName=enrich_pipeline_item, constraintName=fkmjidj39ui867w3ig9iccnmrxb, referencedTableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-84',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.639473',	84,	'EXECUTED',	'9:a110b01510b71de38fd08dee29f12dc5',	'addForeignKeyConstraint baseTableName=doc_type_field, constraintName=fkn1p060bcao42e9fn7l1mb1jju, referencedTableName=doc_type',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-85',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.643848',	85,	'EXECUTED',	'9:de560fda98aac74562022c49ea7a219a',	'addForeignKeyConstraint baseTableName=bucket, constraintName=fkn9yvqd7qsenqu5unq6yfj7j2a, referencedTableName=search_config',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-86',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.65014',	86,	'EXECUTED',	'9:80e188b7cfbd4a591b6903a5f0e6ce95',	'addForeignKeyConstraint baseTableName=datasource, constraintName=fknbh6wdfjww9no5xqxn65qchfy, referencedTableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-87',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.656989',	87,	'EXECUTED',	'9:c7d89d9d217bfc1195aa8be901ad4ad9',	'addForeignKeyConstraint baseTableName=enrich_pipeline_item, constraintName=fkreyxykt1gox8ghwcuobr5pjan, referencedTableName=enrich_pipeline',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1669810381617-88',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-06-17 11:58:35.663147',	88,	'EXECUTED',	'9:73f578ba8b9137703a4dd7f50822d721',	'addForeignKeyConstraint baseTableName=query_parser_config, constraintName=fktmj4jra4g99dajgs7ilywk41b, referencedTableName=search_config',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1671629306048-1',	'openk9',	'db/datasource/2022/12/21-01-changelog.xml',	'2025-06-17 11:58:35.678712',	90,	'EXECUTED',	'9:5249836ae4b802d07574540be30b346d',	'createTable tableName=acl_mapping',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1671629306048-2',	'openk9',	'db/datasource/2022/12/21-01-changelog.xml',	'2025-06-17 11:58:35.683178',	91,	'EXECUTED',	'9:e19aafdc504b08c8be4a0b9cadc4ecdb',	'addForeignKeyConstraint baseTableName=acl_mapping, constraintName=FK_ACL_MAPPING_ON_DOC_TYPE_FIELD, referencedTableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1671629306048-3',	'openk9',	'db/datasource/2022/12/21-01-changelog.xml',	'2025-06-17 11:58:35.686524',	92,	'EXECUTED',	'9:9fec44133097daf90c9572068e7a3f7c',	'addForeignKeyConstraint baseTableName=acl_mapping, constraintName=FK_ACL_MAPPING_ON_PLUGIN_DRIVER, referencedTableName=plugin_driver',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1673543649792-1',	'openk9',	'db/datasource/2023/01/12-01-changelog.xml',	'2025-06-17 11:58:35.693904',	93,	'EXECUTED',	'9:b75641d29d4ccccb3bf538e085c48246',	'createTable tableName=file_resource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1673543649792-2',	'openk9',	'db/datasource/2023/01/12-01-changelog.xml',	'2025-06-17 11:58:35.700235',	94,	'EXECUTED',	'9:cec2d0071996c6c3bfed0a68a3b7ed94',	'addUniqueConstraint constraintName=uc_fileresource_fileid_datasource_id, tableName=file_resource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1673543649792-3',	'openk9',	'db/datasource/2023/01/12-01-changelog.xml',	'2025-06-17 11:58:35.707994',	95,	'EXECUTED',	'9:b0312593e174e6934fab306e8d0230e0',	'addUniqueConstraint constraintName=uc_fileresource_resource_id, tableName=file_resource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1674745590142-1',	'openk9',	'db/datasource/2023/01/26-01-changelog.xml',	'2025-06-17 11:58:35.711832',	96,	'EXECUTED',	'9:a9d55defcd3b41022733a4118549d394',	'addColumn tableName=suggestion_category',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1675070559412-1',	'openk9',	'db/datasource/2023/01/30-01-changelog.xml',	'2025-06-17 11:58:35.716818',	97,	'EXECUTED',	'9:eaecb62d9bceab7ed9ead697c13a116a',	'addColumn tableName=search_config',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1677676535139-1',	'openk9',	'db/datasource/2023/03/01-01-changelog.xml',	'2025-06-17 11:58:35.721978',	98,	'EXECUTED',	'9:65fcad4f789c31f24ef17e6621062887',	'addColumn tableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1677778990419-1',	'openk9',	'db/datasource/2023/03/02-01-changelog.xml',	'2025-06-17 11:58:35.724826',	99,	'EXECUTED',	'9:ebd64ff518bfa8701bf307708c2846b7',	'update tableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1677831817838-1',	'openk9',	'db/datasource/2023/03/03-01-changelog.xml',	'2025-06-17 11:58:35.728611',	100,	'EXECUTED',	'9:3d3c0c81de692e88f103d58473f4285a',	'addColumn tableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1678121122655-1',	'openk9',	'db/datasource/2023/03/06-01-changelog.xml',	'2025-06-17 11:58:35.732733',	101,	'EXECUTED',	'9:f2dd9910a6fc00d685cc39b7428a51ac',	'renameColumn newColumnName=sortable, oldColumnName=sorteable, tableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1679590501751-1',	'openk9',	'db/datasource/2023/03/23-01-changelog.xml',	'2025-06-17 11:58:35.737956',	102,	'EXECUTED',	'9:cb4f308a4af0f33dc090ea8ef3f1576c',	'addColumn tableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1680539051683-1',	'openk9',	'db/datasource/2023/04/03-01-changelog.xml',	'2025-06-17 11:58:35.741298',	103,	'EXECUTED',	'9:2ff4dec03193bbd5dbd99eb946597ada',	'renameColumn newColumnName=script, oldColumnName=validation_script, tableName=enrich_item',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1682504012273-1',	'openk9',	'db/datasource/2023/04/26-01-changelog.xml',	'2025-06-17 11:58:35.757499',	104,	'EXECUTED',	'9:bf7238cff100594076da6c49d0a0184d',	'createTable tableName=tab_token_tab; sql; dropForeignKeyConstraint baseTableName=token_tab, constraintName=fkiyk7xhe4lx9pbr64vv2yit6jd; dropColumn columnName=tab_id, tableName=token_tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1682504012273-2',	'openk9',	'db/datasource/2023/04/26-01-changelog.xml',	'2025-06-17 11:58:35.760775',	105,	'EXECUTED',	'9:f41415094e28642e8d74b9c7d2d2feee',	'addForeignKeyConstraint baseTableName=tab_token_tab, constraintName=fk_tab_token_tab, referencedTableName=tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1682504012273-3',	'openk9',	'db/datasource/2023/04/26-01-changelog.xml',	'2025-06-17 11:58:35.764526',	106,	'EXECUTED',	'9:98b0d083250dd54c99a5613028475567',	'addForeignKeyConstraint baseTableName=tab_token_tab, constraintName=fk_token_tab_tab, referencedTableName=token_tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1682524504592-1',	'openk9',	'db/datasource/2023/04/26-02-changelog.xml',	'2025-06-17 11:58:35.768637',	107,	'EXECUTED',	'9:5e7d3380168ccda6a6c9b883d1aeef57',	'addColumn tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686583762945-1',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-06-17 11:58:35.771375',	108,	'EXECUTED',	'9:f12ca2c964088d6a81765df60d42da9c',	'addColumn tableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686583762945-2',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-06-17 11:58:35.773919',	109,	'EXECUTED',	'9:edef2d4f933c55ef52bbeb8932833f62',	'sql',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686583762945-3',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-06-17 11:58:35.776843',	110,	'EXECUTED',	'9:680a47a555d591b144e554fa4417e31b',	'sql',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686583762945-4',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-06-17 11:58:35.779308',	111,	'EXECUTED',	'9:58335a72c50861f719bc82f898549c5f',	'sql',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686583762945-5',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-06-17 11:58:35.784974',	112,	'EXECUTED',	'9:d32e1d6ebde2419196cbad31eb0a8389',	'addNotNullConstraint columnName=datasource_id, tableName=data_index; addForeignKeyConstraint baseTableName=data_index, constraintName=FK_data_index_datasource, referencedTableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686670878042-1',	'openk9',	'db/datasource/2023/06/13-01-changelog.xml',	'2025-06-17 11:58:35.797217',	113,	'EXECUTED',	'9:6f1b34cf0b5f0a6653978a0ff57dc25f',	'createTable tableName=scheduler',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686670878042-2',	'openk9',	'db/datasource/2023/06/13-01-changelog.xml',	'2025-06-17 11:58:35.805199',	114,	'EXECUTED',	'9:b2bd8874a2f13eb91885641f9c4b716b',	'addForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_datasource, referencedTableName=datasource; addForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_old_data_index, referencedTableName=data_index; a...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1686824425790-1',	'openk9',	'db/datasource/2023/06/15-01-changelog.xml',	'2025-06-17 11:58:35.808373',	115,	'EXECUTED',	'9:f96846f44eda8bdf7e2fa3291ca3a788',	'dropNotNullConstraint columnName=new_data_index_id, tableName=scheduler',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1688720936322-1',	'openk9',	'db/datasource/2023/07/07-01-changelog.xml',	'2025-06-17 11:58:35.812089',	116,	'EXECUTED',	'9:5149ef17998c7498b5299ce3ef65c34e',	'addColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1688630335734-1',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-06-17 11:58:35.820156',	117,	'EXECUTED',	'9:dff13be967b5354efa51c8d3a5e844b6',	'createTable tableName=language',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1688630335734-2',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-06-17 11:58:35.82476',	118,	'EXECUTED',	'9:7b319e690e15795fe830874a107ecd14',	'addUniqueConstraint constraintName=0998a292-02a9-4930-a8ba-e31d7af5cee2, tableName=language',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1688630335734-3',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-06-17 11:58:35.831148',	119,	'EXECUTED',	'9:495c31e090d3e281a5df6acfd7bb5a65',	'createTable tableName=bucket_language',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1688630335734-4',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-06-17 11:58:35.83462',	120,	'EXECUTED',	'9:7d630805403c1c32e6568641951741b4',	'addColumn tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1688630335734-5',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-06-17 11:58:35.837746',	121,	'EXECUTED',	'9:bcba90f083e223850149f7e6450eeb10',	'addForeignKeyConstraint baseTableName=bucket_language, constraintName=313a9cab-a687-486d-92ae-2ce74fa89951, referencedTableName=language',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1689667487108-1',	'openk9',	'db/datasource/2023/07/18-01-changelog.xml',	'2025-06-17 11:58:35.840461',	122,	'EXECUTED',	'9:5ba74901495b95445659072f9c2b5e5e',	'addColumn tableName=tokenizer',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1689667487108-2',	'openk9',	'db/datasource/2023/07/18-01-changelog.xml',	'2025-06-17 11:58:35.843354',	123,	'EXECUTED',	'9:f69f3501aa59fa7424c33d3f9d545d66',	'addColumn tableName=token_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1689667487108-3',	'openk9',	'db/datasource/2023/07/18-01-changelog.xml',	'2025-06-17 11:58:35.846806',	124,	'EXECUTED',	'9:fc3fa24c2233038f3d5a5fa25afe5f60',	'addColumn tableName=char_filter',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1690464708841-1',	'openk9',	'db/datasource/2023/07/29-01-changelog.xml',	'2025-06-17 11:58:35.851268',	125,	'EXECUTED',	'9:27bcb9b5ff0600a653edd5a533f70637',	'dropForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_old_data_index; dropForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_new_data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1691144835465-1',	'openk9',	'db/datasource/2023/08/04-01-changelog.xml',	'2025-06-17 11:58:35.863732',	126,	'EXECUTED',	'9:f70bf798334a52c8222a723c41a8c5fc',	'createTable tableName=translation',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1691144835465-2',	'openk9',	'db/datasource/2023/08/04-01-changelog.xml',	'2025-06-17 11:58:35.87165',	127,	'EXECUTED',	'9:5812e3db3c9119734598f7cc088c5056',	'createIndex indexName=idx_translation_key, tableName=translation; createIndex indexName=idx_translation_entities, tableName=translation',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1693469697309-1',	'openk9',	'db/datasource/2023/08/31-01-changelog.xml',	'2025-06-17 11:58:35.874833',	128,	'EXECUTED',	'9:e8dae26809ec8ff4f88a61e8ea025d84',	'dropIndex indexName=idx_translation_entities, tableName=translation',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1693469697309-2',	'openk9',	'db/datasource/2023/08/31-01-changelog.xml',	'2025-06-17 11:58:35.880447',	129,	'EXECUTED',	'9:2a56335bb4fcd3ad658d472c99c5e6c2',	'createIndex indexName=idx_translation_entities, tableName=translation',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1699525032881-1',	'openk9',	'db/datasource/2023/11/09-01-changelog.xml',	'2025-06-17 11:58:35.886669',	130,	'EXECUTED',	'9:b4ca8cf25ef0af75c9c1e7b5e72e6af2',	'createTable tableName=token_tab_extra_params',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1699525032881-2',	'openk9',	'db/datasource/2023/11/09-01-changelog.xml',	'2025-06-17 11:58:35.891022',	131,	'EXECUTED',	'9:0217e56ebfc671c85c3b404efae5079d',	'createIndex indexName=idx_token_tab_id_extra_params, tableName=token_tab_extra_params',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1699629168661-1',	'openk9',	'db/datasource/2023/11/10-01-changelog.xml',	'2025-06-17 11:58:35.893624',	132,	'EXECUTED',	'9:0ef49ea19b620331f9c14f8087512f7d',	'renameColumn newColumnName=refresh_on_suggestion_category, oldColumnName=handle_dynamic_filters, tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1699629168661-2',	'openk9',	'db/datasource/2023/11/10-01-changelog.xml',	'2025-06-17 11:58:35.898165',	133,	'EXECUTED',	'9:9e58fc46fec568c930b95f3aed47fdfa',	'addColumn tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1700143191681-1',	'openk9',	'db/datasource/2023/11/16-01-changelog.xml',	'2025-06-17 11:58:35.901324',	134,	'EXECUTED',	'9:848f5ccb3c21d040425a69a699f0f08e',	'addColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1700143191681-2',	'openk9',	'db/datasource/2023/11/16-01-changelog.xml',	'2025-06-17 11:58:35.903218',	135,	'EXECUTED',	'9:7f2664223aa2c71c0897fb89cd445247',	'update tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1700143191681-3',	'openk9',	'db/datasource/2023/11/16-01-changelog.xml',	'2025-06-17 11:58:35.906719',	136,	'EXECUTED',	'9:6f482d5bb3bd6994de0a9a620e3f5116',	'dropColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1702639488634-1',	'openk9',	'db/datasource/2023/11/15-01-changelog.xml',	'2025-06-17 11:58:35.911321',	137,	'EXECUTED',	'9:1c9fa2a78cb797b6784acca2c7128e89',	'createTable tableName=annotator_extra_params',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1702639488634-2',	'openk9',	'db/datasource/2023/11/15-01-changelog.xml',	'2025-06-17 11:58:35.915744',	138,	'EXECUTED',	'9:68d4dc6094a44e115e6050d5ca2fc36b',	'createIndex indexName=idx_annotator_id_extra_params, tableName=annotator_extra_params',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1707123456-1',	'openk9',	'db/datasource/2024/02/05-01-changelog.xml',	'2025-06-17 11:58:35.919633',	139,	'EXECUTED',	'9:acda4b8bc4049d31e698e7f7158345dd',	'addColumn tableName=plugin_driver',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1710009174795-1',	'openk9',	'db/datasource/2024/03/09-01-changelog.xml',	'2025-06-17 11:58:35.929694',	140,	'EXECUTED',	'9:888af9fbfa7e2b81b9b0eb0829a75856',	'createTable tableName=sorting',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1710009174795-2',	'openk9',	'db/datasource/2024/03/09-01-changelog.xml',	'2025-06-17 11:58:35.933399',	141,	'EXECUTED',	'9:e71d02971ad482991e376ac3c5d761f3',	'createTable tableName=buckets_sortings',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1711366596747-1',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-06-17 11:58:35.937952',	142,	'EXECUTED',	'9:69b1be08886958090c4280f294f4ec90',	'createTable tableName=tab_sorting',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1711366596747-2',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-06-17 11:58:35.941238',	143,	'EXECUTED',	'9:cca325822876b841a79de530103edd8d',	'addForeignKeyConstraint baseTableName=tab_sorting, constraintName=fk_tab_sorting, referencedTableName=tab',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1711366596747-3',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-06-17 11:58:35.94467',	144,	'EXECUTED',	'9:391bb2401838fa56bc84e3d64ba3d072',	'addForeignKeyConstraint baseTableName=tab_sorting, constraintName=fk_sorting_tab, referencedTableName=sorting',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1711712316688-1',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-06-17 11:58:35.948819',	145,	'EXECUTED',	'9:dbbb1f075a51eb1ead2f26c037063bc5',	'addColumn tableName=scheduler',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1715071011646-1',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-06-17 11:58:35.951789',	146,	'EXECUTED',	'9:086955e0bbbcdf29990aab5eaef8096a',	'update tableName=scheduler',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1719406947449-1',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-06-17 11:58:35.96026',	147,	'EXECUTED',	'9:daf9669eb3d3ed80dd7e718f31cd6af8',	'createTable tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1719406947449-2',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-06-17 11:58:35.969772',	148,	'EXECUTED',	'9:19fac39357021dcc4a77b457bc26e3e7',	'createTable tableName=vector_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1719406947449-3',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-06-17 11:58:35.972613',	149,	'EXECUTED',	'9:722ccd10f7b257f2522227c78d83571f',	'addColumn tableName=tenant_binding',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1719406947449-4',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-06-17 11:58:35.975431',	150,	'EXECUTED',	'9:e8a2f0f15f5d542cd1076923336df292',	'addColumn tableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1719406947449-5',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-06-17 11:58:35.979013',	151,	'EXECUTED',	'9:c634851862a77f7b70f97cedb2929e37',	'addForeignKeyConstraint baseTableName=tenant_binding, constraintName=tenant_binding_embedding_model_fk, referencedTableName=embedding_model; addForeignKeyConstraint baseTableName=data_index, constraintName=data_index_vector_index_fk, referencedTab...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1749464109-1',	'openk9',	'db/datasource/2025/06/09-01-changelog.xml',	'2025-06-17 11:58:36.199459',	209,	'EXECUTED',	'9:54eeb3a734f0d6a7f30cceb3fede7eec',	'renameColumn newColumnName=annotator_size, oldColumnName=size, tableName=annotator',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1719406947449-1',	'openk9',	'db/datasource/2024/07/08-01-changelog.xml',	'2025-06-17 11:58:35.984367',	152,	'EXECUTED',	'9:e6ed55e7a99594b57db0fa7eee1de958',	'renameColumn newColumnName=text_embedding_field, oldColumnName=field_json_path, tableName=vector_index; addColumn tableName=vector_index; addColumn tableName=vector_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1720517315859-1',	'openk9',	'db/datasource/2024/07/09-01-changelog.xml',	'2025-06-17 11:58:35.991396',	153,	'EXECUTED',	'9:e7a5ddade0540f918b17d32f5ccf6320',	'createTable tableName=large_language_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1720517315859-2',	'openk9',	'db/datasource/2024/07/09-01-changelog.xml',	'2025-06-17 11:58:35.99392',	154,	'EXECUTED',	'9:151be23c0511e0bdd6d36e15c1b8d301',	'addColumn tableName=tenant_binding',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1720517315859-3',	'openk9',	'db/datasource/2024/07/09-01-changelog.xml',	'2025-06-17 11:58:35.996964',	155,	'EXECUTED',	'9:69a4d97b191de1ba5e65a6bd060306a7',	'addForeignKeyConstraint baseTableName=tenant_binding, constraintName=tenant_binding_large_language_model_fk, referencedTableName=large_language_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1720539264956-1',	'openk9',	'db/datasource/2024/07/09-02-changelog.xml',	'2025-06-17 11:58:36.000075',	156,	'EXECUTED',	'9:2ca28769e2942b8fc5a6deae8fa0818f',	'renameColumn newColumnName=json_config, oldColumnName=prompt_template, tableName=large_language_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1720602149181-1',	'openk9',	'db/datasource/2024/07/10-01-changelog.xml',	'2025-06-17 11:58:36.002927',	157,	'EXECUTED',	'9:814b189fa3b0afa4e7a35d293622441e',	'addColumn tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1721047170914-1',	'openk9',	'db/datasource/2024/07/15-01-changelog.xml',	'2025-06-17 11:58:36.005904',	158,	'EXECUTED',	'9:c44f65ce875acdf17ed94bbffd5932f7',	'addColumn tableName=vector_index; addColumn tableName=vector_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1721203667145-1',	'openk9',	'db/datasource/2024/07/17-01-changelog.xml',	'2025-06-17 11:58:36.010356',	159,	'EXECUTED',	'9:bba5a3e96dabd5f5da6a2366d90c9231',	'addDefaultValue columnName=chunk_window_size, tableName=vector_index; addDefaultValue columnName=chunk_type, tableName=vector_index; addDefaultValue columnName=json_config, tableName=vector_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1721212233876-1',	'openk9',	'db/datasource/2024/07/17-02-changelog.xml',	'2025-06-17 11:58:36.012931',	160,	'EXECUTED',	'9:ea71c85017ceb35d4664fe26610a3850',	'update tableName=vector_index; update tableName=vector_index; update tableName=vector_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1726562109788-1',	'openk9',	'db/datasource/2024/09/17-01-changelog.xml',	'2025-06-17 11:58:36.016068',	161,	'EXECUTED',	'9:765a1e6a82396177906db7241ac02f4c',	'addColumn tableName=scheduler',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1729064239719-1',	'openk9',	'db/datasource/2024/10/16-01-changelog.xml',	'2025-06-17 11:58:36.018897',	162,	'EXECUTED',	'9:bf07e3a3baf244f0088e59c2f4d96ab2',	'addDefaultValue columnName=retrieve_type, tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1729064239719-2',	'openk9',	'db/datasource/2024/10/16-01-changelog.xml',	'2025-06-17 11:58:36.022599',	163,	'EXECUTED',	'9:7402ec0d4b0c2b27f0292f4fd31e7ed9',	'addNotNullConstraint columnName=retrieve_type, tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1730902560520-1',	'openk9',	'db/datasource/2024/11/06-01-changelog.xml',	'2025-06-17 11:58:36.025469',	164,	'EXECUTED',	'9:e00dc10e2adecff96d5af0e41fe43b36',	'dropForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1734344527725-1',	'openk9',	'db/datasource/2024/12/16-01-changelog.xml',	'2025-06-17 11:58:36.0292',	165,	'EXECUTED',	'9:138d487a0f519eb97487fa7b1aac91cf',	'addColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1736439071799-1',	'openk9',	'db/datasource/2025/01/09-01-changelog.xml',	'2025-06-17 11:58:36.03477',	166,	'EXECUTED',	'9:2b1432c0ce8bd4c8356c181d83bd126e',	'dropColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1736949476865-1',	'openk9',	'db/datasource/2025/01/15-01-changelog.xml',	'2025-06-17 11:58:36.037975',	167,	'EXECUTED',	'9:5f301f593fe02a2ea38088d29f1f00ec',	'addColumn tableName=suggestion_category',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1736949476865-2',	'openk9',	'db/datasource/2025/01/15-01-changelog.xml',	'2025-06-17 11:58:36.041551',	168,	'EXECUTED',	'9:a4e0d1273238ed4897b0eb1281902e38',	'sql',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1736949476865-3',	'openk9',	'db/datasource/2025/01/15-01-changelog.xml',	'2025-06-17 11:58:36.044542',	169,	'EXECUTED',	'9:a92307902acb1819bcee6c3112bcdec7',	'addForeignKeyConstraint baseTableName=suggestion_category, constraintName=FK_suggestion_category_doc_type_field, referencedTableName=doc_type_field',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1736949476865-4',	'openk9',	'db/datasource/2025/01/15-01-changelog.xml',	'2025-06-17 11:58:36.050744',	170,	'EXECUTED',	'9:8840822b087d26043d181662901f4c33',	'dropTable tableName=suggestion_category_doc_type_fields',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1737626720-1',	'openk9',	'db/datasource/2025/01/09-01-changelog.xml',	'2025-06-17 11:58:36.053823',	171,	'EXECUTED',	'9:277beac22e587d74be84983b05931c3b',	'addColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1737988554-1',	'openk9',	'db/datasource/2025/01/27-01-changelog.xml',	'2025-06-17 11:58:36.057431',	172,	'EXECUTED',	'9:cbdf73a5d0502b5ce4c99987b4a3a608',	'dropForeignKeyConstraint baseTableName=data_index, constraintName=data_index_vector_index_fk; dropTable tableName=vector_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1737988554-2',	'openk9',	'db/datasource/2025/01/27-01-changelog.xml',	'2025-06-17 11:58:36.061668',	173,	'EXECUTED',	'9:9b4f77938e50f698e52f61aba979ba69',	'dropColumn columnName=vector_index_id, tableName=data_index; addColumn tableName=data_index',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1738248306-1',	'openk9',	'db/datasource/2025/01/30-01-changelog.xml',	'2025-06-17 11:58:36.065312',	174,	'EXECUTED',	'9:b11c6241a4218e0ca71476476a3629f4',	'addColumn tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1738248306-2',	'openk9',	'db/datasource/2025/01/30-01-changelog.xml',	'2025-06-17 11:58:36.068722',	175,	'EXECUTED',	'9:305d4955389b403d4d21eed81642d26c',	'addDefaultValue columnName=vector_size, tableName=embedding_model; update tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1738594109404-1',	'openk9',	'db/datasource/2025/02/03-01-changelog.xml',	'2025-06-17 11:58:36.072701',	176,	'EXECUTED',	'9:99fa34672dcf0916181691f67cb22a31',	'addColumn tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1738772698414-1',	'openk9',	'db/datasource/2025/02/05-01-changelog.xml',	'2025-06-17 11:58:36.075338',	177,	'EXECUTED',	'9:607df49fb06cfea494177607107977ce',	'addDefaultValue columnName=purging, tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1739374107993-1',	'openk9',	'db/datasource/2025/02/12-01-changelog.xml',	'2025-06-17 11:58:36.079363',	178,	'EXECUTED',	'9:0855bcf83d618eb4d85e58230cd349fe',	'addDefaultValue columnName=reindexable, tableName=datasource; addDefaultValue columnName=reindexing, tableName=datasource; addDefaultValue columnName=schedulable, tableName=datasource; addDefaultValue columnName=scheduling, tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1739459020239-1',	'openk9',	'db/datasource/2025/02/13-01-changelog.xml',	'2025-06-17 11:58:36.084161',	179,	'EXECUTED',	'9:f1c4583247d14b3597717df8a1b8505b',	'addDefaultValue columnName=scheduling, tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1739459020239-2',	'openk9',	'db/datasource/2025/02/13-01-changelog.xml',	'2025-06-17 11:58:36.089639',	180,	'EXECUTED',	'9:bf477802aff7ef922daa6afd5556f781',	'addNotNullConstraint columnName=purgeable, tableName=datasource; addNotNullConstraint columnName=purging, tableName=datasource; addNotNullConstraint columnName=purge_max_age, tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1739804793-1',	'openk9',	'db/datasource/2025/02/17-01-changelog.xml',	'2025-06-17 11:58:36.095541',	181,	'EXECUTED',	'9:b3bbe87dfd0c01d3c4f211c7607c9c05',	'createTable tableName=buckets_suggestion_categories; addForeignKeyConstraint baseTableName=buckets_suggestion_categories, constraintName=fk_buckets_suggestion_categories_bucket, referencedTableName=bucket; addForeignKeyConstraint baseTableName=buc...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1739804793-2',	'openk9',	'db/datasource/2025/02/17-01-changelog.xml',	'2025-06-17 11:58:36.099337',	182,	'EXECUTED',	'9:d0583051f0f8b6617563872bbb00ed16',	'sql',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1739804793-3',	'openk9',	'db/datasource/2025/02/17-01-changelog.xml',	'2025-06-17 11:58:36.102879',	183,	'EXECUTED',	'9:7184042473f0ae1d36bf2fa25e9f6834',	'dropColumn columnName=bucket_id, tableName=suggestion_category',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1742563202304-1',	'openk9',	'db/datasource/2025/03/21-01-changelog.xml',	'2025-06-17 11:58:36.105922',	184,	'EXECUTED',	'9:3dafdd72a0f0cdec208a5c81aa9d4cbd',	'addColumn tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1742826155489-1',	'openk9',	'db/datasource/2025/03/24-01-changelog.xml',	'2025-06-17 11:58:36.108446',	185,	'EXECUTED',	'9:d6cc3e60d23b0d3ac0aa6e887c2ca5ea',	'addColumn tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743067921-1',	'openk9',	'db/datasource/2025/03/27-01-changelog.xml',	'2025-06-17 11:58:36.111052',	186,	'EXECUTED',	'9:7f47974e28deaee8a7257be5d71d5cc1',	'dropColumn columnName=pipeline_type, tableName=datasource',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743086923923-1',	'openk9',	'db/datasource/2025/03/27-01-changelog.xml',	'2025-06-17 11:58:36.121304',	187,	'EXECUTED',	'9:d06a056983dda42cd23d012d3d0ed0e3',	'createTable tableName=rag_configuration',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743086923923-2',	'openk9',	'db/datasource/2025/03/27-01-changelog.xml',	'2025-06-17 11:58:36.124427',	188,	'EXECUTED',	'9:714a43af3baeee6dd42c0907d46266d9',	'addColumn tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743086923923-3',	'openk9',	'db/datasource/2025/03/27-01-changelog.xml',	'2025-06-17 11:58:36.128299',	189,	'EXECUTED',	'9:d40578b2ce3e05caaa8f110eedf224fd',	'addForeignKeyConstraint baseTableName=bucket, constraintName=fk_bucket_rag_configuration, referencedTableName=rag_configuration',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743086923923-4',	'openk9',	'db/datasource/2025/03/27-01-changelog.xml',	'2025-06-17 11:58:36.132604',	190,	'EXECUTED',	'9:765d7242b6e4893dd8867402ad6d521d',	'modifyDataType columnName=json_config, tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743414146836-1',	'openk9',	'db/datasource/2025/03/31-01-changelog.xml',	'2025-06-17 11:58:36.13577',	191,	'EXECUTED',	'9:cde70fcb215b4ef9be00faacf34b6b2a',	'addNotNullConstraint columnName=type, tableName=rag_configuration',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743414146836-2',	'openk9',	'db/datasource/2025/03/31-01-changelog.xml',	'2025-06-17 11:58:36.141068',	192,	'EXECUTED',	'9:660d155a6a7fad6420bf1a16c50fe61a',	'addDefaultValue columnName=prompt, tableName=rag_configuration; addDefaultValue columnName=prompt_no_rag, tableName=rag_configuration; addDefaultValue columnName=rag_tool_description, tableName=rag_configuration; addDefaultValue columnName=rephras...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743515344113-1',	'openk9',	'db/datasource/2025/04/01-01-changelog.xml',	'2025-06-17 11:58:36.144988',	193,	'EXECUTED',	'9:1bd2198b39516fcce251135969996b60',	'renameColumn newColumnName=rag_configuration_chat_id, oldColumnName=rag_configuration_id, tableName=bucket; dropForeignKeyConstraint baseTableName=bucket, constraintName=fk_bucket_rag_configuration; addForeignKeyConstraint baseTableName=bucket, co...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1743515344113-2',	'openk9',	'db/datasource/2025/04/01-01-changelog.xml',	'2025-06-17 11:58:36.151186',	194,	'EXECUTED',	'9:6d072da7f225e290bcdcf7b97ce141e3',	'addColumn tableName=bucket; addForeignKeyConstraint baseTableName=bucket, constraintName=fk_bucket_rag_configuration_chat_tool, referencedTableName=rag_configuration; addForeignKeyConstraint baseTableName=bucket, constraintName=fk_bucket_rag_confi...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744037032315-1',	'openk9',	'db/datasource/2025/04/07-01-changelog.xml',	'2025-06-17 11:58:36.155258',	195,	'EXECUTED',	'9:ee45ff5c7321eaacd1b3fd82b00d893f',	'addNotNullConstraint columnName=type, tableName=embedding_model; addNotNullConstraint columnName=model, tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744038673298-1',	'openk9',	'db/datasource/2025/04/07-02-changelog.xml',	'2025-06-17 11:58:36.160218',	196,	'EXECUTED',	'9:a016fc0f1ad69aa9bbd6c5ceb534d894',	'addColumn tableName=large_language_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744038673298-2',	'openk9',	'db/datasource/2025/04/07-02-changelog.xml',	'2025-06-17 11:58:36.163774',	197,	'EXECUTED',	'9:689b1d2d0edcef6854f34c155879df48',	'addDefaultValue columnName=type, tableName=embedding_model; addDefaultValue columnName=model, tableName=embedding_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744184228907-1',	'openk9',	'db/datasource/2025/04/09-01-changelog.xml',	'2025-06-17 11:58:36.168701',	198,	'EXECUTED',	'9:a2b137ebb4f7ddbc7441ca431dc96bc2',	'addColumn tableName=rag_configuration',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744184228907-2',	'openk9',	'db/datasource/2025/04/09-01-changelog.xml',	'2025-06-17 11:58:36.172412',	199,	'EXECUTED',	'9:1a1fdab35a21fd4c8050c1b0b0f0c7d9',	'renameColumn newColumnName=rag_configuration_simple_generate_id, oldColumnName=rag_configuration_search_id, tableName=bucket; dropForeignKeyConstraint baseTableName=bucket, constraintName=fk_bucket_rag_configuration_search; addForeignKeyConstraint...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744278183842-1',	'openk9',	'db/datasource/2025/04/10-01-changelog.xml',	'2025-06-17 11:58:36.174873',	200,	'EXECUTED',	'9:94f7ed8f10f00c256208325a60f56925',	'renameColumn newColumnName=provider, oldColumnName=type, tableName=embedding_model; renameColumn newColumnName=provider, oldColumnName=type, tableName=large_language_model',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744881599-1',	'openk9',	'db/datasource/2025/04/17-01-changelog.xml',	'2025-06-17 11:58:36.177584',	201,	'EXECUTED',	'9:35c77716c5b22e3eb33dfa4a31c060f4',	'addColumn tableName=token_tab; addColumn tableName=annotator',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744881599-2',	'openk9',	'db/datasource/2025/04/17-01-changelog.xml',	'2025-06-17 11:58:36.180276',	202,	'EXECUTED',	'9:c003f18336641c538dd43a5c3e22a8aa',	'sql',	'Aggregate key-value pairs from annotator_extra_params into JSON and update
                    annotator table',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744881599-3',	'openk9',	'db/datasource/2025/04/17-01-changelog.xml',	'2025-06-17 11:58:36.183174',	203,	'EXECUTED',	'9:552418b395a26d99a9dfee62ea0db96a',	'sql',	'Aggregate key-value pairs from token_tab_extra_params into JSON and update
                    token_tab table',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1744881599-4',	'openk9',	'db/datasource/2025/04/17-01-changelog.xml',	'2025-06-17 11:58:36.187119',	204,	'EXECUTED',	'9:218be3f545c82043b54ddd89babc41eb',	'dropIndex indexName=idx_token_tab_id_extra_params, tableName=token_tab_extra_params; dropTable tableName=token_tab_extra_params; dropIndex indexName=idx_annotator_id_extra_params, tableName=annotator_extra_params; dropTable tableName=annotator_ext...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1746544287297-1',	'openk9',	'db/datasource/2025/05/06-01-changelog.xml',	'2025-06-17 11:58:36.190063',	205,	'EXECUTED',	'9:31bc884b729bef023fef2fc1192a3f6b',	'sql',	'Combina i campi host e port nel nuovo campo baseUri',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1746544287297-2',	'openk9',	'db/datasource/2025/05/06-01-changelog.xml',	'2025-06-17 11:58:36.192685',	206,	'EXECUTED',	'9:2b2526d8bba9f010640bfe9f0b77e6b0',	'sql',	'Rimuovi i campi host e port dopo la migrazione a baseUri',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1749131762-1',	'openk9',	'db/datasource/2025/06/05-01-changelog.xml',	'2025-06-17 11:58:36.195006',	207,	'EXECUTED',	'9:0bc99fce902cee8221542ede809f8c9b',	'addDefaultValue columnName=retrieve_type, tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059'),
        ('1749131762-2',	'openk9',	'db/datasource/2025/06/05-01-changelog.xml',	'2025-06-17 11:58:36.196929',	208,	'EXECUTED',	'9:698e61644fa1829f1d605db3e6853944',	'update tableName=bucket',	'',	NULL,	'4.29.1',	NULL,	NULL,	'0161515059');
        
        CREATE TABLE "openk9_liquibase"."databasechangeloglock" (
            "id" integer NOT NULL,
            "locked" boolean NOT NULL,
            "lockgranted" timestamp,
            "lockedby" character varying(255),
            CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        INSERT INTO "openk9_liquibase"."databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
        (1,	'0',	NULL,	NULL);

        CREATE TABLE "openk9"."acl_mapping" (
            "user_field" character varying(255) NOT NULL,
            "doc_type_field_id" bigint NOT NULL,
            "plugin_driver_id" bigint NOT NULL,
            CONSTRAINT "pk_acl_mapping" PRIMARY KEY ("doc_type_field_id", "plugin_driver_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."analyzer" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "type" character varying(255),
            "tokenizer" bigint,
            "json_config" text,
            CONSTRAINT "analyzer_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_tb9s0i7disnbabdksial8ic43 ON openk9.analyzer USING btree (name);


        CREATE TABLE "openk9"."analyzer_char_filter" (
            "analyzer" bigint NOT NULL,
            "char_filter" bigint NOT NULL,
            CONSTRAINT "analyzer_char_filter_pkey" PRIMARY KEY ("analyzer", "char_filter")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."analyzer_token_filter" (
            "analyzer" bigint NOT NULL,
            "token_filter" bigint NOT NULL,
            CONSTRAINT "analyzer_token_filter_pkey" PRIMARY KEY ("analyzer", "token_filter")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."annotator" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "field_name" character varying(255) NOT NULL,
            "fuziness" character varying(255),
            "name" character varying(255) NOT NULL,
            "annotator_size" integer,
            "type" character varying(255) NOT NULL,
            "doc_type_field_id" bigint,
            "extra_params" text,
            CONSTRAINT "annotator_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_9318c5hvkjhrqjlq8rd3jkhe9 ON openk9.annotator USING btree (name);

        CREATE SEQUENCE IF NOT EXISTS "openk9"."hibernate_sequence";

        CREATE TABLE "openk9"."bucket" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "query_analysis_id" bigint,
            "search_config_id" bigint,
            "refresh_on_suggestion_category" boolean,
            "language_id" bigint,
            "refresh_on_tab" boolean,
            "refresh_on_date" boolean,
            "refresh_on_query" boolean,
            "retrieve_type" character varying(255) DEFAULT 'MATCH' NOT NULL,
            "rag_configuration_chat_id" bigint,
            "rag_configuration_chat_tool_id" bigint,
            "rag_configuration_simple_generate_id" bigint,
            CONSTRAINT "bucket_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_he0xrer6rh4dgaalutt7prhbm ON openk9.bucket USING btree (name);


        CREATE TABLE "openk9"."bucket_language" (
            "language_id" bigint NOT NULL,
            "bucket_id" bigint NOT NULL,
            CONSTRAINT "bucket_language_pkey" PRIMARY KEY ("language_id", "bucket_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."buckets_sortings" (
            "buckets_id" bigint NOT NULL,
            "sortings_id" bigint NOT NULL
        ) WITH (oids = false);


        CREATE TABLE "openk9"."buckets_suggestion_categories" (
            "bucket_id" bigint NOT NULL,
            "suggestion_category_id" bigint NOT NULL
        ) WITH (oids = false);


        CREATE TABLE "openk9"."buckets_tabs" (
            "buckets_id" bigint NOT NULL,
            "tabs_id" bigint NOT NULL
        ) WITH (oids = false);


        CREATE TABLE "openk9"."char_filter" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "type" character varying(255),
            CONSTRAINT "char_filter_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_hgk1crqb2kqnbkad5mr76v4v0 ON openk9.char_filter USING btree (name);


        CREATE TABLE "openk9"."data_index" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "datasource_id" bigint NOT NULL,
            "knn_index" boolean,
            "chunk_type" character varying(255),
            "chunk_window_size" integer,
            "embedding_json_config" text,
            "embedding_doc_type_field_id" bigint,
            CONSTRAINT "data_index_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_ecnihy9mdplwt30pbahqcv9fw ON openk9.data_index USING btree (name);


        CREATE TABLE "openk9"."data_index_doc_types" (
            "data_index_id" bigint NOT NULL,
            "doc_types_id" bigint NOT NULL,
            CONSTRAINT "data_index_doc_types_pkey" PRIMARY KEY ("data_index_id", "doc_types_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."datasource" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "last_ingestion_date" timestamp,
            "name" character varying(255) NOT NULL,
            "schedulable" boolean DEFAULT false NOT NULL,
            "scheduling" character varying(255) DEFAULT '0 */30 * ? * * *' NOT NULL,
            "data_index_id" bigint,
            "enrich_pipeline_id" bigint,
            "plugin_driver_id" bigint,
            "reindexable" boolean DEFAULT false NOT NULL,
            "reindexing" character varying(255) DEFAULT '0 0 1 * * ?' NOT NULL,
            "purgeable" boolean DEFAULT false NOT NULL,
            "purging" character varying(255) DEFAULT '0 0 1 * * ?' NOT NULL,
            "purge_max_age" character varying(255) DEFAULT '2d' NOT NULL,
            CONSTRAINT "datasource_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_c270jcqlmvthpcasvusahxb7h ON openk9.datasource USING btree (name);


        CREATE TABLE "openk9"."datasource_buckets" (
            "datasource_id" bigint NOT NULL,
            "buckets_id" bigint NOT NULL,
            CONSTRAINT "datasource_buckets_pkey" PRIMARY KEY ("datasource_id", "buckets_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."doc_type" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "doc_type_template_id" bigint,
            CONSTRAINT "doc_type_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_2l8a7vqh0i6r6tb8cb9j6yf6n ON openk9.doc_type USING btree (name);


        CREATE TABLE "openk9"."doc_type_field" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "boost" double precision,
            "description" character varying(4096),
            "exclude" boolean,
            "field_name" character varying(4096) NOT NULL,
            "field_type" character varying(255) NOT NULL,
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "searchable" boolean,
            "analyzer" bigint,
            "doc_type_id" bigint,
            "parent_doc_type_field_id" bigint,
            "sortable" boolean,
            CONSTRAINT "doc_type_field_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX field_name_doc_type_id_parent_doc_type_field_id ON openk9.doc_type_field USING btree (field_name, doc_type_id, parent_doc_type_field_id);


        CREATE TABLE "openk9"."doc_type_template" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "compiled" text,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "source" text,
            "template_type" character varying(255) NOT NULL,
            CONSTRAINT "doc_type_template_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_t7tmy5syhhbjxbfktw4l3ejdf ON openk9.doc_type_template USING btree (name);


        CREATE TABLE "openk9"."embedding_model" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "api_url" character varying(255) NOT NULL,
            "api_key" character varying(255),
            "vector_size" integer DEFAULT '0',
            "provider" character varying(255) DEFAULT '' NOT NULL,
            "model" character varying(255) DEFAULT '' NOT NULL,
            "json_config" text,
            CONSTRAINT "embedding_model_id" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."enrich_item" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "service_name" character varying(255) NOT NULL,
            "type" character varying(255) NOT NULL,
            "script" text,
            "behavior_merge_type" character varying(255),
            "json_path" character varying(255),
            "behavior_on_error" character varying(255),
            "request_timeout" bigint,
            CONSTRAINT "enrich_item_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_30eqiwulrtffhilqri8poy0e9 ON openk9.enrich_item USING btree (name);


        CREATE TABLE "openk9"."enrich_pipeline" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "enrich_pipeline_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_hgxv753bx3d7mwghpktnxkk8h ON openk9.enrich_pipeline USING btree (name);


        CREATE TABLE "openk9"."enrich_pipeline_item" (
            "enrich_item_id" bigint NOT NULL,
            "enrich_pipeline_id" bigint NOT NULL,
            "weight" real NOT NULL,
            CONSTRAINT "enrich_pipeline_item_pkey" PRIMARY KEY ("enrich_item_id", "enrich_pipeline_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."file_resource" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "file_id" character varying(255) NOT NULL,
            "resource_id" character varying(255) NOT NULL,
            "datasource_id" character varying(255) NOT NULL,
            CONSTRAINT "file_resource_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uc_fileresource_fileid_datasource_id ON openk9.file_resource USING btree (file_id, datasource_id);

        CREATE UNIQUE INDEX uc_fileresource_resource_id ON openk9.file_resource USING btree (resource_id);


        CREATE TABLE "openk9"."language" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "name" character varying(255) NOT NULL,
            "value" character varying(255) NOT NULL,
            "bucket_id" bigint,
            CONSTRAINT "language_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX "0998a292-02a9-4930-a8ba-e31d7af5cee2" ON openk9.language USING btree (name);


        CREATE TABLE "openk9"."large_language_model" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "api_url" character varying(255) NOT NULL,
            "api_key" character varying(255),
            "json_config" text,
            "provider" character varying(255) DEFAULT '' NOT NULL,
            "model" character varying(255) DEFAULT '' NOT NULL,
            "context_window" integer,
            "retrieve_citations" boolean,
            CONSTRAINT "large_language_model_id" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."plugin_driver" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "type" character varying(255) NOT NULL,
            "provisioning" character varying(50) DEFAULT 'USER',
            CONSTRAINT "plugin_driver_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_o3hjnq90nnkxc3y34jumnhyg9 ON openk9.plugin_driver USING btree (name);


        CREATE TABLE "openk9"."query_analysis" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "stopwords" text,
            CONSTRAINT "query_analysis_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_26k3dv00807y6dy0x82xx8mjd ON openk9.query_analysis USING btree (name);


        CREATE TABLE "openk9"."query_analysis_annotators" (
            "query_analysis_id" bigint NOT NULL,
            "annotators_id" bigint NOT NULL,
            CONSTRAINT "query_analysis_annotators_pkey" PRIMARY KEY ("query_analysis_id", "annotators_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."query_analysis_rules" (
            "query_analysis_id" bigint NOT NULL,
            "rules_id" bigint NOT NULL,
            CONSTRAINT "query_analysis_rules_pkey" PRIMARY KEY ("query_analysis_id", "rules_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."query_parser_config" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "type" character varying(255) NOT NULL,
            "search_config" bigint,
            CONSTRAINT "query_parser_config_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."rag_configuration" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "type" character varying(255) NOT NULL,
            "prompt" text DEFAULT '',
            "rephrase_prompt" text DEFAULT '',
            "prompt_no_rag" text DEFAULT '',
            "rag_tool_description" text DEFAULT '',
            "chunk_window" integer DEFAULT '0',
            "reformulate" boolean DEFAULT false,
            "json_config" text,
            CONSTRAINT "rag_configuration_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."rule" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "lhs" character varying(255) NOT NULL,
            "name" character varying(255) NOT NULL,
            "rhs" character varying(255) NOT NULL,
            CONSTRAINT "rule_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_g0aibm7vybna15mqfxis5nnf1 ON openk9.rule USING btree (name);


        CREATE TABLE "openk9"."scheduler" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "schedule_id" character varying(255) NOT NULL,
            "datasource_id" bigint NOT NULL,
            "old_data_index_id" bigint,
            "new_data_index_id" bigint,
            "status" character varying(255) NOT NULL,
            "last_ingestion_date" timestamp,
            "error_description" character varying(4096),
            CONSTRAINT "scheduler_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uc_schedule_id ON openk9.scheduler USING btree (schedule_id);


        CREATE TABLE "openk9"."search_config" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "min_score" real,
            "name" character varying(255) NOT NULL,
            "min_score_suggestions" boolean,
            "min_score_search" boolean,
            CONSTRAINT "search_config_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_5abkod761uq37sv3j2p4weaf0 ON openk9.search_config USING btree (name);


        CREATE TABLE "openk9"."sorting" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "default_sort" boolean NOT NULL,
            "name" character varying(255) NOT NULL,
            "type" character varying(255) NOT NULL,
            "priority" real NOT NULL,
            "doc_type_field_id" bigint,
            CONSTRAINT "sorting_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."suggestion_category" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "priority" real NOT NULL,
            "multi_select" boolean,
            "doc_type_field_id" bigint,
            CONSTRAINT "suggestion_category_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_4c591r8ch4g8rilsgfvb32kgn ON openk9.suggestion_category USING btree (name);


        CREATE TABLE "openk9"."tab" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "priority" integer NOT NULL,
            CONSTRAINT "tab_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_r17opdmucm7ij4aveppoa8wep ON openk9.tab USING btree (name);


        CREATE TABLE "openk9"."tab_sorting" (
            "tab_id" bigint NOT NULL,
            "sorting_id" bigint NOT NULL,
            CONSTRAINT "tab_sorting_pkey" PRIMARY KEY ("tab_id", "sorting_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."tab_token_tab" (
            "tab_id" bigint NOT NULL,
            "token_tab_id" bigint NOT NULL,
            CONSTRAINT "tab_token_tab_pkey" PRIMARY KEY ("tab_id", "token_tab_id")
        ) WITH (oids = false);


        CREATE TABLE "openk9"."tenant_binding" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "virtual_host" character varying(255) NOT NULL,
            "tenant_binding_bucket_id" bigint,
            "embedding_model_id" bigint,
            "large_language_model_id" bigint,
            CONSTRAINT "tenant_binding_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_6wrs6ncojuw0wde7a5jw2wjh1 ON openk9.tenant_binding USING btree (virtual_host);


        CREATE TABLE "openk9"."token_filter" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "type" character varying(255),
            CONSTRAINT "token_filter_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_mbf4ldxsh6umx4bx0kgcwrmg2 ON openk9.token_filter USING btree (name);


        CREATE TABLE "openk9"."token_tab" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "filter" boolean NOT NULL,
            "name" character varying(255) NOT NULL,
            "token_type" character varying(255) NOT NULL,
            "value" character varying(255),
            "doc_type_field_id" bigint,
            "extra_params" text,
            CONSTRAINT "token_tab_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_712f8fp1ftw1ug66hrmkgphrf ON openk9.token_tab USING btree (name);


        CREATE TABLE "openk9"."tokenizer" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "name" character varying(255) NOT NULL,
            "type" character varying(255),
            CONSTRAINT "tokenizer_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_re72pk1pijmchwmusxdil83sy ON openk9.tokenizer USING btree (name);


        CREATE TABLE "openk9"."translation" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "language" character varying(10) NOT NULL,
            "class_name" character varying(255) NOT NULL,
            "class_pk" bigint NOT NULL,
            "key" character varying(50) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "translation_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX idx_translation_key ON openk9.translation USING btree (language, class_name, class_pk, key);

        CREATE INDEX idx_translation_entities ON openk9.translation USING btree (class_name, class_pk);


        ALTER TABLE ONLY "openk9"."acl_mapping" ADD CONSTRAINT "fk_acl_mapping_on_doc_type_field" FOREIGN KEY (doc_type_field_id) REFERENCES doc_type_field(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."acl_mapping" ADD CONSTRAINT "fk_acl_mapping_on_plugin_driver" FOREIGN KEY (plugin_driver_id) REFERENCES plugin_driver(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."analyzer" ADD CONSTRAINT "fkc7nh3nt723kkehd09dl3jpcmw" FOREIGN KEY (tokenizer) REFERENCES tokenizer(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."analyzer_char_filter" ADD CONSTRAINT "fkfjki6k33j86nxxjtpjfsjk0bg" FOREIGN KEY (analyzer) REFERENCES analyzer(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."analyzer_char_filter" ADD CONSTRAINT "fki9uan06v9eosrqrj0d2ab839l" FOREIGN KEY (char_filter) REFERENCES char_filter(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."analyzer_token_filter" ADD CONSTRAINT "fk84vkkc91bhjv3ye2l19iimcl8" FOREIGN KEY (token_filter) REFERENCES token_filter(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."analyzer_token_filter" ADD CONSTRAINT "fk8knl2njy74t871j2eycwu5fql" FOREIGN KEY (analyzer) REFERENCES analyzer(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."annotator" ADD CONSTRAINT "fkfiv3bsnm26bb802cm7i1ei4q2" FOREIGN KEY (doc_type_field_id) REFERENCES doc_type_field(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fk_bucket_rag_configuration_chat" FOREIGN KEY (rag_configuration_chat_id) REFERENCES rag_configuration(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fk_bucket_rag_configuration_chat_tool" FOREIGN KEY (rag_configuration_chat_tool_id) REFERENCES rag_configuration(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fk_bucket_rag_configuration_simple_generate" FOREIGN KEY (rag_configuration_simple_generate_id) REFERENCES rag_configuration(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fkgj8f2hdk19shy1gwqu3u4ae9x" FOREIGN KEY (query_analysis_id) REFERENCES query_analysis(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fkn9yvqd7qsenqu5unq6yfj7j2a" FOREIGN KEY (search_config_id) REFERENCES search_config(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."bucket_language" ADD CONSTRAINT "313a9cab-a687-486d-92ae-2ce74fa89951" FOREIGN KEY (language_id) REFERENCES language(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."buckets_suggestion_categories" ADD CONSTRAINT "fk_buckets_suggestion_categories_bucket" FOREIGN KEY (bucket_id) REFERENCES bucket(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."buckets_suggestion_categories" ADD CONSTRAINT "fk_buckets_suggestion_categories_suggestion_category" FOREIGN KEY (suggestion_category_id) REFERENCES suggestion_category(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."buckets_tabs" ADD CONSTRAINT "fki0729hs5mp7hisvr1xc4kcnio" FOREIGN KEY (buckets_id) REFERENCES bucket(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."buckets_tabs" ADD CONSTRAINT "fkkivu1a4ehytvoy78u74r3vq3l" FOREIGN KEY (tabs_id) REFERENCES tab(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."data_index" ADD CONSTRAINT "FK_data_index_datasource" FOREIGN KEY (datasource_id) REFERENCES datasource(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."data_index_doc_types" ADD CONSTRAINT "fk1a9m9bg9q7ni7gc9ysg3rq6fp" FOREIGN KEY (doc_types_id) REFERENCES doc_type(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."data_index_doc_types" ADD CONSTRAINT "fk9tu5iup1pex2vpk80jftsfbey" FOREIGN KEY (data_index_id) REFERENCES data_index(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."datasource" ADD CONSTRAINT "fkewjpbr7f4fkff02y19op5g6a" FOREIGN KEY (enrich_pipeline_id) REFERENCES enrich_pipeline(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."datasource" ADD CONSTRAINT "fkeyxlmk7fbek60xtie38cwu2v2" FOREIGN KEY (plugin_driver_id) REFERENCES plugin_driver(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."datasource" ADD CONSTRAINT "fknbh6wdfjww9no5xqxn65qchfy" FOREIGN KEY (data_index_id) REFERENCES data_index(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."datasource_buckets" ADD CONSTRAINT "fk80gj884mrv2t03t7g36qlrae9" FOREIGN KEY (datasource_id) REFERENCES datasource(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."datasource_buckets" ADD CONSTRAINT "fki9t38w4aa24jenr7lp4uguxb2" FOREIGN KEY (buckets_id) REFERENCES bucket(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."doc_type" ADD CONSTRAINT "fk1ocwbmim5560h0bysfpunyq46" FOREIGN KEY (doc_type_template_id) REFERENCES doc_type_template(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."doc_type_field" ADD CONSTRAINT "fk44qu7fg8d1tl3bn4d43sge02x" FOREIGN KEY (parent_doc_type_field_id) REFERENCES doc_type_field(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."doc_type_field" ADD CONSTRAINT "fkfjuk779hqxyt5ngdk9asog0bj" FOREIGN KEY (analyzer) REFERENCES analyzer(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."doc_type_field" ADD CONSTRAINT "fkn1p060bcao42e9fn7l1mb1jju" FOREIGN KEY (doc_type_id) REFERENCES doc_type(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."enrich_pipeline_item" ADD CONSTRAINT "fkmjidj39ui867w3ig9iccnmrxb" FOREIGN KEY (enrich_item_id) REFERENCES enrich_item(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."enrich_pipeline_item" ADD CONSTRAINT "fkreyxykt1gox8ghwcuobr5pjan" FOREIGN KEY (enrich_pipeline_id) REFERENCES enrich_pipeline(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."query_analysis_annotators" ADD CONSTRAINT "fka1ext4cmdvvc0pjxgci94xvvn" FOREIGN KEY (query_analysis_id) REFERENCES query_analysis(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."query_analysis_annotators" ADD CONSTRAINT "fkj6bkvkacmj53kv5o5k8065hvt" FOREIGN KEY (annotators_id) REFERENCES annotator(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."query_analysis_rules" ADD CONSTRAINT "fk5412heryog6cetwbsq5enp335" FOREIGN KEY (query_analysis_id) REFERENCES query_analysis(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."query_analysis_rules" ADD CONSTRAINT "fkmbqqghon1m8h554uh8hi0qkru" FOREIGN KEY (rules_id) REFERENCES rule(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."query_parser_config" ADD CONSTRAINT "fktmj4jra4g99dajgs7ilywk41b" FOREIGN KEY (search_config) REFERENCES search_config(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."suggestion_category" ADD CONSTRAINT "fk_suggestion_category_doc_type_field" FOREIGN KEY (doc_type_field_id) REFERENCES doc_type_field(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."tab_sorting" ADD CONSTRAINT "fk_sorting_tab" FOREIGN KEY (sorting_id) REFERENCES sorting(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tab_sorting" ADD CONSTRAINT "fk_tab_sorting" FOREIGN KEY (tab_id) REFERENCES tab(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."tab_token_tab" ADD CONSTRAINT "fk_tab_token_tab" FOREIGN KEY (tab_id) REFERENCES tab(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tab_token_tab" ADD CONSTRAINT "fk_token_tab_tab" FOREIGN KEY (token_tab_id) REFERENCES token_tab(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."tenant_binding" ADD CONSTRAINT "fki7bn20nxw6x541wfvvwe62t28" FOREIGN KEY (tenant_binding_bucket_id) REFERENCES bucket(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tenant_binding" ADD CONSTRAINT "tenant_binding_embedding_model_fk" FOREIGN KEY (embedding_model_id) REFERENCES embedding_model(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tenant_binding" ADD CONSTRAINT "tenant_binding_large_language_model_fk" FOREIGN KEY (large_language_model_id) REFERENCES large_language_model(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."token_tab" ADD CONSTRAINT "fk37w5m3swa2cebgar0umrnytyy" FOREIGN KEY (doc_type_field_id) REFERENCES doc_type_field(id) NOT DEFERRABLE;

        INSERT INTO "plugin_driver" ("id", "create_date", "modified_date", "description", "json_config", "name", "type", "provisioning") VALUES
        (1,	'2025-06-16 21:28:48.540534',	'2025-06-16 21:28:48.541568',	'',	'{"baseUri":"web-parser:5000","path":"/startSitemapCrawling","method":"POST"}',	'web parser',	'HTTP',	'USER');

        INSERT INTO "enrich_item" ("id", "create_date", "modified_date", "description", "json_config", "name", "service_name", "type", "script", "behavior_merge_type", "json_path", "behavior_on_error", "request_timeout") VALUES
        (1,	'2025-06-29 20:10:56.418262',	'2025-06-29 20:10:56.420208',	'',	'{}',	'tika',	'http://openk9-tika:8080/api/tika/process',	'HTTP_ASYNC',	'',	'MERGE',	'$',	'SKIP',	10000);

        INSERT INTO "enrich_pipeline" ("id", "create_date", "modified_date", "description", "name") VALUES
        (19,	'2025-06-17 10:37:24.460791',	'2025-06-29 20:11:09.268201',	'',	'document pipeline');

        INSERT INTO "enrich_pipeline_item" ("enrich_item_id", "enrich_pipeline_id", "weight") VALUES
        (1,	19,	1);
        
        INSERT INTO "datasource" ("id", "create_date", "modified_date", "description", "json_config", "last_ingestion_date", "name", "schedulable", "scheduling", "data_index_id", "enrich_pipeline_id", "plugin_driver_id", "reindexable", "reindexing", "purgeable", "purging", "purge_max_age") VALUES
        (30,	'2025-06-17 11:24:17.648002',	'2025-06-17 11:54:40.419122',	'',	'{"sitemapUrls":["https://www.smc.it/sitemap.xml"],"allowedDomains":["www.smc.it"],"allowedPaths":[],"excludedPaths":[],"bodyTag":"body","titleTag":"title::text","maxLength":-1,"pageCount":0,"doExtractDocs":false,"documentFileExtensions":[]}',	NULL,	'smc site ',	'0',	'0 */30 * ? * * *',	NULL,	NULL,	1,	'0',	'0 0 1 * * ?',	'0',	'0 0 1 * * ?',	'2d');

        INSERT INTO "doc_type" ("id", "create_date", "modified_date", "description", "name", "doc_type_template_id") VALUES
        (2,	'2025-06-16 21:28:48.947629',	'2025-06-16 21:28:48.947651',	'auto-generated',	'default',	NULL),
        (9,	'2025-06-16 21:28:48.954147',	'2025-06-16 21:28:48.954165',	'auto-generated',	'web',	NULL);

        INSERT INTO "doc_type_field" ("id", "create_date", "modified_date", "boost", "description", "exclude", "field_name", "field_type", "json_config", "name", "searchable", "analyzer", "doc_type_id", "parent_doc_type_field_id", "sortable") VALUES
        (3,	'2025-06-16 21:28:48.94894',	'2025-06-16 21:28:48.948959',	1,	'auto-generated',	NULL,	'contentId',	'TEXT',	NULL,	'contentId',	'1',	NULL,	2,	NULL,	'0'),
        (4,	'2025-06-16 21:28:48.950443',	'2025-06-16 21:28:48.950461',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'contentId.keyword',	'1',	NULL,	2,	3,	'0'),
        (5,	'2025-06-16 21:28:48.951314',	'2025-06-16 21:28:48.951329',	1,	'auto-generated',	NULL,	'documentTypes',	'TEXT',	NULL,	'documentTypes',	'1',	NULL,	2,	NULL,	'0'),
        (6,	'2025-06-16 21:28:48.951746',	'2025-06-16 21:28:48.951757',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'documentTypes.keyword',	'1',	NULL,	2,	5,	'0'),
        (7,	'2025-06-16 21:28:48.952573',	'2025-06-16 21:28:48.952588',	1,	'auto-generated',	NULL,	'rawContent',	'TEXT',	NULL,	'rawContent',	'1',	NULL,	2,	NULL,	'0'),
        (8,	'2025-06-16 21:28:48.953342',	'2025-06-16 21:28:48.95336',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'rawContent.keyword',	'1',	NULL,	2,	7,	'0'),
        (10,	'2025-06-16 21:28:48.954742',	'2025-06-16 21:28:48.954758',	1,	'auto-generated',	NULL,	'content',	'TEXT',	NULL,	'web.content',	'1',	NULL,	9,	NULL,	'0'),
        (11,	'2025-06-16 21:28:48.955467',	'2025-06-16 21:28:48.955484',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.content.keyword',	'1',	NULL,	9,	10,	'0'),
        (12,	'2025-06-16 21:28:48.956058',	'2025-06-16 21:28:48.95607',	1,	'auto-generated',	NULL,	'favicon',	'TEXT',	NULL,	'web.favicon',	'1',	NULL,	9,	NULL,	'0'),
        (13,	'2025-06-16 21:28:48.956499',	'2025-06-16 21:28:48.956512',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.favicon.keyword',	'1',	NULL,	9,	12,	'0'),
        (14,	'2025-06-16 21:28:48.956898',	'2025-06-16 21:28:48.956909',	1,	'auto-generated',	NULL,	'title',	'TEXT',	NULL,	'web.title',	'1',	NULL,	9,	NULL,	'0'),
        (15,	'2025-06-16 21:28:48.957282',	'2025-06-16 21:28:48.957292',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.title.keyword',	'1',	NULL,	9,	14,	'0'),
        (16,	'2025-06-16 21:28:48.957698',	'2025-06-16 21:28:48.957708',	1,	'auto-generated',	NULL,	'url',	'TEXT',	NULL,	'web.url',	'1',	NULL,	9,	NULL,	'0'),
        (17,	'2025-06-16 21:28:48.958211',	'2025-06-16 21:28:48.958224',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.url.keyword',	'1',	NULL,	9,	16,	'0'),
        (33,	'2025-06-17 11:55:28.298892',	'2025-06-17 11:55:28.298916',	1,	'auto-generated',	NULL,	'chunkText',	'TEXT',	NULL,	'chunkText',	'1',	NULL,	2,	NULL,	'0'),
        (34,	'2025-06-17 11:55:28.301116',	'2025-06-17 11:55:28.301139',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'chunkText.keyword',	'1',	NULL,	2,	33,	'0'),
        (35,	'2025-06-17 11:55:28.302387',	'2025-06-17 11:55:28.302408',	1,	'auto-generated',	NULL,	'number',	'INTEGER',	NULL,	'number',	'0',	NULL,	2,	NULL,	'0'),
        (36,	'2025-06-17 11:55:28.303758',	'2025-06-17 11:55:28.30379',	1,	'auto-generated',	NULL,	'total',	'INTEGER',	NULL,	'total',	'0',	NULL,	2,	NULL,	'0'),
        (37,	'2025-06-17 11:55:28.3046',	'2025-06-17 11:55:28.30461',	NULL,	'auto-generated',	NULL,	'next',	'OBJECT',	NULL,	'next',	'0',	NULL,	2,	NULL,	'0'),
        (38,	'2025-06-17 11:55:28.305053',	'2025-06-17 11:55:28.305061',	1,	'auto-generated',	NULL,	'chunkText',	'TEXT',	NULL,	'next.chunkText',	'1',	NULL,	2,	37,	'0'),
        (39,	'2025-06-17 11:55:28.305472',	'2025-06-17 11:55:28.305479',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'next.chunkText.keyword',	'1',	NULL,	2,	38,	'0'),
        (40,	'2025-06-17 11:55:28.305862',	'2025-06-17 11:55:28.305871',	1,	'auto-generated',	NULL,	'number',	'LONG',	NULL,	'next.number',	'0',	NULL,	2,	37,	'0'),
        (41,	'2025-06-17 11:55:28.306243',	'2025-06-17 11:55:28.30625',	NULL,	'auto-generated',	NULL,	'previous',	'OBJECT',	NULL,	'previous',	'0',	NULL,	2,	NULL,	'0'),
        (42,	'2025-06-17 11:55:28.306707',	'2025-06-17 11:55:28.306741',	1,	'auto-generated',	NULL,	'chunkText',	'TEXT',	NULL,	'previous.chunkText',	'1',	NULL,	2,	41,	'0'),
        (43,	'2025-06-17 11:55:28.307233',	'2025-06-17 11:55:28.30724',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'previous.chunkText.keyword',	'1',	NULL,	2,	42,	'0'),
        (44,	'2025-06-17 11:55:28.307652',	'2025-06-17 11:55:28.307659',	1,	'auto-generated',	NULL,	'number',	'LONG',	NULL,	'previous.number',	'0',	NULL,	2,	41,	'0');

        INSERT INTO "tab" ("id", "create_date", "modified_date", "description", "name", "priority") VALUES
        (4,	'2025-06-18 10:27:58.287845',	'2025-06-18 10:27:58.287881',	'',	'Tutti i risultati',	0);

        INSERT INTO "rag_configuration" ("id", "create_date", "modified_date", "description", "name", "type", "prompt", "rephrase_prompt", "prompt_no_rag", "rag_tool_description", "chunk_window", "reformulate", "json_config") VALUES
        (1,	'2025-06-18 10:26:07.107759',	'2025-06-18 10:26:07.108051',	'',	'chat rag configuration',	'CHAT_RAG',	'',	'Given a chat history and the latest user questionwhich might reference context in the chat history, formulate a standalone question which can be understood without the chat history. Do NOT answer the question,  just reformulate it if needed and otherwise return it as is.',	'',	'',	0,	'1',	'{
        "rerank": false,
        "metadata":{
            "web":{
                "title":"title",
                "url":"url",
                "content":"chunkText"
            }
        }
        }
        '),
        (3,	'2025-06-18 10:27:30.198161',	'2025-06-18 10:27:30.198186',	'',	'simple generate configuration',	'SIMPLE_GENERATE',	'',	'',	'',	'',	0,	'0',	'{
        "rerank": false,
        "metadata":{
            "web":{
                "title":"title",
                "url":"url",
                "content":"content"
            }
        }
        }
        ');

        INSERT INTO "bucket" ("id", "create_date", "modified_date", "description", "name", "query_analysis_id", "search_config_id", "refresh_on_suggestion_category", "language_id", "refresh_on_tab", "refresh_on_date", "refresh_on_query", "retrieve_type", "rag_configuration_chat_id", "rag_configuration_chat_tool_id", "rag_configuration_simple_generate_id") VALUES
        (18,	'2025-06-17 10:37:18.417429',	'2025-06-18 10:27:40.952599',	'',	'test',	NULL,	NULL,	'0',	NULL,	'0',	'0',	'0',	'HYBRID',	1,	NULL,	3);

        INSERT INTO "tenant_binding" ("id", "create_date", "modified_date", "virtual_host", "tenant_binding_bucket_id", "embedding_model_id", "large_language_model_id") VALUES
        (1,	NULL,	'2025-06-17 11:55:52.94292',	'demo.openk9.localhost',	18,	NULL,	NULL);

        END IF;
        
END $$;