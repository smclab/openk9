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

        CREATE TABLE IF NOT EXISTS "openk9_liquibase"."databasechangelog" (
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

        INSERT INTO "openk9_liquibase"."databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES
        ('1669810381617-1',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.616321',	1,	'EXECUTED',	'8:df29a16e4b44a7a031b77b33d9fa226b',	'createTable tableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-2',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.625354',	2,	'EXECUTED',	'8:b29f6e6df757a45b04eab136f4f2581e',	'createTable tableName=token_tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-3',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.633468',	3,	'EXECUTED',	'8:077b6ad7ca354263bbab9b80c73622da',	'createTable tableName=query_analysis',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-4',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.640695',	4,	'EXECUTED',	'8:4e132254d8cb88afbe53db7bce58519b',	'createTable tableName=doc_type',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-5',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.64704',	5,	'EXECUTED',	'8:07cabadb09d30651b6e76032bc2ab13f',	'createTable tableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-6',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.655355',	6,	'EXECUTED',	'8:ab17f182ab357c527b5dd7e653a65383',	'createTable tableName=suggestion_category',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-7',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.664069',	7,	'EXECUTED',	'8:ec136ba87b7293524c66eb5e357f1207',	'createTable tableName=search_config',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-8',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.670262',	8,	'EXECUTED',	'8:ca5d809b6d73a91d445f884dfd33147c',	'createTable tableName=tenant_binding',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-9',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.678783',	9,	'EXECUTED',	'8:5cef4b9ab46783732663a197997958d0',	'createTable tableName=annotator',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-10',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.687867',	10,	'EXECUTED',	'8:1c5ca1065335674477c257c317e5245c',	'createTable tableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-11',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.696581',	11,	'EXECUTED',	'8:721e91fdf41dcf128fa9b848a95a2bcd',	'createTable tableName=data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-12',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.706203',	12,	'EXECUTED',	'8:d76c5dd35209381ea041a2ba97adce48',	'createTable tableName=rule',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-13',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.713759',	13,	'EXECUTED',	'8:2b5eebe16b3348fc9f7229ec26b7aafd',	'createTable tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-14',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.722682',	14,	'EXECUTED',	'8:58ee6514e31db665066e769330f0015d',	'createTable tableName=char_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-15',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.731793',	15,	'EXECUTED',	'8:67b552316927a38bd6da4a694a6aaaf9',	'createTable tableName=enrich_pipeline',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-16',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.739812',	16,	'EXECUTED',	'8:9b09835b2c10d8bd9933002de0709efc',	'createTable tableName=token_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-17',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.747763',	17,	'EXECUTED',	'8:7573734622570f023e617ae3a96ae80f',	'createTable tableName=plugin_driver',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-18',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.754601',	18,	'EXECUTED',	'8:7ce928070d733b28884ca54dc4678860',	'createTable tableName=tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-19',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.761242',	19,	'EXECUTED',	'8:0c5f2e6264816f56d42370b55dec8af3',	'createTable tableName=tokenizer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-20',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.772422',	20,	'EXECUTED',	'8:5352b7b0921edef67f171c4e2ed22739',	'createTable tableName=doc_type_template',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-21',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.779334',	21,	'EXECUTED',	'8:11d4958c7f7d3dadbf957dd98704c02e',	'createTable tableName=analyzer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-22',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.784675',	22,	'EXECUTED',	'8:629bdf7ec3190bac38b44f794a1ad657',	'addUniqueConstraint constraintName=field_name_doc_type_id_parent_doc_type_field_id, tableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-23',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.790055',	23,	'EXECUTED',	'8:fd2a2b552d785bfe152a8af0dc0ca64c',	'addUniqueConstraint constraintName=uc_tokentab_name_tab_id, tableName=token_tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-24',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.794761',	24,	'EXECUTED',	'8:ac40e8a879982e6cf83dfc6c3255edc8',	'addUniqueConstraint constraintName=uk_712f8fp1ftw1ug66hrmkgphrf, tableName=token_tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-25',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.799218',	25,	'EXECUTED',	'8:841ff6c0215948ff7a9436336bc39ea8',	'addUniqueConstraint constraintName=uk_26k3dv00807y6dy0x82xx8mjd, tableName=query_analysis',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-26',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.8047',	26,	'EXECUTED',	'8:03f0edb3afba3482d2e11ce9272370f6',	'addUniqueConstraint constraintName=uk_2l8a7vqh0i6r6tb8cb9j6yf6n, tableName=doc_type',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-27',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.809574',	27,	'EXECUTED',	'8:2a5b65a626fa2844d3acbd3f0fa31118',	'addUniqueConstraint constraintName=uk_30eqiwulrtffhilqri8poy0e9, tableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-28',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.814055',	28,	'EXECUTED',	'8:f1ed2052b8f080ae6d813e391e396dee',	'addUniqueConstraint constraintName=uk_4c591r8ch4g8rilsgfvb32kgn, tableName=suggestion_category',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-29',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.818345',	29,	'EXECUTED',	'8:105cfbd9eff2b07a809444270ce358f0',	'addUniqueConstraint constraintName=uk_5abkod761uq37sv3j2p4weaf0, tableName=search_config',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-30',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.824612',	30,	'EXECUTED',	'8:83979c693ed504d48e4b6acd9e266bc7',	'addUniqueConstraint constraintName=uk_6wrs6ncojuw0wde7a5jw2wjh1, tableName=tenant_binding',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-31',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.828972',	31,	'EXECUTED',	'8:be7d8a3e02a000c16365f8a66e39c28a',	'addUniqueConstraint constraintName=uk_9318c5hvkjhrqjlq8rd3jkhe9, tableName=annotator',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-32',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.83334',	32,	'EXECUTED',	'8:4acd1e73ab50aa22adcbb5100cb90aba',	'addUniqueConstraint constraintName=uk_c270jcqlmvthpcasvusahxb7h, tableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-33',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.838992',	33,	'EXECUTED',	'8:7ce9ebc054f3d65749af19d659d7b1d8',	'addUniqueConstraint constraintName=uk_ecnihy9mdplwt30pbahqcv9fw, tableName=data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('add-column-analyzer-table',	'openk9',	'db/datasource/2022/12/06-01-changelog.xml',	'2025-04-27 17:59:37.09741',	89,	'EXECUTED',	'8:df99fbb5e20001b59c61d854b8bd4900',	'addColumn tableName=analyzer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-34',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.844196',	34,	'EXECUTED',	'8:58856d2237464e326feeac44c2421a38',	'addUniqueConstraint constraintName=uk_g0aibm7vybna15mqfxis5nnf1, tableName=rule',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-35',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.849013',	35,	'EXECUTED',	'8:c2d9f6018bfdb14c4b64e3c79773ade3',	'addUniqueConstraint constraintName=uk_he0xrer6rh4dgaalutt7prhbm, tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-36',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.853711',	36,	'EXECUTED',	'8:93390326f1eccffcdf23390c915585a4',	'addUniqueConstraint constraintName=uk_hgk1crqb2kqnbkad5mr76v4v0, tableName=char_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-37',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.860519',	37,	'EXECUTED',	'8:205928088a5c4e460e824533dd374326',	'addUniqueConstraint constraintName=uk_hgxv753bx3d7mwghpktnxkk8h, tableName=enrich_pipeline',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-38',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.865693',	38,	'EXECUTED',	'8:ad26fa6e132f2bb11a5361990757446a',	'addUniqueConstraint constraintName=uk_mbf4ldxsh6umx4bx0kgcwrmg2, tableName=token_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-39',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.871116',	39,	'EXECUTED',	'8:092350b9c4704845ed01a8a453941b34',	'addUniqueConstraint constraintName=uk_o3hjnq90nnkxc3y34jumnhyg9, tableName=plugin_driver',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-40',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.879704',	40,	'EXECUTED',	'8:b7a0958d0f65193e2274f9e70cb4b0a0',	'addUniqueConstraint constraintName=uk_r17opdmucm7ij4aveppoa8wep, tableName=tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-41',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.886034',	41,	'EXECUTED',	'8:09e23f1ef8146f60abd44e7935dbd336',	'addUniqueConstraint constraintName=uk_re72pk1pijmchwmusxdil83sy, tableName=tokenizer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-42',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.89248',	42,	'EXECUTED',	'8:ae476e7657c08eb16cd232d24daac961',	'addUniqueConstraint constraintName=uk_t7tmy5syhhbjxbfktw4l3ejdf, tableName=doc_type_template',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-43',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.898073',	43,	'EXECUTED',	'8:76ce42216b0412e8512945d7a8a3ef38',	'addUniqueConstraint constraintName=uk_tb9s0i7disnbabdksial8ic43, tableName=analyzer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-44',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.902335',	44,	'EXECUTED',	'8:9ceb883b9596bb0ffbea0d025c1339ff',	'createSequence sequenceName=hibernate_sequence',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-45',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.908185',	45,	'EXECUTED',	'8:12ed4400866a2e5b8d44bad9a0e222f7',	'createTable tableName=analyzer_char_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-46',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.914961',	46,	'EXECUTED',	'8:3267c39c21e65243ab8e639ab223af49',	'createTable tableName=analyzer_token_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-47',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.919154',	47,	'EXECUTED',	'8:9b934622b897a5ad70b49459b83ac481',	'createTable tableName=buckets_tabs',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-48',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.92653',	48,	'EXECUTED',	'8:884fb9a54aa3ac602ee73cf036d6f8dc',	'createTable tableName=data_index_doc_types',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-49',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.932236',	49,	'EXECUTED',	'8:7b302e56ea9dbe3528b82fddd43a928e',	'createTable tableName=datasource_buckets',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-50',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.93709',	50,	'EXECUTED',	'8:c1e2d365d91d9a14f9d5dd336fdec554',	'createTable tableName=enrich_pipeline_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-51',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.943513',	51,	'EXECUTED',	'8:a5d92aeaf48081fc3561ceb70286f6e3',	'createTable tableName=query_analysis_annotators',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-52',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.949449',	52,	'EXECUTED',	'8:c69ced372de290c7184d2e3e709cd66d',	'createTable tableName=query_analysis_rules',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-53',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.963134',	53,	'EXECUTED',	'8:36e3949e5f91462ebe8c737455140366',	'createTable tableName=query_parser_config',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-54',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.968542',	54,	'EXECUTED',	'8:928c6a4d5994ebb1de7046117ef87bb0',	'createTable tableName=suggestion_category_doc_type_fields',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-55',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.973565',	55,	'EXECUTED',	'8:abc35e486a6c8ca8562d0f71b019ac71',	'addForeignKeyConstraint baseTableName=data_index_doc_types, constraintName=fk1a9m9bg9q7ni7gc9ysg3rq6fp, referencedTableName=doc_type',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-56',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.977737',	56,	'EXECUTED',	'8:3e0669fadde1c20d12dc17bd08c9270c',	'addForeignKeyConstraint baseTableName=doc_type, constraintName=fk1ocwbmim5560h0bysfpunyq46, referencedTableName=doc_type_template',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-57',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.981917',	57,	'EXECUTED',	'8:d17303c2ce3d30afcfb4b3137c40f2cf',	'addForeignKeyConstraint baseTableName=token_tab, constraintName=fk37w5m3swa2cebgar0umrnytyy, referencedTableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-58',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.98512',	58,	'EXECUTED',	'8:6b7b1651bb821ac59b36e04285b0ad66',	'addForeignKeyConstraint baseTableName=suggestion_category, constraintName=fk3svyhailyqjc0iofjqpqymm78, referencedTableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-59',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.992907',	59,	'EXECUTED',	'8:b551726e1e459c292f1792db1e63323b',	'addForeignKeyConstraint baseTableName=doc_type_field, constraintName=fk44qu7fg8d1tl3bn4d43sge02x, referencedTableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-60',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:36.997706',	60,	'EXECUTED',	'8:cb98f191479db845d69891e791c39363',	'addForeignKeyConstraint baseTableName=query_analysis_rules, constraintName=fk5412heryog6cetwbsq5enp335, referencedTableName=query_analysis',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-61',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.000898',	61,	'EXECUTED',	'8:82b31e6b354662678fbd5c35a3ffb63a',	'addForeignKeyConstraint baseTableName=datasource_buckets, constraintName=fk80gj884mrv2t03t7g36qlrae9, referencedTableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-62',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.004553',	62,	'EXECUTED',	'8:51c011e7b6d22f19ab767d4c2aac43de',	'addForeignKeyConstraint baseTableName=analyzer_token_filter, constraintName=fk84vkkc91bhjv3ye2l19iimcl8, referencedTableName=token_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-63',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.008071',	63,	'EXECUTED',	'8:3bbf37c21d825365d1b1c3167c3755bb',	'addForeignKeyConstraint baseTableName=analyzer_token_filter, constraintName=fk8knl2njy74t871j2eycwu5fql, referencedTableName=analyzer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-64',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.011431',	64,	'EXECUTED',	'8:6d21f818ce4ad5a549eab8291456ec40',	'addForeignKeyConstraint baseTableName=data_index_doc_types, constraintName=fk9tu5iup1pex2vpk80jftsfbey, referencedTableName=data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-65',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.015115',	65,	'EXECUTED',	'8:880c7d8b2c1e64fbd0c08257bf2915e7',	'addForeignKeyConstraint baseTableName=query_analysis_annotators, constraintName=fka1ext4cmdvvc0pjxgci94xvvn, referencedTableName=query_analysis',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-66',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.018415',	66,	'EXECUTED',	'8:31d0f0701e0b54d399aa6c3479459790',	'addForeignKeyConstraint baseTableName=analyzer, constraintName=fkc7nh3nt723kkehd09dl3jpcmw, referencedTableName=tokenizer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-67',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.021714',	67,	'EXECUTED',	'8:1cf68e7700f373e77b91c23c564a3b9f',	'addForeignKeyConstraint baseTableName=datasource, constraintName=fkewjpbr7f4fkff02y19op5g6a, referencedTableName=enrich_pipeline',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-68',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.025242',	68,	'EXECUTED',	'8:f894d53cb573846a83ffd1b503bf50c8',	'addForeignKeyConstraint baseTableName=datasource, constraintName=fkeyxlmk7fbek60xtie38cwu2v2, referencedTableName=plugin_driver',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-69',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.029287',	69,	'EXECUTED',	'8:9b40809693ee0759772384cf9fcde923',	'addForeignKeyConstraint baseTableName=annotator, constraintName=fkfiv3bsnm26bb802cm7i1ei4q2, referencedTableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-70',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.032793',	70,	'EXECUTED',	'8:a06d2515306585751ee8525450b87635',	'addForeignKeyConstraint baseTableName=analyzer_char_filter, constraintName=fkfjki6k33j86nxxjtpjfsjk0bg, referencedTableName=analyzer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-71',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.035886',	71,	'EXECUTED',	'8:bdd79312a7b7dd5f12e8674cf6f77315',	'addForeignKeyConstraint baseTableName=doc_type_field, constraintName=fkfjuk779hqxyt5ngdk9asog0bj, referencedTableName=analyzer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-72',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.040514',	72,	'EXECUTED',	'8:bdefe0b12f3dedcf59a2e1c007959683',	'addForeignKeyConstraint baseTableName=bucket, constraintName=fkgj8f2hdk19shy1gwqu3u4ae9x, referencedTableName=query_analysis',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-73',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.044186',	73,	'EXECUTED',	'8:87e9ac36c07625a3e58f67eb42a4628b',	'addForeignKeyConstraint baseTableName=buckets_tabs, constraintName=fki0729hs5mp7hisvr1xc4kcnio, referencedTableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-74',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.047387',	74,	'EXECUTED',	'8:4d5095f7b687c4a9ac128c9abd78306f',	'addForeignKeyConstraint baseTableName=tenant_binding, constraintName=fki7bn20nxw6x541wfvvwe62t28, referencedTableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-75',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.049964',	75,	'EXECUTED',	'8:71a4c69429a34f945c7282787cfbad18',	'addForeignKeyConstraint baseTableName=datasource_buckets, constraintName=fki9t38w4aa24jenr7lp4uguxb2, referencedTableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-76',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.052435',	76,	'EXECUTED',	'8:22a12f935a72a48fc95fbd39595a5c1f',	'addForeignKeyConstraint baseTableName=analyzer_char_filter, constraintName=fki9uan06v9eosrqrj0d2ab839l, referencedTableName=char_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-77',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.05515',	77,	'EXECUTED',	'8:36180629e128c8882d76584de3076432',	'addForeignKeyConstraint baseTableName=token_tab, constraintName=fkiyk7xhe4lx9pbr64vv2yit6jd, referencedTableName=tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-78',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.057627',	78,	'EXECUTED',	'8:83e9df6d31ae3ce3904217ae5774c342',	'addForeignKeyConstraint baseTableName=query_analysis_annotators, constraintName=fkj6bkvkacmj53kv5o5k8065hvt, referencedTableName=annotator',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-79',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.060092',	79,	'EXECUTED',	'8:1ceac1fd60c17ebaa68fba0ec1aa1b5d',	'addForeignKeyConstraint baseTableName=buckets_tabs, constraintName=fkkivu1a4ehytvoy78u74r3vq3l, referencedTableName=tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-80',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.062678',	80,	'EXECUTED',	'8:6a29821cbcd511b3560111c5870e0fae',	'addForeignKeyConstraint baseTableName=suggestion_category_doc_type_fields, constraintName=fklfdkxdh7p8pkjn7qtwa2qeckc, referencedTableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-81',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.065168',	81,	'EXECUTED',	'8:a53f2d8e636798a15d024f189405d3d8',	'addForeignKeyConstraint baseTableName=suggestion_category_doc_type_fields, constraintName=fkm97oc2r4h2fi5lym1g5f8pkwj, referencedTableName=suggestion_category',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-82',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.067728',	82,	'EXECUTED',	'8:79de77fc4d2c5f0d793f3a25a125fd56',	'addForeignKeyConstraint baseTableName=query_analysis_rules, constraintName=fkmbqqghon1m8h554uh8hi0qkru, referencedTableName=rule',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-83',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.071588',	83,	'EXECUTED',	'8:eab7a6cf47980170de3f14b8b4f7ed91',	'addForeignKeyConstraint baseTableName=enrich_pipeline_item, constraintName=fkmjidj39ui867w3ig9iccnmrxb, referencedTableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-84',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.076372',	84,	'EXECUTED',	'8:4a4e297ec8bb97fba79e20bb21d2996e',	'addForeignKeyConstraint baseTableName=doc_type_field, constraintName=fkn1p060bcao42e9fn7l1mb1jju, referencedTableName=doc_type',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-85',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.079494',	85,	'EXECUTED',	'8:8919fc44a16a1ce51ec6d843bf4d5bf4',	'addForeignKeyConstraint baseTableName=bucket, constraintName=fkn9yvqd7qsenqu5unq6yfj7j2a, referencedTableName=search_config',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-86',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.087231',	86,	'EXECUTED',	'8:e2d157164336eb5bc50c71b3cd01b8a3',	'addForeignKeyConstraint baseTableName=datasource, constraintName=fknbh6wdfjww9no5xqxn65qchfy, referencedTableName=data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-87',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.09092',	87,	'EXECUTED',	'8:fa3c7d29100941c8eedf8f6f2a79eb6d',	'addForeignKeyConstraint baseTableName=enrich_pipeline_item, constraintName=fkreyxykt1gox8ghwcuobr5pjan, referencedTableName=enrich_pipeline',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1669810381617-88',	'openk9',	'db/datasource/2022/11/30-01-changelog.xml',	'2025-04-27 17:59:37.094567',	88,	'EXECUTED',	'8:5f56fabd1bb04aa39265589afbc98493',	'addForeignKeyConstraint baseTableName=query_parser_config, constraintName=fktmj4jra4g99dajgs7ilywk41b, referencedTableName=search_config',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1671629306048-1',	'openk9',	'db/datasource/2022/12/21-01-changelog.xml',	'2025-04-27 17:59:37.10369',	90,	'EXECUTED',	'8:2d46de0b307188767004910561117829',	'createTable tableName=acl_mapping',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1671629306048-2',	'openk9',	'db/datasource/2022/12/21-01-changelog.xml',	'2025-04-27 17:59:37.106894',	91,	'EXECUTED',	'8:e69faf7db1d394cbb071b6226ded87d3',	'addForeignKeyConstraint baseTableName=acl_mapping, constraintName=FK_ACL_MAPPING_ON_DOC_TYPE_FIELD, referencedTableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1671629306048-3',	'openk9',	'db/datasource/2022/12/21-01-changelog.xml',	'2025-04-27 17:59:37.109936',	92,	'EXECUTED',	'8:396c92129364996b53e35b9c66be6fc7',	'addForeignKeyConstraint baseTableName=acl_mapping, constraintName=FK_ACL_MAPPING_ON_PLUGIN_DRIVER, referencedTableName=plugin_driver',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1673543649792-1',	'openk9',	'db/datasource/2023/01/12-01-changelog.xml',	'2025-04-27 17:59:37.119677',	93,	'EXECUTED',	'8:54c80cd5f4aaf6cc8bbecf389ddbeebf',	'createTable tableName=file_resource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1673543649792-2',	'openk9',	'db/datasource/2023/01/12-01-changelog.xml',	'2025-04-27 17:59:37.124827',	94,	'EXECUTED',	'8:80a539622e888a2b170a5900e71c9f3b',	'addUniqueConstraint constraintName=uc_fileresource_fileid_datasource_id, tableName=file_resource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1673543649792-3',	'openk9',	'db/datasource/2023/01/12-01-changelog.xml',	'2025-04-27 17:59:37.130268',	95,	'EXECUTED',	'8:acfbfd0c71ced9aa20da1f112d5470d7',	'addUniqueConstraint constraintName=uc_fileresource_resource_id, tableName=file_resource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1674745590142-1',	'openk9',	'db/datasource/2023/01/26-01-changelog.xml',	'2025-04-27 17:59:37.133251',	96,	'EXECUTED',	'8:9ac854d1b38355ec85656dab512cab25',	'addColumn tableName=suggestion_category',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1675070559412-1',	'openk9',	'db/datasource/2023/01/30-01-changelog.xml',	'2025-04-27 17:59:37.135888',	97,	'EXECUTED',	'8:6a9748198cf7ad5649b4ba7af9fa1915',	'addColumn tableName=search_config',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1677676535139-1',	'openk9',	'db/datasource/2023/03/01-01-changelog.xml',	'2025-04-27 17:59:37.139673',	98,	'EXECUTED',	'8:b4fc6a5d0239ffa8019c87896c0d2cd6',	'addColumn tableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1677778990419-1',	'openk9',	'db/datasource/2023/03/02-01-changelog.xml',	'2025-04-27 17:59:37.142638',	99,	'EXECUTED',	'8:d18782a95be31ee51600fda183f48de6',	'update tableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1677831817838-1',	'openk9',	'db/datasource/2023/03/03-01-changelog.xml',	'2025-04-27 17:59:37.146024',	100,	'EXECUTED',	'8:4e0e7eca091f77a4926237aa06159b1d',	'addColumn tableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1678121122655-1',	'openk9',	'db/datasource/2023/03/06-01-changelog.xml',	'2025-04-27 17:59:37.150998',	101,	'EXECUTED',	'8:f221371d652662e0e2dc6d82ba18e58d',	'renameColumn newColumnName=sortable, oldColumnName=sorteable, tableName=doc_type_field',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1679590501751-1',	'openk9',	'db/datasource/2023/03/23-01-changelog.xml',	'2025-04-27 17:59:37.154742',	102,	'EXECUTED',	'8:a3dc5aabfb614dff9f818103686d9378',	'addColumn tableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1680539051683-1',	'openk9',	'db/datasource/2023/04/03-01-changelog.xml',	'2025-04-27 17:59:37.15939',	103,	'EXECUTED',	'8:807cefc1f3a0d2cf33d2ed1aa6e87574',	'renameColumn newColumnName=script, oldColumnName=validation_script, tableName=enrich_item',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1682504012273-1',	'openk9',	'db/datasource/2023/04/26-01-changelog.xml',	'2025-04-27 17:59:37.171034',	104,	'EXECUTED',	'8:2566b56667fd915aaf07ec9e33c135db',	'createTable tableName=tab_token_tab; sql; dropForeignKeyConstraint baseTableName=token_tab, constraintName=fkiyk7xhe4lx9pbr64vv2yit6jd; dropColumn columnName=tab_id, tableName=token_tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1682504012273-2',	'openk9',	'db/datasource/2023/04/26-01-changelog.xml',	'2025-04-27 17:59:37.175606',	105,	'EXECUTED',	'8:d2d80dc3143fc8734c051f46f810a809',	'addForeignKeyConstraint baseTableName=tab_token_tab, constraintName=fk_tab_token_tab, referencedTableName=tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1682504012273-3',	'openk9',	'db/datasource/2023/04/26-01-changelog.xml',	'2025-04-27 17:59:37.180107',	106,	'EXECUTED',	'8:3f997636819ed4b029b415bd111c6b67',	'addForeignKeyConstraint baseTableName=tab_token_tab, constraintName=fk_token_tab_tab, referencedTableName=token_tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1682524504592-1',	'openk9',	'db/datasource/2023/04/26-02-changelog.xml',	'2025-04-27 17:59:37.183715',	107,	'EXECUTED',	'8:95ad15f6230ae4aa6fbdea8262d7e53b',	'addColumn tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686583762945-1',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-04-27 17:59:37.186923',	108,	'EXECUTED',	'8:325f2e6d5430af3986022ab1bb27f4d8',	'addColumn tableName=data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686583762945-2',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-04-27 17:59:37.189731',	109,	'EXECUTED',	'8:1ef240e809218d6f4955fef4f5677972',	'sql',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686583762945-3',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-04-27 17:59:37.192813',	110,	'EXECUTED',	'8:b6a737641f8314ab9c5792b246c05313',	'sql',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686583762945-4',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-04-27 17:59:37.195869',	111,	'EXECUTED',	'8:69d981ab502a7bfc4d6eb309f3ba139c',	'sql',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686583762945-5',	'openk9',	'db/datasource/2023/06/12-01-changelog.xml',	'2025-04-27 17:59:37.200062',	112,	'EXECUTED',	'8:38097340c5f0bec4ca2c084e165834a5',	'addNotNullConstraint columnName=datasource_id, tableName=data_index; addForeignKeyConstraint baseTableName=data_index, constraintName=FK_data_index_datasource, referencedTableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686670878042-1',	'openk9',	'db/datasource/2023/06/13-01-changelog.xml',	'2025-04-27 17:59:37.208458',	113,	'EXECUTED',	'8:0702e12aee52275d35998da4e4fd5718',	'createTable tableName=scheduler',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686670878042-2',	'openk9',	'db/datasource/2023/06/13-01-changelog.xml',	'2025-04-27 17:59:37.215927',	114,	'EXECUTED',	'8:dd7cf9121b079bb05222dc27fdf528cc',	'addForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_datasource, referencedTableName=datasource; addForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_old_data_index, referencedTableName=data_index; a...',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1686824425790-1',	'openk9',	'db/datasource/2023/06/15-01-changelog.xml',	'2025-04-27 17:59:37.218806',	115,	'EXECUTED',	'8:a491a351ef8f2f6b138ad46a2dff02ca',	'dropNotNullConstraint columnName=new_data_index_id, tableName=scheduler',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1688720936322-1',	'openk9',	'db/datasource/2023/07/07-01-changelog.xml',	'2025-04-27 17:59:37.222133',	116,	'EXECUTED',	'8:8a758b2df3bc4e3507085865e412dc94',	'addColumn tableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1688630335734-1',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-04-27 17:59:37.229656',	117,	'EXECUTED',	'8:a813b0bcb6ca023811ef02026e0ef97d',	'createTable tableName=language',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1688630335734-2',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-04-27 17:59:37.233643',	118,	'EXECUTED',	'8:1c27137a3defe3e874ccf660ac6e2b32',	'addUniqueConstraint constraintName=0998a292-02a9-4930-a8ba-e31d7af5cee2, tableName=language',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1688630335734-3',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-04-27 17:59:37.239226',	119,	'EXECUTED',	'8:70556f5ebfea7614e3804ae90dd5b5d2',	'createTable tableName=bucket_language',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1688630335734-4',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-04-27 17:59:37.242548',	120,	'EXECUTED',	'8:cc4798b16e177dffdd3caef6d01980c7',	'addColumn tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1688630335734-5',	'openk9',	'db/datasource/2023/07/14-01-changelog.xml',	'2025-04-27 17:59:37.246051',	121,	'EXECUTED',	'8:93fb5baddeba813e1c3bdd77e071239d',	'addForeignKeyConstraint baseTableName=bucket_language, constraintName=313a9cab-a687-486d-92ae-2ce74fa89951, referencedTableName=language',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1689667487108-1',	'openk9',	'db/datasource/2023/07/18-01-changelog.xml',	'2025-04-27 17:59:37.248829',	122,	'EXECUTED',	'8:f4f9ef996fbb768e5e7dc45028958ae3',	'addColumn tableName=tokenizer',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1689667487108-2',	'openk9',	'db/datasource/2023/07/18-01-changelog.xml',	'2025-04-27 17:59:37.25143',	123,	'EXECUTED',	'8:228f7673529f0201b74bd91f4883ee64',	'addColumn tableName=token_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1689667487108-3',	'openk9',	'db/datasource/2023/07/18-01-changelog.xml',	'2025-04-27 17:59:37.255912',	124,	'EXECUTED',	'8:dea156d29904f9e5eb603e910e208321',	'addColumn tableName=char_filter',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1690464708841-1',	'openk9',	'db/datasource/2023/07/29-01-changelog.xml',	'2025-04-27 17:59:37.261664',	125,	'EXECUTED',	'8:70a045e87fde9a6759bd3d0b183e5b87',	'dropForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_old_data_index; dropForeignKeyConstraint baseTableName=scheduler, constraintName=fk_scheduler_new_data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1691144835465-1',	'openk9',	'db/datasource/2023/08/04-01-changelog.xml',	'2025-04-27 17:59:37.272485',	126,	'EXECUTED',	'8:099eba6120d429296e3c56e15d12a746',	'createTable tableName=translation',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1691144835465-2',	'openk9',	'db/datasource/2023/08/04-01-changelog.xml',	'2025-04-27 17:59:37.280041',	127,	'EXECUTED',	'8:0d566b429b3c3466d0612caa92dbadfd',	'createIndex indexName=idx_translation_key, tableName=translation; createIndex indexName=idx_translation_entities, tableName=translation',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1693469697309-1',	'openk9',	'db/datasource/2023/08/31-01-changelog.xml',	'2025-04-27 17:59:37.282657',	128,	'EXECUTED',	'8:082b8663bcfb62b074f401a9964421ff',	'dropIndex indexName=idx_translation_entities, tableName=translation',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1693469697309-2',	'openk9',	'db/datasource/2023/08/31-01-changelog.xml',	'2025-04-27 17:59:37.28692',	129,	'EXECUTED',	'8:e563dbe97cd84cd548b4a34745c1d332',	'createIndex indexName=idx_translation_entities, tableName=translation',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1699525032881-1',	'openk9',	'db/datasource/2023/11/09-01-changelog.xml',	'2025-04-27 17:59:37.291387',	130,	'EXECUTED',	'8:5c365853798d14f9894c3f24960bc118',	'createTable tableName=token_tab_extra_params',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1699525032881-2',	'openk9',	'db/datasource/2023/11/09-01-changelog.xml',	'2025-04-27 17:59:37.295974',	131,	'EXECUTED',	'8:fc07c81dbde3d6176916ec8b31b1214a',	'createIndex indexName=idx_token_tab_id_extra_params, tableName=token_tab_extra_params',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1699629168661-1',	'openk9',	'db/datasource/2023/11/10-01-changelog.xml',	'2025-04-27 17:59:37.298269',	132,	'EXECUTED',	'8:a809b7b621462378bdcdfc5eb07f8641',	'renameColumn newColumnName=refresh_on_suggestion_category, oldColumnName=handle_dynamic_filters, tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1699629168661-2',	'openk9',	'db/datasource/2023/11/10-01-changelog.xml',	'2025-04-27 17:59:37.30121',	133,	'EXECUTED',	'8:d8cb29af84c788d2352760ce9c46d54f',	'addColumn tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1700143191681-1',	'openk9',	'db/datasource/2023/11/16-01-changelog.xml',	'2025-04-27 17:59:37.303804',	134,	'EXECUTED',	'8:2b200c688d1bad8e706dab72b398db06',	'addColumn tableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1700143191681-2',	'openk9',	'db/datasource/2023/11/16-01-changelog.xml',	'2025-04-27 17:59:37.305519',	135,	'EXECUTED',	'8:1c0cbe18dacdd061b088268fbe16bc2b',	'update tableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1700143191681-3',	'openk9',	'db/datasource/2023/11/16-01-changelog.xml',	'2025-04-27 17:59:37.308274',	136,	'EXECUTED',	'8:2c818a06d6c641f26c02dff1947af248',	'dropColumn tableName=datasource',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1702639488634-1',	'openk9',	'db/datasource/2023/11/15-01-changelog.xml',	'2025-04-27 17:59:37.314038',	137,	'EXECUTED',	'8:7c3fa0407e6ceb014afcd11063b3abbe',	'createTable tableName=annotator_extra_params',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1702639488634-2',	'openk9',	'db/datasource/2023/11/15-01-changelog.xml',	'2025-04-27 17:59:37.318422',	138,	'EXECUTED',	'8:6aec2634398c28ae424f02791dcb792c',	'createIndex indexName=idx_annotator_id_extra_params, tableName=annotator_extra_params',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1707123456-1',	'openk9',	'db/datasource/2024/02/05-01-changelog.xml',	'2025-04-27 17:59:37.321264',	139,	'EXECUTED',	'8:c1966f9bdf0eff63acaa2cf0968ff58b',	'addColumn tableName=plugin_driver',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1710009174795-1',	'openk9',	'db/datasource/2024/03/09-01-changelog.xml',	'2025-04-27 17:59:37.328665',	140,	'EXECUTED',	'8:42e7dcf2f746d12fa368e9c24b131435',	'createTable tableName=sorting',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1710009174795-2',	'openk9',	'db/datasource/2024/03/09-01-changelog.xml',	'2025-04-27 17:59:37.331533',	141,	'EXECUTED',	'8:d0f7917d6a8115fc336c8d47ed9d7f64',	'createTable tableName=buckets_sortings',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1711366596747-1',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-04-27 17:59:37.33566',	142,	'EXECUTED',	'8:60c28fec289079112fdad61c9f336001',	'createTable tableName=tab_sorting',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1711366596747-2',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-04-27 17:59:37.338538',	143,	'EXECUTED',	'8:3810109e2c2cdebc3f1129653b2cdb38',	'addForeignKeyConstraint baseTableName=tab_sorting, constraintName=fk_tab_sorting, referencedTableName=tab',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1711366596747-3',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-04-27 17:59:37.341329',	144,	'EXECUTED',	'8:d149a464d6ae3e5aeb341880c462dcfa',	'addForeignKeyConstraint baseTableName=tab_sorting, constraintName=fk_sorting_tab, referencedTableName=sorting',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1711712316688-1',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-04-27 17:59:37.344123',	145,	'EXECUTED',	'8:5e4cb7dc78a24143809d9f39bb817802',	'addColumn tableName=scheduler',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1715071011646-1',	'openk9',	'db/datasource/2024/03/25-01-changelog.xml',	'2025-04-27 17:59:37.345973',	146,	'EXECUTED',	'8:7156b1e0f6d83b0ef14e636175a722ef',	'update tableName=scheduler',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1719406947449-1',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-04-27 17:59:37.353004',	147,	'EXECUTED',	'8:393ba94868375ed0b6f162cbe0a5ab0d',	'createTable tableName=embedding_model',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1719406947449-2',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-04-27 17:59:37.358788',	148,	'EXECUTED',	'8:4d53656b8d8ca578ee6b35fb911c1a83',	'createTable tableName=vector_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1719406947449-3',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-04-27 17:59:37.361462',	149,	'EXECUTED',	'8:7b86141cdd595fb0f8b648d7f43d50a6',	'addColumn tableName=tenant_binding',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1719406947449-4',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-04-27 17:59:37.365343',	150,	'EXECUTED',	'8:32e80cd7a277801c93bb025c62c3a65c',	'addColumn tableName=data_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1719406947449-5',	'openk9',	'db/datasource/2024/06/26-01-changelog.xml',	'2025-04-27 17:59:37.368831',	151,	'EXECUTED',	'8:d23eafc9af9b377e02d8d230cf531417',	'addForeignKeyConstraint baseTableName=tenant_binding, constraintName=tenant_binding_embedding_model_fk, referencedTableName=embedding_model; addForeignKeyConstraint baseTableName=data_index, constraintName=data_index_vector_index_fk, referencedTab...',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1719406947449-1',	'openk9',	'db/datasource/2024/07/08-01-changelog.xml',	'2025-04-27 17:59:37.372306',	152,	'EXECUTED',	'8:c9ae2c90b59d72de89368c3304c10b69',	'renameColumn newColumnName=text_embedding_field, oldColumnName=field_json_path, tableName=vector_index; addColumn tableName=vector_index; addColumn tableName=vector_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1720517315859-1',	'openk9',	'db/datasource/2024/07/09-01-changelog.xml',	'2025-04-27 17:59:37.378696',	153,	'EXECUTED',	'8:2eeeb429c8b7f7fd3bfc3c3252be7bb5',	'createTable tableName=large_language_model',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1720517315859-2',	'openk9',	'db/datasource/2024/07/09-01-changelog.xml',	'2025-04-27 17:59:37.381258',	154,	'EXECUTED',	'8:90536a4b78b0abe5434a5bfc11c906f5',	'addColumn tableName=tenant_binding',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1720517315859-3',	'openk9',	'db/datasource/2024/07/09-01-changelog.xml',	'2025-04-27 17:59:37.384272',	155,	'EXECUTED',	'8:444da59c7954e7d5bf7ba3b556b84adb',	'addForeignKeyConstraint baseTableName=tenant_binding, constraintName=tenant_binding_large_language_model_fk, referencedTableName=large_language_model',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1720539264956-1',	'openk9',	'db/datasource/2024/07/09-02-changelog.xml',	'2025-04-27 17:59:37.386886',	156,	'EXECUTED',	'8:b60ce6bda108204f1d0124be8c99de24',	'renameColumn newColumnName=json_config, oldColumnName=prompt_template, tableName=large_language_model',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1720602149181-1',	'openk9',	'db/datasource/2024/07/10-01-changelog.xml',	'2025-04-27 17:59:37.390416',	157,	'EXECUTED',	'8:2d1288ceef897b0eac33e856ab65c9ec',	'addColumn tableName=bucket',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1721047170914-1',	'openk9',	'db/datasource/2024/07/15-01-changelog.xml',	'2025-04-27 17:59:37.393802',	158,	'EXECUTED',	'8:54d62cd9e5b54c8d82ac597c8c444c45',	'addColumn tableName=vector_index; addColumn tableName=vector_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1721203667145-1',	'openk9',	'db/datasource/2024/07/17-01-changelog.xml',	'2025-04-27 17:59:37.39757',	159,	'EXECUTED',	'8:cead421c0719efc16f63039b39d5456b',	'addDefaultValue columnName=chunk_window_size, tableName=vector_index; addDefaultValue columnName=chunk_type, tableName=vector_index; addDefaultValue columnName=json_config, tableName=vector_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1721212233876-1',	'openk9',	'db/datasource/2024/07/17-02-changelog.xml',	'2025-04-27 17:59:37.40016',	160,	'EXECUTED',	'8:7c3fcc189851dc42aa60d4ea4d4ca091',	'update tableName=vector_index; update tableName=vector_index; update tableName=vector_index',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582'),
        ('1726562109788-1',	'openk9',	'db/datasource/2024/09/17-01-changelog.xml',	'2025-04-27 17:59:37.402858',	161,	'EXECUTED',	'8:215b16bd6092b1b3ef91d4a444788955',	'addColumn tableName=scheduler',	'',	NULL,	'4.18.0',	NULL,	NULL,	'5776776582');

        CREATE TABLE IF NOT EXISTS "openk9_liquibase"."databasechangeloglock" (
            "id" integer NOT NULL,
            "locked" boolean NOT NULL,
            "lockgranted" timestamp,
            "lockedby" character varying(255),
            CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        INSERT INTO "openk9_liquibase"."databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
        (1,	'0',	NULL,	NULL);


        CREATE SEQUENCE IF NOT EXISTS "openk9"."hibernate_sequence";

        CREATE TABLE IF NOT EXISTS "openk9"."acl_mapping" (
            "user_field" character varying(255) NOT NULL,
            "doc_type_field_id" bigint NOT NULL,
            "plugin_driver_id" bigint NOT NULL,
            CONSTRAINT "pk_acl_mapping" PRIMARY KEY ("doc_type_field_id", "plugin_driver_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."analyzer" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."analyzer_char_filter" (
            "analyzer" bigint NOT NULL,
            "char_filter" bigint NOT NULL,
            CONSTRAINT "analyzer_char_filter_pkey" PRIMARY KEY ("analyzer", "char_filter")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."analyzer_token_filter" (
            "analyzer" bigint NOT NULL,
            "token_filter" bigint NOT NULL,
            CONSTRAINT "analyzer_token_filter_pkey" PRIMARY KEY ("analyzer", "token_filter")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."annotator" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "field_name" character varying(255) NOT NULL,
            "fuziness" character varying(255),
            "name" character varying(255) NOT NULL,
            "size" integer,
            "type" character varying(255) NOT NULL,
            "doc_type_field_id" bigint,
            CONSTRAINT "annotator_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_9318c5hvkjhrqjlq8rd3jkhe9 ON openk9.annotator USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."annotator_extra_params" (
            "annotator_id" bigint NOT NULL,
            "key" character varying(50) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "annotator_extra_params_pkey" PRIMARY KEY ("annotator_id", "key")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_annotator_id_extra_params ON openk9.annotator_extra_params USING btree (annotator_id);


        CREATE TABLE IF NOT EXISTS "openk9"."bucket" (
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
            "retrieve_type" character varying(255),
            CONSTRAINT "bucket_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_he0xrer6rh4dgaalutt7prhbm ON openk9.bucket USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."bucket_language" (
            "language_id" bigint NOT NULL,
            "bucket_id" bigint NOT NULL,
            CONSTRAINT "bucket_language_pkey" PRIMARY KEY ("language_id", "bucket_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."buckets_sortings" (
            "buckets_id" bigint NOT NULL,
            "sortings_id" bigint NOT NULL
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."buckets_tabs" (
            "buckets_id" bigint NOT NULL,
            "tabs_id" bigint NOT NULL
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."char_filter" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."data_index" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "datasource_id" bigint NOT NULL,
            "vector_index_id" bigint,
            CONSTRAINT "data_index_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_ecnihy9mdplwt30pbahqcv9fw ON openk9.data_index USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."data_index_doc_types" (
            "data_index_id" bigint NOT NULL,
            "doc_types_id" bigint NOT NULL,
            CONSTRAINT "data_index_doc_types_pkey" PRIMARY KEY ("data_index_id", "doc_types_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."datasource" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "json_config" text,
            "last_ingestion_date" timestamp,
            "name" character varying(255) NOT NULL,
            "schedulable" boolean NOT NULL,
            "scheduling" character varying(255) NOT NULL,
            "data_index_id" bigint,
            "enrich_pipeline_id" bigint,
            "plugin_driver_id" bigint,
            "reindex_rate" integer DEFAULT '0',
            CONSTRAINT "datasource_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_c270jcqlmvthpcasvusahxb7h ON openk9.datasource USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."datasource_buckets" (
            "datasource_id" bigint NOT NULL,
            "buckets_id" bigint NOT NULL,
            CONSTRAINT "datasource_buckets_pkey" PRIMARY KEY ("datasource_id", "buckets_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."doc_type" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "doc_type_template_id" bigint,
            CONSTRAINT "doc_type_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_2l8a7vqh0i6r6tb8cb9j6yf6n ON openk9.doc_type USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."doc_type_field" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."doc_type_template" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."embedding_model" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "api_url" character varying(255) NOT NULL,
            "api_key" character varying(255),
            CONSTRAINT "embedding_model_id" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."enrich_item" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."enrich_pipeline" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "enrich_pipeline_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_hgxv753bx3d7mwghpktnxkk8h ON openk9.enrich_pipeline USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."enrich_pipeline_item" (
            "enrich_item_id" bigint NOT NULL,
            "enrich_pipeline_id" bigint NOT NULL,
            "weight" real NOT NULL,
            CONSTRAINT "enrich_pipeline_item_pkey" PRIMARY KEY ("enrich_item_id", "enrich_pipeline_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."file_resource" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."language" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "name" character varying(255) NOT NULL,
            "value" character varying(255) NOT NULL,
            "bucket_id" bigint,
            CONSTRAINT "language_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX "0998a292-02a9-4930-a8ba-e31d7af5cee2" ON openk9.language USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."large_language_model" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "api_url" character varying(255) NOT NULL,
            "api_key" character varying(255),
            "json_config" text,
            CONSTRAINT "large_language_model_id" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."plugin_driver" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."query_analysis" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "stopwords" text,
            CONSTRAINT "query_analysis_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_26k3dv00807y6dy0x82xx8mjd ON openk9.query_analysis USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."query_analysis_annotators" (
            "query_analysis_id" bigint NOT NULL,
            "annotators_id" bigint NOT NULL,
            CONSTRAINT "query_analysis_annotators_pkey" PRIMARY KEY ("query_analysis_id", "annotators_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."query_analysis_rules" (
            "query_analysis_id" bigint NOT NULL,
            "rules_id" bigint NOT NULL,
            CONSTRAINT "query_analysis_rules_pkey" PRIMARY KEY ("query_analysis_id", "rules_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."query_parser_config" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."rule" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."scheduler" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."search_config" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."sorting" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."suggestion_category" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "priority" real NOT NULL,
            "bucket_id" bigint,
            "multi_select" boolean,
            CONSTRAINT "suggestion_category_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_4c591r8ch4g8rilsgfvb32kgn ON openk9.suggestion_category USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."suggestion_category_doc_type_fields" (
            "suggestion_category_id" bigint NOT NULL,
            "doc_type_fields_id" bigint NOT NULL,
            CONSTRAINT "suggestion_category_doc_type_fields_pkey" PRIMARY KEY ("suggestion_category_id", "doc_type_fields_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."tab" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "priority" integer NOT NULL,
            CONSTRAINT "tab_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_r17opdmucm7ij4aveppoa8wep ON openk9.tab USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."tab_sorting" (
            "tab_id" bigint NOT NULL,
            "sorting_id" bigint NOT NULL,
            CONSTRAINT "tab_sorting_pkey" PRIMARY KEY ("tab_id", "sorting_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."tab_token_tab" (
            "tab_id" bigint NOT NULL,
            "token_tab_id" bigint NOT NULL,
            CONSTRAINT "tab_token_tab_pkey" PRIMARY KEY ("tab_id", "token_tab_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "openk9"."tenant_binding" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."token_filter" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."token_tab" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "filter" boolean NOT NULL,
            "name" character varying(255) NOT NULL,
            "token_type" character varying(255) NOT NULL,
            "value" character varying(255),
            "doc_type_field_id" bigint,
            CONSTRAINT "token_tab_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_712f8fp1ftw1ug66hrmkgphrf ON openk9.token_tab USING btree (name);


        CREATE TABLE IF NOT EXISTS "openk9"."token_tab_extra_params" (
            "token_tab_id" bigint NOT NULL,
            "key" character varying(50) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "token_tab_extra_params_pkey" PRIMARY KEY ("token_tab_id", "key")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_token_tab_id_extra_params ON openk9.token_tab_extra_params USING btree (token_tab_id);


        CREATE TABLE IF NOT EXISTS "openk9"."tokenizer" (
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


        CREATE TABLE IF NOT EXISTS "openk9"."translation" (
            "id" bigint not null,
            "create_date" timestamp,
            "modified_date" timestamp,
            "language" character varying(10) NOT NULL,
            "class_name" character varying(255) NOT NULL,
            "class_pk" bigint NOT NULL,
            "key" character varying(50) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "translation_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX IF NOT EXISTS idx_translation_key ON openk9.translation USING btree (language, class_name, class_pk, key);

        CREATE INDEX IF NOT EXISTS idx_translation_entities ON openk9.translation USING btree (class_name, class_pk);


        CREATE TABLE IF NOT EXISTS "openk9"."vector_index" (
            "id" bigint NOT NULL,
            "create_date" timestamp,
            "modified_date" timestamp,
            "description" character varying(4096),
            "name" character varying(255) NOT NULL,
            "text_embedding_field" character varying(255) NOT NULL,
            "chunk_type" character varying(255) DEFAULT 'DEFAULT',
            "json_config" text DEFAULT '{}',
            "title_field" character varying(255) NOT NULL,
            "url_field" character varying(255) NOT NULL,
            "chunk_window_size" integer DEFAULT '0',
            "metadata_mapping" character varying(255),
            CONSTRAINT "vector_index_id" PRIMARY KEY ("id")
        ) WITH (oids = false);


        ALTER TABLE ONLY "openk9"."acl_mapping" ADD CONSTRAINT "fk_acl_mapping_on_doc_type_field" FOREIGN KEY (doc_type_field_id) REFERENCES "openk9"."doc_type_field"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."acl_mapping" ADD CONSTRAINT "fk_acl_mapping_on_plugin_driver" FOREIGN KEY (plugin_driver_id) REFERENCES "openk9"."plugin_driver"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."analyzer" ADD CONSTRAINT "fkc7nh3nt723kkehd09dl3jpcmw" FOREIGN KEY (tokenizer) REFERENCES "openk9"."tokenizer"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."analyzer_char_filter" ADD CONSTRAINT "fkfjki6k33j86nxxjtpjfsjk0bg" FOREIGN KEY (analyzer) REFERENCES "openk9"."analyzer"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."analyzer_char_filter" ADD CONSTRAINT "fki9uan06v9eosrqrj0d2ab839l" FOREIGN KEY (char_filter) REFERENCES "openk9"."char_filter"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."analyzer_token_filter" ADD CONSTRAINT "fk84vkkc91bhjv3ye2l19iimcl8" FOREIGN KEY (token_filter) REFERENCES "openk9"."token_filter"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."analyzer_token_filter" ADD CONSTRAINT "fk8knl2njy74t871j2eycwu5fql" FOREIGN KEY (analyzer) REFERENCES "openk9"."analyzer"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."annotator" ADD CONSTRAINT "fkfiv3bsnm26bb802cm7i1ei4q2" FOREIGN KEY (doc_type_field_id) REFERENCES "openk9"."doc_type_field"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fkgj8f2hdk19shy1gwqu3u4ae9x" FOREIGN KEY (query_analysis_id) REFERENCES "openk9"."query_analysis"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."bucket" ADD CONSTRAINT "fkn9yvqd7qsenqu5unq6yfj7j2a" FOREIGN KEY (search_config_id) REFERENCES "openk9"."search_config"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."bucket_language" ADD CONSTRAINT "313a9cab-a687-486d-92ae-2ce74fa89951" FOREIGN KEY (language_id) REFERENCES "openk9"."language"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."buckets_tabs" ADD CONSTRAINT "fki0729hs5mp7hisvr1xc4kcnio" FOREIGN KEY (buckets_id) REFERENCES "openk9"."bucket"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."buckets_tabs" ADD CONSTRAINT "fkkivu1a4ehytvoy78u74r3vq3l" FOREIGN KEY (tabs_id) REFERENCES "openk9"."tab"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."data_index" ADD CONSTRAINT "FK_data_index_datasource" FOREIGN KEY (datasource_id) REFERENCES "openk9"."datasource"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."data_index" ADD CONSTRAINT "data_index_vector_index_fk" FOREIGN KEY (vector_index_id) REFERENCES "openk9"."vector_index"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."data_index_doc_types" ADD CONSTRAINT "fk1a9m9bg9q7ni7gc9ysg3rq6fp" FOREIGN KEY (doc_types_id) REFERENCES "openk9"."doc_type"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."data_index_doc_types" ADD CONSTRAINT "fk9tu5iup1pex2vpk80jftsfbey" FOREIGN KEY (data_index_id) REFERENCES "openk9"."data_index"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."datasource" ADD CONSTRAINT "fkewjpbr7f4fkff02y19op5g6a" FOREIGN KEY (enrich_pipeline_id) REFERENCES "openk9"."enrich_pipeline"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."datasource" ADD CONSTRAINT "fkeyxlmk7fbek60xtie38cwu2v2" FOREIGN KEY (plugin_driver_id) REFERENCES "openk9"."plugin_driver"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."datasource" ADD CONSTRAINT "fknbh6wdfjww9no5xqxn65qchfy" FOREIGN KEY (data_index_id) REFERENCES "openk9"."data_index"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."datasource_buckets" ADD CONSTRAINT "fk80gj884mrv2t03t7g36qlrae9" FOREIGN KEY (datasource_id) REFERENCES "openk9"."datasource"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."datasource_buckets" ADD CONSTRAINT "fki9t38w4aa24jenr7lp4uguxb2" FOREIGN KEY (buckets_id) REFERENCES "openk9"."bucket"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."doc_type" ADD CONSTRAINT "fk1ocwbmim5560h0bysfpunyq46" FOREIGN KEY (doc_type_template_id) REFERENCES "openk9"."doc_type_template"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."doc_type_field" ADD CONSTRAINT "fk44qu7fg8d1tl3bn4d43sge02x" FOREIGN KEY (parent_doc_type_field_id) REFERENCES "openk9"."doc_type_field"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."doc_type_field" ADD CONSTRAINT "fkfjuk779hqxyt5ngdk9asog0bj" FOREIGN KEY (analyzer) REFERENCES "openk9"."analyzer"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."doc_type_field" ADD CONSTRAINT "fkn1p060bcao42e9fn7l1mb1jju" FOREIGN KEY (doc_type_id) REFERENCES "openk9"."doc_type"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."enrich_pipeline_item" ADD CONSTRAINT "fkmjidj39ui867w3ig9iccnmrxb" FOREIGN KEY (enrich_item_id) REFERENCES "openk9"."enrich_item"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."enrich_pipeline_item" ADD CONSTRAINT "fkreyxykt1gox8ghwcuobr5pjan" FOREIGN KEY (enrich_pipeline_id) REFERENCES "openk9"."enrich_pipeline"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."query_analysis_annotators" ADD CONSTRAINT "fka1ext4cmdvvc0pjxgci94xvvn" FOREIGN KEY (query_analysis_id) REFERENCES "openk9"."query_analysis"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."query_analysis_annotators" ADD CONSTRAINT "fkj6bkvkacmj53kv5o5k8065hvt" FOREIGN KEY (annotators_id) REFERENCES "openk9"."annotator"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."query_analysis_rules" ADD CONSTRAINT "fk5412heryog6cetwbsq5enp335" FOREIGN KEY (query_analysis_id) REFERENCES "openk9"."query_analysis"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."query_analysis_rules" ADD CONSTRAINT "fkmbqqghon1m8h554uh8hi0qkru" FOREIGN KEY (rules_id) REFERENCES "openk9"."rule"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."query_parser_config" ADD CONSTRAINT "fktmj4jra4g99dajgs7ilywk41b" FOREIGN KEY (search_config) REFERENCES "openk9"."search_config"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."scheduler" ADD CONSTRAINT "fk_scheduler_datasource" FOREIGN KEY (datasource_id) REFERENCES "openk9"."datasource"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."suggestion_category" ADD CONSTRAINT "fk3svyhailyqjc0iofjqpqymm78" FOREIGN KEY (bucket_id) REFERENCES "openk9"."bucket"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."suggestion_category_doc_type_fields" ADD CONSTRAINT "fklfdkxdh7p8pkjn7qtwa2qeckc" FOREIGN KEY (doc_type_fields_id) REFERENCES "openk9"."doc_type_field"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."suggestion_category_doc_type_fields" ADD CONSTRAINT "fkm97oc2r4h2fi5lym1g5f8pkwj" FOREIGN KEY (suggestion_category_id) REFERENCES "openk9"."suggestion_category"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."tab_sorting" ADD CONSTRAINT "fk_sorting_tab" FOREIGN KEY (sorting_id) REFERENCES "openk9"."sorting"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tab_sorting" ADD CONSTRAINT "fk_tab_sorting" FOREIGN KEY (tab_id) REFERENCES "openk9"."tab"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."tab_token_tab" ADD CONSTRAINT "fk_tab_token_tab" FOREIGN KEY (tab_id) REFERENCES "openk9"."tab"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tab_token_tab" ADD CONSTRAINT "fk_token_tab_tab" FOREIGN KEY (token_tab_id) REFERENCES "openk9"."token_tab"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."tenant_binding" ADD CONSTRAINT "fki7bn20nxw6x541wfvvwe62t28" FOREIGN KEY (tenant_binding_bucket_id) REFERENCES "openk9"."bucket"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tenant_binding" ADD CONSTRAINT "tenant_binding_embedding_model_fk" FOREIGN KEY (embedding_model_id) REFERENCES "openk9"."embedding_model"(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "openk9"."tenant_binding" ADD CONSTRAINT "tenant_binding_large_language_model_fk" FOREIGN KEY (large_language_model_id) REFERENCES "openk9"."large_language_model"(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "openk9"."token_tab" ADD CONSTRAINT "fk37w5m3swa2cebgar0umrnytyy" FOREIGN KEY (doc_type_field_id) REFERENCES "openk9"."doc_type_field"(id) NOT DEFERRABLE;
        
        INSERT INTO "bucket" ("id", "create_date", "modified_date", "description", "name", "query_analysis_id", "search_config_id", "refresh_on_suggestion_category", "language_id", "refresh_on_tab", "refresh_on_date", "refresh_on_query", "retrieve_type") VALUES
        (2,	'2025-04-27 20:03:04.278319',	'2025-04-27 20:03:04.278385',	'',	'Default Bucket',	NULL,	NULL,	'0',	NULL,	'0',	'0',	'0',	'HYBRID');


        INSERT INTO "doc_type" ("id", "create_date", "modified_date", "description", "name", "doc_type_template_id") VALUES
        (8,	'2025-04-28 08:35:38.370079',	'2025-04-28 08:35:38.370099',	'auto-generated',	'default',	NULL),
        (29,	'2025-04-28 08:35:38.375787',	'2025-04-28 08:35:38.375792',	'auto-generated',	'pdf',	NULL),
        (32,	'2025-04-28 08:35:38.376742',	'2025-04-28 08:35:38.376748',	'auto-generated',	'file',	NULL),
        (40,	'2025-04-28 08:35:38.377987',	'2025-04-28 08:35:38.377992',	'auto-generated',	'document',	NULL),
        (51,	'2025-04-28 09:09:04.608391',	'2025-04-28 09:09:04.608409',	'auto-generated',	'web',	NULL);

        INSERT INTO "doc_type_field" ("id", "create_date", "modified_date", "boost", "description", "exclude", "field_name", "field_type", "json_config", "name", "searchable", "analyzer", "doc_type_id", "parent_doc_type_field_id", "sortable") VALUES
        (9,	'2025-04-28 08:35:38.370241',	'2025-04-28 08:35:38.370248',	1,	'auto-generated',	NULL,	'last',	'BOOLEAN',	NULL,	'last',	'0',	NULL,	8,	NULL,	'0'),
        (10,	'2025-04-28 08:35:38.371825',	'2025-04-28 08:35:38.371843',	1,	'auto-generated',	NULL,	'indexName',	'TEXT',	NULL,	'indexName',	'1',	NULL,	8,	NULL,	'0'),
        (11,	'2025-04-28 08:35:38.371967',	'2025-04-28 08:35:38.37198',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'indexName.keyword',	'1',	NULL,	8,	10,	'0'),
        (12,	'2025-04-28 08:35:38.372145',	'2025-04-28 08:35:38.372166',	1,	'auto-generated',	NULL,	'contentId',	'TEXT',	NULL,	'contentId',	'1',	NULL,	8,	NULL,	'0'),
        (13,	'2025-04-28 08:35:38.372255',	'2025-04-28 08:35:38.372261',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'contentId.keyword',	'1',	NULL,	8,	12,	'0'),
        (14,	'2025-04-28 08:35:38.372324',	'2025-04-28 08:35:38.37233',	1,	'auto-generated',	NULL,	'resources',	'OBJECT',	NULL,	'resources',	'0',	NULL,	8,	NULL,	'0'),
        (15,	'2025-04-28 08:35:38.372392',	'2025-04-28 08:35:38.372398',	1,	'auto-generated',	NULL,	'ingestionId',	'TEXT',	NULL,	'ingestionId',	'1',	NULL,	8,	NULL,	'0'),
        (16,	'2025-04-28 08:35:38.372459',	'2025-04-28 08:35:38.372464',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'ingestionId.keyword',	'1',	NULL,	8,	15,	'0'),
        (17,	'2025-04-28 08:35:38.372594',	'2025-04-28 08:35:38.3726',	NULL,	'auto-generated',	NULL,	'acl',	'OBJECT',	NULL,	'acl',	'0',	NULL,	8,	NULL,	'0'),
        (18,	'2025-04-28 08:35:38.37267',	'2025-04-28 08:35:38.372676',	1,	'auto-generated',	NULL,	'public',	'BOOLEAN',	NULL,	'acl.public',	'0',	NULL,	8,	17,	'0'),
        (19,	'2025-04-28 08:35:38.372747',	'2025-04-28 08:35:38.372753',	1,	'auto-generated',	NULL,	'parsingDate',	'LONG',	NULL,	'parsingDate',	'0',	NULL,	8,	NULL,	'0'),
        (20,	'2025-04-28 08:35:38.372832',	'2025-04-28 08:35:38.372837',	1,	'auto-generated',	NULL,	'rawContent',	'TEXT',	NULL,	'rawContent',	'1',	NULL,	8,	NULL,	'0'),
        (21,	'2025-04-28 08:35:38.372905',	'2025-04-28 08:35:38.372911',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'rawContent.keyword',	'1',	NULL,	8,	20,	'0'),
        (22,	'2025-04-28 08:35:38.372976',	'2025-04-28 08:35:38.372983',	1,	'auto-generated',	NULL,	'documentTypes',	'TEXT',	NULL,	'documentTypes',	'1',	NULL,	8,	NULL,	'0'),
        (23,	'2025-04-28 08:35:38.373054',	'2025-04-28 08:35:38.37306',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'documentTypes.keyword',	'1',	NULL,	8,	22,	'0'),
        (24,	'2025-04-28 08:35:38.373123',	'2025-04-28 08:35:38.373129',	1,	'auto-generated',	NULL,	'datasourceId',	'LONG',	NULL,	'datasourceId',	'0',	NULL,	8,	NULL,	'0'),
        (25,	'2025-04-28 08:35:38.375445',	'2025-04-28 08:35:38.375458',	1,	'auto-generated',	NULL,	'tenantId',	'TEXT',	NULL,	'tenantId',	'1',	NULL,	8,	NULL,	'0'),
        (26,	'2025-04-28 08:35:38.375583',	'2025-04-28 08:35:38.375589',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'tenantId.keyword',	'1',	NULL,	8,	25,	'0'),
        (27,	'2025-04-28 08:35:38.375663',	'2025-04-28 08:35:38.375668',	1,	'auto-generated',	NULL,	'scheduleId',	'TEXT',	NULL,	'scheduleId',	'1',	NULL,	8,	NULL,	'0'),
        (28,	'2025-04-28 08:35:38.375724',	'2025-04-28 08:35:38.37573',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'scheduleId.keyword',	'1',	NULL,	8,	27,	'0'),
        (30,	'2025-04-28 08:35:38.376329',	'2025-04-28 08:35:38.376339',	1,	'auto-generated',	NULL,	'name',	'TEXT',	NULL,	'pdf.name',	'1',	NULL,	29,	NULL,	'0'),
        (31,	'2025-04-28 08:35:38.376656',	'2025-04-28 08:35:38.376664',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'pdf.name.keyword',	'1',	NULL,	29,	30,	'0'),
        (33,	'2025-04-28 08:35:38.377087',	'2025-04-28 08:35:38.377095',	1,	'auto-generated',	NULL,	'size',	'LONG',	NULL,	'file.size',	'0',	NULL,	32,	NULL,	'0'),
        (34,	'2025-04-28 08:35:38.37755',	'2025-04-28 08:35:38.377559',	1,	'auto-generated',	NULL,	'name',	'TEXT',	NULL,	'file.name',	'1',	NULL,	32,	NULL,	'0'),
        (35,	'2025-04-28 08:35:38.377628',	'2025-04-28 08:35:38.377638',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'file.name.keyword',	'1',	NULL,	32,	34,	'0'),
        (36,	'2025-04-28 08:35:38.377698',	'2025-04-28 08:35:38.377704',	1,	'auto-generated',	NULL,	'type',	'TEXT',	NULL,	'file.type',	'1',	NULL,	32,	NULL,	'0'),
        (37,	'2025-04-28 08:35:38.377777',	'2025-04-28 08:35:38.377783',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'file.type.keyword',	'1',	NULL,	32,	36,	'0'),
        (38,	'2025-04-28 08:35:38.377847',	'2025-04-28 08:35:38.377852',	1,	'auto-generated',	NULL,	'contentType',	'TEXT',	NULL,	'file.contentType',	'1',	NULL,	32,	NULL,	'0'),
        (39,	'2025-04-28 08:35:38.377912',	'2025-04-28 08:35:38.377927',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'file.contentType.keyword',	'1',	NULL,	32,	38,	'0'),
        (41,	'2025-04-28 08:35:38.378443',	'2025-04-28 08:35:38.378456',	1,	'auto-generated',	NULL,	'title',	'TEXT',	NULL,	'document.title',	'1',	NULL,	40,	NULL,	'0'),
        (42,	'2025-04-28 08:35:38.378708',	'2025-04-28 08:35:38.378715',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'document.title.keyword',	'1',	NULL,	40,	41,	'0'),
        (52,	'2025-04-28 09:09:04.608541',	'2025-04-28 09:09:04.608545',	1,	'auto-generated',	NULL,	'favicon',	'TEXT',	NULL,	'web.favicon',	'1',	NULL,	51,	NULL,	'0'),
        (53,	'2025-04-28 09:09:04.609196',	'2025-04-28 09:09:04.609206',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.favicon.keyword',	'1',	NULL,	51,	52,	'0'),
        (54,	'2025-04-28 09:09:04.609295',	'2025-04-28 09:09:04.609299',	1,	'auto-generated',	NULL,	'title',	'TEXT',	NULL,	'web.title',	'1',	NULL,	51,	NULL,	'0'),
        (55,	'2025-04-28 09:09:04.609347',	'2025-04-28 09:09:04.609351',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.title.keyword',	'1',	NULL,	51,	54,	'0'),
        (56,	'2025-04-28 09:09:04.609429',	'2025-04-28 09:09:04.609433',	1,	'auto-generated',	NULL,	'content',	'TEXT',	NULL,	'web.content',	'1',	NULL,	51,	NULL,	'0'),
        (57,	'2025-04-28 09:09:04.609488',	'2025-04-28 09:09:04.609491',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.content.keyword',	'1',	NULL,	51,	56,	'0'),
        (58,	'2025-04-28 09:09:04.609538',	'2025-04-28 09:09:04.609542',	1,	'auto-generated',	NULL,	'url',	'TEXT',	NULL,	'web.url',	'1',	NULL,	51,	NULL,	'0'),
        (59,	'2025-04-28 09:09:04.609588',	'2025-04-28 09:09:04.609591',	1,	'auto-generated',	NULL,	'keyword',	'KEYWORD',	'{"ignore_above":256}',	'web.url.keyword',	'1',	NULL,	51,	58,	'0');

        INSERT INTO "enrich_pipeline" ("id", "create_date", "modified_date", "description", "name") VALUES
        (5,	'2025-04-27 20:03:49.881117',	'2025-04-27 20:03:49.881154',	'',	'pipeline pdf');

                INSERT INTO "enrich_item" ("id", "create_date", "modified_date", "description", "json_config", "name", "service_name", "type", "script", "behavior_merge_type", "json_path", "behavior_on_error", "request_timeout") VALUES
        (6,	'2025-04-27 20:04:04.137698',	'2025-04-28 07:27:26.729101',	'',	'{
            "type_mapping": {
                "application/pdf": "pdf",
                "application/msword": "word",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "word",
                "application/vnd.ms-excel": "excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": "excel",
                "application/vnd.ms-powerpoint": "powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation": "powerpoint",
                "text/plain": "plaintext",
                "application/vnd.oasis.opendocument.spreadsheet": "excel",
                "application/vnd.oasis.opendocument.text": "word",
                "application/vnd.oasis.opendocument.presentation": "powerpoint",
                "application/epub+zip": "epub",
                "image/png": "png",
                "image/jpeg": "jpeg",
                "image/jpg": "jpg",
                "message/rfc822": "email",
                "application/x-tika-msoffice": "office",
                "application/mbox": "email",
                "application/x-7z-compressed": "zip",
                "application/gzip": "gz"
            },
            "retain_binaries": false,
            "summary_length": 5000,
            "max_length": 20000,
            "include_last_modified_date": false,
            "include_content_type": true
        }',	'tika',	'http://openk9-tika:8080/api/tika/process',	'HTTP_ASYNC',	'',	'MERGE',	'$',	'SKIP',	10000);

        INSERT INTO "enrich_pipeline_item" ("enrich_item_id", "enrich_pipeline_id", "weight") VALUES
        (6,	5,	1);


        INSERT INTO "plugin_driver" ("id", "create_date", "modified_date", "description", "json_config", "name", "type", "provisioning") VALUES
        (4,	'2025-04-27 20:03:27.304176',	'2025-04-27 20:03:37.983616',	'',	'{"host":"minio-parser","port":5000,"secure":false,"path":"/extract-files","method":"POST"}',	'minio',	'HTTP',	NULL),
        (3,	'2025-04-28 07:28:13.977438',	'2025-04-28 07:28:13.977457',	'',	'{"host":"web-parser","port":5000,"secure":false,"path":"/startSitemapCrawling","method":"POST"}',	'sitemap web crawler',	'HTTP',	NULL);


        INSERT INTO "datasource" ("id", "create_date", "modified_date", "description", "json_config", "last_ingestion_date", "name", "schedulable", "scheduling", "data_index_id", "enrich_pipeline_id", "plugin_driver_id", "reindex_rate") VALUES
        (3,	'2025-04-27 20:03:18.011871',	'2025-04-28 07:34:41.510974',	'',	'{
        "host": "minio",
        "port": 9000,
        "accessKey": "minio",
        "secretKey": "minio123",
        "bucketName": "test",
        "prefix": "",
            "datasourcePayloadKey": "test",
            "additionalMetadata": {
            }
        }',	'2025-04-28 07:34:38.719',	'minio bucket',	'0',	'0 0 * ? * * *',	NULL,	5,	4,	0),
        (43,	'2025-04-28 09:03:04.06353',	'2025-04-28 09:07:19.850018',	'',	'{
        "sitemapUrls": [
            "https://www.smc.it/sitemap.xml"
        ],
        "bodyTag": "body",
        "titleTag": "title::text",
        "allowedDomains": ["www.smc.it"],
        "maxLength": -1,
        "excludedPaths": [],
        "allowedPaths": [],
        "documentFileExtensions": [],
        "specificTags": [],
        "pageCount": 0,
        "additionalMetadata": {}
        }',	'2025-04-28 09:06:17.639',	'smc site',	'0',	'0 0 * ? * * *',	NULL,	NULL,	3,	0);

        INSERT INTO "datasource_buckets" ("datasource_id", "buckets_id") VALUES
        (3,	2),
        (43,	2);

        INSERT INTO "embedding_model" ("id", "create_date", "modified_date", "description", "name", "api_url", "api_key") VALUES
        (2,	'2025-04-28 07:27:13.388617',	'2025-04-28 07:27:13.388645',	'',	'openai Embdding Model',	'embedding-module:5000',	'');

        INSERT INTO "large_language_model" ("id", "create_date", "modified_date", "description", "name", "api_url", "api_key", "json_config") VALUES
        (1,	'2025-04-28 07:26:06.010886',	'2025-05-07 12:28:22.133977',	'',	'openai',	'',	'',	'{
        "promptTalkTo":"### [INST] Instruction: Answer the question based on your knowledge. Use Italian language only to answer. Here is context to help: {context}. If you do not find relevant information in the context, reply that you are not able to answer [/INST]",
        "promptGenerative":"### [INST] Instruction: Answer the question based on your knowledge. Use Italian language only to answer. Here is context to help: {context} ### QUESTION: {question} If you don''t have information in context anwer you don''t know the answer.[/INST]",
        "CHAT_SYSTEM_PROMPT":"Lavori per il Ministero Della Cultura Italiano. Il tuo compito è assistere gli utenti nella consultazione di procedimenti e manuali. Rispondi alle domande degli utenti. Se una domanda è vaga o poco chiara chiedi chiarimenti e informazioni aggiuntive all''utente. Se la domanda non è pertinente rispondi: ''Mi spiace, non posso rispondere a questa domanda''. Usa la documentazione fornita per rispondere alle domande.",
        "RAG_SYSTEM_PROMPT":"Lavori per il Ministero Della Cultura Italiano. Il tuo compito è assistere gli utenti nella consultazione di procedimenti e manuali. Rispondi alle domande degli utenti. Se una domanda è vaga o poco chiara chiedi chiarimenti e informazioni aggiuntive all''utente. Se la domanda non è pertinente rispondi: ''Mi spiace, non posso rispondere a questa domanda''. Usa la documentazione fornita per rispondere alle domande.",
        "RAG_TOOL_DESCRIPTION":"Risponde a domande relative al ministero della cultura.",
        "CHAT_MODEL":"gpt-4o",
        "REPHRASE_MODEL":"gpt-4o",
        "RAG_MODEL":"gpt-4o",
        "model": "gpt-4o",
        "type": "openai",
        "prompt": "test"
        }');

        INSERT INTO "tab" ("id", "create_date", "modified_date", "description", "name", "priority") VALUES
        (46,	'2025-04-28 09:08:01.153428',	'2025-04-28 09:08:01.153443',	'',	'Tutti i risultati',	0),
        (47,	'2025-04-28 09:08:04.219613',	'2025-04-28 09:08:04.219629',	'',	'web',	0),
        (48,	'2025-04-28 09:08:06.975567',	'2025-04-28 09:08:06.975584',	'',	'pdf',	0);


        INSERT INTO "token_tab" ("id", "create_date", "modified_date", "description", "filter", "name", "token_type", "value", "doc_type_field_id") VALUES
        (49,	'2025-04-28 09:08:14.129782',	'2025-04-28 09:08:14.129798',	'',	'1',	'web',	'DOCTYPE',	'web',	NULL),
        (50,	'2025-04-28 09:08:23.828582',	'2025-04-28 09:08:23.828602',	'',	'1',	'pdf',	'DOCTYPE',	'pdf',	NULL);

        INSERT INTO "tab_token_tab" ("tab_id", "token_tab_id") VALUES
        (47,	49),
        (48,	50);

        INSERT INTO "tenant_binding" ("id", "create_date", "modified_date", "virtual_host", "tenant_binding_bucket_id", "embedding_model_id", "large_language_model_id") VALUES
        (1,	NULL,	'2025-04-27 20:03:05.695535',	'demo.openk9.localhost',	2,	2,	1);

        END IF;
END $$;