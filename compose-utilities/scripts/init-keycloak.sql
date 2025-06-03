\connect "keycloak";

DO $$
BEGIN
    -- Check if the foreign key constraint exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'admin_event_entity'
    ) THEN

        CREATE TABLE "public"."admin_event_entity" (
            "id" character varying(36) NOT NULL,
            "admin_event_time" bigint,
            "realm_id" character varying(255),
            "operation_type" character varying(255),
            "auth_realm_id" character varying(255),
            "auth_client_id" character varying(255),
            "auth_user_id" character varying(255),
            "ip_address" character varying(255),
            "resource_path" character varying(2550),
            "representation" text,
            "error" character varying(255),
            "resource_type" character varying(64),
            CONSTRAINT "constraint_admin_event_entity" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_admin_event_time ON public.admin_event_entity USING btree (realm_id, admin_event_time);


        CREATE TABLE "public"."associated_policy" (
            "policy_id" character varying(36) NOT NULL,
            "associated_policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrpap" PRIMARY KEY ("policy_id", "associated_policy_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_assoc_pol_assoc_pol_id ON public.associated_policy USING btree (associated_policy_id);


        CREATE TABLE "public"."authentication_execution" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "authenticator" character varying(36),
            "realm_id" character varying(36),
            "flow_id" character varying(36),
            "requirement" integer,
            "priority" integer,
            "authenticator_flow" boolean DEFAULT false NOT NULL,
            "auth_flow_id" character varying(36),
            "auth_config" character varying(36),
            CONSTRAINT "constraint_auth_exec_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_auth_exec_realm_flow ON public.authentication_execution USING btree (realm_id, flow_id);

        CREATE INDEX idx_auth_exec_flow ON public.authentication_execution USING btree (flow_id);

        INSERT INTO "authentication_execution" ("id", "alias", "authenticator", "realm_id", "flow_id", "requirement", "priority", "authenticator_flow", "auth_flow_id", "auth_config") VALUES
        ('1f692abb-e9c3-42b0-a979-8e9ed73d580e',	NULL,	'auth-cookie',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'7d0f2abd-9d70-47ef-80f5-9e169d3c70b8',	2,	10,	'0',	NULL,	NULL),
        ('2632b144-33b4-402e-b2ea-cc8ec6e34253',	NULL,	'auth-spnego',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'7d0f2abd-9d70-47ef-80f5-9e169d3c70b8',	3,	20,	'0',	NULL,	NULL),
        ('c5b6b54d-7e0e-4d1d-812e-9e11c0713196',	NULL,	'identity-provider-redirector',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'7d0f2abd-9d70-47ef-80f5-9e169d3c70b8',	2,	25,	'0',	NULL,	NULL),
        ('7f8271b9-1194-4f79-8f43-ea91c85fe5a5',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'7d0f2abd-9d70-47ef-80f5-9e169d3c70b8',	2,	30,	'1',	'23bd8840-1965-47dc-92fb-99849971c494',	NULL),
        ('10fa8458-0338-43be-b45b-5637ccdcf043',	NULL,	'auth-username-password-form',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'23bd8840-1965-47dc-92fb-99849971c494',	0,	10,	'0',	NULL,	NULL),
        ('c62c84ff-30bd-49bb-9f15-5ecb4d9ccf9b',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'23bd8840-1965-47dc-92fb-99849971c494',	1,	20,	'1',	'42b230a5-b80b-475f-a8cf-27d829b386e4',	NULL),
        ('909204ba-f649-4fc8-9ebe-3e60e1766839',	NULL,	'conditional-user-configured',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'42b230a5-b80b-475f-a8cf-27d829b386e4',	0,	10,	'0',	NULL,	NULL),
        ('10a42470-7e42-4e24-ab98-2950e603e7dc',	NULL,	'auth-otp-form',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'42b230a5-b80b-475f-a8cf-27d829b386e4',	0,	20,	'0',	NULL,	NULL),
        ('b0683c3a-8ffc-4abe-aa42-141749896569',	NULL,	'direct-grant-validate-username',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'4f083f99-a46d-4606-a4b4-8630ea1a8e7c',	0,	10,	'0',	NULL,	NULL),
        ('501d7092-49cb-474a-8227-419edaa75e64',	NULL,	'direct-grant-validate-password',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'4f083f99-a46d-4606-a4b4-8630ea1a8e7c',	0,	20,	'0',	NULL,	NULL),
        ('16edbb4f-c3bd-43e7-852f-cf01ad15e704',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'4f083f99-a46d-4606-a4b4-8630ea1a8e7c',	1,	30,	'1',	'2d654eda-2df0-4566-b488-ae48e5ac3982',	NULL),
        ('c0fd42ae-ec6b-4ed7-9c1d-5cdc10e37920',	NULL,	'conditional-user-configured',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'2d654eda-2df0-4566-b488-ae48e5ac3982',	0,	10,	'0',	NULL,	NULL),
        ('99a91b3e-ffb1-4ed4-b9ff-acdad9142bdf',	NULL,	'direct-grant-validate-otp',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'2d654eda-2df0-4566-b488-ae48e5ac3982',	0,	20,	'0',	NULL,	NULL),
        ('86d2f7ed-55ad-4826-b7aa-548f67b6d6b5',	NULL,	'registration-page-form',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'fc88446a-6029-4148-85d6-1da3a01c0210',	0,	10,	'1',	'520e8459-ea44-4fa4-9d7e-b033fd80d818',	NULL),
        ('c17dcc3d-1eca-49a7-aa65-d9ca86696b51',	NULL,	'registration-user-creation',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'520e8459-ea44-4fa4-9d7e-b033fd80d818',	0,	20,	'0',	NULL,	NULL),
        ('4ac81ede-07e7-4f22-864b-db65b76f802e',	NULL,	'registration-profile-action',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'520e8459-ea44-4fa4-9d7e-b033fd80d818',	0,	40,	'0',	NULL,	NULL),
        ('d6719d39-e3b2-46ec-bab4-a8efac2de9d3',	NULL,	'registration-password-action',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'520e8459-ea44-4fa4-9d7e-b033fd80d818',	0,	50,	'0',	NULL,	NULL),
        ('59e4864b-b8fd-461e-b115-02a95b8490d9',	NULL,	'registration-recaptcha-action',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'520e8459-ea44-4fa4-9d7e-b033fd80d818',	3,	60,	'0',	NULL,	NULL),
        ('a71d39b4-67c8-4e9e-9b78-d5f4c9a90e61',	NULL,	'reset-credentials-choose-user',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'276d4d54-a9dd-4cdf-86e9-b015293acdc2',	0,	10,	'0',	NULL,	NULL),
        ('3f2f2cc1-592a-4da8-b45a-e5047d8d855c',	NULL,	'reset-credential-email',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'276d4d54-a9dd-4cdf-86e9-b015293acdc2',	0,	20,	'0',	NULL,	NULL),
        ('ee525ddd-5e0a-4cd7-bbbc-0cf2a75469c8',	NULL,	'reset-password',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'276d4d54-a9dd-4cdf-86e9-b015293acdc2',	0,	30,	'0',	NULL,	NULL),
        ('3595fe06-53c7-4e8c-927c-1e3fbb3ec118',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'276d4d54-a9dd-4cdf-86e9-b015293acdc2',	1,	40,	'1',	'207fa6e8-ddbc-463b-a225-88cf45905df5',	NULL),
        ('852e1059-3e92-45d1-bf56-2418ce0b6003',	NULL,	'conditional-user-configured',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'207fa6e8-ddbc-463b-a225-88cf45905df5',	0,	10,	'0',	NULL,	NULL),
        ('ebcfb7ff-7686-452c-9d69-2f3990768249',	NULL,	'reset-otp',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'207fa6e8-ddbc-463b-a225-88cf45905df5',	0,	20,	'0',	NULL,	NULL),
        ('645658ea-6b93-479c-9cd0-727f53d7ceca',	NULL,	'client-secret',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'610dcb35-21dc-45da-a1c9-4f382bda66fa',	2,	10,	'0',	NULL,	NULL),
        ('cd363905-5422-43e3-aca8-708f17862deb',	NULL,	'client-jwt',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'610dcb35-21dc-45da-a1c9-4f382bda66fa',	2,	20,	'0',	NULL,	NULL),
        ('58142d92-1f37-42e8-9d55-ee7b43391926',	NULL,	'client-secret-jwt',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'610dcb35-21dc-45da-a1c9-4f382bda66fa',	2,	30,	'0',	NULL,	NULL),
        ('da0275a1-6425-481c-8498-082115aeebf1',	NULL,	'client-x509',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'610dcb35-21dc-45da-a1c9-4f382bda66fa',	2,	40,	'0',	NULL,	NULL),
        ('f1f3051d-7a5d-4fd4-b061-23aa70514627',	NULL,	'idp-review-profile',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'c26b9fa1-14cc-43bf-941e-7c8ba45fbbad',	0,	10,	'0',	NULL,	'a264de64-bcf0-4cc2-8b94-cded9eac1825'),
        ('eed0c182-6372-4f76-ae32-523e99ee28f1',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'c26b9fa1-14cc-43bf-941e-7c8ba45fbbad',	0,	20,	'1',	'45912315-d6c4-482a-83d9-3ab0c0878824',	NULL),
        ('95a4ae29-8c8d-446b-a97f-37e3b6c65c23',	NULL,	'idp-create-user-if-unique',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'45912315-d6c4-482a-83d9-3ab0c0878824',	2,	10,	'0',	NULL,	'5ee7987d-5445-48df-84b7-773fea0fc02c'),
        ('1f58091b-eb2f-4c72-8f3c-cbdf66c88b6d',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'45912315-d6c4-482a-83d9-3ab0c0878824',	2,	20,	'1',	'a0ca28e6-f7a5-4b48-a29d-65fbfaefe928',	NULL),
        ('f57199e4-fc0b-4916-ad63-6734b226c78a',	NULL,	'idp-confirm-link',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'a0ca28e6-f7a5-4b48-a29d-65fbfaefe928',	0,	10,	'0',	NULL,	NULL),
        ('eeff3e84-893e-45ee-bfa5-865c333bab5a',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'a0ca28e6-f7a5-4b48-a29d-65fbfaefe928',	0,	20,	'1',	'dcb12d08-0574-4f0e-98de-4b76379505b3',	NULL),
        ('2e3a536c-bc96-4987-a33b-edf1b2329ca5',	NULL,	'idp-email-verification',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'dcb12d08-0574-4f0e-98de-4b76379505b3',	2,	10,	'0',	NULL,	NULL),
        ('4230e682-7429-44a6-8bd3-840132ec4dd8',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'dcb12d08-0574-4f0e-98de-4b76379505b3',	2,	20,	'1',	'6c1e49c2-ef85-45da-a855-ac35ef9f3b70',	NULL),
        ('65cdd856-a95a-41ad-aaf2-2faffae66d48',	NULL,	'idp-username-password-form',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'6c1e49c2-ef85-45da-a855-ac35ef9f3b70',	0,	10,	'0',	NULL,	NULL),
        ('86cb9a89-2d97-4ea2-90c9-7515498a42bd',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'6c1e49c2-ef85-45da-a855-ac35ef9f3b70',	1,	20,	'1',	'20b2b772-529f-49c5-8ae0-21cf199c7704',	NULL),
        ('3e6f036c-7d44-4ba9-a0cf-30f4e5e3e413',	NULL,	'conditional-user-configured',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'20b2b772-529f-49c5-8ae0-21cf199c7704',	0,	10,	'0',	NULL,	NULL),
        ('ce4ddadc-9e69-47f1-b91d-5d5f0ef3d987',	NULL,	'auth-otp-form',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'20b2b772-529f-49c5-8ae0-21cf199c7704',	0,	20,	'0',	NULL,	NULL),
        ('80e0e891-b755-4999-aa09-b05a96b292df',	NULL,	'http-basic-authenticator',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'8fb4f31a-507e-41f2-9fe8-2af11d3279fa',	0,	10,	'0',	NULL,	NULL),
        ('75274060-38bf-48d8-9c13-72540baa7939',	NULL,	'docker-http-basic-authenticator',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'748c9c80-b84c-46dc-a967-0de8b6823df2',	0,	10,	'0',	NULL,	NULL),
        ('fbfb98b4-d4ec-4b21-99c6-8751487b2adb',	NULL,	'no-cookie-redirect',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'21517d28-af1a-426b-9b78-c30f37b230b2',	0,	10,	'0',	NULL,	NULL),
        ('1f180c54-881c-495b-8053-1be58ecee416',	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'21517d28-af1a-426b-9b78-c30f37b230b2',	0,	20,	'1',	'51401281-d826-40fc-b335-1cddf061ab40',	NULL),
        ('5f460db9-1dfc-49bf-9a43-dbabb5502f5b',	NULL,	'basic-auth',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'51401281-d826-40fc-b335-1cddf061ab40',	0,	10,	'0',	NULL,	NULL),
        ('45ff47c1-e734-47f3-97fe-c8640e65e9ed',	NULL,	'basic-auth-otp',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'51401281-d826-40fc-b335-1cddf061ab40',	3,	20,	'0',	NULL,	NULL),
        ('a3c0ee84-2a04-457e-b445-234ba7d70964',	NULL,	'auth-spnego',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'51401281-d826-40fc-b335-1cddf061ab40',	3,	30,	'0',	NULL,	NULL),
        ('8f2aea50-ff46-4196-8090-3d9ec6c4f778',	NULL,	'auth-cookie',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'68092db5-6964-4265-8a78-a22759ef4886',	2,	10,	'0',	NULL,	NULL),
        ('c7cd794c-8024-4afa-9950-6e60d00148c4',	NULL,	'auth-spnego',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'68092db5-6964-4265-8a78-a22759ef4886',	3,	20,	'0',	NULL,	NULL),
        ('8999f8a3-558a-4f8f-a54a-29616b92e3bc',	NULL,	'identity-provider-redirector',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'68092db5-6964-4265-8a78-a22759ef4886',	2,	25,	'0',	NULL,	NULL),
        ('7b2f5872-84fe-4b9f-8de1-7142a34694a3',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'68092db5-6964-4265-8a78-a22759ef4886',	2,	30,	'1',	'576b32dc-75e3-4921-b122-f1dd54d9106c',	NULL),
        ('47c070ca-4777-473a-a3a2-3f9c46febdc3',	NULL,	'auth-username-password-form',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'576b32dc-75e3-4921-b122-f1dd54d9106c',	0,	10,	'0',	NULL,	NULL),
        ('cd87f3e4-e15b-44c3-b854-f0231e26f370',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'576b32dc-75e3-4921-b122-f1dd54d9106c',	1,	20,	'1',	'd18e2169-e581-4498-a0d8-cac57a72a697',	NULL),
        ('957fd835-235c-47c9-aee2-99899dc3e5cf',	NULL,	'conditional-user-configured',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'd18e2169-e581-4498-a0d8-cac57a72a697',	0,	10,	'0',	NULL,	NULL),
        ('80cd8dad-d233-4e2d-846a-585348d5435f',	NULL,	'auth-otp-form',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'd18e2169-e581-4498-a0d8-cac57a72a697',	0,	20,	'0',	NULL,	NULL),
        ('12e66a1d-b433-4f2e-a136-e569ba380ba7',	NULL,	'direct-grant-validate-username',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'fcfbf8ed-8109-4a5c-a162-d712712b16ca',	0,	10,	'0',	NULL,	NULL),
        ('ca11ece6-10f7-479b-a5fe-b196e813624c',	NULL,	'direct-grant-validate-password',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'fcfbf8ed-8109-4a5c-a162-d712712b16ca',	0,	20,	'0',	NULL,	NULL),
        ('e172dd31-7d32-43db-be0c-52df7a4973f4',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'fcfbf8ed-8109-4a5c-a162-d712712b16ca',	1,	30,	'1',	'7e710042-4b42-44df-b989-6866bb65e41d',	NULL),
        ('b00ce17b-4f6e-4179-a3d0-790f44a9507a',	NULL,	'conditional-user-configured',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'7e710042-4b42-44df-b989-6866bb65e41d',	0,	10,	'0',	NULL,	NULL),
        ('6cbfe054-e5ec-4184-9e3f-8be3cdae9253',	NULL,	'direct-grant-validate-otp',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'7e710042-4b42-44df-b989-6866bb65e41d',	0,	20,	'0',	NULL,	NULL),
        ('af3bcefb-28b2-4312-bfd7-dc0fcced11a2',	NULL,	'registration-page-form',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'f8881c96-f327-4f85-85a3-d160b09b33d4',	0,	10,	'1',	'c6e6599d-5dc4-4195-ab44-abbff1c213bf',	NULL),
        ('156fecfc-c9e5-4342-b0a8-ababf343582d',	NULL,	'registration-user-creation',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'c6e6599d-5dc4-4195-ab44-abbff1c213bf',	0,	20,	'0',	NULL,	NULL),
        ('8c31fee0-1897-4ab1-af64-ff64f21ee190',	NULL,	'registration-profile-action',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'c6e6599d-5dc4-4195-ab44-abbff1c213bf',	0,	40,	'0',	NULL,	NULL),
        ('34acb9eb-83bc-4170-8aff-af8c18c78855',	NULL,	'registration-password-action',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'c6e6599d-5dc4-4195-ab44-abbff1c213bf',	0,	50,	'0',	NULL,	NULL),
        ('37f5e6cb-c41e-4108-b1f7-b2a9d974ccef',	NULL,	'registration-recaptcha-action',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'c6e6599d-5dc4-4195-ab44-abbff1c213bf',	3,	60,	'0',	NULL,	NULL),
        ('abe9012a-f21d-4ec8-bad5-60c74c8afd07',	NULL,	'reset-credentials-choose-user',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'05fd3eeb-b4ad-48ac-bb43-fd253e1f40b8',	0,	10,	'0',	NULL,	NULL),
        ('0649acda-94e5-4047-8630-2e99a1ca3eb3',	NULL,	'reset-credential-email',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'05fd3eeb-b4ad-48ac-bb43-fd253e1f40b8',	0,	20,	'0',	NULL,	NULL),
        ('4ba27c6f-a680-4052-be89-19d9ac02a733',	NULL,	'reset-password',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'05fd3eeb-b4ad-48ac-bb43-fd253e1f40b8',	0,	30,	'0',	NULL,	NULL),
        ('f5721353-5e5d-4245-88c8-fec0314a2201',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'05fd3eeb-b4ad-48ac-bb43-fd253e1f40b8',	1,	40,	'1',	'a2e6179e-ac22-4371-9dc9-83ab9dd8cdf0',	NULL),
        ('4af3321e-ed37-42cd-801d-3c8989fd48e2',	NULL,	'conditional-user-configured',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'a2e6179e-ac22-4371-9dc9-83ab9dd8cdf0',	0,	10,	'0',	NULL,	NULL),
        ('fde45e39-60db-4852-b12c-fe2da05e372c',	NULL,	'reset-otp',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'a2e6179e-ac22-4371-9dc9-83ab9dd8cdf0',	0,	20,	'0',	NULL,	NULL),
        ('8cee9f50-e947-430e-b57c-1b0c611d8243',	NULL,	'client-secret',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'66b2b973-53e0-4329-b65b-09a6fec61bd7',	2,	10,	'0',	NULL,	NULL),
        ('93dac874-b11a-4a31-b27a-c4dec4ffcf70',	NULL,	'client-jwt',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'66b2b973-53e0-4329-b65b-09a6fec61bd7',	2,	20,	'0',	NULL,	NULL),
        ('6354ae08-ba8d-43ff-9131-73bdec6925e4',	NULL,	'client-secret-jwt',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'66b2b973-53e0-4329-b65b-09a6fec61bd7',	2,	30,	'0',	NULL,	NULL),
        ('d9a1a488-2f55-45a9-b1ab-6ff70e621c63',	NULL,	'client-x509',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'66b2b973-53e0-4329-b65b-09a6fec61bd7',	2,	40,	'0',	NULL,	NULL),
        ('a3cb9f19-1968-470d-a45b-fd5565be7e81',	NULL,	'idp-review-profile',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'eeddde6a-0c5e-479c-bbf1-a3d8186d8ada',	0,	10,	'0',	NULL,	'b512bf68-611e-4c92-874b-619a8aad6dfb'),
        ('fbbac55e-c9f6-47ed-93c8-a3c9b2745140',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'eeddde6a-0c5e-479c-bbf1-a3d8186d8ada',	0,	20,	'1',	'fde2f20c-39fc-49db-ab91-1ba32e9e9583',	NULL),
        ('8aed646b-631a-4bc1-99e1-2cade024b1c4',	NULL,	'idp-create-user-if-unique',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'fde2f20c-39fc-49db-ab91-1ba32e9e9583',	2,	10,	'0',	NULL,	'cb9aa846-336f-410b-88b4-37123228a573'),
        ('cb4cff35-a2a7-4330-8400-58bd39a7c644',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'fde2f20c-39fc-49db-ab91-1ba32e9e9583',	2,	20,	'1',	'8c9136ba-6602-4d78-8902-008263de172b',	NULL),
        ('f645dcdd-9e36-4f38-a010-e5468f6082a1',	NULL,	'idp-confirm-link',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'8c9136ba-6602-4d78-8902-008263de172b',	0,	10,	'0',	NULL,	NULL),
        ('a293094e-3685-4aef-bb12-1200348e8e46',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'8c9136ba-6602-4d78-8902-008263de172b',	0,	20,	'1',	'29c79264-a77a-490d-a6f1-9680a384dbbc',	NULL),
        ('1f351d59-7331-449b-b571-36996cf4b836',	NULL,	'idp-email-verification',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'29c79264-a77a-490d-a6f1-9680a384dbbc',	2,	10,	'0',	NULL,	NULL),
        ('e4aa8fc2-8e60-4ac4-91e4-f82872b65c5c',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'29c79264-a77a-490d-a6f1-9680a384dbbc',	2,	20,	'1',	'7803c944-eb92-470e-9060-39bf464a92eb',	NULL),
        ('2dcb021e-ca7f-4469-8a30-74c91b31d84f',	NULL,	'idp-username-password-form',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'7803c944-eb92-470e-9060-39bf464a92eb',	0,	10,	'0',	NULL,	NULL),
        ('e3b3b169-5efb-46f3-8629-7ab5dc4a71e8',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'7803c944-eb92-470e-9060-39bf464a92eb',	1,	20,	'1',	'1a21607b-be73-42a0-b0a4-1d49d3f0b1a4',	NULL),
        ('933614ab-4185-4d65-89a8-2ad5e7121ba0',	NULL,	'conditional-user-configured',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1a21607b-be73-42a0-b0a4-1d49d3f0b1a4',	0,	10,	'0',	NULL,	NULL),
        ('f49d19bf-1b43-4474-a0eb-faf5630966e4',	NULL,	'auth-otp-form',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1a21607b-be73-42a0-b0a4-1d49d3f0b1a4',	0,	20,	'0',	NULL,	NULL),
        ('a02bd79f-8d7f-4b57-922f-649674cd336a',	NULL,	'http-basic-authenticator',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'73d12ba5-2a08-4768-b5bf-22a9897ef789',	0,	10,	'0',	NULL,	NULL),
        ('959c8247-40f6-4b16-be04-a144006a7b26',	NULL,	'docker-http-basic-authenticator',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'c2086ade-478e-45e6-aeda-cc19a866f725',	0,	10,	'0',	NULL,	NULL),
        ('f0c0b49e-c10c-4a6a-beb7-d1ab1822643a',	NULL,	'no-cookie-redirect',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'db323ebd-9996-42dd-b28d-8ab995a55dd2',	0,	10,	'0',	NULL,	NULL),
        ('1fb31722-f4ed-457f-b048-8a5bd4a64ac6',	NULL,	NULL,	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'db323ebd-9996-42dd-b28d-8ab995a55dd2',	0,	20,	'1',	'49d15118-9acf-4105-897e-4be2adba9fcc',	NULL),
        ('da0a02f2-8480-4f66-939c-41f4dbd75bb7',	NULL,	'basic-auth',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'49d15118-9acf-4105-897e-4be2adba9fcc',	0,	10,	'0',	NULL,	NULL),
        ('1467ccd7-b4ac-4b43-b53f-ce01bd04cdb0',	NULL,	'basic-auth-otp',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'49d15118-9acf-4105-897e-4be2adba9fcc',	3,	20,	'0',	NULL,	NULL),
        ('6989a599-71d3-4d60-a7aa-2d13b5d16aa2',	NULL,	'auth-spnego',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'49d15118-9acf-4105-897e-4be2adba9fcc',	3,	30,	'0',	NULL,	NULL);

        CREATE TABLE "public"."authentication_flow" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "description" character varying(255),
            "realm_id" character varying(36),
            "provider_id" character varying(36) DEFAULT 'basic-flow' NOT NULL,
            "top_level" boolean DEFAULT false NOT NULL,
            "built_in" boolean DEFAULT false NOT NULL,
            CONSTRAINT "constraint_auth_flow_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_auth_flow_realm ON public.authentication_flow USING btree (realm_id);

        INSERT INTO "authentication_flow" ("id", "alias", "description", "realm_id", "provider_id", "top_level", "built_in") VALUES
        ('7d0f2abd-9d70-47ef-80f5-9e169d3c70b8',	'browser',	'browser based authentication',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('23bd8840-1965-47dc-92fb-99849971c494',	'forms',	'Username, password, otp and other auth forms.',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('42b230a5-b80b-475f-a8cf-27d829b386e4',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('4f083f99-a46d-4606-a4b4-8630ea1a8e7c',	'direct grant',	'OpenID Connect Resource Owner Grant',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('2d654eda-2df0-4566-b488-ae48e5ac3982',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('fc88446a-6029-4148-85d6-1da3a01c0210',	'registration',	'registration flow',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('520e8459-ea44-4fa4-9d7e-b033fd80d818',	'registration form',	'registration form',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'form-flow',	'0',	'1'),
        ('276d4d54-a9dd-4cdf-86e9-b015293acdc2',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('207fa6e8-ddbc-463b-a225-88cf45905df5',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('610dcb35-21dc-45da-a1c9-4f382bda66fa',	'clients',	'Base authentication for clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'client-flow',	'1',	'1'),
        ('c26b9fa1-14cc-43bf-941e-7c8ba45fbbad',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('45912315-d6c4-482a-83d9-3ab0c0878824',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('a0ca28e6-f7a5-4b48-a29d-65fbfaefe928',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('dcb12d08-0574-4f0e-98de-4b76379505b3',	'Account verification options',	'Method with which to verity the existing account',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('6c1e49c2-ef85-45da-a855-ac35ef9f3b70',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('20b2b772-529f-49c5-8ae0-21cf199c7704',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('8fb4f31a-507e-41f2-9fe8-2af11d3279fa',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('748c9c80-b84c-46dc-a967-0de8b6823df2',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('21517d28-af1a-426b-9b78-c30f37b230b2',	'http challenge',	'An authentication flow based on challenge-response HTTP Authentication Schemes',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'1',	'1'),
        ('51401281-d826-40fc-b335-1cddf061ab40',	'Authentication Options',	'Authentication options.',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'basic-flow',	'0',	'1'),
        ('68092db5-6964-4265-8a78-a22759ef4886',	'browser',	'browser based authentication',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('576b32dc-75e3-4921-b122-f1dd54d9106c',	'forms',	'Username, password, otp and other auth forms.',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('d18e2169-e581-4498-a0d8-cac57a72a697',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('fcfbf8ed-8109-4a5c-a162-d712712b16ca',	'direct grant',	'OpenID Connect Resource Owner Grant',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('7e710042-4b42-44df-b989-6866bb65e41d',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('f8881c96-f327-4f85-85a3-d160b09b33d4',	'registration',	'registration flow',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('c6e6599d-5dc4-4195-ab44-abbff1c213bf',	'registration form',	'registration form',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'form-flow',	'0',	'1'),
        ('05fd3eeb-b4ad-48ac-bb43-fd253e1f40b8',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('a2e6179e-ac22-4371-9dc9-83ab9dd8cdf0',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('66b2b973-53e0-4329-b65b-09a6fec61bd7',	'clients',	'Base authentication for clients',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'client-flow',	'1',	'1'),
        ('eeddde6a-0c5e-479c-bbf1-a3d8186d8ada',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('fde2f20c-39fc-49db-ab91-1ba32e9e9583',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('8c9136ba-6602-4d78-8902-008263de172b',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('29c79264-a77a-490d-a6f1-9680a384dbbc',	'Account verification options',	'Method with which to verity the existing account',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('7803c944-eb92-470e-9060-39bf464a92eb',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('1a21607b-be73-42a0-b0a4-1d49d3f0b1a4',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1'),
        ('73d12ba5-2a08-4768-b5bf-22a9897ef789',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('c2086ade-478e-45e6-aeda-cc19a866f725',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('db323ebd-9996-42dd-b28d-8ab995a55dd2',	'http challenge',	'An authentication flow based on challenge-response HTTP Authentication Schemes',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'1',	'1'),
        ('49d15118-9acf-4105-897e-4be2adba9fcc',	'Authentication Options',	'Authentication options.',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'basic-flow',	'0',	'1');

        CREATE TABLE "public"."authenticator_config" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "realm_id" character varying(36),
            CONSTRAINT "constraint_auth_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_auth_config_realm ON public.authenticator_config USING btree (realm_id);

        INSERT INTO "authenticator_config" ("id", "alias", "realm_id") VALUES
        ('a264de64-bcf0-4cc2-8b94-cded9eac1825',	'review profile config',	'add3ae74-abd2-4e73-96ea-a80026fa73c5'),
        ('5ee7987d-5445-48df-84b7-773fea0fc02c',	'create unique user config',	'add3ae74-abd2-4e73-96ea-a80026fa73c5'),
        ('b512bf68-611e-4c92-874b-619a8aad6dfb',	'review profile config',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03'),
        ('cb9aa846-336f-410b-88b4-37123228a573',	'create unique user config',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03');

        CREATE TABLE "public"."authenticator_config_entry" (
            "authenticator_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_auth_cfg_pk" PRIMARY KEY ("authenticator_id", "name")
        )
        WITH (oids = false);

        INSERT INTO "authenticator_config_entry" ("authenticator_id", "value", "name") VALUES
        ('5ee7987d-5445-48df-84b7-773fea0fc02c',	'false',	'require.password.update.after.registration'),
        ('a264de64-bcf0-4cc2-8b94-cded9eac1825',	'missing',	'update.profile.on.first.login'),
        ('b512bf68-611e-4c92-874b-619a8aad6dfb',	'missing',	'update.profile.on.first.login'),
        ('cb9aa846-336f-410b-88b4-37123228a573',	'false',	'require.password.update.after.registration');

        CREATE TABLE "public"."broker_link" (
            "identity_provider" character varying(255) NOT NULL,
            "storage_provider_id" character varying(255),
            "realm_id" character varying(36) NOT NULL,
            "broker_user_id" character varying(255),
            "broker_username" character varying(255),
            "token" text,
            "user_id" character varying(255) NOT NULL,
            CONSTRAINT "constr_broker_link_pk" PRIMARY KEY ("identity_provider", "user_id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client" (
            "id" character varying(36) NOT NULL,
            "enabled" boolean DEFAULT false NOT NULL,
            "full_scope_allowed" boolean DEFAULT false NOT NULL,
            "client_id" character varying(255),
            "not_before" integer,
            "public_client" boolean DEFAULT false NOT NULL,
            "secret" character varying(255),
            "base_url" character varying(255),
            "bearer_only" boolean DEFAULT false NOT NULL,
            "management_url" character varying(255),
            "surrogate_auth_required" boolean DEFAULT false NOT NULL,
            "realm_id" character varying(36),
            "protocol" character varying(255),
            "node_rereg_timeout" integer DEFAULT '0',
            "frontchannel_logout" boolean DEFAULT false NOT NULL,
            "consent_required" boolean DEFAULT false NOT NULL,
            "name" character varying(255),
            "service_accounts_enabled" boolean DEFAULT false NOT NULL,
            "client_authenticator_type" character varying(255),
            "root_url" character varying(255),
            "description" character varying(255),
            "registration_token" character varying(255),
            "standard_flow_enabled" boolean DEFAULT true NOT NULL,
            "implicit_flow_enabled" boolean DEFAULT false NOT NULL,
            "direct_access_grants_enabled" boolean DEFAULT false NOT NULL,
            "always_display_in_console" boolean DEFAULT false NOT NULL,
            CONSTRAINT "constraint_7" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_b71cjlbenv945rb6gcon438at ON public.client USING btree (realm_id, client_id);

        CREATE INDEX idx_client_id ON public.client USING btree (client_id);

        INSERT INTO "client" ("id", "enabled", "full_scope_allowed", "client_id", "not_before", "public_client", "secret", "base_url", "bearer_only", "management_url", "surrogate_auth_required", "realm_id", "protocol", "node_rereg_timeout", "frontchannel_logout", "consent_required", "name", "service_accounts_enabled", "client_authenticator_type", "root_url", "description", "registration_token", "standard_flow_enabled", "implicit_flow_enabled", "direct_access_grants_enabled", "always_display_in_console") VALUES
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'0',	'master-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	0,	'0',	'0',	'master Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/master/account/',	'0',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/master/account/',	'0',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'1',	'0',	'security-admin-console',	0,	'1',	NULL,	'/admin/master/console/',	'0',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'1',	'0',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'0',	'openk9-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	0,	'0',	'0',	'openk9 Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'0',	'realm-management',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	0,	'0',	'0',	'${client_realm-management}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/openk9/account/',	'0',	NULL,	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/openk9/account/',	'0',	NULL,	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'1',	'0',	'security-admin-console',	0,	'1',	NULL,	'/admin/openk9/console/',	'0',	NULL,	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'1',	'0',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'1',	'1',	'openk9',	0,	'1',	NULL,	'',	'0',	'',	'0',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'openid-connect',	-1,	'1',	'0',	'',	'0',	'client-secret',	'',	'',	NULL,	'1',	'0',	'1',	'0');

        CREATE TABLE "public"."client_attributes" (
            "client_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_3c" PRIMARY KEY ("client_id", "name")
        )
        WITH (oids = false);

        INSERT INTO "client_attributes" ("client_id", "name", "value") VALUES
        ('739d472a-69a5-4544-8342-fb084f397457',	'post.logout.redirect.uris',	'+'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'post.logout.redirect.uris',	'+'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'pkce.code.challenge.method',	'S256'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'post.logout.redirect.uris',	'+'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'pkce.code.challenge.method',	'S256'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'post.logout.redirect.uris',	'+'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'post.logout.redirect.uris',	'+'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'pkce.code.challenge.method',	'S256'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'post.logout.redirect.uris',	'+'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'pkce.code.challenge.method',	'S256'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'post.logout.redirect.uris',	'+'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'display.on.consent.screen',	'false'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'backchannel.logout.revoke.offline.tokens',	'false'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'backchannel.logout.session.required',	'true'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'oauth2.device.authorization.grant.enabled',	'false'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'oidc.ciba.grant.enabled',	'false');

        CREATE TABLE "public"."client_auth_flow_bindings" (
            "client_id" character varying(36) NOT NULL,
            "flow_id" character varying(36),
            "binding_name" character varying(255) NOT NULL,
            CONSTRAINT "c_cli_flow_bind" PRIMARY KEY ("client_id", "binding_name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client_initial_access" (
            "id" character varying(36) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "timestamp" integer,
            "expiration" integer,
            "count" integer,
            "remaining_count" integer,
            CONSTRAINT "cnstr_client_init_acc_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_client_init_acc_realm ON public.client_initial_access USING btree (realm_id);


        CREATE TABLE "public"."client_node_registrations" (
            "client_id" character varying(36) NOT NULL,
            "value" integer,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_84" PRIMARY KEY ("client_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client_scope" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "realm_id" character varying(36),
            "description" character varying(255),
            "protocol" character varying(255),
            CONSTRAINT "pk_cli_template" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_cli_scope ON public.client_scope USING btree (realm_id, name);

        CREATE INDEX idx_realm_clscope ON public.client_scope USING btree (realm_id);

        INSERT INTO "client_scope" ("id", "name", "realm_id", "description", "protocol") VALUES
        ('b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'offline_access',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('da4b14d8-fe98-4ee5-b8b1-bd0780aa500e',	'role_list',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'SAML role list',	'saml'),
        ('3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'profile',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'email',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('fac87716-08dc-46f5-b3af-06df9b8fb236',	'address',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('b76153b2-3529-4e91-a333-3df0681fc6aa',	'phone',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'roles',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('297e9618-ab35-42d0-9805-86c019d5c054',	'web-origins',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'microprofile-jwt',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('0083e4d4-6524-4fca-a921-2e7b5a028141',	'acr',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect'),
        ('37ec9784-252c-4358-acfb-b4405d80d20c',	'offline_access',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('743a9fdf-febf-49b9-9cb7-d342219e25ac',	'role_list',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'SAML role list',	'saml'),
        ('4aff707c-2e3c-445e-8b93-e69fb34cf145',	'profile',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'email',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'address',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'phone',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'roles',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('e9ecf03e-974c-4e15-871f-d41b33f8c242',	'web-origins',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('bc5d0051-0e15-46db-853f-1ab67e771cad',	'microprofile-jwt',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('d41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'acr',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect');

        CREATE TABLE "public"."client_scope_attributes" (
            "scope_id" character varying(36) NOT NULL,
            "value" character varying(2048),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "pk_cl_tmpl_attr" PRIMARY KEY ("scope_id", "name")
        )
        WITH (oids = false);

        CREATE INDEX idx_clscope_attrs ON public.client_scope_attributes USING btree (scope_id);

        INSERT INTO "client_scope_attributes" ("scope_id", "value", "name") VALUES
        ('b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'true',	'display.on.consent.screen'),
        ('b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('da4b14d8-fe98-4ee5-b8b1-bd0780aa500e',	'true',	'display.on.consent.screen'),
        ('da4b14d8-fe98-4ee5-b8b1-bd0780aa500e',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'true',	'display.on.consent.screen'),
        ('3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'true',	'include.in.token.scope'),
        ('a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'true',	'display.on.consent.screen'),
        ('a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'true',	'include.in.token.scope'),
        ('fac87716-08dc-46f5-b3af-06df9b8fb236',	'true',	'display.on.consent.screen'),
        ('fac87716-08dc-46f5-b3af-06df9b8fb236',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('fac87716-08dc-46f5-b3af-06df9b8fb236',	'true',	'include.in.token.scope'),
        ('b76153b2-3529-4e91-a333-3df0681fc6aa',	'true',	'display.on.consent.screen'),
        ('b76153b2-3529-4e91-a333-3df0681fc6aa',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('b76153b2-3529-4e91-a333-3df0681fc6aa',	'true',	'include.in.token.scope'),
        ('bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'true',	'display.on.consent.screen'),
        ('bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'false',	'include.in.token.scope'),
        ('297e9618-ab35-42d0-9805-86c019d5c054',	'false',	'display.on.consent.screen'),
        ('297e9618-ab35-42d0-9805-86c019d5c054',	'',	'consent.screen.text'),
        ('297e9618-ab35-42d0-9805-86c019d5c054',	'false',	'include.in.token.scope'),
        ('2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'false',	'display.on.consent.screen'),
        ('2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'true',	'include.in.token.scope'),
        ('0083e4d4-6524-4fca-a921-2e7b5a028141',	'false',	'display.on.consent.screen'),
        ('0083e4d4-6524-4fca-a921-2e7b5a028141',	'false',	'include.in.token.scope'),
        ('37ec9784-252c-4358-acfb-b4405d80d20c',	'true',	'display.on.consent.screen'),
        ('37ec9784-252c-4358-acfb-b4405d80d20c',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('743a9fdf-febf-49b9-9cb7-d342219e25ac',	'true',	'display.on.consent.screen'),
        ('743a9fdf-febf-49b9-9cb7-d342219e25ac',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('4aff707c-2e3c-445e-8b93-e69fb34cf145',	'true',	'display.on.consent.screen'),
        ('4aff707c-2e3c-445e-8b93-e69fb34cf145',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('4aff707c-2e3c-445e-8b93-e69fb34cf145',	'true',	'include.in.token.scope'),
        ('2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'true',	'display.on.consent.screen'),
        ('2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'true',	'include.in.token.scope'),
        ('eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'true',	'display.on.consent.screen'),
        ('eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'true',	'include.in.token.scope'),
        ('05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'true',	'display.on.consent.screen'),
        ('05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'true',	'include.in.token.scope'),
        ('514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'true',	'display.on.consent.screen'),
        ('514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'false',	'include.in.token.scope'),
        ('e9ecf03e-974c-4e15-871f-d41b33f8c242',	'false',	'display.on.consent.screen'),
        ('e9ecf03e-974c-4e15-871f-d41b33f8c242',	'',	'consent.screen.text'),
        ('e9ecf03e-974c-4e15-871f-d41b33f8c242',	'false',	'include.in.token.scope'),
        ('bc5d0051-0e15-46db-853f-1ab67e771cad',	'false',	'display.on.consent.screen'),
        ('bc5d0051-0e15-46db-853f-1ab67e771cad',	'true',	'include.in.token.scope'),
        ('d41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'false',	'display.on.consent.screen'),
        ('d41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'false',	'include.in.token.scope');

        CREATE TABLE "public"."client_scope_client" (
            "client_id" character varying(255) NOT NULL,
            "scope_id" character varying(255) NOT NULL,
            "default_scope" boolean DEFAULT false NOT NULL,
            CONSTRAINT "c_cli_scope_bind" PRIMARY KEY ("client_id", "scope_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_clscope_cl ON public.client_scope_client USING btree (client_id);

        CREATE INDEX idx_cl_clscope ON public.client_scope_client USING btree (scope_id);

        INSERT INTO "client_scope_client" ("client_id", "scope_id", "default_scope") VALUES
        ('739d472a-69a5-4544-8342-fb084f397457',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('739d472a-69a5-4544-8342-fb084f397457',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('ed3d8d07-6789-4e64-9bac-83ec3ec5fb74',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('d2594db5-c5f6-4276-9ecd-ff737f9c2957',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('2d7a4580-db88-43df-a503-697ab83d3446',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0');

        CREATE TABLE "public"."client_scope_role_mapping" (
            "scope_id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            CONSTRAINT "pk_template_scope" PRIMARY KEY ("scope_id", "role_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_clscope_role ON public.client_scope_role_mapping USING btree (scope_id);

        CREATE INDEX idx_role_clscope ON public.client_scope_role_mapping USING btree (role_id);

        INSERT INTO "client_scope_role_mapping" ("scope_id", "role_id") VALUES
        ('b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'80bd0d9b-0994-4e68-84f6-fb1e5cd237e0'),
        ('37ec9784-252c-4358-acfb-b4405d80d20c',	'e4719bd9-57c0-4746-aa28-ee3bd2f281e9');

        CREATE TABLE "public"."client_session" (
            "id" character varying(36) NOT NULL,
            "client_id" character varying(36),
            "redirect_uri" character varying(255),
            "state" character varying(255),
            "timestamp" integer,
            "session_id" character varying(36),
            "auth_method" character varying(255),
            "realm_id" character varying(255),
            "auth_user_id" character varying(36),
            "current_action" character varying(36),
            CONSTRAINT "constraint_8" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_client_session_session ON public.client_session USING btree (session_id);


        CREATE TABLE "public"."client_session_auth_status" (
            "authenticator" character varying(36) NOT NULL,
            "status" integer,
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_auth_status_pk" PRIMARY KEY ("client_session", "authenticator")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client_session_note" (
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_5e" PRIMARY KEY ("client_session", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client_session_prot_mapper" (
            "protocol_mapper_id" character varying(36) NOT NULL,
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_cs_pmp_pk" PRIMARY KEY ("client_session", "protocol_mapper_id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client_session_role" (
            "role_id" character varying(255) NOT NULL,
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_5" PRIMARY KEY ("client_session", "role_id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."client_user_session_note" (
            "name" character varying(255) NOT NULL,
            "value" character varying(2048),
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constr_cl_usr_ses_note" PRIMARY KEY ("client_session", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."component" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "parent_id" character varying(36),
            "provider_id" character varying(36),
            "provider_type" character varying(255),
            "realm_id" character varying(36),
            "sub_type" character varying(255),
            CONSTRAINT "constr_component_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_component_realm ON public.component USING btree (realm_id);

        CREATE INDEX idx_component_provider_type ON public.component USING btree (provider_type);

        INSERT INTO "component" ("id", "name", "parent_id", "provider_id", "provider_type", "realm_id", "sub_type") VALUES
        ('a2102784-10a7-4e89-b0b5-cae1e0e0bd92',	'Trusted Hosts',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'anonymous'),
        ('274ba0ba-88eb-4597-9b11-5bf6b8c55d27',	'Consent Required',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'anonymous'),
        ('44fb6476-94b4-4cbf-80dd-7cce32fa40cb',	'Full Scope Disabled',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'anonymous'),
        ('1a3b79e7-3481-4c5e-8a12-0d1f5a8337c9',	'Max Clients Limit',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'anonymous'),
        ('6ff27003-84a2-4859-aacc-b2319edb1a2c',	'Allowed Protocol Mapper Types',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'anonymous'),
        ('eee2b601-3390-4976-a5d9-bcbabaab3ff4',	'Allowed Client Scopes',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'anonymous'),
        ('14ec6aa3-fe59-4669-be46-5de19145b6da',	'Allowed Protocol Mapper Types',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'authenticated'),
        ('7ef135dc-8f05-44e3-a37e-3cc276d104be',	'Allowed Client Scopes',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'authenticated'),
        ('9372fa0b-30ef-4bb6-b25f-3ead4ea7654c',	'rsa-generated',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL),
        ('45d3105d-9fb4-496a-999a-5d7b3198049b',	'rsa-enc-generated',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL),
        ('f88132b7-66e6-42d0-b295-8efe3f4f66cf',	'hmac-generated',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL),
        ('1519ed5e-c99e-4d59-b2af-64d7a75444ef',	'aes-generated',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL),
        ('53b8fde0-91dc-496c-b079-ce29488094ae',	'rsa-generated',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL),
        ('1a984ec6-75d2-421f-a11a-db916c5f17c8',	'rsa-enc-generated',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL),
        ('376295b9-283d-4e5b-8f08-8d5b12ce65df',	'hmac-generated',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL),
        ('2cbf66ab-1e3c-4706-94da-ad008cf252c7',	'aes-generated',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL),
        ('92e8ee60-7fd3-4063-9a8c-a4e3e0bc8068',	'Trusted Hosts',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'anonymous'),
        ('1ddfda8b-8f09-4627-864a-df025a038b31',	'Consent Required',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'anonymous'),
        ('ed4a3a87-810e-46a0-9a75-a6c109abc1dc',	'Full Scope Disabled',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'anonymous'),
        ('03fa57aa-d6a5-45b8-98ad-9a41bd0f4e05',	'Max Clients Limit',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'anonymous'),
        ('bab1a67e-24c3-4a18-80d4-9ffceccace65',	'Allowed Protocol Mapper Types',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'anonymous'),
        ('2ebb80b3-5f14-4339-97d2-ce7b4909d883',	'Allowed Client Scopes',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'anonymous'),
        ('de4d160e-253c-496b-985d-6decf1865f86',	'Allowed Protocol Mapper Types',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'authenticated'),
        ('dc72caa8-9a3d-4bfd-9102-238b3938ec3b',	'Allowed Client Scopes',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'authenticated');

        CREATE TABLE "public"."component_config" (
            "id" character varying(36) NOT NULL,
            "component_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(4000),
            CONSTRAINT "constr_component_config_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_compo_config_compo ON public.component_config USING btree (component_id);

        INSERT INTO "component_config" ("id", "component_id", "name", "value") VALUES
        ('e6290f9d-982c-4c95-a0b5-6b7cdada37fe',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('acda80b3-b5f4-481f-83eb-2f9189724d6a',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('96c8e677-d6f6-4fcb-9ede-027ae2af227c',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('5352ce93-6608-482e-9e7f-fef6b40a3f5d',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('5667d075-a50e-44f5-9e4b-95a263c41dc3',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('25b61632-1f09-4b7e-8374-f2fbefbb9b83',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('efe995b7-28c4-4e83-b147-a4ab5285fab8',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('bd2ec9f4-c034-4e55-997e-5fb7f332abce',	'6ff27003-84a2-4859-aacc-b2319edb1a2c',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('93efca72-e9bd-4698-9e7c-52a355a853cc',	'a2102784-10a7-4e89-b0b5-cae1e0e0bd92',	'client-uris-must-match',	'true'),
        ('f83f3e8a-9c80-4243-b24c-b5262179edb4',	'a2102784-10a7-4e89-b0b5-cae1e0e0bd92',	'host-sending-registration-request-must-match',	'true'),
        ('8982b15a-9b2e-4594-a3ab-db811e8cddc2',	'eee2b601-3390-4976-a5d9-bcbabaab3ff4',	'allow-default-scopes',	'true'),
        ('17990159-302b-477b-9721-3ed8753498b0',	'1a3b79e7-3481-4c5e-8a12-0d1f5a8337c9',	'max-clients',	'200'),
        ('a2816bba-d6a4-4563-9352-ac5909a9b611',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('1555d27c-8979-46c6-abdd-2776b2bd690e',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('cef84130-e521-4f63-8dac-2f015d38f6de',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('b67c45a8-a4cb-48c5-9330-c0517d290f6b',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('4f07c6e4-e251-4ef5-86be-18df6712a10b',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('7e8c37ad-c1de-42d2-91e4-ddd113a71172',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('ab74f034-3253-46be-8303-2f2e9d3b07c6',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('5f454b24-d76e-4c3f-8def-5ad7d23674af',	'14ec6aa3-fe59-4669-be46-5de19145b6da',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('172d0f1d-3ff4-4f50-b127-c5340e71d32c',	'7ef135dc-8f05-44e3-a37e-3cc276d104be',	'allow-default-scopes',	'true'),
        ('1d5b208e-3b11-47eb-b34e-5bb398270cc5',	'f88132b7-66e6-42d0-b295-8efe3f4f66cf',	'kid',	'09507d71-fa16-4bee-9b64-66ad073d1237'),
        ('b1525eb0-666e-4168-97b3-513d6add19ae',	'f88132b7-66e6-42d0-b295-8efe3f4f66cf',	'priority',	'100'),
        ('b38066b3-ecd1-4fc1-a508-53f515aedf17',	'f88132b7-66e6-42d0-b295-8efe3f4f66cf',	'algorithm',	'HS256'),
        ('fa1d543f-1fca-453d-8925-6a7936f7e80c',	'f88132b7-66e6-42d0-b295-8efe3f4f66cf',	'secret',	'CeCQFmD5FQmpO32mEbeEjZr_Yeeh0oxIqQvuYsX1o2KZ5Xr-8-YbywwchVeeUCcB2iI1XEsWUQEyok8QT6RUMg'),
        ('98c76a14-0372-4977-bd37-7a3de34821d5',	'9372fa0b-30ef-4bb6-b25f-3ead4ea7654c',	'certificate',	'MIICmzCCAYMCBgGXJlcjGTANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNTMxMTIzMzU0WhcNMzUwNTMxMTIzNTM0WjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCop/qCyQlAbT6ogPjSKuxdWZJIhd9jb5oKtVts2yF6wkzPvH0gvPnkfNJ21it/5NHNSyyL9vhH+YebDc3YfC3npA+8DJuRBhCXNy2ulFERLjpWDqSZupFLh1Xmrq2ML7ESIzumH2ZO4gFhSYsXO97wo5v/TsUBE+Q/zWJ6gemzg5Pcc/W9r9MoPws7ze3M0/BMaqUJldtjW3TOeMH3oWY9g4peWhJKw5zmCFd5Fsk2eWJMH2IuBpsiRUNzD+twDLZiRzok/GyNXJ4U8Zy0WxuVPGlRKpQLxhfu0FVwmTpminQ2QlzBAO/8K0c7tSyNnE/nouaptVIbhNfW4QJ/MY6lAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAHKQMV26QhqLzcf6mZoI+vwYMMSf0eVF5CEC4P6wICz3lk/EqDykSwg497ATmw6pU6fPfUzFZTgev8UnlF74RKrk6FFKIjDgB6lgRUOfaqdHtKASCZoG4HQVhatWuve+u/QvnmeMkJjtYWwkdwZYIEdLQCVWUmlzFSQWSko3NWdBDUcrVKjEU+N4Tr86cIE6XjIF4Vyqp7G9q9y5k+ePwtjzU05YeT05fx0CIIcWa9v/OUXObEToMbWmTqj2he9DGOwbfxof9DNX3e/c8koeV7RnHHY8TnFNP98PXgxLeRPCLEkEOJIwLtsxobD4o/iSOuqPfjS2a45nbSmqdpDMSLY='),
        ('f886363b-0b6f-4538-be25-712ce53e04e2',	'9372fa0b-30ef-4bb6-b25f-3ead4ea7654c',	'priority',	'100'),
        ('1a7541a4-6028-48e6-a2ed-516c4b710002',	'9372fa0b-30ef-4bb6-b25f-3ead4ea7654c',	'keyUse',	'SIG'),
        ('797ae12b-9f05-40fc-9d87-3974e2c28a9b',	'9372fa0b-30ef-4bb6-b25f-3ead4ea7654c',	'privateKey',	'MIIEowIBAAKCAQEAqKf6gskJQG0+qID40irsXVmSSIXfY2+aCrVbbNshesJMz7x9ILz55HzSdtYrf+TRzUssi/b4R/mHmw3N2Hwt56QPvAybkQYQlzctrpRRES46Vg6kmbqRS4dV5q6tjC+xEiM7ph9mTuIBYUmLFzve8KOb/07FARPkP81ieoHps4OT3HP1va/TKD8LO83tzNPwTGqlCZXbY1t0znjB96FmPYOKXloSSsOc5ghXeRbJNnliTB9iLgabIkVDcw/rcAy2Ykc6JPxsjVyeFPGctFsblTxpUSqUC8YX7tBVcJk6Zop0NkJcwQDv/CtHO7UsjZxP56LmqbVSG4TX1uECfzGOpQIDAQABAoIBABhYm1CK3cOHoZ5oc8K/AHzfQ78lWCB+CUIXePmnTlduufDY+EUkEny5yhXuh7wAF4snmWghACk/q6Ql43unPWctZzoRpdlTA00Y4YbMkUFGJ5Spq3gnlvbMtAlHWPUeI1UHcgJikP29IU7ec24ord+V9pxtzgGAUhgluVwO6eIIVb4rxQweC8ar1simCV1Sj7r0Q0VFWeomEdNH+ql1EH572sLywA0E5wqgKDo/t4DU5zLQgg4fCf2gjdbCKxz3/c+nzedkswzYG/UHxmpODYQfyBABxj8NcvugvKx7etOL5CdZdCYZQXUi4/kw8Fu1LxJPEo4NbZTVrxNg5wHFgKcCgYEA4SnST/LbqBoBJZtnfCbofXqOAJZwYS1lbGMmowZuB+/4qqZ6l7YmHgnRih76obMeNbrvlsqIniVwDydYE3kbLVZnopi7nlwmjATHk7Ld1Yr0jCa6nhYnDTPF9RX6VIw8qEgBPXAGyIsASjGPwCjcoNFiqs2h8zfvSK7nKQtZRkMCgYEAv8EG5ljBXUzJnzQchBfe8gchI21oCifj7jc8XcNvr6g5Z0so2gpeF7UKHeJ+R7oBkIHFP+6avH/bBegcSxnHv0+dXH9zdotKC8779CP26aA5mSuGLwGhbBcJxdz6PmDhXLww0ZzJupi7y+IWHqWD9IlKxv+JwQcwFkqgclmD7PcCgYEAvgbbthmShEaeHS0Qo1j68ZtS6PAOHBWB+X2ciA3PIufGB8/EuvFBLiy/KUCH6ZwPrOz9c1evATk7X66myJh29UZs5G7eOQIwjg8imGZmjeheB0JQUUcE4kGFmhhYUq6gKq3KTh2TvACD2BoH5fcWbKsKCHjPa0MTsVXbRWqMh50CgYA6eVFyUuR8YBsnyktb2K+q3JO1avqMBkgSb5OgACYByenfXuMRQpIY5qM0I8pgcw9hEsGk0k0raE3RCkv+dtkbtNqGx3LdVnJ5EWBxvbng2nRWChi8IuInxQq85LsG5KI0lkrI0OgPV94EuWgHZm/L1UYRUwGd9GQsmAs/c56wVwKBgAnJFnwQ0UdeKePRKDjm2rXGcXv8ORqSGfzAfUfZjeuNb+sBFeBfZ0x6ikL2pxS6VmU0T/+F1As5dLbRHWd01LqSUu5OWkGYyS/ZSPpcRlKAPgvf/KREmzmIFR+CwTOWc2JtktIJ7TV+bqQ2wDLD24dNlMvi8oN029n8bg84rlwM'),
        ('afeb06ef-b3a2-4508-b638-ce900b51e203',	'45d3105d-9fb4-496a-999a-5d7b3198049b',	'algorithm',	'RSA-OAEP'),
        ('582b49fd-d81d-48be-b45b-9e5ef2a49879',	'45d3105d-9fb4-496a-999a-5d7b3198049b',	'certificate',	'MIICmzCCAYMCBgGXJlcj9DANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNTMxMTIzMzU0WhcNMzUwNTMxMTIzNTM0WjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDg0LtjzOaB7mLLu+ojkMPWttTmlw4198piXiN/Gb6BPwr1T8odMMW40d8BW+WGqbwTfuOFci9Zt/laPtrNly9ITxnMCaA+3CNDV3Ne2j4WBO9Vx78x81ESmrNBWiIxwF5XaZEi/zqyJ2/C6hhsUrOsmOBp1rKaKpwjy5Wpqtf4e9mQRRsD8h6J5QiDMuGulGogULaGpP2A4xncrDjxkkoW6weespJ92tBlxf9Sonsnj27sO+bPMInULV1+QZUPgv+WL5eVJ89Ks5SvtcLR3PefupCCL3L1/Kr4VgvzCAhI3IKOX397f3gdWsTKnmbh8UTCw/RIzYIjUJ7cRdbLkQ6FAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAGTEA9MkvkGv8zrkGCW1v/jVpuZxQlJtT5MhGP/5uxaJSdoMoHZOVwUHqfY3CpkGxi86OzWCV71bhmyjP5undO8oipZf6ZKpVxIzF5jb4vseOmDKW76cRSRMpYFXd9UglorDfMtfOtqGl2V8dSRA50/N4zog+ox/vIPfc+NKf3+cLZgmTGQqG21jeCGkrIjJ91bhEGxZj9u/uZdZJ0Do237ZcRW5oFj9ocC5ddP/6Z9hk+Qi2zocfB23oOcHnHbTw9jvuqMMcXfLl6kpZKHqFoIGBSurDeo8ZawnWXMoz2SgnZ3y8mhpuDzQNf7cTaCZgiWse7BZCW9qHYHKXiT/k38='),
        ('4431d608-6ba6-499e-9c29-f9b370a2616e',	'45d3105d-9fb4-496a-999a-5d7b3198049b',	'priority',	'100'),
        ('9264b777-278a-4587-a23f-91ee78a7311d',	'45d3105d-9fb4-496a-999a-5d7b3198049b',	'privateKey',	'MIIEpAIBAAKCAQEA4NC7Y8zmge5iy7vqI5DD1rbU5pcONffKYl4jfxm+gT8K9U/KHTDFuNHfAVvlhqm8E37jhXIvWbf5Wj7azZcvSE8ZzAmgPtwjQ1dzXto+FgTvVce/MfNREpqzQVoiMcBeV2mRIv86sidvwuoYbFKzrJjgadaymiqcI8uVqarX+HvZkEUbA/IeieUIgzLhrpRqIFC2hqT9gOMZ3Kw48ZJKFusHnrKSfdrQZcX/UqJ7J49u7DvmzzCJ1C1dfkGVD4L/li+XlSfPSrOUr7XC0dz3n7qQgi9y9fyq+FYL8wgISNyCjl9/e394HVrEyp5m4fFEwsP0SM2CI1Ce3EXWy5EOhQIDAQABAoIBAAMesnMMnWhRNBrKtVGgCS+6ItM+ZmqWUT8zOj/hzhSCGPdVj6L/EcdiVjtxGIrZYSxOFv87me+fT30SoTu4LOZOfrenrIsix/R/yrCWy8THdcDVgFBDpazh67ns88uH9Wc2Jlb7fseJJ5JnaEZckXTEPF0LpzfffoI6qY2MuumOW8evLFN2V20SaeQMjX8KTLW/XH5S3hlgpnFTaX/jCKS20JUqee8Rm/ekRkuj4uCuyKCAfD9ibVqW3TSKA7o2rfUGGYCwmjcNU7Dloys5ElfVCw/abgfSuE1xERZFcmZugBNuR1NCqqqV80qU9dsEE62cjVCUj85Y7/EKDzfMYnECgYEA/gaaIjoL3+zbZMDCiLzvqzYrWEyc+j9+mO1nP2RdzHOpcd7NN0FIHDgmLmhqCKyfSGebuNxmmYssVsDHjDuXhJHqdqggBDTzFjYhVOktCy0QpYte/J+MsdaUyVgXGXowR7I65lSusfwI6WmT7JQ/0gU8PhuDvP0m5e2jj3nILvkCgYEA4pADog6IH4j1GkUy+TeBocTUqHvLYH6vAQMDcM/no9kri+dJHFCLqCUcqsydWbDvgBZBrMntDkHn5G159Z+Xja1rZsphMBclwYsUVyYfFkYVDFkNAwDMiuO6ExTzRvFScBPqZuXMJna2zLu+x28RGDGStbeqxQiuNwnvwIikou0CgYAlKeoa38sk844Va24KeznFHqOww2Sj5+4piXH1gEWIx9Meaa40S0S1fDF50KTSqA6VlTLfFL+d7xKJjqKjDYTZTE+1FFu6wdReh4TQ21xPqkQpM/6tjtoSmoYh/tVCUPIHSzf6wqDcQTc8jsmpbvb3TvTFUVkwqQir0tx+/R4gYQKBgQDcvMuNXPMrJlkHm1wqi/X1ErzO2q4v+b2wEquLgkSB0tkmmdUVjTREQsRHdMvp0wXFmemq26HnPjccy70DntYd51S49XzzSdozU7ohnrazENz5Btoyjti6iV2FsefEuuJC6GR8lG/vGcLIfjNGQ+Q32jTCb4wmJGr4nOnGgFtHkQKBgQCrCwx90MH02POtBr4cHh9JRRczd8Ol2wYnA0wt1mteNNuOk24YULl07rqtacr/u1FXqo691o4YugmbCojFLSHK/1Up17Ksrihbm764cfPlsdxd0hHIJjOwXHBld1p8xsN+HxKimOeF/MlzaFGbABLfkLJWKuyqTn/CGe63AJxGgg=='),
        ('0caaf5c0-c2c1-486c-a14f-cbee0f23d896',	'45d3105d-9fb4-496a-999a-5d7b3198049b',	'keyUse',	'ENC'),
        ('dee93c5d-9517-4847-ae9c-dde34e822b37',	'1519ed5e-c99e-4d59-b2af-64d7a75444ef',	'secret',	'v88KMl1JnlM89E79JfbxsQ'),
        ('ef56f0e7-7cc4-4b6d-bd5f-7f5f827b34a2',	'1519ed5e-c99e-4d59-b2af-64d7a75444ef',	'kid',	'f9216526-6b65-4027-9526-616b673daeb7'),
        ('448d2bba-3492-4728-974e-fa4feb8685a8',	'1519ed5e-c99e-4d59-b2af-64d7a75444ef',	'priority',	'100'),
        ('0817423d-202a-4ad4-9ecc-c1220579afba',	'2cbf66ab-1e3c-4706-94da-ad008cf252c7',	'kid',	'bf28f85d-0798-47c2-99c3-07eebc289df4'),
        ('d0de1b99-fa9e-4921-ace7-e95ea4af4f15',	'2cbf66ab-1e3c-4706-94da-ad008cf252c7',	'secret',	'-YUBQ0ySSgo-xEo71Dqs-A'),
        ('ad822324-429f-4f81-93a2-1eec4167b453',	'2cbf66ab-1e3c-4706-94da-ad008cf252c7',	'priority',	'100'),
        ('98ce4b8f-ab6c-4db7-9cfb-0be74c7262ee',	'1a984ec6-75d2-421f-a11a-db916c5f17c8',	'certificate',	'MIICmzCCAYMCBgGXJlqqxjANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZvcGVuazkwHhcNMjUwNTMxMTIzNzQ1WhcNMzUwNTMxMTIzOTI1WjARMQ8wDQYDVQQDDAZvcGVuazkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDppm92xn2S5YDaiJU8u6zm1BL0ABTdAI82tACKcLNqbmCIfOH1uZIMkSv9OysCtwk1YD49D0bMgOdARuHvAeSEjFl6juZzHcxQIY+LZi7IwHITwWDZevUZcB6DzXvtLSN211rZqhXizYfv5ScNMIyIUkOqTBA+kcOcpJZ3ZCJX35dtno+MyK3+RTRLMGuTl0DfrdMj8an8ThaRilDzDAAngvvd0Rery1V2hAJdB1ipUPQReYk/vq5XFrTN1BpHTBGuYN3FA/BwMKXPlhz0CxSmqAu56V+mHIw7o1f/Gl9RBby9tL7fG1FnLew9kk7Mn/9pRqDqj5wSLr7oRZKYOHRzAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAJdZT9kwSGvMFamGoO+xsN2RDmYHci35gyInt6NqKFRiQo3yBWnanZhdIuAkpudl5wcS+CP6SvNIm4rHd88veEiIULdiUMZ7VReTPEnyNDcO4hK44eOzSpbLBjDSyD+BBxvb2+YXVkDpnoMmn2rPOTHOSQNuGhrBnJHlTrS8gDAJaTYdUYOde1G+qMKkrQSEeRUNZzQ2PlMT8ojQ/OynbchUGEpMt2M2IpK+SJZeMa1eGYJ6vU3fzIf/l6pVZRNtakaKqsPqPTSKYu0N6TMB9stB/x1/gtSqh9OcKtHFMBROBsUl0cccw9vCfDEqowBjqcY46PrbaDfoCkYDpSxrNj0='),
        ('95640bd9-3a0d-44e8-85ee-cefa6e6bee55',	'1a984ec6-75d2-421f-a11a-db916c5f17c8',	'privateKey',	'MIIEowIBAAKCAQEA6aZvdsZ9kuWA2oiVPLus5tQS9AAU3QCPNrQAinCzam5giHzh9bmSDJEr/TsrArcJNWA+PQ9GzIDnQEbh7wHkhIxZeo7mcx3MUCGPi2YuyMByE8Fg2Xr1GXAeg8177S0jdtda2aoV4s2H7+UnDTCMiFJDqkwQPpHDnKSWd2QiV9+XbZ6PjMit/kU0SzBrk5dA363TI/Gp/E4WkYpQ8wwAJ4L73dEXq8tVdoQCXQdYqVD0EXmJP76uVxa0zdQaR0wRrmDdxQPwcDClz5Yc9AsUpqgLuelfphyMO6NX/xpfUQW8vbS+3xtRZy3sPZJOzJ//aUag6o+cEi6+6EWSmDh0cwIDAQABAoIBAAClVNQClJ1XSejeWDRy+8G0WLPJ0+3JTmWQx6269t6LuCd7fXthdcLdbecv96jO0KArUGbauEDeKHk5DVwu3vxn1AOSwJvOYZPEkkcsPCeCJ9FKVPNvJ1v73/3Nh3kDjD7zjMF13Lleh8/UnzbiPxZNdfOGxBWUcikT9W3VGKQOeGy1pqMkeMQlpUKM7VktR/CAPQYG6Vtom85+cKPGRxmofLC6DDzSN6CiKwIcp75EhTfIySpQF8jX9u2U9oN+1Fio8l1IpapzS6/ek9mLf1aZ7RBY3LphFwJh8NcFbnEc2seXApa1m3kDh8Bvtsz4VAvKr03eEvtflCh4JogXJwECgYEA90kZyTm1nrBSYB1PMM966IFhlfTWhih23UxQf8qphmYPnzyGPCA893mVbuVdP+MjTba5WUlrbGsE6Q0L6BcM8oDj6eeC5x1Y5ZWoXGbZph6kYZUznvayyChJ9uX/7M1tblXNBBBJ1qSzFZyNAon34dH5Ebb4gdXTsae23LFNzEECgYEA8eJSdi8rXRaVIfWCHd+50/SwE2AHQ216KMxsp1xPBKQGn8VLDu+znnRZ0v8lVxgSeT4e4glasm/C+rLsn7XV3c87OCeqc4lpJYh0IetjDxNtb7nVAewq4rV08CAtPzsK5c1Z+gvwu8qQXim+ZJxof68bPXpJeAwdp0DXzgl147MCgYANgCYyQD5ULZgxuyfpqEozi2zCfR2BMZBbwKDceToMJmJP9UP24GOztyWbalZO+J2izokMAOAmiSk5eAbgYvHIA5Vt3b/d0lJaJnOMp+jWEvIyiazJzvovx1NScXgpe4Wv8mtA/4qod1F3qpZgPxwTbh1FBFJI9F2cY2WuYte2gQKBgCxUiT5luLHP0LcvkpA9D4acAqUaZVpjNw9BH+ywSz8TfK9NbksnfSjeNBQqdMJ4g/I6g1hz1G59cyOVJS7EGZUURMsu4dtY5K9fScFprXb23YirazF2AgoYrXzQuNoszPt+3i2ogI86Oak4R0+TJse35OsofUyGE+0yQhrduwBnAoGBALHTjyt1UVBmCBeBjCe4nXhiiREfcpyyTfQ93ui3IUVs8pQ//h5taQ9nqDyadfUA2YZRRtqWRyZ5FwOH/WJtbibkpyUct6KXzlt/k2hkfmwuwjWg51GdGmzlkJlPBOQkp31Pmnsu0/QMyV5gk6I3VtGsyX/phlyhPzMGVLqx8Q3b'),
        ('cbc11296-feee-4bb1-86fb-a56848d1d268',	'1a984ec6-75d2-421f-a11a-db916c5f17c8',	'algorithm',	'RSA-OAEP'),
        ('82f83a30-1ca5-47c4-a89c-c2da7ecf538d',	'1a984ec6-75d2-421f-a11a-db916c5f17c8',	'keyUse',	'ENC'),
        ('1772e8aa-ab54-4373-b865-cf5105a7940d',	'1a984ec6-75d2-421f-a11a-db916c5f17c8',	'priority',	'100'),
        ('66c2a654-ad51-43c6-833c-7432ee4a573c',	'376295b9-283d-4e5b-8f08-8d5b12ce65df',	'secret',	'6p_BMKmFk3i464ASok86YPFukRZ-FVrWrL9w-igS4qZ0q-KOIKTyNyU2JdORJCNjgGh_w83E5kKjr8j8QQ1GWw'),
        ('bd02427a-4c9b-44ec-86b7-60e6b2c249d3',	'376295b9-283d-4e5b-8f08-8d5b12ce65df',	'priority',	'100'),
        ('d0bb4bf0-a3ad-4495-b50a-88905b6c7566',	'376295b9-283d-4e5b-8f08-8d5b12ce65df',	'kid',	'40c37965-a14d-4864-b041-2c83b3860190'),
        ('ee487629-6949-404f-8769-d41bae37dea7',	'376295b9-283d-4e5b-8f08-8d5b12ce65df',	'algorithm',	'HS256'),
        ('fb597894-1f7a-43d1-a07d-878444b5822c',	'53b8fde0-91dc-496c-b079-ce29488094ae',	'privateKey',	'MIIEpQIBAAKCAQEA383vKKUA6cBd82Ue24Xrnfa+9VtwJ6g/dOL9qzY/Ldi2ctGblEz4TOefmMCIgPs3lhXi4eTI0bJXt1JT27xOsVOCodCzYJWdf3mCEwaiZZ/B5vhnlBoOEJ+pTFOrwsM9ZOEZx1kEDOZh2lV8XuVZhPWCYzDrtr+RGCf+Nk5BMDb+jp11dmOEIjYm2kOKfJau8nPsFUQkWlucBisfH9BeZ5p5cB+wOvgEyjsmRgcSk0fZx+Vl+7cvrXJVyPKyTF1mfCZrnK8Uqrlba2POtn99q4s8wkAC8FiVB8RCNTwcxSRmHHzuA2Eix8P3VmretKA24fe20aUO1xh4GYa1xejSNQIDAQABAoIBAAMZW2vxKPuTpdlmd5BroNEdpxRHsVxjOIm6UO/rfuF4JEKfw2tAy5lpBa08UUeT8veZkvw0ylEZCpznv3CGAYL0GJV8CdfXCojG2nW//ESy+yDjJLAI0lQJmfMMH49BW9rHQS+g/GzfTdCTqXa4703nhmgwN7temLhAaYU//SgZPDdW/De32hXu9GXy6crup1pZzT9X2rtzo0GAhV/7w12gAGGEYvquQHc7+AYHsghWl1cnS5V4ObV2tyR0+DpQTsOdUsBZUDu/M6dgWgycGlmCZbVbhIEeIZdaohYKoRXOzarPhlT0F5bcpoIHI4JMdWwTKwX7A0J2gZ52F7/1FgsCgYEA+o9fZ4cAyeBEaSjUkHWE0MWwZks+3yzlKUOY9Fp5ObA+rKYdWj83Z/FPEw5tvKgcbVT6orPaP1o3et6OaCnUFX92tbbdbT/CjtAKUmOlMoG5XcuV9WSQ5S+xlibO2fQNAhGVDfbhCiJPp/qfrPiriUJI+LkmbQsDNg60ROfodHMCgYEA5KnaLVTHWWtCd566Oj9c0wfoKsXmyGfkjBvqo11ZlXG4CvxJkMfsS7/Vk23XGLHepMxxiWB1gEfTlDPYdceNGkhnZC7c6h2wTcifv3kIkYOY05ugE9reekAaKhpa46C+ZbP3fb7OFUYuGvraarwbHygwwpE4dsjnrT87Ufa7HLcCgYEA3tl/4ENpxyOplOJU1LtBSrJZZ1ILdFGo7F+L/eEuKsn/pG0GdEr/i3pLe0Z9Aat/xRos5WMTfP9Pkv+5Jxn04L4CpjIjlNR9xalxVL/9oQNSDANTt/MfEEiwKT0RAzcWDr32lXn7w5iqwludgymU37rNzjJW4+tEiuLfIN4mmNMCgYEAprfsmlaQznJ00NCLCUL/g+Hk9aK7JG0TZtBH4HqEgYCbZm8RmB0b/RLOPqK8TIYLRe7F7RrGMRStgAXZEe6/w2T89T9x1MR2sg/P4YP2qlnfiJGUQkW3Jj0slnwGvqaJi4+OqEEA8uPrY8J4k4+42pusCYBmQb0zpc5PivAw9rsCgYEA39jnsyCH5DVR0kqDffdjvHABHKI8qsMpQRcFObXsJqnknLaIX7mcrOOwz7B4kSeXjfPApGY1dcnt+CZKpsTjTSyyyL/JMnBKG1S29k7RP9XAVrQpg2W6NiHcU9273laPjez756spTwlfeFLIBwf6/JY7YgdXwtYjUwMq6kh+FRo='),
        ('593c9b28-70c5-4994-bee4-cad6e6572832',	'53b8fde0-91dc-496c-b079-ce29488094ae',	'certificate',	'MIICmzCCAYMCBgGXJlqplTANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZvcGVuazkwHhcNMjUwNTMxMTIzNzQ1WhcNMzUwNTMxMTIzOTI1WjARMQ8wDQYDVQQDDAZvcGVuazkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDfze8opQDpwF3zZR7bheud9r71W3AnqD904v2rNj8t2LZy0ZuUTPhM55+YwIiA+zeWFeLh5MjRsle3UlPbvE6xU4Kh0LNglZ1/eYITBqJln8Hm+GeUGg4Qn6lMU6vCwz1k4RnHWQQM5mHaVXxe5VmE9YJjMOu2v5EYJ/42TkEwNv6OnXV2Y4QiNibaQ4p8lq7yc+wVRCRaW5wGKx8f0F5nmnlwH7A6+ATKOyZGBxKTR9nH5WX7ty+tclXI8rJMXWZ8JmucrxSquVtrY862f32rizzCQALwWJUHxEI1PBzFJGYcfO4DYSLHw/dWat60oDbh97bRpQ7XGHgZhrXF6NI1AgMBAAEwDQYJKoZIhvcNAQELBQADggEBADer2uhSjUmtMPlAEpiYD0v5/gqAi0s1neW7/PAVaP5V+NCDeAwOmHwZcyX9tmdiDx9e1Xnp5UbyR9bB68BcGhq/0kjMFG6i2FxdBkZolrJZjY2d9FWzedrLSfc76xnkflBuWw1bOtqnzkiUKjqbC/Wz55IzLjZarmDJmAe+1grO5UXL79ZrI1xUaCnA5PJ/wyX+Uq29oNEvTdu6RhKzepwK8G6kIy1DLwbxl7/0AbU92HYblwJi25zJvn2Zlf2RTtjQxwQezUF1wus2P9UekFzzlzWVkgojwAx/XLqjz1x7HiCtUY3bKkcXvHYgXRduLlyi+C62PROkta3SG4NBhec='),
        ('88be8237-79d2-4649-a805-84707f19182c',	'53b8fde0-91dc-496c-b079-ce29488094ae',	'priority',	'100'),
        ('136a8b04-b230-43d3-bebd-6b4e14024fba',	'53b8fde0-91dc-496c-b079-ce29488094ae',	'keyUse',	'SIG'),
        ('8c27beff-0b13-49ea-8340-27a74255ed1e',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('b512cb4f-ffd6-434d-9f1d-01b084fd62ef',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('9e16b4ac-41c8-46b9-be6e-9080172b6863',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('83561ba0-d022-4e71-84e4-40983d158a29',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('f403b414-08ae-447c-893c-8818866fcc69',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('829714e4-cd6e-4c1d-8520-3daf131819fa',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('81daa89e-cec6-480e-bc8e-dc8b34b37cca',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('b361e4cf-e595-40aa-919a-f8475a5fa668',	'bab1a67e-24c3-4a18-80d4-9ffceccace65',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('b577289f-9732-4228-b5ca-d45ad78a5af9',	'03fa57aa-d6a5-45b8-98ad-9a41bd0f4e05',	'max-clients',	'200'),
        ('17037d25-ff88-491f-ade5-7a82de51538a',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('93441efd-9f6a-45d3-879f-8d6560beaa8e',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('f5a735a3-e570-46a4-9316-4e9d27b349e3',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('2fd68e51-b964-477e-8dc6-f384012cf772',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('17ca8bef-76b9-4400-a460-81aa17cfb7b7',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('852ae07a-6a9b-4691-bc81-fdd061b06405',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('89e05ce9-33f5-4cc5-803a-9f806ec26858',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('2b4c90f6-ed2f-4df8-8823-5de26c8a7e91',	'de4d160e-253c-496b-985d-6decf1865f86',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('e20a4a1f-6fd2-45f8-a600-e7922f3f633f',	'92e8ee60-7fd3-4063-9a8c-a4e3e0bc8068',	'client-uris-must-match',	'true'),
        ('9a9dee9b-ecb2-456b-9b13-36ab2a5d80da',	'92e8ee60-7fd3-4063-9a8c-a4e3e0bc8068',	'host-sending-registration-request-must-match',	'true'),
        ('3aa79c09-102d-4e84-8c22-c49e93377d65',	'2ebb80b3-5f14-4339-97d2-ce7b4909d883',	'allow-default-scopes',	'true'),
        ('dc6fc6be-fbde-4574-984f-01f529e72e3a',	'dc72caa8-9a3d-4bfd-9102-238b3938ec3b',	'allow-default-scopes',	'true');

        CREATE TABLE "public"."composite_role" (
            "composite" character varying(36) NOT NULL,
            "child_role" character varying(36) NOT NULL,
            CONSTRAINT "constraint_composite_role" PRIMARY KEY ("composite", "child_role")
        )
        WITH (oids = false);

        CREATE INDEX idx_composite ON public.composite_role USING btree (composite);

        CREATE INDEX idx_composite_child ON public.composite_role USING btree (child_role);

        INSERT INTO "composite_role" ("composite", "child_role") VALUES
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'faa4c457-9ef8-4fe1-8493-cd33bc4d32df'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'4583d0fa-f3ae-4a1d-a398-448c46973081'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'd7c448b2-e77d-4d19-99b3-cd588a2b0433'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'0ec723fb-6eb8-4d4d-9c5c-55c23815fb4b'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'283ac388-5489-46bb-9414-5520c6b3ae9f'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'2622b3e0-2687-466c-846e-55edf881198d'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'6455bfb0-eb5f-411c-a683-0bef6f4deafb'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'f03a10e5-0a08-4315-8336-08d8c8c2cc0d'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'898618a7-1828-49c1-9e99-c47936553124'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'98a178d7-0f4f-4033-a2ea-543ab6ed8dba'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'0a9d13fc-822a-47ae-a4bb-4e76db4a5852'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'599fc6dd-5019-41a6-aeb3-789bec369fba'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'9aa128bb-12c6-45f2-a912-b677e23504e1'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'197c3c69-6e13-46ff-a455-e62fecbb3dad'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'b7c4464f-b99f-4724-97e9-75a68573a2a3'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'71ef8d5e-bfff-4844-91ad-cc2b4d4be905'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'edf7ede0-b97e-4843-bcfa-6cf1014df306'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'8abe6e10-8217-4a21-a739-123b808472be'),
        ('0ec723fb-6eb8-4d4d-9c5c-55c23815fb4b',	'b7c4464f-b99f-4724-97e9-75a68573a2a3'),
        ('0ec723fb-6eb8-4d4d-9c5c-55c23815fb4b',	'8abe6e10-8217-4a21-a739-123b808472be'),
        ('283ac388-5489-46bb-9414-5520c6b3ae9f',	'71ef8d5e-bfff-4844-91ad-cc2b4d4be905'),
        ('cdb7a7b3-1c27-4c46-8e22-3edc962f26ac',	'4b1866fb-3498-402a-b69e-0cc735e6998e'),
        ('cdb7a7b3-1c27-4c46-8e22-3edc962f26ac',	'fb622d47-2346-4def-b901-2d7bfe1a17d5'),
        ('fb622d47-2346-4def-b901-2d7bfe1a17d5',	'a7d17c65-a97c-4612-ad1c-937ba99d8b8b'),
        ('ba1aa283-bbaa-4d57-a5e9-27de19933e8e',	'36a7077b-d566-49e9-8be3-c2d1f1a84f8a'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'7a83a181-baa6-4dff-bfe0-8224a753512b'),
        ('cdb7a7b3-1c27-4c46-8e22-3edc962f26ac',	'80bd0d9b-0994-4e68-84f6-fb1e5cd237e0'),
        ('cdb7a7b3-1c27-4c46-8e22-3edc962f26ac',	'34e7d2a7-9396-47ed-b973-85892ca9395b'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'9d8bc7c1-8d77-4dab-9dea-1f3d4d26c703'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'd1f39f14-202a-4864-951b-3889a127d9be'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'7a911def-a37e-4900-b6d3-593f880c9432'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'2f016158-342b-4ea8-b233-b721cfde5524'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'f195f514-21a6-4c96-85e4-d8c405453e7c'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'19474e5b-cf18-4536-b4d3-782b414a6309'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'd6675c66-7595-4e36-8c3d-02ba42d42178'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'6a36fd3f-f548-4b52-9dc8-dc6372fc018a'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'2d92a9eb-2d41-43c3-adb9-15defac17cc3'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'96a74632-95d1-45e3-a1e2-e3c3a804c356'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'a38bea4e-f485-4d8d-88f5-655813683249'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'0222cc70-2bba-4a94-a2b3-1a7be3dc49cb'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'7e6f7a47-be90-4b4d-846e-2c0e9c989096'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'b3e5d545-3013-4619-a624-c7f845b7ae90'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'90e6c101-689f-44f4-968f-3f5d3a638e8f'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'c8551b05-1af1-4fc8-ba2c-128cc78dfdbe'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'4c966fc5-402a-4fcb-99da-9815043eca27'),
        ('2f016158-342b-4ea8-b233-b721cfde5524',	'90e6c101-689f-44f4-968f-3f5d3a638e8f'),
        ('7a911def-a37e-4900-b6d3-593f880c9432',	'4c966fc5-402a-4fcb-99da-9815043eca27'),
        ('7a911def-a37e-4900-b6d3-593f880c9432',	'b3e5d545-3013-4619-a624-c7f845b7ae90'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'22988268-9152-4ee7-9dec-805bb97d3900'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'a3df954d-2357-4cb9-8830-d8c236f07a1a'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'ad026cfc-81b0-41be-86f3-4a2fa51facbd'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'2661e145-04b2-43b8-857d-c245363c54d0'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'a8696de1-93e0-4783-80cb-8476a661c6e7'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'24ddcbe7-a67d-4725-ab15-cc64f12c0926'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'8abca6a3-0f88-49c6-8cbf-e7280424dbd1'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'c7e53c75-aa4a-4289-bf6d-1b85217e363b'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'060b45c6-c395-4bb7-8914-9808b5899467'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'f3f0df33-d069-495a-8f1f-123d2fa58b9a'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'1f23158e-cc62-4c35-a866-d5e62d11413a'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'd6fb12a6-9976-4c3d-a888-d6f1378ccca2'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'30a6b5db-84f9-41d0-9ed8-71ff891f65a5'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'b413c8eb-e81a-47b3-81d7-a90362351af2'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'81fc68e5-0e95-4e2f-8c71-523c384922c6'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'450f822a-0680-42df-832c-2f804c3e8f23'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'6830d6c1-a9e1-4a94-b164-579bf0dbf7f2'),
        ('2661e145-04b2-43b8-857d-c245363c54d0',	'81fc68e5-0e95-4e2f-8c71-523c384922c6'),
        ('877d5a18-80dd-4135-b1c6-d8a6bb5a911a',	'ededef8e-7f65-4c77-b45e-e016843ae605'),
        ('ad026cfc-81b0-41be-86f3-4a2fa51facbd',	'b413c8eb-e81a-47b3-81d7-a90362351af2'),
        ('ad026cfc-81b0-41be-86f3-4a2fa51facbd',	'6830d6c1-a9e1-4a94-b164-579bf0dbf7f2'),
        ('877d5a18-80dd-4135-b1c6-d8a6bb5a911a',	'd7589b89-d577-44ea-adf3-20feea91a9d8'),
        ('d7589b89-d577-44ea-adf3-20feea91a9d8',	'728199b8-7a79-4a69-8068-3432099f4b1b'),
        ('7485926d-57db-4576-b66b-5ee92d528208',	'2bfaaa56-6815-4c1d-9457-fd63fa335437'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'6cbdac12-5bb9-417a-8e84-ef8e4909476d'),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'0761061a-b3cd-42a8-a19c-6d843b4bf750'),
        ('877d5a18-80dd-4135-b1c6-d8a6bb5a911a',	'e4719bd9-57c0-4746-aa28-ee3bd2f281e9'),
        ('877d5a18-80dd-4135-b1c6-d8a6bb5a911a',	'76204f75-e059-415b-8d74-6fcfcd9b0f60');

        CREATE TABLE "public"."credential" (
            "id" character varying(36) NOT NULL,
            "salt" bytea,
            "type" character varying(255),
            "user_id" character varying(36),
            "created_date" bigint,
            "user_label" character varying(255),
            "secret_data" text,
            "credential_data" text,
            "priority" integer,
            CONSTRAINT "constraint_f" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_user_credential ON public.credential USING btree (user_id);

        INSERT INTO "credential" ("id", "salt", "type", "user_id", "created_date", "user_label", "secret_data", "credential_data", "priority") VALUES
        ('8ac8b9bd-f23e-4404-99f9-8a2346d7afdb',	NULL,	'password',	'3a64daa1-b7f7-4511-aaf8-373d890516eb',	1748694934958,	NULL,	'{"value":"jvPr8ZvGImz59T9d6+PppyQSufP1OxQpjGk5C21fio9wqyvTd8bH/OH0hidkGCXVVD9ucLnSUylNK06nhQLN1w==","salt":"1THovgTaGR0t2zurZrmWCg==","additionalParameters":{}}',	'{"hashIterations":27500,"algorithm":"pbkdf2-sha256","additionalParameters":{}}',	10),
        ('ade24548-1ae8-4ae5-9749-730123acb990',	NULL,	'password',	'ed4ee6f1-9a47-455e-b029-fe246f36c193',	1748695206522,	'My password',	'{"value":"aXB/lI7o3/chjJPAh9LYtd0mx+VWmPXhnP/jZ7OpqleXM5ehOzPjTX7QYhJ9nqDqAI47mKxeGvrUZVEWd6GJJQ==","salt":"fv7JvMf0ojbSHEkJrJJ/PA==","additionalParameters":{}}',	'{"hashIterations":27500,"algorithm":"pbkdf2-sha256","additionalParameters":{}}',	10);

        CREATE TABLE "public"."databasechangelog" (
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

        INSERT INTO "databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES
        ('1.0.0.Final-KEYCLOAK-5461',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.0.0.Final.xml',	'2025-05-31 12:35:32.059394',	1,	'EXECUTED',	'8:bda77d94bf90182a1e30c24f1c155ec7',	'createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.0.0.Final-KEYCLOAK-5461',	'sthorger@redhat.com',	'META-INF/db2-jpa-changelog-1.0.0.Final.xml',	'2025-05-31 12:35:32.067417',	2,	'MARK_RAN',	'8:1ecb330f30986693d1cba9ab579fa219',	'createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.1.0.Beta1',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.1.0.Beta1.xml',	'2025-05-31 12:35:32.100846',	3,	'EXECUTED',	'8:cb7ace19bc6d959f305605d255d4c843',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=CLIENT_ATTRIBUTES; createTable tableName=CLIENT_SESSION_NOTE; createTable tableName=APP_NODE_REGISTRATIONS; addColumn table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.1.0.Final',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.1.0.Final.xml',	'2025-05-31 12:35:32.104573',	4,	'EXECUTED',	'8:80230013e961310e6872e871be424a63',	'renameColumn newColumnName=EVENT_TIME, oldColumnName=TIME, tableName=EVENT_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.2.0.Beta1',	'psilva@redhat.com',	'META-INF/jpa-changelog-1.2.0.Beta1.xml',	'2025-05-31 12:35:32.164668',	5,	'EXECUTED',	'8:67f4c20929126adc0c8e9bf48279d244',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.2.0.Beta1',	'psilva@redhat.com',	'META-INF/db2-jpa-changelog-1.2.0.Beta1.xml',	'2025-05-31 12:35:32.168131',	6,	'MARK_RAN',	'8:7311018b0b8179ce14628ab412bb6783',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.2.0.RC1',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.2.0.CR1.xml',	'2025-05-31 12:35:32.230889',	7,	'EXECUTED',	'8:037ba1216c3640f8785ee6b8e7c8e3c1',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.2.0.RC1',	'bburke@redhat.com',	'META-INF/db2-jpa-changelog-1.2.0.CR1.xml',	'2025-05-31 12:35:32.2336',	8,	'MARK_RAN',	'8:7fe6ffe4af4df289b3157de32c624263',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.2.0.Final',	'keycloak',	'META-INF/jpa-changelog-1.2.0.Final.xml',	'2025-05-31 12:35:32.237691',	9,	'EXECUTED',	'8:9c136bc3187083a98745c7d03bc8a303',	'update tableName=CLIENT; update tableName=CLIENT; update tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.3.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.3.0.xml',	'2025-05-31 12:35:32.296147',	10,	'EXECUTED',	'8:b5f09474dca81fb56a97cf5b6553d331',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=ADMI...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.4.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.4.0.xml',	'2025-05-31 12:35:32.336128',	11,	'EXECUTED',	'8:ca924f31bd2a3b219fdcfe78c82dacf4',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.4.0',	'bburke@redhat.com',	'META-INF/db2-jpa-changelog-1.4.0.xml',	'2025-05-31 12:35:32.338543',	12,	'MARK_RAN',	'8:8acad7483e106416bcfa6f3b824a16cd',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.5.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.5.0.xml',	'2025-05-31 12:35:32.353029',	13,	'EXECUTED',	'8:9b1266d17f4f87c78226f5055408fd5e',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.6.1_from15',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-31 12:35:32.364706',	14,	'EXECUTED',	'8:d80ec4ab6dbfe573550ff72396c7e910',	'addColumn tableName=REALM; addColumn tableName=KEYCLOAK_ROLE; addColumn tableName=CLIENT; createTable tableName=OFFLINE_USER_SESSION; createTable tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_US_SES_PK2, tableName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.6.1_from16-pre',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-31 12:35:32.366686',	15,	'MARK_RAN',	'8:d86eb172171e7c20b9c849b584d147b2',	'delete tableName=OFFLINE_CLIENT_SESSION; delete tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.6.1_from16',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-31 12:35:32.36888',	16,	'MARK_RAN',	'8:5735f46f0fa60689deb0ecdc2a0dea22',	'dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_US_SES_PK, tableName=OFFLINE_USER_SESSION; dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_CL_SES_PK, tableName=OFFLINE_CLIENT_SESSION; addColumn tableName=OFFLINE_USER_SESSION; update tableName=OF...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.6.1',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-31 12:35:32.370893',	17,	'EXECUTED',	'8:d41d8cd98f00b204e9800998ecf8427e',	'empty',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.7.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.7.0.xml',	'2025-05-31 12:35:32.403921',	18,	'EXECUTED',	'8:5c1a8fd2014ac7fc43b90a700f117b23',	'createTable tableName=KEYCLOAK_GROUP; createTable tableName=GROUP_ROLE_MAPPING; createTable tableName=GROUP_ATTRIBUTE; createTable tableName=USER_GROUP_MEMBERSHIP; createTable tableName=REALM_DEFAULT_GROUPS; addColumn tableName=IDENTITY_PROVIDER; ...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.8.0',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.8.0.xml',	'2025-05-31 12:35:32.432602',	19,	'EXECUTED',	'8:1f6c2c2dfc362aff4ed75b3f0ef6b331',	'addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.8.0-2',	'keycloak',	'META-INF/jpa-changelog-1.8.0.xml',	'2025-05-31 12:35:32.439987',	20,	'EXECUTED',	'8:dee9246280915712591f83a127665107',	'dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part1',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-31 12:35:32.741',	45,	'EXECUTED',	'8:a164ae073c56ffdbc98a615493609a52',	'addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_RESOURCE; addColumn tableName=RESOURCE_SERVER_SCOPE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.8.0',	'mposolda@redhat.com',	'META-INF/db2-jpa-changelog-1.8.0.xml',	'2025-05-31 12:35:32.443495',	21,	'MARK_RAN',	'8:9eb2ee1fa8ad1c5e426421a6f8fdfa6a',	'addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.8.0-2',	'keycloak',	'META-INF/db2-jpa-changelog-1.8.0.xml',	'2025-05-31 12:35:32.446703',	22,	'MARK_RAN',	'8:dee9246280915712591f83a127665107',	'dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.9.0',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.9.0.xml',	'2025-05-31 12:35:32.471721',	23,	'EXECUTED',	'8:d9fa18ffa355320395b86270680dd4fe',	'update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=REALM; update tableName=REALM; customChange; dr...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.9.1',	'keycloak',	'META-INF/jpa-changelog-1.9.1.xml',	'2025-05-31 12:35:32.47861',	24,	'EXECUTED',	'8:90cff506fedb06141ffc1c71c4a1214c',	'modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=PUBLIC_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.9.1',	'keycloak',	'META-INF/db2-jpa-changelog-1.9.1.xml',	'2025-05-31 12:35:32.480589',	25,	'MARK_RAN',	'8:11a788aed4961d6d29c427c063af828c',	'modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('1.9.2',	'keycloak',	'META-INF/jpa-changelog-1.9.2.xml',	'2025-05-31 12:35:32.493962',	26,	'EXECUTED',	'8:a4218e51e1faf380518cce2af5d39b43',	'createIndex indexName=IDX_USER_EMAIL, tableName=USER_ENTITY; createIndex indexName=IDX_USER_ROLE_MAPPING, tableName=USER_ROLE_MAPPING; createIndex indexName=IDX_USER_GROUP_MAPPING, tableName=USER_GROUP_MEMBERSHIP; createIndex indexName=IDX_USER_CO...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-2.0.0',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-2.0.0.xml',	'2025-05-31 12:35:32.548947',	27,	'EXECUTED',	'8:d9e9a1bfaa644da9952456050f07bbdc',	'createTable tableName=RESOURCE_SERVER; addPrimaryKey constraintName=CONSTRAINT_FARS, tableName=RESOURCE_SERVER; addUniqueConstraint constraintName=UK_AU8TT6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER; createTable tableName=RESOURCE_SERVER_RESOU...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-2.5.1',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-2.5.1.xml',	'2025-05-31 12:35:32.55325',	28,	'EXECUTED',	'8:d1bf991a6163c0acbfe664b615314505',	'update tableName=RESOURCE_SERVER_POLICY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.1.0-KEYCLOAK-5461',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.1.0.xml',	'2025-05-31 12:35:32.588658',	29,	'EXECUTED',	'8:88a743a1e87ec5e30bf603da68058a8c',	'createTable tableName=BROKER_LINK; createTable tableName=FED_USER_ATTRIBUTE; createTable tableName=FED_USER_CONSENT; createTable tableName=FED_USER_CONSENT_ROLE; createTable tableName=FED_USER_CONSENT_PROT_MAPPER; createTable tableName=FED_USER_CR...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.2.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.2.0.xml',	'2025-05-31 12:35:32.600083',	30,	'EXECUTED',	'8:c5517863c875d325dea463d00ec26d7a',	'addColumn tableName=ADMIN_EVENT_ENTITY; createTable tableName=CREDENTIAL_ATTRIBUTE; createTable tableName=FED_CREDENTIAL_ATTRIBUTE; modifyDataType columnName=VALUE, tableName=CREDENTIAL; addForeignKeyConstraint baseTableName=FED_CREDENTIAL_ATTRIBU...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.3.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.3.0.xml',	'2025-05-31 12:35:32.620363',	31,	'EXECUTED',	'8:ada8b4833b74a498f376d7136bc7d327',	'createTable tableName=FEDERATED_USER; addPrimaryKey constraintName=CONSTR_FEDERATED_USER, tableName=FEDERATED_USER; dropDefaultValue columnName=TOTP, tableName=USER_ENTITY; dropColumn columnName=TOTP, tableName=USER_ENTITY; addColumn tableName=IDE...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.4.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.4.0.xml',	'2025-05-31 12:35:32.626072',	32,	'EXECUTED',	'8:b9b73c8ea7299457f99fcbb825c263ba',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.5.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-31 12:35:32.630895',	33,	'EXECUTED',	'8:07724333e625ccfcfc5adc63d57314f3',	'customChange; modifyDataType columnName=USER_ID, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.5.0-unicode-oracle',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-31 12:35:32.632864',	34,	'MARK_RAN',	'8:8b6fd445958882efe55deb26fc541a7b',	'modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.5.0-unicode-other-dbs',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-31 12:35:32.651524',	35,	'EXECUTED',	'8:29b29cfebfd12600897680147277a9d7',	'modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.5.0-duplicate-email-support',	'slawomir@dabek.name',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-31 12:35:32.657179',	36,	'EXECUTED',	'8:73ad77ca8fd0410c7f9f15a471fa52bc',	'addColumn tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.5.0-unique-group-names',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-31 12:35:32.662279',	37,	'EXECUTED',	'8:64f27a6fdcad57f6f9153210f2ec1bdb',	'addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('2.5.1',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.5.1.xml',	'2025-05-31 12:35:32.666788',	38,	'EXECUTED',	'8:27180251182e6c31846c2ddab4bc5781',	'addColumn tableName=FED_USER_CONSENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.0.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-3.0.0.xml',	'2025-05-31 12:35:32.671782',	39,	'EXECUTED',	'8:d56f201bfcfa7a1413eb3e9bc02978f9',	'addColumn tableName=IDENTITY_PROVIDER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.2.0-fix',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-31 12:35:32.674019',	40,	'MARK_RAN',	'8:91f5522bf6afdc2077dfab57fbd3455c',	'addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.2.0-fix-with-keycloak-5416',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-31 12:35:32.676512',	41,	'MARK_RAN',	'8:0f01b554f256c22caeb7d8aee3a1cdc8',	'dropIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS; addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS; createIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.2.0-fix-offline-sessions',	'hmlnarik',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-31 12:35:32.681034',	42,	'EXECUTED',	'8:ab91cf9cee415867ade0e2df9651a947',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.2.0-fixed',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-31 12:35:32.7319',	43,	'EXECUTED',	'8:ceac9b1889e97d602caf373eadb0d4b7',	'addColumn tableName=REALM; dropPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_PK2, tableName=OFFLINE_CLIENT_SESSION; dropColumn columnName=CLIENT_SESSION_ID, tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_P...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.3.0',	'keycloak',	'META-INF/jpa-changelog-3.3.0.xml',	'2025-05-31 12:35:32.736131',	44,	'EXECUTED',	'8:84b986e628fe8f7fd8fd3c275c5259f2',	'addColumn tableName=USER_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part2-KEYCLOAK-6095',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-31 12:35:32.745509',	46,	'EXECUTED',	'8:70a2b4f1f4bd4dbf487114bdb1810e64',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part3-fixed',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-31 12:35:32.7475',	47,	'MARK_RAN',	'8:7be68b71d2f5b94b8df2e824f2860fa2',	'dropIndex indexName=IDX_RES_SERV_POL_RES_SERV, tableName=RESOURCE_SERVER_POLICY; dropIndex indexName=IDX_RES_SRV_RES_RES_SRV, tableName=RESOURCE_SERVER_RESOURCE; dropIndex indexName=IDX_RES_SRV_SCOPE_RES_SRV, tableName=RESOURCE_SERVER_SCOPE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part3-fixed-nodropindex',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-31 12:35:32.775025',	48,	'EXECUTED',	'8:bab7c631093c3861d6cf6144cd944982',	'addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_POLICY; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_RESOURCE; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, ...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authn-3.4.0.CR1-refresh-token-max-reuse',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-31 12:35:32.779461',	49,	'EXECUTED',	'8:fa809ac11877d74d76fe40869916daad',	'addColumn tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.4.0',	'keycloak',	'META-INF/jpa-changelog-3.4.0.xml',	'2025-05-31 12:35:32.799245',	50,	'EXECUTED',	'8:fac23540a40208f5f5e326f6ceb4d291',	'addPrimaryKey constraintName=CONSTRAINT_REALM_DEFAULT_ROLES, tableName=REALM_DEFAULT_ROLES; addPrimaryKey constraintName=CONSTRAINT_COMPOSITE_ROLE, tableName=COMPOSITE_ROLE; addPrimaryKey constraintName=CONSTR_REALM_DEFAULT_GROUPS, tableName=REALM...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.4.0-KEYCLOAK-5230',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-3.4.0.xml',	'2025-05-31 12:35:32.813812',	51,	'EXECUTED',	'8:2612d1b8a97e2b5588c346e817307593',	'createIndex indexName=IDX_FU_ATTRIBUTE, tableName=FED_USER_ATTRIBUTE; createIndex indexName=IDX_FU_CONSENT, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CONSENT_RU, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CREDENTIAL, t...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.4.1',	'psilva@redhat.com',	'META-INF/jpa-changelog-3.4.1.xml',	'2025-05-31 12:35:32.819852',	52,	'EXECUTED',	'8:9842f155c5db2206c88bcb5d1046e941',	'modifyDataType columnName=VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.4.2',	'keycloak',	'META-INF/jpa-changelog-3.4.2.xml',	'2025-05-31 12:35:32.824151',	53,	'EXECUTED',	'8:2e12e06e45498406db72d5b3da5bbc76',	'update tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('3.4.2-KEYCLOAK-5172',	'mkanis@redhat.com',	'META-INF/jpa-changelog-3.4.2.xml',	'2025-05-31 12:35:32.827119',	54,	'EXECUTED',	'8:33560e7c7989250c40da3abdabdc75a4',	'update tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.0.0-KEYCLOAK-6335',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-31 12:35:32.831756',	55,	'EXECUTED',	'8:87a8d8542046817a9107c7eb9cbad1cd',	'createTable tableName=CLIENT_AUTH_FLOW_BINDINGS; addPrimaryKey constraintName=C_CLI_FLOW_BIND, tableName=CLIENT_AUTH_FLOW_BINDINGS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.0.0-CLEANUP-UNUSED-TABLE',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-31 12:35:32.835958',	56,	'EXECUTED',	'8:3ea08490a70215ed0088c273d776311e',	'dropTable tableName=CLIENT_IDENTITY_PROV_MAPPING',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.0.0-KEYCLOAK-6228',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-31 12:35:32.850512',	57,	'EXECUTED',	'8:2d56697c8723d4592ab608ce14b6ed68',	'dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; dropNotNullConstraint columnName=CLIENT_ID, tableName=USER_CONSENT; addColumn tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHO...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.0.0-KEYCLOAK-5579-fixed',	'mposolda@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-31 12:35:32.920537',	58,	'EXECUTED',	'8:3e423e249f6068ea2bbe48bf907f9d86',	'dropForeignKeyConstraint baseTableName=CLIENT_TEMPLATE_ATTRIBUTES, constraintName=FK_CL_TEMPL_ATTR_TEMPL; renameTable newTableName=CLIENT_SCOPE_ATTRIBUTES, oldTableName=CLIENT_TEMPLATE_ATTRIBUTES; renameColumn newColumnName=SCOPE_ID, oldColumnName...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-4.0.0.CR1',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-4.0.0.CR1.xml',	'2025-05-31 12:35:32.94996',	59,	'EXECUTED',	'8:15cabee5e5df0ff099510a0fc03e4103',	'createTable tableName=RESOURCE_SERVER_PERM_TICKET; addPrimaryKey constraintName=CONSTRAINT_FAPMT, tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRHO213XCX4WNKOG82SSPMT...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-4.0.0.Beta3',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-4.0.0.Beta3.xml',	'2025-05-31 12:35:32.960307',	60,	'EXECUTED',	'8:4b80200af916ac54d2ffbfc47918ab0e',	'addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRPO2128CX4WNKOG82SSRFY, referencedTableName=RESOURCE_SERVER_POLICY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-4.2.0.Final',	'mhajas@redhat.com',	'META-INF/jpa-changelog-authz-4.2.0.Final.xml',	'2025-05-31 12:35:32.973118',	61,	'EXECUTED',	'8:66564cd5e168045d52252c5027485bbb',	'createTable tableName=RESOURCE_URIS; addForeignKeyConstraint baseTableName=RESOURCE_URIS, constraintName=FK_RESOURCE_SERVER_URIS, referencedTableName=RESOURCE_SERVER_RESOURCE; customChange; dropColumn columnName=URI, tableName=RESOURCE_SERVER_RESO...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-4.2.0.Final-KEYCLOAK-9944',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-authz-4.2.0.Final.xml',	'2025-05-31 12:35:32.979492',	62,	'EXECUTED',	'8:1c7064fafb030222be2bd16ccf690f6f',	'addPrimaryKey constraintName=CONSTRAINT_RESOUR_URIS_PK, tableName=RESOURCE_URIS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.2.0-KEYCLOAK-6313',	'wadahiro@gmail.com',	'META-INF/jpa-changelog-4.2.0.xml',	'2025-05-31 12:35:32.984486',	63,	'EXECUTED',	'8:2de18a0dce10cdda5c7e65c9b719b6e5',	'addColumn tableName=REQUIRED_ACTION_PROVIDER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.3.0-KEYCLOAK-7984',	'wadahiro@gmail.com',	'META-INF/jpa-changelog-4.3.0.xml',	'2025-05-31 12:35:32.98923',	64,	'EXECUTED',	'8:03e413dd182dcbd5c57e41c34d0ef682',	'update tableName=REQUIRED_ACTION_PROVIDER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.6.0-KEYCLOAK-7950',	'psilva@redhat.com',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-05-31 12:35:32.994421',	65,	'EXECUTED',	'8:d27b42bb2571c18fbe3fe4e4fb7582a7',	'update tableName=RESOURCE_SERVER_RESOURCE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.6.0-KEYCLOAK-8377',	'keycloak',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-05-31 12:35:33.007111',	66,	'EXECUTED',	'8:698baf84d9fd0027e9192717c2154fb8',	'createTable tableName=ROLE_ATTRIBUTE; addPrimaryKey constraintName=CONSTRAINT_ROLE_ATTRIBUTE_PK, tableName=ROLE_ATTRIBUTE; addForeignKeyConstraint baseTableName=ROLE_ATTRIBUTE, constraintName=FK_ROLE_ATTRIBUTE_ID, referencedTableName=KEYCLOAK_ROLE...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.6.0-KEYCLOAK-8555',	'gideonray@gmail.com',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-05-31 12:35:33.013334',	67,	'EXECUTED',	'8:ced8822edf0f75ef26eb51582f9a821a',	'createIndex indexName=IDX_COMPONENT_PROVIDER_TYPE, tableName=COMPONENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.7.0-KEYCLOAK-1267',	'sguilhen@redhat.com',	'META-INF/jpa-changelog-4.7.0.xml',	'2025-05-31 12:35:33.020876',	68,	'EXECUTED',	'8:f0abba004cf429e8afc43056df06487d',	'addColumn tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.7.0-KEYCLOAK-7275',	'keycloak',	'META-INF/jpa-changelog-4.7.0.xml',	'2025-05-31 12:35:33.035031',	69,	'EXECUTED',	'8:6662f8b0b611caa359fcf13bf63b4e24',	'renameColumn newColumnName=CREATED_ON, oldColumnName=LAST_SESSION_REFRESH, tableName=OFFLINE_USER_SESSION; addNotNullConstraint columnName=CREATED_ON, tableName=OFFLINE_USER_SESSION; addColumn tableName=OFFLINE_USER_SESSION; customChange; createIn...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('4.8.0-KEYCLOAK-8835',	'sguilhen@redhat.com',	'META-INF/jpa-changelog-4.8.0.xml',	'2025-05-31 12:35:33.044376',	70,	'EXECUTED',	'8:9e6b8009560f684250bdbdf97670d39e',	'addNotNullConstraint columnName=SSO_MAX_LIFESPAN_REMEMBER_ME, tableName=REALM; addNotNullConstraint columnName=SSO_IDLE_TIMEOUT_REMEMBER_ME, tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('authz-7.0.0-KEYCLOAK-10443',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-7.0.0.xml',	'2025-05-31 12:35:33.050233',	71,	'EXECUTED',	'8:4223f561f3b8dc655846562b57bb502e',	'addColumn tableName=RESOURCE_SERVER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('8.0.0-adding-credential-columns',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-31 12:35:33.059055',	72,	'EXECUTED',	'8:215a31c398b363ce383a2b301202f29e',	'addColumn tableName=CREDENTIAL; addColumn tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('8.0.0-updating-credential-data-not-oracle-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-31 12:35:33.065809',	73,	'EXECUTED',	'8:83f7a671792ca98b3cbd3a1a34862d3d',	'update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('8.0.0-updating-credential-data-oracle-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-31 12:35:33.069868',	74,	'MARK_RAN',	'8:f58ad148698cf30707a6efbdf8061aa7',	'update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('8.0.0-credential-cleanup-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-31 12:35:33.103691',	75,	'EXECUTED',	'8:79e4fd6c6442980e58d52ffc3ee7b19c',	'dropDefaultValue columnName=COUNTER, tableName=CREDENTIAL; dropDefaultValue columnName=DIGITS, tableName=CREDENTIAL; dropDefaultValue columnName=PERIOD, tableName=CREDENTIAL; dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; dropColumn ...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('8.0.0-resource-tag-support',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-31 12:35:33.110312',	76,	'EXECUTED',	'8:87af6a1e6d241ca4b15801d1f86a297d',	'addColumn tableName=MIGRATION_MODEL; createIndex indexName=IDX_UPDATE_TIME, tableName=MIGRATION_MODEL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.0-always-display-client',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-31 12:35:33.11607',	77,	'EXECUTED',	'8:b44f8d9b7b6ea455305a6d72a200ed15',	'addColumn tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.0-drop-constraints-for-column-increase',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-31 12:35:33.119826',	78,	'MARK_RAN',	'8:2d8ed5aaaeffd0cb004c046b4a903ac5',	'dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5PMT, tableName=RESOURCE_SERVER_PERM_TICKET; dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER_RESOURCE; dropPrimaryKey constraintName=CONSTRAINT_O...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.0-increase-column-size-federated-fk',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-31 12:35:33.144347',	79,	'EXECUTED',	'8:e290c01fcbc275326c511633f6e2acde',	'modifyDataType columnName=CLIENT_ID, tableName=FED_USER_CONSENT; modifyDataType columnName=CLIENT_REALM_CONSTRAINT, tableName=KEYCLOAK_ROLE; modifyDataType columnName=OWNER, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=CLIENT_ID, ta...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.0-recreate-constraints-after-column-increase',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-31 12:35:33.148049',	80,	'MARK_RAN',	'8:c9db8784c33cea210872ac2d805439f8',	'addNotNullConstraint columnName=CLIENT_ID, tableName=OFFLINE_CLIENT_SESSION; addNotNullConstraint columnName=OWNER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNullConstraint columnName=REQUESTER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNull...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.1-add-index-to-client.client_id',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-31 12:35:33.156936',	81,	'EXECUTED',	'8:95b676ce8fc546a1fcfb4c92fae4add5',	'createIndex indexName=IDX_CLIENT_ID, tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.1-KEYCLOAK-12579-drop-constraints',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-31 12:35:33.159593',	82,	'MARK_RAN',	'8:38a6b2a41f5651018b1aca93a41401e5',	'dropUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.1-KEYCLOAK-12579-add-not-null-constraint',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-31 12:35:33.163876',	83,	'EXECUTED',	'8:3fb99bcad86a0229783123ac52f7609c',	'addNotNullConstraint columnName=PARENT_GROUP, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.1-KEYCLOAK-12579-recreate-constraints',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-31 12:35:33.16576',	84,	'MARK_RAN',	'8:64f27a6fdcad57f6f9153210f2ec1bdb',	'addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('9.0.1-add-index-to-events',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-31 12:35:33.171993',	85,	'EXECUTED',	'8:ab4f863f39adafd4c862f7ec01890abc',	'createIndex indexName=IDX_EVENT_TIME, tableName=EVENT_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('map-remove-ri',	'keycloak',	'META-INF/jpa-changelog-11.0.0.xml',	'2025-05-31 12:35:33.180728',	86,	'EXECUTED',	'8:13c419a0eb336e91ee3a3bf8fda6e2a7',	'dropForeignKeyConstraint baseTableName=REALM, constraintName=FK_TRAF444KK6QRKMS7N56AIWQ5Y; dropForeignKeyConstraint baseTableName=KEYCLOAK_ROLE, constraintName=FK_KJHO5LE2C0RAL09FL8CM9WFW9',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('map-remove-ri',	'keycloak',	'META-INF/jpa-changelog-12.0.0.xml',	'2025-05-31 12:35:33.194004',	87,	'EXECUTED',	'8:e3fb1e698e0471487f51af1ed80fe3ac',	'dropForeignKeyConstraint baseTableName=REALM_DEFAULT_GROUPS, constraintName=FK_DEF_GROUPS_GROUP; dropForeignKeyConstraint baseTableName=REALM_DEFAULT_ROLES, constraintName=FK_H4WPD7W4HSOOLNI3H0SW7BTJE; dropForeignKeyConstraint baseTableName=CLIENT...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('12.1.0-add-realm-localization-table',	'keycloak',	'META-INF/jpa-changelog-12.0.0.xml',	'2025-05-31 12:35:33.201291',	88,	'EXECUTED',	'8:babadb686aab7b56562817e60bf0abd0',	'createTable tableName=REALM_LOCALIZATIONS; addPrimaryKey tableName=REALM_LOCALIZATIONS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('default-roles',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.208975',	89,	'EXECUTED',	'8:72d03345fda8e2f17093d08801947773',	'addColumn tableName=REALM; customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('default-roles-cleanup',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.215932',	90,	'EXECUTED',	'8:61c9233951bd96ffecd9ba75f7d978a4',	'dropTable tableName=REALM_DEFAULT_ROLES; dropTable tableName=CLIENT_DEFAULT_ROLES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('13.0.0-KEYCLOAK-16844',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.220002',	91,	'EXECUTED',	'8:ea82e6ad945cec250af6372767b25525',	'createIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('map-remove-ri-13.0.0',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.227512',	92,	'EXECUTED',	'8:d3f4a33f41d960ddacd7e2ef30d126b3',	'dropForeignKeyConstraint baseTableName=DEFAULT_CLIENT_SCOPE, constraintName=FK_R_DEF_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SCOPE_CLIENT, constraintName=FK_C_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SC...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('13.0.0-KEYCLOAK-17992-drop-constraints',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.229354',	93,	'MARK_RAN',	'8:1284a27fbd049d65831cb6fc07c8a783',	'dropPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CLSCOPE_CL, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CL_CLSCOPE, tableName=CLIENT_SCOPE_CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('13.0.0-increase-column-size-federated',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.23669',	94,	'EXECUTED',	'8:9d11b619db2ae27c25853b8a37cd0dea',	'modifyDataType columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; modifyDataType columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('13.0.0-KEYCLOAK-17992-recreate-constraints',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.238869',	95,	'MARK_RAN',	'8:3002bb3997451bb9e8bac5c5cd8d6327',	'addNotNullConstraint columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; addNotNullConstraint columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT; addPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; createIndex indexName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('json-string-accomodation-fixed',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-31 12:35:33.244212',	96,	'EXECUTED',	'8:dfbee0d6237a23ef4ccbb7a4e063c163',	'addColumn tableName=REALM_ATTRIBUTE; update tableName=REALM_ATTRIBUTE; dropColumn columnName=VALUE, tableName=REALM_ATTRIBUTE; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=REALM_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('14.0.0-KEYCLOAK-11019',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.249237',	97,	'EXECUTED',	'8:75f3e372df18d38c62734eebb986b960',	'createIndex indexName=IDX_OFFLINE_CSS_PRELOAD, tableName=OFFLINE_CLIENT_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USER, tableName=OFFLINE_USER_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('14.0.0-KEYCLOAK-18286',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.251156',	98,	'MARK_RAN',	'8:7fee73eddf84a6035691512c85637eef',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('14.0.0-KEYCLOAK-18286-revert',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.261332',	99,	'MARK_RAN',	'8:7a11134ab12820f999fbf3bb13c3adc8',	'dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('14.0.0-KEYCLOAK-18286-supported-dbs',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.265437',	100,	'EXECUTED',	'8:c0f6eaac1f3be773ffe54cb5b8482b70',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('14.0.0-KEYCLOAK-18286-unsupported-dbs',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.267482',	101,	'MARK_RAN',	'8:18186f0008b86e0f0f49b0c4d0e842ac',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('KEYCLOAK-17267-add-index-to-user-attributes',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.270735',	102,	'EXECUTED',	'8:09c2780bcb23b310a7019d217dc7b433',	'createIndex indexName=IDX_USER_ATTRIBUTE_NAME, tableName=USER_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('KEYCLOAK-18146-add-saml-art-binding-identifier',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-31 12:35:33.274822',	103,	'EXECUTED',	'8:276a44955eab693c970a42880197fff2',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('15.0.0-KEYCLOAK-18467',	'keycloak',	'META-INF/jpa-changelog-15.0.0.xml',	'2025-05-31 12:35:33.281155',	104,	'EXECUTED',	'8:ba8ee3b694d043f2bfc1a1079d0760d7',	'addColumn tableName=REALM_LOCALIZATIONS; update tableName=REALM_LOCALIZATIONS; dropColumn columnName=TEXTS, tableName=REALM_LOCALIZATIONS; renameColumn newColumnName=TEXTS, oldColumnName=TEXTS_NEW, tableName=REALM_LOCALIZATIONS; addNotNullConstrai...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('17.0.0-9562',	'keycloak',	'META-INF/jpa-changelog-17.0.0.xml',	'2025-05-31 12:35:33.284916',	105,	'EXECUTED',	'8:5e06b1d75f5d17685485e610c2851b17',	'createIndex indexName=IDX_USER_SERVICE_ACCOUNT, tableName=USER_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('18.0.0-10625-IDX_ADMIN_EVENT_TIME',	'keycloak',	'META-INF/jpa-changelog-18.0.0.xml',	'2025-05-31 12:35:33.288529',	106,	'EXECUTED',	'8:4b80546c1dc550ac552ee7b24a4ab7c0',	'createIndex indexName=IDX_ADMIN_EVENT_TIME, tableName=ADMIN_EVENT_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('19.0.0-10135',	'keycloak',	'META-INF/jpa-changelog-19.0.0.xml',	'2025-05-31 12:35:33.29395',	107,	'EXECUTED',	'8:af510cd1bb2ab6339c45372f3e491696',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('20.0.0-12964-supported-dbs',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-05-31 12:35:33.297823',	108,	'EXECUTED',	'8:05c99fc610845ef66ee812b7921af0ef',	'createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('20.0.0-12964-unsupported-dbs',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-05-31 12:35:33.299636',	109,	'MARK_RAN',	'8:314e803baf2f1ec315b3464e398b8247',	'createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730'),
        ('client-attributes-string-accomodation-fixed',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-05-31 12:35:33.304549',	110,	'EXECUTED',	'8:56e4677e7e12556f70b604c573840100',	'addColumn tableName=CLIENT_ATTRIBUTES; update tableName=CLIENT_ATTRIBUTES; dropColumn columnName=VALUE, tableName=CLIENT_ATTRIBUTES; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'8694931730');

        CREATE TABLE "public"."databasechangeloglock" (
            "id" integer NOT NULL,
            "locked" boolean NOT NULL,
            "lockgranted" timestamp,
            "lockedby" character varying(255),
            CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        INSERT INTO "databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
        (1,	'0',	NULL,	NULL),
        (1000,	'0',	NULL,	NULL),
        (1001,	'0',	NULL,	NULL);

        CREATE TABLE "public"."default_client_scope" (
            "realm_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            "default_scope" boolean DEFAULT false NOT NULL,
            CONSTRAINT "r_def_cli_scope_bind" PRIMARY KEY ("realm_id", "scope_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_defcls_realm ON public.default_client_scope USING btree (realm_id);

        CREATE INDEX idx_defcls_scope ON public.default_client_scope USING btree (scope_id);

        INSERT INTO "default_client_scope" ("realm_id", "scope_id", "default_scope") VALUES
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'b30619fb-e305-40a7-aaf2-6a8ba2b7b253',	'0'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'da4b14d8-fe98-4ee5-b8b1-bd0780aa500e',	'1'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e',	'1'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9',	'1'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'fac87716-08dc-46f5-b3af-06df9b8fb236',	'0'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'b76153b2-3529-4e91-a333-3df0681fc6aa',	'0'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'bdc49ea5-0368-49d0-aeed-d92b8b07a507',	'1'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'297e9618-ab35-42d0-9805-86c019d5c054',	'1'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'2f811ed3-9f92-4558-9e8d-ab92ac81595a',	'0'),
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0083e4d4-6524-4fca-a921-2e7b5a028141',	'1'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'37ec9784-252c-4358-acfb-b4405d80d20c',	'0'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'743a9fdf-febf-49b9-9cb7-d342219e25ac',	'1'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'4aff707c-2e3c-445e-8b93-e69fb34cf145',	'1'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd',	'1'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66',	'0'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b',	'0'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'514e7fdf-3df2-43a8-b850-02958b8bf7f9',	'1'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'e9ecf03e-974c-4e15-871f-d41b33f8c242',	'1'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'bc5d0051-0e15-46db-853f-1ab67e771cad',	'0'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810',	'1');

        CREATE TABLE "public"."event_entity" (
            "id" character varying(36) NOT NULL,
            "client_id" character varying(255),
            "details_json" character varying(2550),
            "error" character varying(255),
            "ip_address" character varying(255),
            "realm_id" character varying(255),
            "session_id" character varying(255),
            "event_time" bigint,
            "type" character varying(255),
            "user_id" character varying(255),
            CONSTRAINT "constraint_4" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_event_time ON public.event_entity USING btree (realm_id, event_time);


        CREATE TABLE "public"."fed_user_attribute" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            "value" character varying(2024),
            CONSTRAINT "constr_fed_user_attr_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fu_attribute ON public.fed_user_attribute USING btree (user_id, realm_id, name);


        CREATE TABLE "public"."fed_user_consent" (
            "id" character varying(36) NOT NULL,
            "client_id" character varying(255),
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            "created_date" bigint,
            "last_updated_date" bigint,
            "client_storage_provider" character varying(36),
            "external_client_id" character varying(255),
            CONSTRAINT "constr_fed_user_consent_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fu_consent_ru ON public.fed_user_consent USING btree (realm_id, user_id);

        CREATE INDEX idx_fu_cnsnt_ext ON public.fed_user_consent USING btree (user_id, client_storage_provider, external_client_id);

        CREATE INDEX idx_fu_consent ON public.fed_user_consent USING btree (user_id, client_id);


        CREATE TABLE "public"."fed_user_consent_cl_scope" (
            "user_consent_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_fgrntcsnt_clsc_pm" PRIMARY KEY ("user_consent_id", "scope_id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."fed_user_credential" (
            "id" character varying(36) NOT NULL,
            "salt" bytea,
            "type" character varying(255),
            "created_date" bigint,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            "user_label" character varying(255),
            "secret_data" text,
            "credential_data" text,
            "priority" integer,
            CONSTRAINT "constr_fed_user_cred_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fu_credential ON public.fed_user_credential USING btree (user_id, type);

        CREATE INDEX idx_fu_credential_ru ON public.fed_user_credential USING btree (realm_id, user_id);


        CREATE TABLE "public"."fed_user_group_membership" (
            "group_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_user_group" PRIMARY KEY ("group_id", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fu_group_membership ON public.fed_user_group_membership USING btree (user_id, group_id);

        CREATE INDEX idx_fu_group_membership_ru ON public.fed_user_group_membership USING btree (realm_id, user_id);


        CREATE TABLE "public"."fed_user_required_action" (
            "required_action" character varying(255) DEFAULT ' ' NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_required_action" PRIMARY KEY ("required_action", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fu_required_action ON public.fed_user_required_action USING btree (user_id, required_action);

        CREATE INDEX idx_fu_required_action_ru ON public.fed_user_required_action USING btree (realm_id, user_id);


        CREATE TABLE "public"."fed_user_role_mapping" (
            "role_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_user_role" PRIMARY KEY ("role_id", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fu_role_mapping ON public.fed_user_role_mapping USING btree (user_id, role_id);

        CREATE INDEX idx_fu_role_mapping_ru ON public.fed_user_role_mapping USING btree (realm_id, user_id);


        CREATE TABLE "public"."federated_identity" (
            "identity_provider" character varying(255) NOT NULL,
            "realm_id" character varying(36),
            "federated_user_id" character varying(255),
            "federated_username" character varying(255),
            "token" text,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_40" PRIMARY KEY ("identity_provider", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_fedidentity_user ON public.federated_identity USING btree (user_id);

        CREATE INDEX idx_fedidentity_feduser ON public.federated_identity USING btree (federated_user_id);


        CREATE TABLE "public"."federated_user" (
            "id" character varying(255) NOT NULL,
            "storage_provider_id" character varying(255),
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constr_federated_user" PRIMARY KEY ("id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."group_attribute" (
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_group_attribute_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_group_attr_group ON public.group_attribute USING btree (group_id);

        CREATE INDEX idx_group_att_by_name_value ON public.group_attribute USING btree (name, ((value)::character varying(250)));


        CREATE TABLE "public"."group_role_mapping" (
            "role_id" character varying(36) NOT NULL,
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_group_role" PRIMARY KEY ("role_id", "group_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_group_role_mapp_group ON public.group_role_mapping USING btree (group_id);


        CREATE TABLE "public"."identity_provider" (
            "internal_id" character varying(36) NOT NULL,
            "enabled" boolean DEFAULT false NOT NULL,
            "provider_alias" character varying(255),
            "provider_id" character varying(255),
            "store_token" boolean DEFAULT false NOT NULL,
            "authenticate_by_default" boolean DEFAULT false NOT NULL,
            "realm_id" character varying(36),
            "add_token_role" boolean DEFAULT true NOT NULL,
            "trust_email" boolean DEFAULT false NOT NULL,
            "first_broker_login_flow_id" character varying(36),
            "post_broker_login_flow_id" character varying(36),
            "provider_display_name" character varying(255),
            "link_only" boolean DEFAULT false NOT NULL,
            CONSTRAINT "constraint_2b" PRIMARY KEY ("internal_id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_2daelwnibji49avxsrtuf6xj33 ON public.identity_provider USING btree (provider_alias, realm_id);

        CREATE INDEX idx_ident_prov_realm ON public.identity_provider USING btree (realm_id);


        CREATE TABLE "public"."identity_provider_config" (
            "identity_provider_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_d" PRIMARY KEY ("identity_provider_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."identity_provider_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "idp_alias" character varying(255) NOT NULL,
            "idp_mapper_name" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_idpm" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_id_prov_mapp_realm ON public.identity_provider_mapper USING btree (realm_id);


        CREATE TABLE "public"."idp_mapper_config" (
            "idp_mapper_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_idpmconfig" PRIMARY KEY ("idp_mapper_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."keycloak_group" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "parent_group" character varying(36) NOT NULL,
            "realm_id" character varying(36),
            CONSTRAINT "constraint_group" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX sibling_names ON public.keycloak_group USING btree (realm_id, parent_group, name);


        CREATE TABLE "public"."keycloak_role" (
            "id" character varying(36) NOT NULL,
            "client_realm_constraint" character varying(255),
            "client_role" boolean DEFAULT false NOT NULL,
            "description" character varying(255),
            "name" character varying(255),
            "realm_id" character varying(255),
            "client" character varying(36),
            "realm" character varying(36),
            CONSTRAINT "constraint_a" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_keycloak_role_client ON public.keycloak_role USING btree (client);

        CREATE INDEX idx_keycloak_role_realm ON public.keycloak_role USING btree (realm);

        CREATE UNIQUE INDEX "UK_J3RWUVD56ONTGSUHOGM184WW2-2" ON public.keycloak_role USING btree (name, client_realm_constraint);

        INSERT INTO "keycloak_role" ("id", "client_realm_constraint", "client_role", "description", "name", "realm_id", "client", "realm") VALUES
        ('cdb7a7b3-1c27-4c46-8e22-3edc962f26ac',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'${role_default-roles}',	'default-roles-master',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	NULL),
        ('faa4c457-9ef8-4fe1-8493-cd33bc4d32df',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'${role_create-realm}',	'create-realm',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	NULL),
        ('4583d0fa-f3ae-4a1d-a398-448c46973081',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_create-client}',	'create-client',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('d7c448b2-e77d-4d19-99b3-cd588a2b0433',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_view-realm}',	'view-realm',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('0ec723fb-6eb8-4d4d-9c5c-55c23815fb4b',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_view-users}',	'view-users',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('283ac388-5489-46bb-9414-5520c6b3ae9f',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_view-clients}',	'view-clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('2622b3e0-2687-466c-846e-55edf881198d',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_view-events}',	'view-events',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('6455bfb0-eb5f-411c-a683-0bef6f4deafb',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('f03a10e5-0a08-4315-8336-08d8c8c2cc0d',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_view-authorization}',	'view-authorization',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('898618a7-1828-49c1-9e99-c47936553124',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_manage-realm}',	'manage-realm',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('98a178d7-0f4f-4033-a2ea-543ab6ed8dba',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_manage-users}',	'manage-users',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('0a9d13fc-822a-47ae-a4bb-4e76db4a5852',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_manage-clients}',	'manage-clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('599fc6dd-5019-41a6-aeb3-789bec369fba',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_manage-events}',	'manage-events',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('9aa128bb-12c6-45f2-a912-b677e23504e1',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('197c3c69-6e13-46ff-a455-e62fecbb3dad',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_manage-authorization}',	'manage-authorization',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('b7c4464f-b99f-4724-97e9-75a68573a2a3',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_query-users}',	'query-users',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('71ef8d5e-bfff-4844-91ad-cc2b4d4be905',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_query-clients}',	'query-clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('edf7ede0-b97e-4843-bcfa-6cf1014df306',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_query-realms}',	'query-realms',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'${role_admin}',	'admin',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	NULL),
        ('8abe6e10-8217-4a21-a739-123b808472be',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_query-groups}',	'query-groups',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('4b1866fb-3498-402a-b69e-0cc735e6998e',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_view-profile}',	'view-profile',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('fb622d47-2346-4def-b901-2d7bfe1a17d5',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_manage-account}',	'manage-account',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('a7d17c65-a97c-4612-ad1c-937ba99d8b8b',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_manage-account-links}',	'manage-account-links',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('05212d14-ee53-4b84-bdbd-bbf3fcc48b88',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_view-applications}',	'view-applications',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('36a7077b-d566-49e9-8be3-c2d1f1a84f8a',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_view-consent}',	'view-consent',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('ba1aa283-bbaa-4d57-a5e9-27de19933e8e',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_manage-consent}',	'manage-consent',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('7502f896-9adb-4580-a4d5-40d5adc142c5',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_view-groups}',	'view-groups',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('9ceb0d8f-5593-4f41-ac93-8240539a1a6c',	'739d472a-69a5-4544-8342-fb084f397457',	'1',	'${role_delete-account}',	'delete-account',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'739d472a-69a5-4544-8342-fb084f397457',	NULL),
        ('6d141520-afc1-4dca-9c33-6951c7764891',	'beb7b8c2-a1f1-40bc-beb0-b1810846b768',	'1',	'${role_read-token}',	'read-token',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'beb7b8c2-a1f1-40bc-beb0-b1810846b768',	NULL),
        ('7a83a181-baa6-4dff-bfe0-8224a753512b',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	'1',	'${role_impersonation}',	'impersonation',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	NULL),
        ('80bd0d9b-0994-4e68-84f6-fb1e5cd237e0',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'${role_offline-access}',	'offline_access',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	NULL),
        ('34e7d2a7-9396-47ed-b973-85892ca9395b',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'${role_uma_authorization}',	'uma_authorization',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	NULL,	NULL),
        ('877d5a18-80dd-4135-b1c6-d8a6bb5a911a',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'${role_default-roles}',	'default-roles-openk9',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL,	NULL),
        ('9d8bc7c1-8d77-4dab-9dea-1f3d4d26c703',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_create-client}',	'create-client',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('d1f39f14-202a-4864-951b-3889a127d9be',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_view-realm}',	'view-realm',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('7a911def-a37e-4900-b6d3-593f880c9432',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_view-users}',	'view-users',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('2f016158-342b-4ea8-b233-b721cfde5524',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_view-clients}',	'view-clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('f195f514-21a6-4c96-85e4-d8c405453e7c',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_view-events}',	'view-events',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('19474e5b-cf18-4536-b4d3-782b414a6309',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('d6675c66-7595-4e36-8c3d-02ba42d42178',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_view-authorization}',	'view-authorization',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('6a36fd3f-f548-4b52-9dc8-dc6372fc018a',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_manage-realm}',	'manage-realm',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('2d92a9eb-2d41-43c3-adb9-15defac17cc3',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_manage-users}',	'manage-users',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('96a74632-95d1-45e3-a1e2-e3c3a804c356',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_manage-clients}',	'manage-clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('a38bea4e-f485-4d8d-88f5-655813683249',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_manage-events}',	'manage-events',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('0222cc70-2bba-4a94-a2b3-1a7be3dc49cb',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('7e6f7a47-be90-4b4d-846e-2c0e9c989096',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_manage-authorization}',	'manage-authorization',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('b3e5d545-3013-4619-a624-c7f845b7ae90',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_query-users}',	'query-users',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('90e6c101-689f-44f4-968f-3f5d3a638e8f',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_query-clients}',	'query-clients',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('c8551b05-1af1-4fc8-ba2c-128cc78dfdbe',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_query-realms}',	'query-realms',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('4c966fc5-402a-4fcb-99da-9815043eca27',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_query-groups}',	'query-groups',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('213e29d4-6a42-476a-ad7f-0eea1eba0c79',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_realm-admin}',	'realm-admin',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('22988268-9152-4ee7-9dec-805bb97d3900',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_create-client}',	'create-client',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('a3df954d-2357-4cb9-8830-d8c236f07a1a',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_view-realm}',	'view-realm',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('ad026cfc-81b0-41be-86f3-4a2fa51facbd',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_view-users}',	'view-users',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('2661e145-04b2-43b8-857d-c245363c54d0',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_view-clients}',	'view-clients',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('a8696de1-93e0-4783-80cb-8476a661c6e7',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_view-events}',	'view-events',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('24ddcbe7-a67d-4725-ab15-cc64f12c0926',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('8abca6a3-0f88-49c6-8cbf-e7280424dbd1',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_view-authorization}',	'view-authorization',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('c7e53c75-aa4a-4289-bf6d-1b85217e363b',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_manage-realm}',	'manage-realm',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('060b45c6-c395-4bb7-8914-9808b5899467',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_manage-users}',	'manage-users',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('f3f0df33-d069-495a-8f1f-123d2fa58b9a',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_manage-clients}',	'manage-clients',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('1f23158e-cc62-4c35-a866-d5e62d11413a',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_manage-events}',	'manage-events',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('d6fb12a6-9976-4c3d-a888-d6f1378ccca2',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('30a6b5db-84f9-41d0-9ed8-71ff891f65a5',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_manage-authorization}',	'manage-authorization',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('b413c8eb-e81a-47b3-81d7-a90362351af2',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_query-users}',	'query-users',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('81fc68e5-0e95-4e2f-8c71-523c384922c6',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_query-clients}',	'query-clients',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('450f822a-0680-42df-832c-2f804c3e8f23',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_query-realms}',	'query-realms',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('6830d6c1-a9e1-4a94-b164-579bf0dbf7f2',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_query-groups}',	'query-groups',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('ededef8e-7f65-4c77-b45e-e016843ae605',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_view-profile}',	'view-profile',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('d7589b89-d577-44ea-adf3-20feea91a9d8',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_manage-account}',	'manage-account',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('728199b8-7a79-4a69-8068-3432099f4b1b',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_manage-account-links}',	'manage-account-links',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('171672fe-e2e9-4493-8da8-4de7b9f5445d',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_view-applications}',	'view-applications',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('2bfaaa56-6815-4c1d-9457-fd63fa335437',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_view-consent}',	'view-consent',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('7485926d-57db-4576-b66b-5ee92d528208',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_manage-consent}',	'manage-consent',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('377c1c08-958e-4429-aa22-2c2b06ad2ae8',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_view-groups}',	'view-groups',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('a287c375-dbc3-485a-a0b3-e806b7b3103b',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'1',	'${role_delete-account}',	'delete-account',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	NULL),
        ('6cbdac12-5bb9-417a-8e84-ef8e4909476d',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	'1',	'${role_impersonation}',	'impersonation',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	NULL),
        ('0761061a-b3cd-42a8-a19c-6d843b4bf750',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	'1',	'${role_impersonation}',	'impersonation',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'3521bf97-19cc-4f08-8ce9-7bc532eeac7b',	NULL),
        ('e202be57-963b-4cab-81a1-85bb2378638a',	'5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	'1',	'${role_read-token}',	'read-token',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'5a8c8bb5-5fb6-488e-bfc6-84de780c1ae2',	NULL),
        ('e4719bd9-57c0-4746-aa28-ee3bd2f281e9',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'${role_offline-access}',	'offline_access',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL,	NULL),
        ('76204f75-e059-415b-8d74-6fcfcd9b0f60',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'${role_uma_authorization}',	'uma_authorization',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL,	NULL),
        ('5a274a35-3e6d-466e-ae85-3c6b18e01a0a',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'',	'k9-admin',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL,	NULL),
        ('1dee5984-cdaf-4b36-8348-d095e14096c1',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'',	'k9-read',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL,	NULL),
        ('68f3acf4-12e8-4e59-8882-a63bd4666b1e',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'',	'k9-write',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	NULL,	NULL);

        CREATE TABLE "public"."migration_model" (
            "id" character varying(36) NOT NULL,
            "version" character varying(36),
            "update_time" bigint DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_migmod" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_update_time ON public.migration_model USING btree (update_time);

        INSERT INTO "migration_model" ("id", "version", "update_time") VALUES
        ('atbj1',	'20.0.5',	1748694933);

        CREATE TABLE "public"."offline_client_session" (
            "user_session_id" character varying(36) NOT NULL,
            "client_id" character varying(255) NOT NULL,
            "offline_flag" character varying(4) NOT NULL,
            "timestamp" integer,
            "data" text,
            "client_storage_provider" character varying(36) DEFAULT 'local' NOT NULL,
            "external_client_id" character varying(255) DEFAULT 'local' NOT NULL,
            CONSTRAINT "constraint_offl_cl_ses_pk3" PRIMARY KEY ("user_session_id", "client_id", "client_storage_provider", "external_client_id", "offline_flag")
        )
        WITH (oids = false);

        CREATE INDEX idx_us_sess_id_on_cl_sess ON public.offline_client_session USING btree (user_session_id);

        CREATE INDEX idx_offline_css_preload ON public.offline_client_session USING btree (client_id, offline_flag);


        CREATE TABLE "public"."offline_user_session" (
            "user_session_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "created_on" integer NOT NULL,
            "offline_flag" character varying(4) NOT NULL,
            "data" text,
            "last_session_refresh" integer DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_offl_us_ses_pk2" PRIMARY KEY ("user_session_id", "offline_flag")
        )
        WITH (oids = false);

        CREATE INDEX idx_offline_uss_createdon ON public.offline_user_session USING btree (created_on);

        CREATE INDEX idx_offline_uss_preload ON public.offline_user_session USING btree (offline_flag, created_on, user_session_id);

        CREATE INDEX idx_offline_uss_by_user ON public.offline_user_session USING btree (user_id, realm_id, offline_flag);

        CREATE INDEX idx_offline_uss_by_usersess ON public.offline_user_session USING btree (realm_id, offline_flag, user_session_id);


        CREATE TABLE "public"."policy_config" (
            "policy_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_dpc" PRIMARY KEY ("policy_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."protocol_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "protocol" character varying(255) NOT NULL,
            "protocol_mapper_name" character varying(255) NOT NULL,
            "client_id" character varying(36),
            "client_scope_id" character varying(36),
            CONSTRAINT "constraint_pcm" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_protocol_mapper_client ON public.protocol_mapper USING btree (client_id);

        CREATE INDEX idx_clscope_protmap ON public.protocol_mapper USING btree (client_scope_id);

        INSERT INTO "protocol_mapper" ("id", "name", "protocol", "protocol_mapper_name", "client_id", "client_scope_id") VALUES
        ('9f7929dd-975e-4d1a-b614-b01068d04eb7',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	NULL),
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	NULL),
        ('d3eae8e9-8f48-47bc-b64d-504f47b55c37',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'da4b14d8-fe98-4ee5-b8b1-bd0780aa500e'),
        ('e7466ef4-c1ab-477c-adbf-3abed1183077',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'family name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'given name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'username',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c638de2-db11-41c0-aeeb-e82f5a8ab84e'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'email',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'a84ab4da-7d25-4d87-ab62-60d910b2e4a9'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'fac87716-08dc-46f5-b3af-06df9b8fb236'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'b76153b2-3529-4e91-a333-3df0681fc6aa'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'b76153b2-3529-4e91-a333-3df0681fc6aa'),
        ('3925d55d-f32e-4f83-a46b-2f3a644e93b3',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'bdc49ea5-0368-49d0-aeed-d92b8b07a507'),
        ('aff0f39c-2124-4311-88d5-9f566b787268',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'bdc49ea5-0368-49d0-aeed-d92b8b07a507'),
        ('5ff98273-7de0-47a2-ab63-90b226a99e61',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'bdc49ea5-0368-49d0-aeed-d92b8b07a507'),
        ('6abf00dd-1a8c-4cbb-b441-8ef23df140fb',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'297e9618-ab35-42d0-9805-86c019d5c054'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'upn',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'2f811ed3-9f92-4558-9e8d-ab92ac81595a'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'2f811ed3-9f92-4558-9e8d-ab92ac81595a'),
        ('0513a485-c814-48aa-9bd2-968c69e7f9d6',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'0083e4d4-6524-4fca-a921-2e7b5a028141'),
        ('49ba4975-4253-4e84-84df-a1585b35b0f8',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	NULL),
        ('b1703073-8dfa-4724-853a-2b72e854b83b',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'743a9fdf-febf-49b9-9cb7-d342219e25ac'),
        ('aa7ed1f7-dcc6-4e53-b1e9-35f42374dc60',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'family name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'given name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'username',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4aff707c-2e3c-445e-8b93-e69fb34cf145'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'email',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'2657f8ce-a1ba-4bd8-bc6a-548d502d28dd'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'eddc8dcc-b4fd-4d66-8e0b-3cd0b27f4f66'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'05f5f190-c5fc-4951-a3b9-ee2a611c4e3b'),
        ('beba7e82-a620-4528-b131-8dfce814f2f6',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'514e7fdf-3df2-43a8-b850-02958b8bf7f9'),
        ('ad9347ba-ba11-4276-bb29-0c18a17fb43a',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'514e7fdf-3df2-43a8-b850-02958b8bf7f9'),
        ('f3600098-24ee-4a9d-ab86-13eee107df81',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'514e7fdf-3df2-43a8-b850-02958b8bf7f9'),
        ('8754f785-2068-4710-8af4-ab8d3bc59c3d',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'e9ecf03e-974c-4e15-871f-d41b33f8c242'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'upn',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'bc5d0051-0e15-46db-853f-1ab67e771cad'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'bc5d0051-0e15-46db-853f-1ab67e771cad'),
        ('9b10acc8-605c-4b2b-974b-581dc23be47e',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'd41fd839-1735-4fe2-8a7f-d6fbdda4b810'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	NULL);

        CREATE TABLE "public"."protocol_mapper_config" (
            "protocol_mapper_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_pmconfig" PRIMARY KEY ("protocol_mapper_id", "name")
        )
        WITH (oids = false);

        INSERT INTO "protocol_mapper_config" ("protocol_mapper_id", "value", "name") VALUES
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'true',	'userinfo.token.claim'),
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'locale',	'user.attribute'),
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'true',	'id.token.claim'),
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'true',	'access.token.claim'),
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'locale',	'claim.name'),
        ('2695b725-71bd-4742-8d43-68b2625a4111',	'String',	'jsonType.label'),
        ('d3eae8e9-8f48-47bc-b64d-504f47b55c37',	'false',	'single'),
        ('d3eae8e9-8f48-47bc-b64d-504f47b55c37',	'Basic',	'attribute.nameformat'),
        ('d3eae8e9-8f48-47bc-b64d-504f47b55c37',	'Role',	'attribute.name'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'true',	'userinfo.token.claim'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'nickname',	'user.attribute'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'true',	'id.token.claim'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'true',	'access.token.claim'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'nickname',	'claim.name'),
        ('039bf958-fa6d-4891-ba5c-07c99a873f6f',	'String',	'jsonType.label'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'true',	'userinfo.token.claim'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'website',	'user.attribute'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'true',	'id.token.claim'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'true',	'access.token.claim'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'website',	'claim.name'),
        ('1dfff390-24c2-4e72-adc7-37feb682132b',	'String',	'jsonType.label'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'true',	'userinfo.token.claim'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'locale',	'user.attribute'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'true',	'id.token.claim'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'true',	'access.token.claim'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'locale',	'claim.name'),
        ('35553431-23e4-444e-bc9f-6038f4d00ca9',	'String',	'jsonType.label'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'true',	'userinfo.token.claim'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'updatedAt',	'user.attribute'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'true',	'id.token.claim'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'true',	'access.token.claim'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'updated_at',	'claim.name'),
        ('3a3d55ef-4180-4b5c-98f2-81b7538bb577',	'long',	'jsonType.label'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'true',	'userinfo.token.claim'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'zoneinfo',	'user.attribute'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'true',	'id.token.claim'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'true',	'access.token.claim'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'zoneinfo',	'claim.name'),
        ('44743fb4-3fd1-44f1-bff4-0a8dabc61df3',	'String',	'jsonType.label'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'true',	'userinfo.token.claim'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'firstName',	'user.attribute'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'true',	'id.token.claim'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'true',	'access.token.claim'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'given_name',	'claim.name'),
        ('6cadc81d-0d4c-41b2-a29c-169cc9e146b0',	'String',	'jsonType.label'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'true',	'userinfo.token.claim'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'gender',	'user.attribute'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'true',	'id.token.claim'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'true',	'access.token.claim'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'gender',	'claim.name'),
        ('7291cdbd-e0cf-49dc-a683-d75afd0d10ad',	'String',	'jsonType.label'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'true',	'userinfo.token.claim'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'picture',	'user.attribute'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'true',	'id.token.claim'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'true',	'access.token.claim'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'picture',	'claim.name'),
        ('7f82b9ac-6ccb-4059-9da6-94f17d201741',	'String',	'jsonType.label'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'true',	'userinfo.token.claim'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'username',	'user.attribute'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'true',	'id.token.claim'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'true',	'access.token.claim'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'preferred_username',	'claim.name'),
        ('89e16a90-9d7c-482d-bb16-57a9210cda76',	'String',	'jsonType.label'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'true',	'userinfo.token.claim'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'lastName',	'user.attribute'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'true',	'id.token.claim'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'true',	'access.token.claim'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'family_name',	'claim.name'),
        ('9643211e-cf1f-48eb-b24d-55ee08dc61e8',	'String',	'jsonType.label'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'true',	'userinfo.token.claim'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'birthdate',	'user.attribute'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'true',	'id.token.claim'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'true',	'access.token.claim'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'birthdate',	'claim.name'),
        ('99c8332c-97c9-454e-82ef-b11cf04d4fb5',	'String',	'jsonType.label'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'true',	'userinfo.token.claim'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'profile',	'user.attribute'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'true',	'id.token.claim'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'true',	'access.token.claim'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'profile',	'claim.name'),
        ('9be0a4d3-072b-430a-be83-1046fe3f1165',	'String',	'jsonType.label'),
        ('e7466ef4-c1ab-477c-adbf-3abed1183077',	'true',	'userinfo.token.claim'),
        ('e7466ef4-c1ab-477c-adbf-3abed1183077',	'true',	'id.token.claim'),
        ('e7466ef4-c1ab-477c-adbf-3abed1183077',	'true',	'access.token.claim'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'true',	'userinfo.token.claim'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'middleName',	'user.attribute'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'true',	'id.token.claim'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'true',	'access.token.claim'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'middle_name',	'claim.name'),
        ('ff8a41f8-dd92-4b7c-92f0-59f1a2e1f3dc',	'String',	'jsonType.label'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'true',	'userinfo.token.claim'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'email',	'user.attribute'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'true',	'id.token.claim'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'true',	'access.token.claim'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'email',	'claim.name'),
        ('071da55e-c8e0-4496-a3b3-c816fcf25ab6',	'String',	'jsonType.label'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'true',	'userinfo.token.claim'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'emailVerified',	'user.attribute'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'true',	'id.token.claim'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'true',	'access.token.claim'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'email_verified',	'claim.name'),
        ('83f841fd-7397-4fef-9f09-1e8a95d2434e',	'boolean',	'jsonType.label'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'formatted',	'user.attribute.formatted'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'country',	'user.attribute.country'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'postal_code',	'user.attribute.postal_code'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'true',	'userinfo.token.claim'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'street',	'user.attribute.street'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'true',	'id.token.claim'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'region',	'user.attribute.region'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'true',	'access.token.claim'),
        ('2c26ca10-fb4d-499c-bb6d-687089ab273a',	'locality',	'user.attribute.locality'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'true',	'userinfo.token.claim'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'phoneNumber',	'user.attribute'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'true',	'id.token.claim'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'true',	'access.token.claim'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'phone_number',	'claim.name'),
        ('88b02e5f-1409-45f3-b44b-322a9d96fc5c',	'String',	'jsonType.label'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'true',	'userinfo.token.claim'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'phoneNumberVerified',	'user.attribute'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'true',	'id.token.claim'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'true',	'access.token.claim'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'phone_number_verified',	'claim.name'),
        ('96866915-dcbc-4905-bb7b-2e7bbf0b22a4',	'boolean',	'jsonType.label'),
        ('3925d55d-f32e-4f83-a46b-2f3a644e93b3',	'true',	'multivalued'),
        ('3925d55d-f32e-4f83-a46b-2f3a644e93b3',	'foo',	'user.attribute'),
        ('3925d55d-f32e-4f83-a46b-2f3a644e93b3',	'true',	'access.token.claim'),
        ('3925d55d-f32e-4f83-a46b-2f3a644e93b3',	'realm_access.roles',	'claim.name'),
        ('3925d55d-f32e-4f83-a46b-2f3a644e93b3',	'String',	'jsonType.label'),
        ('aff0f39c-2124-4311-88d5-9f566b787268',	'true',	'multivalued'),
        ('aff0f39c-2124-4311-88d5-9f566b787268',	'foo',	'user.attribute'),
        ('aff0f39c-2124-4311-88d5-9f566b787268',	'true',	'access.token.claim'),
        ('aff0f39c-2124-4311-88d5-9f566b787268',	'resource_access.${client_id}.roles',	'claim.name'),
        ('aff0f39c-2124-4311-88d5-9f566b787268',	'String',	'jsonType.label'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'true',	'userinfo.token.claim'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'username',	'user.attribute'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'true',	'id.token.claim'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'true',	'access.token.claim'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'upn',	'claim.name'),
        ('a44829e6-2ac4-4054-b74e-16b3ed6fcd2c',	'String',	'jsonType.label'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'true',	'multivalued'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'foo',	'user.attribute'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'true',	'id.token.claim'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'true',	'access.token.claim'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'groups',	'claim.name'),
        ('cf90d78b-4349-4ecc-a29c-d3565206bfb1',	'String',	'jsonType.label'),
        ('0513a485-c814-48aa-9bd2-968c69e7f9d6',	'true',	'id.token.claim'),
        ('0513a485-c814-48aa-9bd2-968c69e7f9d6',	'true',	'access.token.claim'),
        ('b1703073-8dfa-4724-853a-2b72e854b83b',	'false',	'single'),
        ('b1703073-8dfa-4724-853a-2b72e854b83b',	'Basic',	'attribute.nameformat'),
        ('b1703073-8dfa-4724-853a-2b72e854b83b',	'Role',	'attribute.name'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'true',	'userinfo.token.claim'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'middleName',	'user.attribute'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'true',	'id.token.claim'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'true',	'access.token.claim'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'middle_name',	'claim.name'),
        ('045b355b-a4a5-4aed-af81-b780ea889864',	'String',	'jsonType.label'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'true',	'userinfo.token.claim'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'username',	'user.attribute'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'true',	'id.token.claim'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'true',	'access.token.claim'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'preferred_username',	'claim.name'),
        ('0652eca8-25ad-44f2-9b33-cc6026de6a9f',	'String',	'jsonType.label'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'true',	'userinfo.token.claim'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'website',	'user.attribute'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'true',	'id.token.claim'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'true',	'access.token.claim'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'website',	'claim.name'),
        ('11e90832-29a3-40c1-b81a-a87484bea353',	'String',	'jsonType.label'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'true',	'userinfo.token.claim'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'gender',	'user.attribute'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'true',	'id.token.claim'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'true',	'access.token.claim'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'gender',	'claim.name'),
        ('4a0e5789-846b-4dda-885a-49925953d9e7',	'String',	'jsonType.label'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'true',	'userinfo.token.claim'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'firstName',	'user.attribute'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'true',	'id.token.claim'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'true',	'access.token.claim'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'given_name',	'claim.name'),
        ('4c9ca342-25d1-48b3-a4e4-67a1ac62aa74',	'String',	'jsonType.label'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'true',	'userinfo.token.claim'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'updatedAt',	'user.attribute'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'true',	'id.token.claim'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'true',	'access.token.claim'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'updated_at',	'claim.name'),
        ('54ebbba2-de1c-4190-a894-aa71a7dd5d87',	'long',	'jsonType.label'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'true',	'userinfo.token.claim'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'picture',	'user.attribute'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'true',	'id.token.claim'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'true',	'access.token.claim'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'picture',	'claim.name'),
        ('55cfd021-7fe4-4f40-9946-e4f21efd58d2',	'String',	'jsonType.label'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'true',	'userinfo.token.claim'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'birthdate',	'user.attribute'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'true',	'id.token.claim'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'true',	'access.token.claim'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'birthdate',	'claim.name'),
        ('57a53415-e504-433f-bcc6-0db9716db379',	'String',	'jsonType.label'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'true',	'userinfo.token.claim'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'zoneinfo',	'user.attribute'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'true',	'id.token.claim'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'true',	'access.token.claim'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'zoneinfo',	'claim.name'),
        ('71dc0043-24c6-4276-8232-bce44d822afe',	'String',	'jsonType.label'),
        ('aa7ed1f7-dcc6-4e53-b1e9-35f42374dc60',	'true',	'userinfo.token.claim'),
        ('aa7ed1f7-dcc6-4e53-b1e9-35f42374dc60',	'true',	'id.token.claim'),
        ('aa7ed1f7-dcc6-4e53-b1e9-35f42374dc60',	'true',	'access.token.claim'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'true',	'userinfo.token.claim'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'nickname',	'user.attribute'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'true',	'id.token.claim'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'true',	'access.token.claim'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'nickname',	'claim.name'),
        ('ac34c0c3-51f1-45b9-b20e-77874e41dc19',	'String',	'jsonType.label'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'true',	'userinfo.token.claim'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'locale',	'user.attribute'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'true',	'id.token.claim'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'true',	'access.token.claim'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'locale',	'claim.name'),
        ('b2ea6242-6b31-4abe-80c3-c0b63acc3e41',	'String',	'jsonType.label'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'true',	'userinfo.token.claim'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'lastName',	'user.attribute'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'true',	'id.token.claim'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'true',	'access.token.claim'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'family_name',	'claim.name'),
        ('c5fdec57-96cd-4e2c-84fa-29ba72166233',	'String',	'jsonType.label'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'true',	'userinfo.token.claim'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'profile',	'user.attribute'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'true',	'id.token.claim'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'true',	'access.token.claim'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'profile',	'claim.name'),
        ('f376f21c-b1ee-444e-9b3e-135b9a2e90b9',	'String',	'jsonType.label'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'true',	'userinfo.token.claim'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'email',	'user.attribute'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'true',	'id.token.claim'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'true',	'access.token.claim'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'email',	'claim.name'),
        ('73165cdd-4cee-48fb-9be4-8db2cf777e3d',	'String',	'jsonType.label'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'true',	'userinfo.token.claim'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'emailVerified',	'user.attribute'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'true',	'id.token.claim'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'true',	'access.token.claim'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'email_verified',	'claim.name'),
        ('9a5662dd-bbca-4811-8c59-1d353a4d42e6',	'boolean',	'jsonType.label'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'formatted',	'user.attribute.formatted'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'country',	'user.attribute.country'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'postal_code',	'user.attribute.postal_code'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'true',	'userinfo.token.claim'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'street',	'user.attribute.street'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'true',	'id.token.claim'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'region',	'user.attribute.region'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'true',	'access.token.claim'),
        ('0cda029d-dad2-4bb6-b44e-5d53677e84e1',	'locality',	'user.attribute.locality'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'true',	'userinfo.token.claim'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'phoneNumber',	'user.attribute'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'true',	'id.token.claim'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'true',	'access.token.claim'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'phone_number',	'claim.name'),
        ('0387114d-4f57-47d4-9daf-ca075c1c0112',	'String',	'jsonType.label'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'true',	'userinfo.token.claim'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'phoneNumberVerified',	'user.attribute'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'true',	'id.token.claim'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'true',	'access.token.claim'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'phone_number_verified',	'claim.name'),
        ('0c11f15f-c070-467b-8d52-a4beced25e7b',	'boolean',	'jsonType.label'),
        ('ad9347ba-ba11-4276-bb29-0c18a17fb43a',	'true',	'multivalued'),
        ('ad9347ba-ba11-4276-bb29-0c18a17fb43a',	'foo',	'user.attribute'),
        ('ad9347ba-ba11-4276-bb29-0c18a17fb43a',	'true',	'access.token.claim'),
        ('ad9347ba-ba11-4276-bb29-0c18a17fb43a',	'resource_access.${client_id}.roles',	'claim.name'),
        ('ad9347ba-ba11-4276-bb29-0c18a17fb43a',	'String',	'jsonType.label'),
        ('beba7e82-a620-4528-b131-8dfce814f2f6',	'true',	'multivalued'),
        ('beba7e82-a620-4528-b131-8dfce814f2f6',	'foo',	'user.attribute'),
        ('beba7e82-a620-4528-b131-8dfce814f2f6',	'true',	'access.token.claim'),
        ('beba7e82-a620-4528-b131-8dfce814f2f6',	'realm_access.roles',	'claim.name'),
        ('beba7e82-a620-4528-b131-8dfce814f2f6',	'String',	'jsonType.label'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'true',	'userinfo.token.claim'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'username',	'user.attribute'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'true',	'id.token.claim'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'true',	'access.token.claim'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'upn',	'claim.name'),
        ('b003d10f-4723-4c1d-98ba-040c93a623b0',	'String',	'jsonType.label'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'true',	'multivalued'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'foo',	'user.attribute'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'true',	'id.token.claim'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'true',	'access.token.claim'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'groups',	'claim.name'),
        ('defa7747-34fd-40fc-af1a-3e82e17838c9',	'String',	'jsonType.label'),
        ('9b10acc8-605c-4b2b-974b-581dc23be47e',	'true',	'id.token.claim'),
        ('9b10acc8-605c-4b2b-974b-581dc23be47e',	'true',	'access.token.claim'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'true',	'userinfo.token.claim'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'locale',	'user.attribute'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'true',	'id.token.claim'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'true',	'access.token.claim'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'locale',	'claim.name'),
        ('30684318-6f25-4e63-b15f-295bdb231d21',	'String',	'jsonType.label');

        CREATE TABLE "public"."realm" (
            "id" character varying(36) NOT NULL,
            "access_code_lifespan" integer,
            "user_action_lifespan" integer,
            "access_token_lifespan" integer,
            "account_theme" character varying(255),
            "admin_theme" character varying(255),
            "email_theme" character varying(255),
            "enabled" boolean DEFAULT false NOT NULL,
            "events_enabled" boolean DEFAULT false NOT NULL,
            "events_expiration" bigint,
            "login_theme" character varying(255),
            "name" character varying(255),
            "not_before" integer,
            "password_policy" character varying(2550),
            "registration_allowed" boolean DEFAULT false NOT NULL,
            "remember_me" boolean DEFAULT false NOT NULL,
            "reset_password_allowed" boolean DEFAULT false NOT NULL,
            "social" boolean DEFAULT false NOT NULL,
            "ssl_required" character varying(255),
            "sso_idle_timeout" integer,
            "sso_max_lifespan" integer,
            "update_profile_on_soc_login" boolean DEFAULT false NOT NULL,
            "verify_email" boolean DEFAULT false NOT NULL,
            "master_admin_client" character varying(36),
            "login_lifespan" integer,
            "internationalization_enabled" boolean DEFAULT false NOT NULL,
            "default_locale" character varying(255),
            "reg_email_as_username" boolean DEFAULT false NOT NULL,
            "admin_events_enabled" boolean DEFAULT false NOT NULL,
            "admin_events_details_enabled" boolean DEFAULT false NOT NULL,
            "edit_username_allowed" boolean DEFAULT false NOT NULL,
            "otp_policy_counter" integer DEFAULT '0',
            "otp_policy_window" integer DEFAULT '1',
            "otp_policy_period" integer DEFAULT '30',
            "otp_policy_digits" integer DEFAULT '6',
            "otp_policy_alg" character varying(36) DEFAULT 'HmacSHA1',
            "otp_policy_type" character varying(36) DEFAULT 'totp',
            "browser_flow" character varying(36),
            "registration_flow" character varying(36),
            "direct_grant_flow" character varying(36),
            "reset_credentials_flow" character varying(36),
            "client_auth_flow" character varying(36),
            "offline_session_idle_timeout" integer DEFAULT '0',
            "revoke_refresh_token" boolean DEFAULT false NOT NULL,
            "access_token_life_implicit" integer DEFAULT '0',
            "login_with_email_allowed" boolean DEFAULT true NOT NULL,
            "duplicate_emails_allowed" boolean DEFAULT false NOT NULL,
            "docker_auth_flow" character varying(36),
            "refresh_token_max_reuse" integer DEFAULT '0',
            "allow_user_managed_access" boolean DEFAULT false NOT NULL,
            "sso_max_lifespan_remember_me" integer DEFAULT '0' NOT NULL,
            "sso_idle_timeout_remember_me" integer DEFAULT '0' NOT NULL,
            "default_role" character varying(255),
            CONSTRAINT "constraint_4a" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_orvsdmla56612eaefiq6wl5oi ON public.realm USING btree (name);

        CREATE INDEX idx_realm_master_adm_cli ON public.realm USING btree (master_admin_client);

        INSERT INTO "realm" ("id", "access_code_lifespan", "user_action_lifespan", "access_token_lifespan", "account_theme", "admin_theme", "email_theme", "enabled", "events_enabled", "events_expiration", "login_theme", "name", "not_before", "password_policy", "registration_allowed", "remember_me", "reset_password_allowed", "social", "ssl_required", "sso_idle_timeout", "sso_max_lifespan", "update_profile_on_soc_login", "verify_email", "master_admin_client", "login_lifespan", "internationalization_enabled", "default_locale", "reg_email_as_username", "admin_events_enabled", "admin_events_details_enabled", "edit_username_allowed", "otp_policy_counter", "otp_policy_window", "otp_policy_period", "otp_policy_digits", "otp_policy_alg", "otp_policy_type", "browser_flow", "registration_flow", "direct_grant_flow", "reset_credentials_flow", "client_auth_flow", "offline_session_idle_timeout", "revoke_refresh_token", "access_token_life_implicit", "login_with_email_allowed", "duplicate_emails_allowed", "docker_auth_flow", "refresh_token_max_reuse", "allow_user_managed_access", "sso_max_lifespan_remember_me", "sso_idle_timeout_remember_me", "default_role") VALUES
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	60,	300,	60,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'master',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'd2594db5-c5f6-4276-9ecd-ff737f9c2957',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'7d0f2abd-9d70-47ef-80f5-9e169d3c70b8',	'fc88446a-6029-4148-85d6-1da3a01c0210',	'4f083f99-a46d-4606-a4b4-8630ea1a8e7c',	'276d4d54-a9dd-4cdf-86e9-b015293acdc2',	'610dcb35-21dc-45da-a1c9-4f382bda66fa',	2592000,	'0',	900,	'1',	'0',	'748c9c80-b84c-46dc-a967-0de8b6823df2',	0,	'0',	0,	0,	'cdb7a7b3-1c27-4c46-8e22-3edc962f26ac'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	60,	300,	300,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'openk9',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'1a486121-4601-4a55-87e7-3e28d5462c7d',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'68092db5-6964-4265-8a78-a22759ef4886',	'f8881c96-f327-4f85-85a3-d160b09b33d4',	'fcfbf8ed-8109-4a5c-a162-d712712b16ca',	'05fd3eeb-b4ad-48ac-bb43-fd253e1f40b8',	'66b2b973-53e0-4329-b65b-09a6fec61bd7',	2592000,	'0',	900,	'1',	'0',	'c2086ade-478e-45e6-aeda-cc19a866f725',	0,	'0',	0,	0,	'877d5a18-80dd-4135-b1c6-d8a6bb5a911a');

        CREATE TABLE "public"."realm_attribute" (
            "name" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_9" PRIMARY KEY ("name", "realm_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_realm_attr_realm ON public.realm_attribute USING btree (realm_id);

        INSERT INTO "realm_attribute" ("name", "realm_id", "value") VALUES
        ('_browser_header.contentSecurityPolicyReportOnly',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	''),
        ('_browser_header.xContentTypeOptions',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'nosniff'),
        ('_browser_header.xRobotsTag',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'none'),
        ('_browser_header.xFrameOptions',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'false'),
        ('permanentLockout',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'false'),
        ('maxFailureWaitSeconds',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'900'),
        ('minimumQuickLoginWaitSeconds',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'60'),
        ('waitIncrementSeconds',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'60'),
        ('quickLoginCheckMilliSeconds',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1000'),
        ('maxDeltaTimeSeconds',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'43200'),
        ('failureFactor',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'30'),
        ('realmReusableOtpCode',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'false'),
        ('displayName',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'Keycloak'),
        ('displayNameHtml',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'<div class="kc-logo-text"><span>Keycloak</span></div>'),
        ('defaultSignatureAlgorithm',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'false'),
        ('offlineSessionMaxLifespan',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'5184000'),
        ('_browser_header.contentSecurityPolicyReportOnly',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	''),
        ('_browser_header.xContentTypeOptions',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'nosniff'),
        ('_browser_header.xRobotsTag',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'none'),
        ('_browser_header.xFrameOptions',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'false'),
        ('permanentLockout',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'false'),
        ('maxFailureWaitSeconds',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'900'),
        ('minimumQuickLoginWaitSeconds',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'60'),
        ('waitIncrementSeconds',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'60'),
        ('quickLoginCheckMilliSeconds',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1000'),
        ('maxDeltaTimeSeconds',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'43200'),
        ('failureFactor',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'30'),
        ('realmReusableOtpCode',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'false'),
        ('defaultSignatureAlgorithm',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'false'),
        ('offlineSessionMaxLifespan',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'5184000'),
        ('actionTokenGeneratedByAdminLifespan',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'43200'),
        ('actionTokenGeneratedByUserLifespan',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'300'),
        ('oauth2DeviceCodeLifespan',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'600'),
        ('oauth2DevicePollingInterval',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'5'),
        ('webAuthnPolicyRpEntityName',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithms',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ES256'),
        ('webAuthnPolicyRpId',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	''),
        ('webAuthnPolicyAttestationConveyancePreference',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachment',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyRequireResidentKey',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirement',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyCreateTimeout',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegister',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'false'),
        ('webAuthnPolicyRpEntityNamePasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithmsPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'ES256'),
        ('webAuthnPolicyRpIdPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	''),
        ('webAuthnPolicyAttestationConveyancePreferencePasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachmentPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyRequireResidentKeyPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirementPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'not specified'),
        ('webAuthnPolicyCreateTimeoutPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'false'),
        ('cibaBackchannelTokenDeliveryMode',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'poll'),
        ('cibaExpiresIn',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'120'),
        ('cibaInterval',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'5'),
        ('cibaAuthRequestedUserHint',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'login_hint'),
        ('parRequestUriLifespan',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'60');

        CREATE TABLE "public"."realm_default_groups" (
            "realm_id" character varying(36) NOT NULL,
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constr_realm_default_groups" PRIMARY KEY ("realm_id", "group_id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX con_group_id_def_groups ON public.realm_default_groups USING btree (group_id);

        CREATE INDEX idx_realm_def_grp_realm ON public.realm_default_groups USING btree (realm_id);


        CREATE TABLE "public"."realm_enabled_event_types" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_enabl_event_types" PRIMARY KEY ("realm_id", "value")
        )
        WITH (oids = false);

        CREATE INDEX idx_realm_evt_types_realm ON public.realm_enabled_event_types USING btree (realm_id);


        CREATE TABLE "public"."realm_events_listeners" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_events_listeners" PRIMARY KEY ("realm_id", "value")
        )
        WITH (oids = false);

        CREATE INDEX idx_realm_evt_list_realm ON public.realm_events_listeners USING btree (realm_id);

        INSERT INTO "realm_events_listeners" ("realm_id", "value") VALUES
        ('add3ae74-abd2-4e73-96ea-a80026fa73c5',	'jboss-logging'),
        ('cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'jboss-logging');

        CREATE TABLE "public"."realm_localizations" (
            "realm_id" character varying(255) NOT NULL,
            "locale" character varying(255) NOT NULL,
            "texts" text NOT NULL,
            CONSTRAINT "realm_localizations_pkey" PRIMARY KEY ("realm_id", "locale")
        )
        WITH (oids = false);


        CREATE TABLE "public"."realm_required_credential" (
            "type" character varying(255) NOT NULL,
            "form_label" character varying(255),
            "input" boolean DEFAULT false NOT NULL,
            "secret" boolean DEFAULT false NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_92" PRIMARY KEY ("realm_id", "type")
        )
        WITH (oids = false);

        INSERT INTO "realm_required_credential" ("type", "form_label", "input", "secret", "realm_id") VALUES
        ('password',	'password',	'1',	'1',	'add3ae74-abd2-4e73-96ea-a80026fa73c5'),
        ('password',	'password',	'1',	'1',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03');

        CREATE TABLE "public"."realm_smtp_config" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_e" PRIMARY KEY ("realm_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."realm_supported_locales" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_supported_locales" PRIMARY KEY ("realm_id", "value")
        )
        WITH (oids = false);

        CREATE INDEX idx_realm_supp_local_realm ON public.realm_supported_locales USING btree (realm_id);


        CREATE TABLE "public"."redirect_uris" (
            "client_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_redirect_uris" PRIMARY KEY ("client_id", "value")
        )
        WITH (oids = false);

        CREATE INDEX idx_redir_uri_client ON public.redirect_uris USING btree (client_id);

        INSERT INTO "redirect_uris" ("client_id", "value") VALUES
        ('739d472a-69a5-4544-8342-fb084f397457',	'/realms/master/account/*'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'/realms/master/account/*'),
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'/admin/master/console/*'),
        ('ca73dc25-64bc-4cb1-9fe0-8f8034a8ce63',	'/realms/openk9/account/*'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'/realms/openk9/account/*'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'/admin/openk9/console/*'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'http://demo.openk9.localhost/*');

        CREATE TABLE "public"."required_action_config" (
            "required_action_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_req_act_cfg_pk" PRIMARY KEY ("required_action_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."required_action_provider" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "name" character varying(255),
            "realm_id" character varying(36),
            "enabled" boolean DEFAULT false NOT NULL,
            "default_action" boolean DEFAULT false NOT NULL,
            "provider_id" character varying(255),
            "priority" integer,
            CONSTRAINT "constraint_req_act_prv_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_req_act_prov_realm ON public.required_action_provider USING btree (realm_id);

        INSERT INTO "required_action_provider" ("id", "alias", "name", "realm_id", "enabled", "default_action", "provider_id", "priority") VALUES
        ('024115fd-f15c-4ae1-bbc6-a1db8d9e30ba',	'VERIFY_EMAIL',	'Verify Email',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('8fa6ca69-f4d8-4f57-b650-a713ef97544e',	'UPDATE_PROFILE',	'Update Profile',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('894f2673-f15a-4db4-b6b0-c7650b38cfdb',	'CONFIGURE_TOTP',	'Configure OTP',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('bda19132-96b5-4b33-b693-9b8a388793d3',	'UPDATE_PASSWORD',	'Update Password',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('b78668ef-7794-49d8-a0cf-0617b287a5f4',	'terms_and_conditions',	'Terms and Conditions',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'0',	'terms_and_conditions',	20),
        ('f52abf8d-c4c6-4e28-9950-25a95d371f3f',	'delete_account',	'Delete Account',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'0',	'0',	'delete_account',	60),
        ('1ca56d70-63c0-4934-a821-e06ff2c4dc69',	'update_user_locale',	'Update User Locale',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'update_user_locale',	1000),
        ('e5e06652-8061-4cbd-b368-e518fb078fc7',	'webauthn-register',	'Webauthn Register',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'webauthn-register',	70),
        ('b2583cdf-b8f6-4f0a-ac53-abde288131a1',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'1',	'0',	'webauthn-register-passwordless',	80),
        ('34c286cf-39fd-427f-afda-93422943a924',	'VERIFY_EMAIL',	'Verify Email',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('91516ca2-8326-4ebd-ac7b-576308bcc2a5',	'UPDATE_PROFILE',	'Update Profile',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('9e500520-e929-4b09-a3b4-1029d0e6f421',	'CONFIGURE_TOTP',	'Configure OTP',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('98306754-e776-470e-a361-c6b1e1d397f6',	'UPDATE_PASSWORD',	'Update Password',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('e05f76dd-f440-4412-955e-8d0e22e41ef1',	'terms_and_conditions',	'Terms and Conditions',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'0',	'terms_and_conditions',	20),
        ('481d5ff2-6078-4cee-8cfb-d381a70ef5b0',	'delete_account',	'Delete Account',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'0',	'0',	'delete_account',	60),
        ('3ce219a2-81aa-421b-bfd4-196d65ccbf7b',	'update_user_locale',	'Update User Locale',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'update_user_locale',	1000),
        ('bde9a475-411a-45f0-9d63-0f54fcdeede6',	'webauthn-register',	'Webauthn Register',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'webauthn-register',	70),
        ('e931c4b4-b500-42f9-968e-d9f6a646bb9e',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'1',	'0',	'webauthn-register-passwordless',	80);

        CREATE TABLE "public"."resource_attribute" (
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "resource_id" character varying(36) NOT NULL,
            CONSTRAINT "res_attr_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."resource_policy" (
            "resource_id" character varying(36) NOT NULL,
            "policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrpp" PRIMARY KEY ("resource_id", "policy_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_res_policy_policy ON public.resource_policy USING btree (policy_id);


        CREATE TABLE "public"."resource_scope" (
            "resource_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrsp" PRIMARY KEY ("resource_id", "scope_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_res_scope_scope ON public.resource_scope USING btree (scope_id);


        CREATE TABLE "public"."resource_server" (
            "id" character varying(36) NOT NULL,
            "allow_rs_remote_mgmt" boolean DEFAULT false NOT NULL,
            "policy_enforce_mode" character varying(15) NOT NULL,
            "decision_strategy" smallint DEFAULT '1' NOT NULL,
            CONSTRAINT "pk_resource_server" PRIMARY KEY ("id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."resource_server_perm_ticket" (
            "id" character varying(36) NOT NULL,
            "owner" character varying(255) NOT NULL,
            "requester" character varying(255) NOT NULL,
            "created_timestamp" bigint NOT NULL,
            "granted_timestamp" bigint,
            "resource_id" character varying(36) NOT NULL,
            "scope_id" character varying(36),
            "resource_server_id" character varying(36) NOT NULL,
            "policy_id" character varying(36),
            CONSTRAINT "constraint_fapmt" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsr6t700s9v50bu18ws5pmt ON public.resource_server_perm_ticket USING btree (owner, requester, resource_server_id, resource_id, scope_id);


        CREATE TABLE "public"."resource_server_policy" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "description" character varying(255),
            "type" character varying(255) NOT NULL,
            "decision_strategy" character varying(20),
            "logic" character varying(20),
            "resource_server_id" character varying(36) NOT NULL,
            "owner" character varying(255),
            CONSTRAINT "constraint_farsrp" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsrpt700s9v50bu18ws5ha6 ON public.resource_server_policy USING btree (name, resource_server_id);

        CREATE INDEX idx_res_serv_pol_res_serv ON public.resource_server_policy USING btree (resource_server_id);


        CREATE TABLE "public"."resource_server_resource" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "type" character varying(255),
            "icon_uri" character varying(255),
            "owner" character varying(255) NOT NULL,
            "resource_server_id" character varying(36) NOT NULL,
            "owner_managed_access" boolean DEFAULT false NOT NULL,
            "display_name" character varying(255),
            CONSTRAINT "constraint_farsr" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_res_srv_res_res_srv ON public.resource_server_resource USING btree (resource_server_id);

        CREATE UNIQUE INDEX uk_frsr6t700s9v50bu18ws5ha6 ON public.resource_server_resource USING btree (name, owner, resource_server_id);


        CREATE TABLE "public"."resource_server_scope" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "icon_uri" character varying(255),
            "resource_server_id" character varying(36) NOT NULL,
            "display_name" character varying(255),
            CONSTRAINT "constraint_farsrs" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsrst700s9v50bu18ws5ha6 ON public.resource_server_scope USING btree (name, resource_server_id);

        CREATE INDEX idx_res_srv_scope_res_srv ON public.resource_server_scope USING btree (resource_server_id);


        CREATE TABLE "public"."resource_uris" (
            "resource_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_resour_uris_pk" PRIMARY KEY ("resource_id", "value")
        )
        WITH (oids = false);


        CREATE TABLE "public"."role_attribute" (
            "id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "constraint_role_attribute_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_role_attribute ON public.role_attribute USING btree (role_id);


        CREATE TABLE "public"."scope_mapping" (
            "client_id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_81" PRIMARY KEY ("client_id", "role_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_scope_mapping_role ON public.scope_mapping USING btree (role_id);

        INSERT INTO "scope_mapping" ("client_id", "role_id") VALUES
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'7502f896-9adb-4580-a4d5-40d5adc142c5'),
        ('f21c5563-2adc-4c03-b6e7-ae47bc68fa67',	'fb622d47-2346-4def-b901-2d7bfe1a17d5'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'd7589b89-d577-44ea-adf3-20feea91a9d8'),
        ('92ad9f7e-322d-41c6-83ac-2eb0ffe10d8d',	'377c1c08-958e-4429-aa22-2c2b06ad2ae8');

        CREATE TABLE "public"."scope_policy" (
            "scope_id" character varying(36) NOT NULL,
            "policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrsps" PRIMARY KEY ("scope_id", "policy_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_scope_policy_policy ON public.scope_policy USING btree (policy_id);


        CREATE TABLE "public"."user_attribute" (
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "user_id" character varying(36) NOT NULL,
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            CONSTRAINT "constraint_user_attribute_pk" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_user_attribute ON public.user_attribute USING btree (user_id);

        CREATE INDEX idx_user_attribute_name ON public.user_attribute USING btree (name, value);


        CREATE TABLE "public"."user_consent" (
            "id" character varying(36) NOT NULL,
            "client_id" character varying(255),
            "user_id" character varying(36) NOT NULL,
            "created_date" bigint,
            "last_updated_date" bigint,
            "client_storage_provider" character varying(36),
            "external_client_id" character varying(255),
            CONSTRAINT "constraint_grntcsnt_pm" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_user_consent ON public.user_consent USING btree (user_id);

        CREATE UNIQUE INDEX uk_jkuwuvd56ontgsuhogm8uewrt ON public.user_consent USING btree (client_id, client_storage_provider, external_client_id, user_id);


        CREATE TABLE "public"."user_consent_client_scope" (
            "user_consent_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_grntcsnt_clsc_pm" PRIMARY KEY ("user_consent_id", "scope_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_usconsent_clscope ON public.user_consent_client_scope USING btree (user_consent_id);


        CREATE TABLE "public"."user_entity" (
            "id" character varying(36) NOT NULL,
            "email" character varying(255),
            "email_constraint" character varying(255),
            "email_verified" boolean DEFAULT false NOT NULL,
            "enabled" boolean DEFAULT false NOT NULL,
            "federation_link" character varying(255),
            "first_name" character varying(255),
            "last_name" character varying(255),
            "realm_id" character varying(255),
            "username" character varying(255),
            "created_timestamp" bigint,
            "service_account_client_link" character varying(255),
            "not_before" integer DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_fb" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE UNIQUE INDEX uk_dykn684sl8up1crfei6eckhd7 ON public.user_entity USING btree (realm_id, email_constraint);

        CREATE INDEX idx_user_email ON public.user_entity USING btree (email);

        CREATE UNIQUE INDEX uk_ru8tt6t700s9v50bu18ws5ha6 ON public.user_entity USING btree (realm_id, username);

        CREATE INDEX idx_user_service_account ON public.user_entity USING btree (realm_id, service_account_client_link);

        INSERT INTO "user_entity" ("id", "email", "email_constraint", "email_verified", "enabled", "federation_link", "first_name", "last_name", "realm_id", "username", "created_timestamp", "service_account_client_link", "not_before") VALUES
        ('3a64daa1-b7f7-4511-aaf8-373d890516eb',	NULL,	'7ea56efc-c588-4f30-ad8d-5b96ae66b8dc',	'0',	'1',	NULL,	NULL,	NULL,	'add3ae74-abd2-4e73-96ea-a80026fa73c5',	'user',	1748694934805,	NULL,	0),
        ('ed4ee6f1-9a47-455e-b029-fe246f36c193',	NULL,	'936886e0-9631-46a0-bda5-915db81c2327',	'0',	'1',	NULL,	'',	'',	'cc042c4c-1bd2-4f46-9614-6e7d441b9b03',	'k9admin',	1748695193618,	NULL,	0);

        CREATE TABLE "public"."user_federation_config" (
            "user_federation_provider_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_f9" PRIMARY KEY ("user_federation_provider_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."user_federation_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "federation_provider_id" character varying(36) NOT NULL,
            "federation_mapper_type" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_fedmapperpm" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_usr_fed_map_fed_prv ON public.user_federation_mapper USING btree (federation_provider_id);

        CREATE INDEX idx_usr_fed_map_realm ON public.user_federation_mapper USING btree (realm_id);


        CREATE TABLE "public"."user_federation_mapper_config" (
            "user_federation_mapper_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_fedmapper_cfg_pm" PRIMARY KEY ("user_federation_mapper_id", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."user_federation_provider" (
            "id" character varying(36) NOT NULL,
            "changed_sync_period" integer,
            "display_name" character varying(255),
            "full_sync_period" integer,
            "last_sync" integer,
            "priority" integer,
            "provider_name" character varying(255),
            "realm_id" character varying(36),
            CONSTRAINT "constraint_5c" PRIMARY KEY ("id")
        )
        WITH (oids = false);

        CREATE INDEX idx_usr_fed_prv_realm ON public.user_federation_provider USING btree (realm_id);


        CREATE TABLE "public"."user_group_membership" (
            "group_id" character varying(36) NOT NULL,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_user_group" PRIMARY KEY ("group_id", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_user_group_mapping ON public.user_group_membership USING btree (user_id);


        CREATE TABLE "public"."user_required_action" (
            "user_id" character varying(36) NOT NULL,
            "required_action" character varying(255) DEFAULT ' ' NOT NULL,
            CONSTRAINT "constraint_required_action" PRIMARY KEY ("required_action", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_user_reqactions ON public.user_required_action USING btree (user_id);


        CREATE TABLE "public"."user_role_mapping" (
            "role_id" character varying(255) NOT NULL,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_c" PRIMARY KEY ("role_id", "user_id")
        )
        WITH (oids = false);

        CREATE INDEX idx_user_role_mapping ON public.user_role_mapping USING btree (user_id);

        INSERT INTO "user_role_mapping" ("role_id", "user_id") VALUES
        ('cdb7a7b3-1c27-4c46-8e22-3edc962f26ac',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('b5914d6d-aa62-4380-a9f1-91bec1094853',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('9d8bc7c1-8d77-4dab-9dea-1f3d4d26c703',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('d1f39f14-202a-4864-951b-3889a127d9be',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('7a911def-a37e-4900-b6d3-593f880c9432',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('2f016158-342b-4ea8-b233-b721cfde5524',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('f195f514-21a6-4c96-85e4-d8c405453e7c',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('19474e5b-cf18-4536-b4d3-782b414a6309',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('d6675c66-7595-4e36-8c3d-02ba42d42178',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('6a36fd3f-f548-4b52-9dc8-dc6372fc018a',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('2d92a9eb-2d41-43c3-adb9-15defac17cc3',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('96a74632-95d1-45e3-a1e2-e3c3a804c356',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('a38bea4e-f485-4d8d-88f5-655813683249',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('0222cc70-2bba-4a94-a2b3-1a7be3dc49cb',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('7e6f7a47-be90-4b4d-846e-2c0e9c989096',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('b3e5d545-3013-4619-a624-c7f845b7ae90',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('90e6c101-689f-44f4-968f-3f5d3a638e8f',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('c8551b05-1af1-4fc8-ba2c-128cc78dfdbe',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('4c966fc5-402a-4fcb-99da-9815043eca27',	'3a64daa1-b7f7-4511-aaf8-373d890516eb'),
        ('877d5a18-80dd-4135-b1c6-d8a6bb5a911a',	'ed4ee6f1-9a47-455e-b029-fe246f36c193'),
        ('5a274a35-3e6d-466e-ae85-3c6b18e01a0a',	'ed4ee6f1-9a47-455e-b029-fe246f36c193'),
        ('1dee5984-cdaf-4b36-8348-d095e14096c1',	'ed4ee6f1-9a47-455e-b029-fe246f36c193'),
        ('68f3acf4-12e8-4e59-8882-a63bd4666b1e',	'ed4ee6f1-9a47-455e-b029-fe246f36c193');

        CREATE TABLE "public"."user_session" (
            "id" character varying(36) NOT NULL,
            "auth_method" character varying(255),
            "ip_address" character varying(255),
            "last_session_refresh" integer,
            "login_username" character varying(255),
            "realm_id" character varying(255),
            "remember_me" boolean DEFAULT false NOT NULL,
            "started" integer,
            "user_id" character varying(255),
            "user_session_state" integer,
            "broker_session_id" character varying(255),
            "broker_user_id" character varying(255),
            CONSTRAINT "constraint_57" PRIMARY KEY ("id")
        )
        WITH (oids = false);


        CREATE TABLE "public"."user_session_note" (
            "user_session" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(2048),
            CONSTRAINT "constraint_usn_pk" PRIMARY KEY ("user_session", "name")
        )
        WITH (oids = false);


        CREATE TABLE "public"."username_login_failure" (
            "realm_id" character varying(36) NOT NULL,
            "username" character varying(255) NOT NULL,
            "failed_login_not_before" integer,
            "last_failure" bigint,
            "last_ip_failure" character varying(255),
            "num_failures" integer,
            CONSTRAINT "CONSTRAINT_17-2" PRIMARY KEY ("realm_id", "username")
        )
        WITH (oids = false);


        CREATE TABLE "public"."web_origins" (
            "client_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_web_origins" PRIMARY KEY ("client_id", "value")
        )
        WITH (oids = false);

        CREATE INDEX idx_web_orig_client ON public.web_origins USING btree (client_id);

        INSERT INTO "web_origins" ("client_id", "value") VALUES
        ('8e10d1c8-41ab-4d5a-bbd4-e1ee4818fe16',	'+'),
        ('8d91470b-d0ab-4ee4-bbb6-a9df5edab533',	'+'),
        ('5198270c-fffb-4634-8dc3-2e42c59a7dd2',	'+');

        ALTER TABLE ONLY "public"."associated_policy" ADD CONSTRAINT "fk_frsr5s213xcx4wnkog82ssrfy" FOREIGN KEY (associated_policy_id) REFERENCES resource_server_policy(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."associated_policy" ADD CONSTRAINT "fk_frsrpas14xcx4wnkog82ssrfy" FOREIGN KEY (policy_id) REFERENCES resource_server_policy(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."authentication_execution" ADD CONSTRAINT "fk_auth_exec_flow" FOREIGN KEY (flow_id) REFERENCES authentication_flow(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."authentication_execution" ADD CONSTRAINT "fk_auth_exec_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."authentication_flow" ADD CONSTRAINT "fk_auth_flow_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."authenticator_config" ADD CONSTRAINT "fk_auth_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_attributes" ADD CONSTRAINT "fk3c47c64beacca966" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_initial_access" ADD CONSTRAINT "fk_client_init_acc_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_node_registrations" ADD CONSTRAINT "fk4129723ba992f594" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_scope_attributes" ADD CONSTRAINT "fk_cl_scope_attr_scope" FOREIGN KEY (scope_id) REFERENCES client_scope(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_scope_role_mapping" ADD CONSTRAINT "fk_cl_scope_rm_scope" FOREIGN KEY (scope_id) REFERENCES client_scope(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_session" ADD CONSTRAINT "fk_b4ao2vcvat6ukau74wbwtfqo1" FOREIGN KEY (session_id) REFERENCES user_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_session_auth_status" ADD CONSTRAINT "auth_status_constraint" FOREIGN KEY (client_session) REFERENCES client_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_session_note" ADD CONSTRAINT "fk5edfb00ff51c2736" FOREIGN KEY (client_session) REFERENCES client_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_session_prot_mapper" ADD CONSTRAINT "fk_33a8sgqw18i532811v7o2dk89" FOREIGN KEY (client_session) REFERENCES client_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_session_role" ADD CONSTRAINT "fk_11b7sgqw18i532811v7o2dv76" FOREIGN KEY (client_session) REFERENCES client_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."client_user_session_note" ADD CONSTRAINT "fk_cl_usr_ses_note" FOREIGN KEY (client_session) REFERENCES client_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."component" ADD CONSTRAINT "fk_component_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."component_config" ADD CONSTRAINT "fk_component_config" FOREIGN KEY (component_id) REFERENCES component(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."composite_role" ADD CONSTRAINT "fk_a63wvekftu8jo1pnj81e7mce2" FOREIGN KEY (composite) REFERENCES keycloak_role(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."composite_role" ADD CONSTRAINT "fk_gr7thllb9lu8q4vqa4524jjy8" FOREIGN KEY (child_role) REFERENCES keycloak_role(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."credential" ADD CONSTRAINT "fk_pfyr0glasqyl0dei3kl69r6v0" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."default_client_scope" ADD CONSTRAINT "fk_r_def_cli_scope_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."federated_identity" ADD CONSTRAINT "fk404288b92ef007a6" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."group_attribute" ADD CONSTRAINT "fk_group_attribute_group" FOREIGN KEY (group_id) REFERENCES keycloak_group(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."group_role_mapping" ADD CONSTRAINT "fk_group_role_group" FOREIGN KEY (group_id) REFERENCES keycloak_group(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."identity_provider" ADD CONSTRAINT "fk2b4ebc52ae5c3b34" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."identity_provider_config" ADD CONSTRAINT "fkdc4897cf864c4e43" FOREIGN KEY (identity_provider_id) REFERENCES identity_provider(internal_id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."identity_provider_mapper" ADD CONSTRAINT "fk_idpm_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."idp_mapper_config" ADD CONSTRAINT "fk_idpmconfig" FOREIGN KEY (idp_mapper_id) REFERENCES identity_provider_mapper(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."keycloak_role" ADD CONSTRAINT "fk_6vyqfe4cn4wlq8r6kt5vdsj5c" FOREIGN KEY (realm) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."policy_config" ADD CONSTRAINT "fkdc34197cf864c4e43" FOREIGN KEY (policy_id) REFERENCES resource_server_policy(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."protocol_mapper" ADD CONSTRAINT "fk_cli_scope_mapper" FOREIGN KEY (client_scope_id) REFERENCES client_scope(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."protocol_mapper" ADD CONSTRAINT "fk_pcm_realm" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."protocol_mapper_config" ADD CONSTRAINT "fk_pmconfig" FOREIGN KEY (protocol_mapper_id) REFERENCES protocol_mapper(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_attribute" ADD CONSTRAINT "fk_8shxd6l3e9atqukacxgpffptw" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_default_groups" ADD CONSTRAINT "fk_def_groups_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_enabled_event_types" ADD CONSTRAINT "fk_h846o4h0w8epx5nwedrf5y69j" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_events_listeners" ADD CONSTRAINT "fk_h846o4h0w8epx5nxev9f5y69j" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_required_credential" ADD CONSTRAINT "fk_5hg65lybevavkqfki3kponh9v" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_smtp_config" ADD CONSTRAINT "fk_70ej8xdxgxd0b9hh6180irr0o" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."realm_supported_locales" ADD CONSTRAINT "fk_supported_locales_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."redirect_uris" ADD CONSTRAINT "fk_1burs8pb4ouj97h5wuppahv9f" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."required_action_provider" ADD CONSTRAINT "fk_req_act_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_attribute" ADD CONSTRAINT "fk_5hrm2vlf9ql5fu022kqepovbr" FOREIGN KEY (resource_id) REFERENCES resource_server_resource(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_policy" ADD CONSTRAINT "fk_frsrpos53xcx4wnkog82ssrfy" FOREIGN KEY (resource_id) REFERENCES resource_server_resource(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."resource_policy" ADD CONSTRAINT "fk_frsrpp213xcx4wnkog82ssrfy" FOREIGN KEY (policy_id) REFERENCES resource_server_policy(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_scope" ADD CONSTRAINT "fk_frsrpos13xcx4wnkog82ssrfy" FOREIGN KEY (resource_id) REFERENCES resource_server_resource(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."resource_scope" ADD CONSTRAINT "fk_frsrps213xcx4wnkog82ssrfy" FOREIGN KEY (scope_id) REFERENCES resource_server_scope(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_server_perm_ticket" ADD CONSTRAINT "fk_frsrho213xcx4wnkog82sspmt" FOREIGN KEY (resource_server_id) REFERENCES resource_server(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."resource_server_perm_ticket" ADD CONSTRAINT "fk_frsrho213xcx4wnkog83sspmt" FOREIGN KEY (resource_id) REFERENCES resource_server_resource(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."resource_server_perm_ticket" ADD CONSTRAINT "fk_frsrho213xcx4wnkog84sspmt" FOREIGN KEY (scope_id) REFERENCES resource_server_scope(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."resource_server_perm_ticket" ADD CONSTRAINT "fk_frsrpo2128cx4wnkog82ssrfy" FOREIGN KEY (policy_id) REFERENCES resource_server_policy(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_server_policy" ADD CONSTRAINT "fk_frsrpo213xcx4wnkog82ssrfy" FOREIGN KEY (resource_server_id) REFERENCES resource_server(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_server_resource" ADD CONSTRAINT "fk_frsrho213xcx4wnkog82ssrfy" FOREIGN KEY (resource_server_id) REFERENCES resource_server(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_server_scope" ADD CONSTRAINT "fk_frsrso213xcx4wnkog82ssrfy" FOREIGN KEY (resource_server_id) REFERENCES resource_server(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."resource_uris" ADD CONSTRAINT "fk_resource_server_uris" FOREIGN KEY (resource_id) REFERENCES resource_server_resource(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."role_attribute" ADD CONSTRAINT "fk_role_attribute_id" FOREIGN KEY (role_id) REFERENCES keycloak_role(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."scope_mapping" ADD CONSTRAINT "fk_ouse064plmlr732lxjcn1q5f1" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."scope_policy" ADD CONSTRAINT "fk_frsrasp13xcx4wnkog82ssrfy" FOREIGN KEY (policy_id) REFERENCES resource_server_policy(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."scope_policy" ADD CONSTRAINT "fk_frsrpass3xcx4wnkog82ssrfy" FOREIGN KEY (scope_id) REFERENCES resource_server_scope(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_attribute" ADD CONSTRAINT "fk_5hrm2vlf9ql5fu043kqepovbr" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_consent" ADD CONSTRAINT "fk_grntcsnt_user" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_consent_client_scope" ADD CONSTRAINT "fk_grntcsnt_clsc_usc" FOREIGN KEY (user_consent_id) REFERENCES user_consent(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_federation_config" ADD CONSTRAINT "fk_t13hpu1j94r2ebpekr39x5eu5" FOREIGN KEY (user_federation_provider_id) REFERENCES user_federation_provider(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_federation_mapper" ADD CONSTRAINT "fk_fedmapperpm_fedprv" FOREIGN KEY (federation_provider_id) REFERENCES user_federation_provider(id) NOT DEFERRABLE;
        ALTER TABLE ONLY "public"."user_federation_mapper" ADD CONSTRAINT "fk_fedmapperpm_realm" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_federation_mapper_config" ADD CONSTRAINT "fk_fedmapper_cfg" FOREIGN KEY (user_federation_mapper_id) REFERENCES user_federation_mapper(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_federation_provider" ADD CONSTRAINT "fk_1fj32f6ptolw2qy60cd8n01e8" FOREIGN KEY (realm_id) REFERENCES realm(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_group_membership" ADD CONSTRAINT "fk_user_group_user" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_required_action" ADD CONSTRAINT "fk_6qj3w1jw9cvafhe19bwsiuvmd" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_role_mapping" ADD CONSTRAINT "fk_c4fqv34p1mbylloxang7b1q3l" FOREIGN KEY (user_id) REFERENCES user_entity(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."user_session_note" ADD CONSTRAINT "fk5edfb00ff51d3472" FOREIGN KEY (user_session) REFERENCES user_session(id) NOT DEFERRABLE;

        ALTER TABLE ONLY "public"."web_origins" ADD CONSTRAINT "fk_lojpho213xcx4wnkog82ssrfy" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;
        END IF;
END $$;