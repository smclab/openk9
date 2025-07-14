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

        CREATE TABLE IF NOT EXISTS "public"."admin_event_entity" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_admin_event_time ON public.admin_event_entity USING btree (realm_id, admin_event_time);


        CREATE TABLE IF NOT EXISTS "public"."associated_policy" (
            "policy_id" character varying(36) NOT NULL,
            "associated_policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrpap" PRIMARY KEY ("policy_id", "associated_policy_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_assoc_pol_assoc_pol_id ON public.associated_policy USING btree (associated_policy_id);


        CREATE TABLE IF NOT EXISTS "public"."authentication_execution" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_auth_exec_realm_flow ON public.authentication_execution USING btree (realm_id, flow_id);

        CREATE INDEX IF NOT EXISTS idx_auth_exec_flow ON public.authentication_execution USING btree (flow_id);

        INSERT INTO "authentication_execution" ("id", "alias", "authenticator", "realm_id", "flow_id", "requirement", "priority", "authenticator_flow", "auth_flow_id", "auth_config") VALUES
        ('10cccca7-e44a-4c85-93b8-81fcaf235569',	NULL,	'auth-cookie',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'abf0b79f-cb5f-4d3d-910b-6384ae8e49a7',	2,	10,	'0',	NULL,	NULL),
        ('01a8cfa3-cbeb-4741-81cf-e1d95c79675a',	NULL,	'auth-spnego',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'abf0b79f-cb5f-4d3d-910b-6384ae8e49a7',	3,	20,	'0',	NULL,	NULL),
        ('ccb97dd9-3c9b-4044-89ec-cc494193e476',	NULL,	'identity-provider-redirector',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'abf0b79f-cb5f-4d3d-910b-6384ae8e49a7',	2,	25,	'0',	NULL,	NULL),
        ('7a614366-893a-4d0d-9c34-9e623ddc80f2',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'abf0b79f-cb5f-4d3d-910b-6384ae8e49a7',	2,	30,	'1',	'4506e54b-d2e1-4367-b605-66d9fe5bf091',	NULL),
        ('11248a76-17da-46f4-a69f-185b3679b898',	NULL,	'auth-username-password-form',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'4506e54b-d2e1-4367-b605-66d9fe5bf091',	0,	10,	'0',	NULL,	NULL),
        ('37b740d3-b9af-49c4-9a40-540cce992391',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'4506e54b-d2e1-4367-b605-66d9fe5bf091',	1,	20,	'1',	'190940a9-6020-42db-a6d3-f6ac5aa3926c',	NULL),
        ('8c4c8253-99a9-4145-a035-2d81557f548c',	NULL,	'conditional-user-configured',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'190940a9-6020-42db-a6d3-f6ac5aa3926c',	0,	10,	'0',	NULL,	NULL),
        ('8584cc8b-3f7f-4867-8db7-e2f87ab01db0',	NULL,	'auth-otp-form',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'190940a9-6020-42db-a6d3-f6ac5aa3926c',	0,	20,	'0',	NULL,	NULL),
        ('251f95e7-e94b-464e-ac2b-b8026b59d0ec',	NULL,	'direct-grant-validate-username',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'a7bccc73-600e-48a7-9fb3-84bb95256599',	0,	10,	'0',	NULL,	NULL),
        ('79351fd4-0082-49c6-be39-b15f06d039c9',	NULL,	'direct-grant-validate-password',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'a7bccc73-600e-48a7-9fb3-84bb95256599',	0,	20,	'0',	NULL,	NULL),
        ('b9715922-509f-4c2d-ad5d-4956769fd663',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'a7bccc73-600e-48a7-9fb3-84bb95256599',	1,	30,	'1',	'e98b05be-f5a8-45d5-92c0-175416e90495',	NULL),
        ('ccd0c110-0f15-4bc7-a28d-d3f6ba3fc500',	NULL,	'conditional-user-configured',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e98b05be-f5a8-45d5-92c0-175416e90495',	0,	10,	'0',	NULL,	NULL),
        ('2921bb13-1441-4e7a-b2df-7963da3a2e68',	NULL,	'direct-grant-validate-otp',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e98b05be-f5a8-45d5-92c0-175416e90495',	0,	20,	'0',	NULL,	NULL),
        ('3f154950-f289-48be-a841-cd6e8db560e7',	NULL,	'registration-page-form',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ca19c414-05fc-464a-a500-6ba676768e69',	0,	10,	'1',	'063a383b-7b52-4971-90ff-75b95ff4b960',	NULL),
        ('4c0ef0b0-d8fd-4782-b996-981aa2367001',	NULL,	'registration-user-creation',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'063a383b-7b52-4971-90ff-75b95ff4b960',	0,	20,	'0',	NULL,	NULL),
        ('bfd918da-fd75-433f-808c-3fadc49d114d',	NULL,	'registration-profile-action',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'063a383b-7b52-4971-90ff-75b95ff4b960',	0,	40,	'0',	NULL,	NULL),
        ('61255444-6588-48c9-a1f8-025245ce1d71',	NULL,	'registration-password-action',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'063a383b-7b52-4971-90ff-75b95ff4b960',	0,	50,	'0',	NULL,	NULL),
        ('155b56be-47bf-4565-b0d6-3744357cb879',	NULL,	'registration-recaptcha-action',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'063a383b-7b52-4971-90ff-75b95ff4b960',	3,	60,	'0',	NULL,	NULL),
        ('293c6140-35b9-45bf-9d4f-1f7aca2d519c',	NULL,	'reset-credentials-choose-user',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'8afc682d-8c78-4fb6-834c-e0625a4b0d9e',	0,	10,	'0',	NULL,	NULL),
        ('e4bac698-62e9-4b2e-bf1a-746e429d7ff3',	NULL,	'reset-credential-email',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'8afc682d-8c78-4fb6-834c-e0625a4b0d9e',	0,	20,	'0',	NULL,	NULL),
        ('9ca860d6-d698-4957-95ca-8c25f019a442',	NULL,	'reset-password',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'8afc682d-8c78-4fb6-834c-e0625a4b0d9e',	0,	30,	'0',	NULL,	NULL),
        ('50ed86c5-c38e-4356-b0cd-3abce2762221',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'8afc682d-8c78-4fb6-834c-e0625a4b0d9e',	1,	40,	'1',	'e8edb1a1-c6d7-483e-b64a-b15d5ae090eb',	NULL),
        ('f2f87b44-e869-435a-b553-d968bc58177d',	NULL,	'conditional-user-configured',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e8edb1a1-c6d7-483e-b64a-b15d5ae090eb',	0,	10,	'0',	NULL,	NULL),
        ('f9c5d179-efd9-4729-a3d8-93dcb1e7f487',	NULL,	'reset-otp',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e8edb1a1-c6d7-483e-b64a-b15d5ae090eb',	0,	20,	'0',	NULL,	NULL),
        ('a4197703-dfcf-494c-89ef-f718dbccabc9',	NULL,	'client-secret',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'18dca5e1-350a-4a9d-9acb-9c0d5bf919c6',	2,	10,	'0',	NULL,	NULL),
        ('0453478b-858f-4de2-909b-1063e2bab007',	NULL,	'client-jwt',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'18dca5e1-350a-4a9d-9acb-9c0d5bf919c6',	2,	20,	'0',	NULL,	NULL),
        ('0716e751-8571-478a-ad5e-d11266057077',	NULL,	'client-secret-jwt',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'18dca5e1-350a-4a9d-9acb-9c0d5bf919c6',	2,	30,	'0',	NULL,	NULL),
        ('54556e6e-1f23-4fed-a685-c1b5bc7bda86',	NULL,	'client-x509',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'18dca5e1-350a-4a9d-9acb-9c0d5bf919c6',	2,	40,	'0',	NULL,	NULL),
        ('ad14289e-4443-4823-b19d-36cb72e3e31b',	NULL,	'idp-review-profile',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ac8ec9e1-c292-4d4e-bb49-935ca8da17d7',	0,	10,	'0',	NULL,	'a7d749d8-1e27-4b13-aa7b-2b72a46fc7bf'),
        ('b97e5e5a-3815-4b9e-985a-b5fc7a5df736',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ac8ec9e1-c292-4d4e-bb49-935ca8da17d7',	0,	20,	'1',	'fc1b56f0-d4c7-4f95-bfc6-871cd11959bf',	NULL),
        ('e8e13552-e7f3-452b-9e83-eaa532281fca',	NULL,	'idp-create-user-if-unique',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'fc1b56f0-d4c7-4f95-bfc6-871cd11959bf',	2,	10,	'0',	NULL,	'bd874942-6536-4912-8bac-332cae99f467'),
        ('b6b59625-5ce4-421c-9a20-8cc83baf01ff',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'fc1b56f0-d4c7-4f95-bfc6-871cd11959bf',	2,	20,	'1',	'9cb01dc6-d86d-41a2-8d75-3f9fd042a62b',	NULL),
        ('5b364f66-0f50-4102-a0ff-202d023c8994',	NULL,	'idp-confirm-link',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'9cb01dc6-d86d-41a2-8d75-3f9fd042a62b',	0,	10,	'0',	NULL,	NULL),
        ('56e550c1-e601-43e8-9966-3739aed560f4',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'9cb01dc6-d86d-41a2-8d75-3f9fd042a62b',	0,	20,	'1',	'fe929966-6e74-4db8-8f8d-ac32e214b9ff',	NULL),
        ('a5eefcc0-6b97-48b0-9649-6e6d85c86ea5',	NULL,	'idp-email-verification',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'fe929966-6e74-4db8-8f8d-ac32e214b9ff',	2,	10,	'0',	NULL,	NULL),
        ('956261a5-808c-45c0-99d9-1bf3301b8776',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'fe929966-6e74-4db8-8f8d-ac32e214b9ff',	2,	20,	'1',	'e505f2c8-f94c-406a-982c-0dc8b9a787d4',	NULL),
        ('eecb31a9-7fc5-4fb3-b4d8-0c1a5c30fdf5',	NULL,	'idp-username-password-form',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e505f2c8-f94c-406a-982c-0dc8b9a787d4',	0,	10,	'0',	NULL,	NULL),
        ('83f1b278-97f6-47b5-990f-2138de1be39c',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e505f2c8-f94c-406a-982c-0dc8b9a787d4',	1,	20,	'1',	'6c07720f-8652-409a-99c0-a83dcbd06d8b',	NULL),
        ('0f8b5ceb-40fb-4e2c-b384-d36313a38723',	NULL,	'conditional-user-configured',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'6c07720f-8652-409a-99c0-a83dcbd06d8b',	0,	10,	'0',	NULL,	NULL),
        ('98601417-a166-44b6-b5f0-8b73342d1ee1',	NULL,	'auth-otp-form',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'6c07720f-8652-409a-99c0-a83dcbd06d8b',	0,	20,	'0',	NULL,	NULL),
        ('3268d54c-c441-46ec-8b82-4c0224f30de5',	NULL,	'http-basic-authenticator',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'bd50fe65-5262-40d3-9e54-2c6e816611fb',	0,	10,	'0',	NULL,	NULL),
        ('13a24d3b-54b0-4722-b8d7-d63fec6472f7',	NULL,	'docker-http-basic-authenticator',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'dd38a4bf-9af2-4f76-998c-115d3197bbe9',	0,	10,	'0',	NULL,	NULL),
        ('d3fde05e-eeff-4dbc-b5c5-0f6ce76ffc9f',	NULL,	'no-cookie-redirect',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5622a7ad-e399-4f9e-b905-21bdcf4c9415',	0,	10,	'0',	NULL,	NULL),
        ('0e347f8a-32d8-4d11-b342-6a63c362be1f',	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5622a7ad-e399-4f9e-b905-21bdcf4c9415',	0,	20,	'1',	'5b90162a-4420-4dd5-9086-d82ddea81075',	NULL),
        ('8ee1e321-0fa8-4e67-80b2-ce37af0602bf',	NULL,	'basic-auth',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5b90162a-4420-4dd5-9086-d82ddea81075',	0,	10,	'0',	NULL,	NULL),
        ('fe9981d0-5fbd-4e97-bf5f-920655d427d8',	NULL,	'basic-auth-otp',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5b90162a-4420-4dd5-9086-d82ddea81075',	3,	20,	'0',	NULL,	NULL),
        ('acf73164-487b-483a-a7ca-7200d0fad622',	NULL,	'auth-spnego',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5b90162a-4420-4dd5-9086-d82ddea81075',	3,	30,	'0',	NULL,	NULL),
        ('c59c6137-faec-41a6-8cb2-b8a1f440dd3a',	NULL,	'auth-cookie',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'6cb9ed68-6ca4-41ac-ace6-596b392bbaea',	2,	10,	'0',	NULL,	NULL),
        ('f74b57b6-7ed5-48d1-b6ae-ef732529cf01',	NULL,	'auth-spnego',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'6cb9ed68-6ca4-41ac-ace6-596b392bbaea',	3,	20,	'0',	NULL,	NULL),
        ('9566c90d-7ebe-4dba-a936-e9d9cb5ca344',	NULL,	'identity-provider-redirector',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'6cb9ed68-6ca4-41ac-ace6-596b392bbaea',	2,	25,	'0',	NULL,	NULL),
        ('765fd1cf-2dda-473b-a689-ea545061ae30',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'6cb9ed68-6ca4-41ac-ace6-596b392bbaea',	2,	30,	'1',	'17e5978a-fb05-40f5-a213-5456638da1d7',	NULL),
        ('0e0b1ffd-2269-4133-a80f-1f82b6135730',	NULL,	'auth-username-password-form',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'17e5978a-fb05-40f5-a213-5456638da1d7',	0,	10,	'0',	NULL,	NULL),
        ('19d383b1-f38c-47b6-b1e3-8970b425a717',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'17e5978a-fb05-40f5-a213-5456638da1d7',	1,	20,	'1',	'c387f70e-4aa1-4ddd-9ef2-d6181b456611',	NULL),
        ('07df6dc5-2f8c-4e27-9b16-bfa8e87f6c11',	NULL,	'conditional-user-configured',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'c387f70e-4aa1-4ddd-9ef2-d6181b456611',	0,	10,	'0',	NULL,	NULL),
        ('b0518e01-9eea-43b4-9f76-310e747c3fc8',	NULL,	'auth-otp-form',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'c387f70e-4aa1-4ddd-9ef2-d6181b456611',	0,	20,	'0',	NULL,	NULL),
        ('97c1d94c-2cf4-455c-9074-1f8ab61d2b69',	NULL,	'direct-grant-validate-username',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'e4bcd017-fa57-4f51-9327-a9a74dc3cda6',	0,	10,	'0',	NULL,	NULL),
        ('6c00acdc-da5d-4f74-89d5-443e606b4154',	NULL,	'direct-grant-validate-password',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'e4bcd017-fa57-4f51-9327-a9a74dc3cda6',	0,	20,	'0',	NULL,	NULL),
        ('f9c3fac8-c820-4bc6-a905-f76f86972c23',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'e4bcd017-fa57-4f51-9327-a9a74dc3cda6',	1,	30,	'1',	'd30d01c0-faa6-40fd-9422-e2b27a8bd141',	NULL),
        ('3b0f9d6a-7010-411a-94ac-677ffedf277e',	NULL,	'conditional-user-configured',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'd30d01c0-faa6-40fd-9422-e2b27a8bd141',	0,	10,	'0',	NULL,	NULL),
        ('e1c30a7a-01ea-4a32-acc7-5a2b253a1df7',	NULL,	'direct-grant-validate-otp',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'd30d01c0-faa6-40fd-9422-e2b27a8bd141',	0,	20,	'0',	NULL,	NULL),
        ('471dacbc-ca63-476e-b866-39de90f267e7',	NULL,	'registration-page-form',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'ef1b5db8-4eaa-4e17-8f89-86487af81789',	0,	10,	'1',	'f1003484-67b6-4b89-899d-3f5e61d2f47f',	NULL),
        ('ccd45425-5986-4bba-97cf-b7c256c63602',	NULL,	'registration-user-creation',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'f1003484-67b6-4b89-899d-3f5e61d2f47f',	0,	20,	'0',	NULL,	NULL),
        ('37c9b261-5c0b-4c82-9eea-400e89cfd9b5',	NULL,	'registration-profile-action',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'f1003484-67b6-4b89-899d-3f5e61d2f47f',	0,	40,	'0',	NULL,	NULL),
        ('a6f9cf09-ed93-48d8-9427-ca07826f2a5a',	NULL,	'registration-password-action',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'f1003484-67b6-4b89-899d-3f5e61d2f47f',	0,	50,	'0',	NULL,	NULL),
        ('0108c6e4-5dbe-4e2f-ba93-afd51e158a10',	NULL,	'registration-recaptcha-action',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'f1003484-67b6-4b89-899d-3f5e61d2f47f',	3,	60,	'0',	NULL,	NULL),
        ('35fd5046-f1ee-4a36-83c1-c1375b4eef24',	NULL,	'reset-credentials-choose-user',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'fd4dc7bd-4022-49bb-806b-85dfb41b8f3e',	0,	10,	'0',	NULL,	NULL),
        ('c65468fb-5fed-4003-b54f-36aa0c0a5cb9',	NULL,	'reset-credential-email',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'fd4dc7bd-4022-49bb-806b-85dfb41b8f3e',	0,	20,	'0',	NULL,	NULL),
        ('66ecb94c-d6ad-4673-af0c-694d5bfa9b23',	NULL,	'reset-password',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'fd4dc7bd-4022-49bb-806b-85dfb41b8f3e',	0,	30,	'0',	NULL,	NULL),
        ('6dc03fd9-035f-44cf-b033-be65712e123b',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'fd4dc7bd-4022-49bb-806b-85dfb41b8f3e',	1,	40,	'1',	'2134bfde-8507-4c8a-a95a-337430f5958b',	NULL),
        ('b7b6aaf3-2e1d-4b69-9651-457e52a65a76',	NULL,	'conditional-user-configured',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'2134bfde-8507-4c8a-a95a-337430f5958b',	0,	10,	'0',	NULL,	NULL),
        ('e73278f3-641d-4ec6-a12a-4ed6568f59c8',	NULL,	'reset-otp',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'2134bfde-8507-4c8a-a95a-337430f5958b',	0,	20,	'0',	NULL,	NULL),
        ('d2ac5fa5-1a85-4624-859a-0c49013f37f9',	NULL,	'client-secret',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'79af612f-c9f2-4b71-93df-394b73dbea39',	2,	10,	'0',	NULL,	NULL),
        ('54c3cf2a-fd21-46a4-b379-96b8f7865c01',	NULL,	'client-jwt',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'79af612f-c9f2-4b71-93df-394b73dbea39',	2,	20,	'0',	NULL,	NULL),
        ('1401c0f9-f07d-480a-ae02-64ac065714fb',	NULL,	'client-secret-jwt',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'79af612f-c9f2-4b71-93df-394b73dbea39',	2,	30,	'0',	NULL,	NULL),
        ('598d8376-7cf9-45e1-92c4-0a3b8182bd48',	NULL,	'client-x509',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'79af612f-c9f2-4b71-93df-394b73dbea39',	2,	40,	'0',	NULL,	NULL),
        ('9f85ed90-7445-440d-9ffb-bdcf377b270b',	NULL,	'idp-review-profile',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'14513823-939f-4788-a952-262f3917ce4f',	0,	10,	'0',	NULL,	'77c52bfe-5009-4729-91bc-0bc1a313419f'),
        ('b2b33c1d-c5ff-4a7f-9b78-d42102791a11',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'14513823-939f-4788-a952-262f3917ce4f',	0,	20,	'1',	'8ff5e177-ffdc-4c7c-abfd-55fa600fb3b9',	NULL),
        ('1f5e2b1d-8d37-421f-ad2e-b691ac72dcc8',	NULL,	'idp-create-user-if-unique',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'8ff5e177-ffdc-4c7c-abfd-55fa600fb3b9',	2,	10,	'0',	NULL,	'6f0f1e61-80e2-414f-94ec-196228040859'),
        ('a99bcc42-a12e-4d48-8c51-abbda5f725f6',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'8ff5e177-ffdc-4c7c-abfd-55fa600fb3b9',	2,	20,	'1',	'5e2ea226-6af3-4160-b8e6-881864df78b0',	NULL),
        ('ae1a5e3c-2dbb-45cb-b0b8-f54522265656',	NULL,	'idp-confirm-link',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'5e2ea226-6af3-4160-b8e6-881864df78b0',	0,	10,	'0',	NULL,	NULL),
        ('bb0aa923-9d8b-46af-b9d1-2a61a62ac859',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'5e2ea226-6af3-4160-b8e6-881864df78b0',	0,	20,	'1',	'4008f70c-cfff-48cc-8ef4-32c6229a001a',	NULL),
        ('08f46ff1-294e-4fe6-8346-1a325ca45072',	NULL,	'idp-email-verification',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'4008f70c-cfff-48cc-8ef4-32c6229a001a',	2,	10,	'0',	NULL,	NULL),
        ('b85e162c-066b-4163-8e92-1a0d7297b4a2',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'4008f70c-cfff-48cc-8ef4-32c6229a001a',	2,	20,	'1',	'd5b8205d-111b-427b-81e1-8db984a2e170',	NULL),
        ('0bc0ac09-57a0-4aef-9de1-781155c27031',	NULL,	'idp-username-password-form',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'd5b8205d-111b-427b-81e1-8db984a2e170',	0,	10,	'0',	NULL,	NULL),
        ('7393e415-084c-42e1-9c76-2f153232989b',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'd5b8205d-111b-427b-81e1-8db984a2e170',	1,	20,	'1',	'75f1b693-98a1-4032-a911-aa7a67434912',	NULL),
        ('cd68194e-db78-4cc5-bb31-c0c75cedce4f',	NULL,	'conditional-user-configured',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'75f1b693-98a1-4032-a911-aa7a67434912',	0,	10,	'0',	NULL,	NULL),
        ('7580870a-4051-4f75-8fa4-843f50e23026',	NULL,	'auth-otp-form',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'75f1b693-98a1-4032-a911-aa7a67434912',	0,	20,	'0',	NULL,	NULL),
        ('374e6c16-e2fe-41a8-8464-293c2d1d7066',	NULL,	'http-basic-authenticator',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'939ebf02-cbae-437b-8296-52cdc23ee6e2',	0,	10,	'0',	NULL,	NULL),
        ('390ddc2a-a081-4627-a7cd-a9493ed278cf',	NULL,	'docker-http-basic-authenticator',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'808dba35-dc5d-413d-9160-740e05897f63',	0,	10,	'0',	NULL,	NULL),
        ('1c5f373e-a4df-470c-a71d-e14bdee4025b',	NULL,	'no-cookie-redirect',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'405d33e1-187c-4ef5-bed6-cc3f8f61626c',	0,	10,	'0',	NULL,	NULL),
        ('3cd6d62b-f994-45fc-a7c8-26aaa361aa39',	NULL,	NULL,	'8057e71d-86e9-4b84-8438-269c52eea27b',	'405d33e1-187c-4ef5-bed6-cc3f8f61626c',	0,	20,	'1',	'cc6a47c2-7d16-4991-b737-be6d0c6fd999',	NULL),
        ('79eec060-978f-4095-9e80-20e7fb669da9',	NULL,	'basic-auth',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'cc6a47c2-7d16-4991-b737-be6d0c6fd999',	0,	10,	'0',	NULL,	NULL),
        ('deaaa1f3-ee3e-4ac1-a035-29310d2ff708',	NULL,	'basic-auth-otp',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'cc6a47c2-7d16-4991-b737-be6d0c6fd999',	3,	20,	'0',	NULL,	NULL),
        ('6e08d829-4d7f-428f-9d8c-eaffdc0d0d1b',	NULL,	'auth-spnego',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'cc6a47c2-7d16-4991-b737-be6d0c6fd999',	3,	30,	'0',	NULL,	NULL),
        ('b1d7ae21-e6bc-4a10-953e-3985eab18c57',	NULL,	'auth-cookie',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'923d001f-1282-4cb6-8f44-e46b876d2cd6',	2,	10,	'0',	NULL,	NULL),
        ('3acb3ebb-7b3d-46c9-9f98-67b036c32e78',	NULL,	'auth-spnego',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'923d001f-1282-4cb6-8f44-e46b876d2cd6',	3,	20,	'0',	NULL,	NULL),
        ('8aad5492-12d6-4d6c-9154-54159a80032d',	NULL,	'identity-provider-redirector',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'923d001f-1282-4cb6-8f44-e46b876d2cd6',	2,	25,	'0',	NULL,	NULL),
        ('6d570451-b7f4-41bf-bf83-11277620195f',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'923d001f-1282-4cb6-8f44-e46b876d2cd6',	2,	30,	'1',	'e6a71636-1f1a-4802-a90b-c3c81b24fa2a',	NULL),
        ('9c38a308-3e67-48dd-be04-50a001c7de58',	NULL,	'auth-username-password-form',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'e6a71636-1f1a-4802-a90b-c3c81b24fa2a',	0,	10,	'0',	NULL,	NULL),
        ('875aa6dd-8156-43bc-bb95-53223fc08548',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'e6a71636-1f1a-4802-a90b-c3c81b24fa2a',	1,	20,	'1',	'7fdf6fe5-170e-49f1-9157-9738b23290b0',	NULL),
        ('b6b31c02-57ae-458d-8769-fafcd13f1ccf',	NULL,	'conditional-user-configured',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'7fdf6fe5-170e-49f1-9157-9738b23290b0',	0,	10,	'0',	NULL,	NULL),
        ('14844464-514d-493e-b01e-dc1d1f00556a',	NULL,	'auth-otp-form',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'7fdf6fe5-170e-49f1-9157-9738b23290b0',	0,	20,	'0',	NULL,	NULL),
        ('a625491d-d7e0-4df4-9a79-bf821e3ef2ad',	NULL,	'direct-grant-validate-username',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'62f0f7ac-b435-4c86-bebe-782964eba51e',	0,	10,	'0',	NULL,	NULL),
        ('d213f56e-58f9-4a13-8382-abe5c2492d8e',	NULL,	'direct-grant-validate-password',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'62f0f7ac-b435-4c86-bebe-782964eba51e',	0,	20,	'0',	NULL,	NULL),
        ('37f1ae2f-3602-4495-b2e7-781e89e1014c',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'62f0f7ac-b435-4c86-bebe-782964eba51e',	1,	30,	'1',	'9d487af0-b79f-4d0a-85a0-e86a45e86658',	NULL),
        ('b5486491-5f8b-43f7-8c4a-a47ee55e0f6b',	NULL,	'conditional-user-configured',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'9d487af0-b79f-4d0a-85a0-e86a45e86658',	0,	10,	'0',	NULL,	NULL),
        ('760c8122-3a6d-4ff0-8464-53e302c76f40',	NULL,	'direct-grant-validate-otp',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'9d487af0-b79f-4d0a-85a0-e86a45e86658',	0,	20,	'0',	NULL,	NULL),
        ('0c7eec85-6b2a-49ff-bf40-709fe990964e',	NULL,	'registration-page-form',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0566cc92-8616-4097-b667-5ddb691b79b0',	0,	10,	'1',	'f09c4c93-e0a0-44c1-96fb-b5a9a7c5913f',	NULL),
        ('18e15a5a-9b03-426a-8dda-a938eeba081f',	NULL,	'registration-user-creation',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'f09c4c93-e0a0-44c1-96fb-b5a9a7c5913f',	0,	20,	'0',	NULL,	NULL),
        ('f6a6550c-db38-4cb7-ba59-92868dc282ae',	NULL,	'registration-profile-action',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'f09c4c93-e0a0-44c1-96fb-b5a9a7c5913f',	0,	40,	'0',	NULL,	NULL),
        ('e2038b3e-505b-4477-bd8d-e6dabc5a9c38',	NULL,	'registration-password-action',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'f09c4c93-e0a0-44c1-96fb-b5a9a7c5913f',	0,	50,	'0',	NULL,	NULL),
        ('ddfc3fb2-7de9-499e-bb9e-b9d0ce86e682',	NULL,	'registration-recaptcha-action',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'f09c4c93-e0a0-44c1-96fb-b5a9a7c5913f',	3,	60,	'0',	NULL,	NULL),
        ('46759754-f76e-4a1b-82c3-e8d09906f3e6',	NULL,	'reset-credentials-choose-user',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a6b03e9a-cf71-4fbb-82a5-c5c20359ea87',	0,	10,	'0',	NULL,	NULL),
        ('0ede12e3-ff05-48b5-8108-30c6f6b70d5a',	NULL,	'reset-credential-email',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a6b03e9a-cf71-4fbb-82a5-c5c20359ea87',	0,	20,	'0',	NULL,	NULL),
        ('23f8e911-7313-4587-9e8a-66fe2257b1eb',	NULL,	'reset-password',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a6b03e9a-cf71-4fbb-82a5-c5c20359ea87',	0,	30,	'0',	NULL,	NULL),
        ('f6238418-ea88-43f3-abf1-4cb7a3f9702b',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a6b03e9a-cf71-4fbb-82a5-c5c20359ea87',	1,	40,	'1',	'098f350f-892d-49ab-a56d-35195af63864',	NULL),
        ('522bacb5-37d3-4665-ae84-eaa8a774893a',	NULL,	'conditional-user-configured',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'098f350f-892d-49ab-a56d-35195af63864',	0,	10,	'0',	NULL,	NULL),
        ('94e63fee-3774-4ca6-96e6-4f2beddd51ae',	NULL,	'reset-otp',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'098f350f-892d-49ab-a56d-35195af63864',	0,	20,	'0',	NULL,	NULL),
        ('ff89a285-004d-44f9-9e37-27fd2e46d457',	NULL,	'client-secret',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a216a725-432f-45e8-9d25-ca1281693434',	2,	10,	'0',	NULL,	NULL),
        ('86802048-1e29-4365-a98f-0158e4827242',	NULL,	'client-jwt',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a216a725-432f-45e8-9d25-ca1281693434',	2,	20,	'0',	NULL,	NULL),
        ('bce44a7c-92c8-4569-9b28-7167080b755c',	NULL,	'client-secret-jwt',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a216a725-432f-45e8-9d25-ca1281693434',	2,	30,	'0',	NULL,	NULL),
        ('25e3fde8-7a8d-4a6f-a31f-edb34b18fdb1',	NULL,	'client-x509',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a216a725-432f-45e8-9d25-ca1281693434',	2,	40,	'0',	NULL,	NULL),
        ('6b29a172-bb47-4ddf-847b-e67187bce03d',	NULL,	'idp-review-profile',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'3a601100-de27-471a-a0a4-baf9cca0a1a8',	0,	10,	'0',	NULL,	'9d8afc5d-a745-42c7-8fa8-d21f48f9f5b9'),
        ('d23c663d-7270-4d4f-84da-16b96cd1f97e',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'3a601100-de27-471a-a0a4-baf9cca0a1a8',	0,	20,	'1',	'6cb5ef02-a38b-4dcf-9a22-9f5a42be2269',	NULL),
        ('a843d3e3-e6f8-46c9-af1b-dfb2b5c8324e',	NULL,	'idp-create-user-if-unique',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'6cb5ef02-a38b-4dcf-9a22-9f5a42be2269',	2,	10,	'0',	NULL,	'7a3b81e0-ffd6-4ed0-bf57-6c9b570f2791'),
        ('e1ffb6b3-58cb-40e2-8ae3-823c4d26df0f',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'6cb5ef02-a38b-4dcf-9a22-9f5a42be2269',	2,	20,	'1',	'1ea798ee-d440-45f6-97d0-d1fb1af70b1c',	NULL),
        ('6b8ca6da-e86f-4a19-9609-95cb15653d8a',	NULL,	'idp-confirm-link',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1ea798ee-d440-45f6-97d0-d1fb1af70b1c',	0,	10,	'0',	NULL,	NULL),
        ('94d87d02-5ca3-424c-8185-e77c9aae7734',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1ea798ee-d440-45f6-97d0-d1fb1af70b1c',	0,	20,	'1',	'87a626cd-a4a2-46f1-a8a3-96dc615026c6',	NULL),
        ('83535865-e34b-43d2-a10c-281a26ba6665',	NULL,	'idp-email-verification',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'87a626cd-a4a2-46f1-a8a3-96dc615026c6',	2,	10,	'0',	NULL,	NULL),
        ('02a92e5f-060a-413d-b3f1-c1a3b702a131',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'87a626cd-a4a2-46f1-a8a3-96dc615026c6',	2,	20,	'1',	'35fdd7f0-a629-4823-801c-9d2a0f057feb',	NULL),
        ('d600c96b-bcde-4f14-84a0-7a6385d3459b',	NULL,	'idp-username-password-form',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'35fdd7f0-a629-4823-801c-9d2a0f057feb',	0,	10,	'0',	NULL,	NULL),
        ('5574b467-24f0-4040-9ddc-a37f2e168f23',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'35fdd7f0-a629-4823-801c-9d2a0f057feb',	1,	20,	'1',	'0418f06e-2624-4787-aa5d-fa2ba392ec83',	NULL),
        ('577afdda-0593-40fa-8d7f-b808a657d98b',	NULL,	'conditional-user-configured',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0418f06e-2624-4787-aa5d-fa2ba392ec83',	0,	10,	'0',	NULL,	NULL),
        ('d06bb128-1f75-4f4a-a1ee-75c8b1e924dd',	NULL,	'auth-otp-form',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0418f06e-2624-4787-aa5d-fa2ba392ec83',	0,	20,	'0',	NULL,	NULL),
        ('a7035760-bea7-468c-ab00-b541a82a6036',	NULL,	'http-basic-authenticator',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'e6702dbc-9b32-443c-befc-0668ee2e132d',	0,	10,	'0',	NULL,	NULL),
        ('5239fa2f-12bf-409e-b3e9-65abf40729ef',	NULL,	'docker-http-basic-authenticator',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'38e8e54c-af57-488d-ac97-b6a9b45317ba',	0,	10,	'0',	NULL,	NULL),
        ('651744c1-74d0-4afe-a5d3-11e4e20d48d8',	NULL,	'no-cookie-redirect',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'513b8d55-5361-4c71-9da4-c357f6f0f5e7',	0,	10,	'0',	NULL,	NULL),
        ('637f0e9f-90a8-4976-ae8c-536b74fabcb2',	NULL,	NULL,	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'513b8d55-5361-4c71-9da4-c357f6f0f5e7',	0,	20,	'1',	'b7fac6e7-5f52-4040-aaf4-dd68ebec81c3',	NULL),
        ('5008cd57-41b3-4266-8c3a-522905599045',	NULL,	'basic-auth',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'b7fac6e7-5f52-4040-aaf4-dd68ebec81c3',	0,	10,	'0',	NULL,	NULL),
        ('a48ae440-8010-47c8-b7e2-bb6f438182f1',	NULL,	'basic-auth-otp',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'b7fac6e7-5f52-4040-aaf4-dd68ebec81c3',	3,	20,	'0',	NULL,	NULL),
        ('310222d1-b8d3-4e59-ab84-68b5fc16ef4c',	NULL,	'auth-spnego',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'b7fac6e7-5f52-4040-aaf4-dd68ebec81c3',	3,	30,	'0',	NULL,	NULL);

        CREATE TABLE IF NOT EXISTS "public"."authentication_flow" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "description" character varying(255),
            "realm_id" character varying(36),
            "provider_id" character varying(36) DEFAULT 'basic-flow' NOT NULL,
            "top_level" boolean DEFAULT false NOT NULL,
            "built_in" boolean DEFAULT false NOT NULL,
            CONSTRAINT "constraint_auth_flow_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_auth_flow_realm ON public.authentication_flow USING btree (realm_id);

        INSERT INTO "authentication_flow" ("id", "alias", "description", "realm_id", "provider_id", "top_level", "built_in") VALUES
        ('abf0b79f-cb5f-4d3d-910b-6384ae8e49a7',	'browser',	'browser based authentication',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('4506e54b-d2e1-4367-b605-66d9fe5bf091',	'forms',	'Username, password, otp and other auth forms.',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('190940a9-6020-42db-a6d3-f6ac5aa3926c',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('a7bccc73-600e-48a7-9fb3-84bb95256599',	'direct grant',	'OpenID Connect Resource Owner Grant',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('e98b05be-f5a8-45d5-92c0-175416e90495',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('ca19c414-05fc-464a-a500-6ba676768e69',	'registration',	'registration flow',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('063a383b-7b52-4971-90ff-75b95ff4b960',	'registration form',	'registration form',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'form-flow',	'0',	'1'),
        ('8afc682d-8c78-4fb6-834c-e0625a4b0d9e',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('e8edb1a1-c6d7-483e-b64a-b15d5ae090eb',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('18dca5e1-350a-4a9d-9acb-9c0d5bf919c6',	'clients',	'Base authentication for clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'client-flow',	'1',	'1'),
        ('ac8ec9e1-c292-4d4e-bb49-935ca8da17d7',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('fc1b56f0-d4c7-4f95-bfc6-871cd11959bf',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('9cb01dc6-d86d-41a2-8d75-3f9fd042a62b',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('fe929966-6e74-4db8-8f8d-ac32e214b9ff',	'Account verification options',	'Method with which to verity the existing account',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('e505f2c8-f94c-406a-982c-0dc8b9a787d4',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('6c07720f-8652-409a-99c0-a83dcbd06d8b',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('bd50fe65-5262-40d3-9e54-2c6e816611fb',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('dd38a4bf-9af2-4f76-998c-115d3197bbe9',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('5622a7ad-e399-4f9e-b905-21bdcf4c9415',	'http challenge',	'An authentication flow based on challenge-response HTTP Authentication Schemes',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'1',	'1'),
        ('5b90162a-4420-4dd5-9086-d82ddea81075',	'Authentication Options',	'Authentication options.',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'basic-flow',	'0',	'1'),
        ('6cb9ed68-6ca4-41ac-ace6-596b392bbaea',	'browser',	'browser based authentication',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('17e5978a-fb05-40f5-a213-5456638da1d7',	'forms',	'Username, password, otp and other auth forms.',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('c387f70e-4aa1-4ddd-9ef2-d6181b456611',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('e4bcd017-fa57-4f51-9327-a9a74dc3cda6',	'direct grant',	'OpenID Connect Resource Owner Grant',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('d30d01c0-faa6-40fd-9422-e2b27a8bd141',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('ef1b5db8-4eaa-4e17-8f89-86487af81789',	'registration',	'registration flow',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('f1003484-67b6-4b89-899d-3f5e61d2f47f',	'registration form',	'registration form',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'form-flow',	'0',	'1'),
        ('fd4dc7bd-4022-49bb-806b-85dfb41b8f3e',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('2134bfde-8507-4c8a-a95a-337430f5958b',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('79af612f-c9f2-4b71-93df-394b73dbea39',	'clients',	'Base authentication for clients',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'client-flow',	'1',	'1'),
        ('14513823-939f-4788-a952-262f3917ce4f',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('8ff5e177-ffdc-4c7c-abfd-55fa600fb3b9',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('5e2ea226-6af3-4160-b8e6-881864df78b0',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('4008f70c-cfff-48cc-8ef4-32c6229a001a',	'Account verification options',	'Method with which to verity the existing account',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('d5b8205d-111b-427b-81e1-8db984a2e170',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('75f1b693-98a1-4032-a911-aa7a67434912',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('939ebf02-cbae-437b-8296-52cdc23ee6e2',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('808dba35-dc5d-413d-9160-740e05897f63',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('405d33e1-187c-4ef5-bed6-cc3f8f61626c',	'http challenge',	'An authentication flow based on challenge-response HTTP Authentication Schemes',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'1',	'1'),
        ('cc6a47c2-7d16-4991-b737-be6d0c6fd999',	'Authentication Options',	'Authentication options.',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'basic-flow',	'0',	'1'),
        ('923d001f-1282-4cb6-8f44-e46b876d2cd6',	'browser',	'browser based authentication',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('e6a71636-1f1a-4802-a90b-c3c81b24fa2a',	'forms',	'Username, password, otp and other auth forms.',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('7fdf6fe5-170e-49f1-9157-9738b23290b0',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('62f0f7ac-b435-4c86-bebe-782964eba51e',	'direct grant',	'OpenID Connect Resource Owner Grant',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('9d487af0-b79f-4d0a-85a0-e86a45e86658',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('0566cc92-8616-4097-b667-5ddb691b79b0',	'registration',	'registration flow',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('f09c4c93-e0a0-44c1-96fb-b5a9a7c5913f',	'registration form',	'registration form',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'form-flow',	'0',	'1'),
        ('a6b03e9a-cf71-4fbb-82a5-c5c20359ea87',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('098f350f-892d-49ab-a56d-35195af63864',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('a216a725-432f-45e8-9d25-ca1281693434',	'clients',	'Base authentication for clients',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'client-flow',	'1',	'1'),
        ('3a601100-de27-471a-a0a4-baf9cca0a1a8',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('6cb5ef02-a38b-4dcf-9a22-9f5a42be2269',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('1ea798ee-d440-45f6-97d0-d1fb1af70b1c',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('87a626cd-a4a2-46f1-a8a3-96dc615026c6',	'Account verification options',	'Method with which to verity the existing account',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('35fdd7f0-a629-4823-801c-9d2a0f057feb',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('0418f06e-2624-4787-aa5d-fa2ba392ec83',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1'),
        ('e6702dbc-9b32-443c-befc-0668ee2e132d',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('38e8e54c-af57-488d-ac97-b6a9b45317ba',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('513b8d55-5361-4c71-9da4-c357f6f0f5e7',	'http challenge',	'An authentication flow based on challenge-response HTTP Authentication Schemes',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'1',	'1'),
        ('b7fac6e7-5f52-4040-aaf4-dd68ebec81c3',	'Authentication Options',	'Authentication options.',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'basic-flow',	'0',	'1');

        CREATE TABLE IF NOT EXISTS "public"."authenticator_config" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "realm_id" character varying(36),
            CONSTRAINT "constraint_auth_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_auth_config_realm ON public.authenticator_config USING btree (realm_id);

        INSERT INTO "authenticator_config" ("id", "alias", "realm_id") VALUES
        ('a7d749d8-1e27-4b13-aa7b-2b72a46fc7bf',	'review profile config',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5'),
        ('bd874942-6536-4912-8bac-332cae99f467',	'create unique user config',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5'),
        ('77c52bfe-5009-4729-91bc-0bc1a313419f',	'review profile config',	'8057e71d-86e9-4b84-8438-269c52eea27b'),
        ('6f0f1e61-80e2-414f-94ec-196228040859',	'create unique user config',	'8057e71d-86e9-4b84-8438-269c52eea27b'),
        ('9d8afc5d-a745-42c7-8fa8-d21f48f9f5b9',	'review profile config',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e'),
        ('7a3b81e0-ffd6-4ed0-bf57-6c9b570f2791',	'create unique user config',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e');

        CREATE TABLE IF NOT EXISTS "public"."authenticator_config_entry" (
            "authenticator_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_auth_cfg_pk" PRIMARY KEY ("authenticator_id", "name")
        ) WITH (oids = false);

        INSERT INTO "authenticator_config_entry" ("authenticator_id", "value", "name") VALUES
        ('a7d749d8-1e27-4b13-aa7b-2b72a46fc7bf',	'missing',	'update.profile.on.first.login'),
        ('bd874942-6536-4912-8bac-332cae99f467',	'false',	'require.password.update.after.registration'),
        ('6f0f1e61-80e2-414f-94ec-196228040859',	'false',	'require.password.update.after.registration'),
        ('77c52bfe-5009-4729-91bc-0bc1a313419f',	'missing',	'update.profile.on.first.login'),
        ('7a3b81e0-ffd6-4ed0-bf57-6c9b570f2791',	'false',	'require.password.update.after.registration'),
        ('9d8afc5d-a745-42c7-8fa8-d21f48f9f5b9',	'missing',	'update.profile.on.first.login');

        CREATE TABLE IF NOT EXISTS "public"."broker_link" (
            "identity_provider" character varying(255) NOT NULL,
            "storage_provider_id" character varying(255),
            "realm_id" character varying(36) NOT NULL,
            "broker_user_id" character varying(255),
            "broker_username" character varying(255),
            "token" text,
            "user_id" character varying(255) NOT NULL,
            CONSTRAINT "constr_broker_link_pk" PRIMARY KEY ("identity_provider", "user_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client" (
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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_b71cjlbenv945rb6gcon438at ON public.client USING btree (realm_id, client_id);

        CREATE INDEX IF NOT EXISTS idx_client_id ON public.client USING btree (client_id);

        INSERT INTO "client" ("id", "enabled", "full_scope_allowed", "client_id", "not_before", "public_client", "secret", "base_url", "bearer_only", "management_url", "surrogate_auth_required", "realm_id", "protocol", "node_rereg_timeout", "frontchannel_logout", "consent_required", "name", "service_accounts_enabled", "client_authenticator_type", "root_url", "description", "registration_token", "standard_flow_enabled", "implicit_flow_enabled", "direct_access_grants_enabled", "always_display_in_console") VALUES
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'0',	'master-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	0,	'0',	'0',	'master Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/master/account/',	'0',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/master/account/',	'0',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'1',	'0',	'security-admin-console',	0,	'1',	NULL,	'/admin/master/console/',	'0',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('30508ea9-7511-4947-abed-792472b86182',	'1',	'0',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'0',	'tenant-manager-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	0,	'0',	'0',	'tenant-manager Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'0',	'realm-management',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	0,	'0',	'0',	'${client_realm-management}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/tenant-manager/account/',	'0',	NULL,	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/tenant-manager/account/',	'0',	NULL,	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'1',	'0',	'security-admin-console',	0,	'1',	NULL,	'/admin/tenant-manager/console/',	'0',	NULL,	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'1',	'0',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'1',	'1',	'tenant-manager',	0,	'1',	NULL,	'',	'0',	'',	'0',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'openid-connect',	-1,	'1',	'0',	'',	'0',	'client-secret',	'',	'',	NULL,	'1',	'0',	'1',	'0'),
        ('25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'0',	'openk9-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	0,	'0',	'0',	'openk9 Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'0',	'realm-management',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	0,	'0',	'0',	'${client_realm-management}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/openk9/account/',	'0',	NULL,	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/openk9/account/',	'0',	NULL,	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'1',	'0',	'security-admin-console',	0,	'1',	NULL,	'/admin/openk9/console/',	'0',	NULL,	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'1',	'0',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'1',	'1',	'openk9',	0,	'1',	NULL,	'',	'0',	'',	'0',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'openid-connect',	-1,	'1',	'0',	'',	'0',	'client-secret',	'',	'',	NULL,	'1',	'0',	'1',	'0');

        CREATE TABLE IF NOT EXISTS "public"."client_attributes" (
            "client_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_3c" PRIMARY KEY ("client_id", "name")
        ) WITH (oids = false);

        INSERT INTO "client_attributes" ("client_id", "name", "value") VALUES
        ('df39997b-873d-441b-8c40-4c85a5648424',	'post.logout.redirect.uris',	'+'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'post.logout.redirect.uris',	'+'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'pkce.code.challenge.method',	'S256'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'post.logout.redirect.uris',	'+'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'pkce.code.challenge.method',	'S256'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'post.logout.redirect.uris',	'+'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'post.logout.redirect.uris',	'+'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'pkce.code.challenge.method',	'S256'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'post.logout.redirect.uris',	'+'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'pkce.code.challenge.method',	'S256'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'post.logout.redirect.uris',	'+'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'display.on.consent.screen',	'false'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'backchannel.logout.revoke.offline.tokens',	'false'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'backchannel.logout.session.required',	'true'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'oauth2.device.authorization.grant.enabled',	'false'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'oidc.ciba.grant.enabled',	'false'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'post.logout.redirect.uris',	'+'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'post.logout.redirect.uris',	'+'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'pkce.code.challenge.method',	'S256'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'post.logout.redirect.uris',	'+'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'pkce.code.challenge.method',	'S256'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'post.logout.redirect.uris',	'+'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'display.on.consent.screen',	'false'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'backchannel.logout.revoke.offline.tokens',	'false'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'backchannel.logout.session.required',	'true'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'oauth2.device.authorization.grant.enabled',	'false'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'oidc.ciba.grant.enabled',	'false');

        CREATE TABLE IF NOT EXISTS "public"."client_auth_flow_bindings" (
            "client_id" character varying(36) NOT NULL,
            "flow_id" character varying(36),
            "binding_name" character varying(255) NOT NULL,
            CONSTRAINT "c_cli_flow_bind" PRIMARY KEY ("client_id", "binding_name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client_initial_access" (
            "id" character varying(36) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "timestamp" integer,
            "expiration" integer,
            "count" integer,
            "remaining_count" integer,
            CONSTRAINT "cnstr_client_init_acc_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_client_init_acc_realm ON public.client_initial_access USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."client_node_registrations" (
            "client_id" character varying(36) NOT NULL,
            "value" integer,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_84" PRIMARY KEY ("client_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client_scope" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "realm_id" character varying(36),
            "description" character varying(255),
            "protocol" character varying(255),
            CONSTRAINT "pk_cli_template" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_cli_scope ON public.client_scope USING btree (realm_id, name);

        CREATE INDEX IF NOT EXISTS idx_realm_clscope ON public.client_scope USING btree (realm_id);

        INSERT INTO "client_scope" ("id", "name", "realm_id", "description", "protocol") VALUES
        ('9cc4bf72-80ca-4543-a50d-e564d70ef530',	'offline_access',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('c63e8029-e161-42e8-b6c8-391b4ce4e8bf',	'role_list',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'SAML role list',	'saml'),
        ('6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'profile',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('180fae93-bd13-4947-b45e-71184ff6cb8e',	'email',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'address',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('3c29835b-9afa-408f-b312-020e2d382171',	'phone',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('27a38596-04c0-449f-8aa0-dd04ba078dbd',	'roles',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'web-origins',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('3b4cb516-3d86-4be4-ad28-445db543020f',	'microprofile-jwt',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('e8be51d3-0671-4356-bc8f-7236c55b5eae',	'acr',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect'),
        ('925d0d04-389b-4355-a073-763af5634342',	'offline_access',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('3bf53c49-b355-481f-b6f0-ad5dd177529e',	'role_list',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'SAML role list',	'saml'),
        ('a2486c02-5694-4cd3-9372-74783e105f85',	'profile',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('0986bbff-258c-4976-aff5-8a886bf9a10a',	'email',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('6c7d666e-eed3-40df-a2f1-954ac62d9270',	'address',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'phone',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('428550b5-ca79-4b63-85c3-4a26fe4555d5',	'roles',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('dc349458-e591-48e7-8f4f-8dde93668a78',	'web-origins',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'microprofile-jwt',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('59846590-87dd-4430-b6d7-a6c6d184379b',	'acr',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect'),
        ('9960ffce-59b3-4df9-816e-cabef361c972',	'offline_access',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('198e2431-2db6-47b7-a15a-6af52e68a6b2',	'role_list',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'SAML role list',	'saml'),
        ('d6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'profile',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'email',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('1714e6d4-8929-4921-a9d7-bfe4970becd4',	'address',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('e0587a64-8227-4bdc-b923-2cc098bc1954',	'phone',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'roles',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('d3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'web-origins',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('7113307d-79a0-4e25-a362-e95b4ef1614f',	'microprofile-jwt',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'acr',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect');

        CREATE TABLE IF NOT EXISTS "public"."client_scope_attributes" (
            "scope_id" character varying(36) NOT NULL,
            "value" character varying(2048),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "pk_cl_tmpl_attr" PRIMARY KEY ("scope_id", "name")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_clscope_attrs ON public.client_scope_attributes USING btree (scope_id);

        INSERT INTO "client_scope_attributes" ("scope_id", "value", "name") VALUES
        ('9cc4bf72-80ca-4543-a50d-e564d70ef530',	'true',	'display.on.consent.screen'),
        ('9cc4bf72-80ca-4543-a50d-e564d70ef530',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('c63e8029-e161-42e8-b6c8-391b4ce4e8bf',	'true',	'display.on.consent.screen'),
        ('c63e8029-e161-42e8-b6c8-391b4ce4e8bf',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'true',	'display.on.consent.screen'),
        ('6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'true',	'include.in.token.scope'),
        ('180fae93-bd13-4947-b45e-71184ff6cb8e',	'true',	'display.on.consent.screen'),
        ('180fae93-bd13-4947-b45e-71184ff6cb8e',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('180fae93-bd13-4947-b45e-71184ff6cb8e',	'true',	'include.in.token.scope'),
        ('cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'true',	'display.on.consent.screen'),
        ('cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'true',	'include.in.token.scope'),
        ('3c29835b-9afa-408f-b312-020e2d382171',	'true',	'display.on.consent.screen'),
        ('3c29835b-9afa-408f-b312-020e2d382171',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('3c29835b-9afa-408f-b312-020e2d382171',	'true',	'include.in.token.scope'),
        ('27a38596-04c0-449f-8aa0-dd04ba078dbd',	'true',	'display.on.consent.screen'),
        ('27a38596-04c0-449f-8aa0-dd04ba078dbd',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('27a38596-04c0-449f-8aa0-dd04ba078dbd',	'false',	'include.in.token.scope'),
        ('5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'false',	'display.on.consent.screen'),
        ('5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'',	'consent.screen.text'),
        ('5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'false',	'include.in.token.scope'),
        ('3b4cb516-3d86-4be4-ad28-445db543020f',	'false',	'display.on.consent.screen'),
        ('3b4cb516-3d86-4be4-ad28-445db543020f',	'true',	'include.in.token.scope'),
        ('e8be51d3-0671-4356-bc8f-7236c55b5eae',	'false',	'display.on.consent.screen'),
        ('e8be51d3-0671-4356-bc8f-7236c55b5eae',	'false',	'include.in.token.scope'),
        ('925d0d04-389b-4355-a073-763af5634342',	'true',	'display.on.consent.screen'),
        ('925d0d04-389b-4355-a073-763af5634342',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('3bf53c49-b355-481f-b6f0-ad5dd177529e',	'true',	'display.on.consent.screen'),
        ('3bf53c49-b355-481f-b6f0-ad5dd177529e',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('a2486c02-5694-4cd3-9372-74783e105f85',	'true',	'display.on.consent.screen'),
        ('a2486c02-5694-4cd3-9372-74783e105f85',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('a2486c02-5694-4cd3-9372-74783e105f85',	'true',	'include.in.token.scope'),
        ('0986bbff-258c-4976-aff5-8a886bf9a10a',	'true',	'display.on.consent.screen'),
        ('0986bbff-258c-4976-aff5-8a886bf9a10a',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('0986bbff-258c-4976-aff5-8a886bf9a10a',	'true',	'include.in.token.scope'),
        ('6c7d666e-eed3-40df-a2f1-954ac62d9270',	'true',	'display.on.consent.screen'),
        ('6c7d666e-eed3-40df-a2f1-954ac62d9270',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('6c7d666e-eed3-40df-a2f1-954ac62d9270',	'true',	'include.in.token.scope'),
        ('bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'true',	'display.on.consent.screen'),
        ('bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'true',	'include.in.token.scope'),
        ('428550b5-ca79-4b63-85c3-4a26fe4555d5',	'true',	'display.on.consent.screen'),
        ('428550b5-ca79-4b63-85c3-4a26fe4555d5',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('428550b5-ca79-4b63-85c3-4a26fe4555d5',	'false',	'include.in.token.scope'),
        ('dc349458-e591-48e7-8f4f-8dde93668a78',	'false',	'display.on.consent.screen'),
        ('dc349458-e591-48e7-8f4f-8dde93668a78',	'',	'consent.screen.text'),
        ('dc349458-e591-48e7-8f4f-8dde93668a78',	'false',	'include.in.token.scope'),
        ('fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'false',	'display.on.consent.screen'),
        ('fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'true',	'include.in.token.scope'),
        ('59846590-87dd-4430-b6d7-a6c6d184379b',	'false',	'display.on.consent.screen'),
        ('59846590-87dd-4430-b6d7-a6c6d184379b',	'false',	'include.in.token.scope'),
        ('9960ffce-59b3-4df9-816e-cabef361c972',	'true',	'display.on.consent.screen'),
        ('9960ffce-59b3-4df9-816e-cabef361c972',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('198e2431-2db6-47b7-a15a-6af52e68a6b2',	'true',	'display.on.consent.screen'),
        ('198e2431-2db6-47b7-a15a-6af52e68a6b2',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('d6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'true',	'display.on.consent.screen'),
        ('d6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('d6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'true',	'include.in.token.scope'),
        ('37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'true',	'display.on.consent.screen'),
        ('37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'true',	'include.in.token.scope'),
        ('1714e6d4-8929-4921-a9d7-bfe4970becd4',	'true',	'display.on.consent.screen'),
        ('1714e6d4-8929-4921-a9d7-bfe4970becd4',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('1714e6d4-8929-4921-a9d7-bfe4970becd4',	'true',	'include.in.token.scope'),
        ('e0587a64-8227-4bdc-b923-2cc098bc1954',	'true',	'display.on.consent.screen'),
        ('e0587a64-8227-4bdc-b923-2cc098bc1954',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('e0587a64-8227-4bdc-b923-2cc098bc1954',	'true',	'include.in.token.scope'),
        ('160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'true',	'display.on.consent.screen'),
        ('160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'false',	'include.in.token.scope'),
        ('d3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'false',	'display.on.consent.screen'),
        ('d3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'',	'consent.screen.text'),
        ('d3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'false',	'include.in.token.scope'),
        ('7113307d-79a0-4e25-a362-e95b4ef1614f',	'false',	'display.on.consent.screen'),
        ('7113307d-79a0-4e25-a362-e95b4ef1614f',	'true',	'include.in.token.scope'),
        ('7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'false',	'display.on.consent.screen'),
        ('7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'false',	'include.in.token.scope');

        CREATE TABLE IF NOT EXISTS "public"."client_scope_client" (
            "client_id" character varying(255) NOT NULL,
            "scope_id" character varying(255) NOT NULL,
            "default_scope" boolean DEFAULT false NOT NULL,
            CONSTRAINT "c_cli_scope_bind" PRIMARY KEY ("client_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_clscope_cl ON public.client_scope_client USING btree (client_id);

        CREATE INDEX IF NOT EXISTS idx_cl_clscope ON public.client_scope_client USING btree (scope_id);

        INSERT INTO "client_scope_client" ("client_id", "scope_id", "default_scope") VALUES
        ('df39997b-873d-441b-8c40-4c85a5648424',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('df39997b-873d-441b-8c40-4c85a5648424',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('30508ea9-7511-4947-abed-792472b86182',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('30508ea9-7511-4947-abed-792472b86182',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('30508ea9-7511-4947-abed-792472b86182',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('30508ea9-7511-4947-abed-792472b86182',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('30508ea9-7511-4947-abed-792472b86182',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('30508ea9-7511-4947-abed-792472b86182',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('30508ea9-7511-4947-abed-792472b86182',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('30508ea9-7511-4947-abed-792472b86182',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('30508ea9-7511-4947-abed-792472b86182',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('2e8b984f-c411-416b-88e8-a19092d77a62',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('284f063e-1ad4-4c28-8243-9e181600363f',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('48fa6527-3abe-45be-af21-5cfc1194a32c',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('63a798ec-a937-491a-a61c-bf30a566c1e6',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('0acb0086-f221-4a52-b079-8af1f150c7ce',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('3576883c-04f4-4d95-8c0a-92878d1a0606',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0');

        CREATE TABLE IF NOT EXISTS "public"."client_scope_role_mapping" (
            "scope_id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            CONSTRAINT "pk_template_scope" PRIMARY KEY ("scope_id", "role_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_clscope_role ON public.client_scope_role_mapping USING btree (scope_id);

        CREATE INDEX IF NOT EXISTS idx_role_clscope ON public.client_scope_role_mapping USING btree (role_id);

        INSERT INTO "client_scope_role_mapping" ("scope_id", "role_id") VALUES
        ('9cc4bf72-80ca-4543-a50d-e564d70ef530',	'd2ad7d1b-7432-4d63-af55-e8dcbfdfb525'),
        ('925d0d04-389b-4355-a073-763af5634342',	'd8af271a-f459-408d-8c70-f793a831b551'),
        ('9960ffce-59b3-4df9-816e-cabef361c972',	'95ec85b4-d1bf-4a2f-8c35-35c464da6f05');

        CREATE TABLE IF NOT EXISTS "public"."client_session" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_client_session_session ON public.client_session USING btree (session_id);


        CREATE TABLE IF NOT EXISTS "public"."client_session_auth_status" (
            "authenticator" character varying(36) NOT NULL,
            "status" integer,
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_auth_status_pk" PRIMARY KEY ("client_session", "authenticator")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client_session_note" (
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_5e" PRIMARY KEY ("client_session", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client_session_prot_mapper" (
            "protocol_mapper_id" character varying(36) NOT NULL,
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_cs_pmp_pk" PRIMARY KEY ("client_session", "protocol_mapper_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client_session_role" (
            "role_id" character varying(255) NOT NULL,
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constraint_5" PRIMARY KEY ("client_session", "role_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."client_user_session_note" (
            "name" character varying(255) NOT NULL,
            "value" character varying(2048),
            "client_session" character varying(36) NOT NULL,
            CONSTRAINT "constr_cl_usr_ses_note" PRIMARY KEY ("client_session", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."component" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "parent_id" character varying(36),
            "provider_id" character varying(36),
            "provider_type" character varying(255),
            "realm_id" character varying(36),
            "sub_type" character varying(255),
            CONSTRAINT "constr_component_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_component_realm ON public.component USING btree (realm_id);

        CREATE INDEX IF NOT EXISTS idx_component_provider_type ON public.component USING btree (provider_type);

        INSERT INTO "component" ("id", "name", "parent_id", "provider_id", "provider_type", "realm_id", "sub_type") VALUES
        ('f8b0a269-4ad2-481e-a439-55f24570b991',	'Trusted Hosts',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'anonymous'),
        ('a18ced84-490b-49bf-ada8-512f8b3d73b9',	'Consent Required',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'anonymous'),
        ('60c706b8-dcf6-4228-94c4-9a3e721f75f2',	'Full Scope Disabled',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'anonymous'),
        ('c95a3196-c118-4d74-9d3e-1ea47a2f166b',	'Max Clients Limit',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'anonymous'),
        ('42a0af72-9c6f-4c44-a549-f21c2efac54b',	'Allowed Protocol Mapper Types',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'anonymous'),
        ('831b654d-7166-467e-b997-07f909aa8236',	'Allowed Client Scopes',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'anonymous'),
        ('80f923e5-61b7-486a-8a1c-502c34fe3cde',	'Allowed Protocol Mapper Types',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'authenticated'),
        ('ba2e1ad1-40c6-47e9-94b6-078ce20ab47f',	'Allowed Client Scopes',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'authenticated'),
        ('38076f95-a7f2-4f37-bc49-5bd0c46dbe92',	'rsa-generated',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL),
        ('f4ef2a92-de5a-4c87-9a93-01d7e46b3efc',	'rsa-enc-generated',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL),
        ('8084148a-19ef-4492-87dd-85c7388b26fc',	'hmac-generated',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL),
        ('1cb32a32-7192-4c87-a669-b12410ac8968',	'aes-generated',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL),
        ('cfba7b9e-3bf2-4473-91a3-950bfcdf8bdf',	'rsa-generated',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL),
        ('45c7f8a1-81e5-448c-b093-9a7799f2a384',	'rsa-enc-generated',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL),
        ('97d7ca09-f545-4bda-b8ce-da04797c94e6',	'hmac-generated',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL),
        ('98260899-2625-4667-97db-6a3900469453',	'aes-generated',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL),
        ('c4213884-2a98-4dc4-9891-aae511b0c5c8',	'Trusted Hosts',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'anonymous'),
        ('c639b615-742a-495d-91f8-c6f9ea20e3bc',	'Consent Required',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'anonymous'),
        ('cb1d1e82-9bc0-4845-af63-43b605404c00',	'Full Scope Disabled',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'anonymous'),
        ('6cf63e89-b6cd-44cd-9f4b-5ca2c2171229',	'Max Clients Limit',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'anonymous'),
        ('8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'Allowed Protocol Mapper Types',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'anonymous'),
        ('c515e23b-a04c-42ec-bf69-e10c5fe8ec84',	'Allowed Client Scopes',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'anonymous'),
        ('818c52bd-c7bd-4dea-9921-6735ed99780b',	'Allowed Protocol Mapper Types',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'authenticated'),
        ('befbaf64-a5fd-4817-8eee-d7be429b802f',	'Allowed Client Scopes',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'authenticated'),
        ('f34eafa4-6647-4d37-a205-f50eaa65f36d',	'rsa-generated',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL),
        ('975ec143-0a21-4b05-8524-e5b1038201ff',	'rsa-enc-generated',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL),
        ('d0d871fd-50f8-4585-85ad-97f22231c8ac',	'hmac-generated',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL),
        ('08bb2a76-373b-4e2a-a2e6-a2e7a387b261',	'aes-generated',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL),
        ('e797bd57-3010-47e7-8434-b3cd81121beb',	'Trusted Hosts',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'anonymous'),
        ('3621f004-e5d8-4034-b017-bbf99eb417ad',	'Consent Required',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'anonymous'),
        ('2dd2a86c-3ea7-40d6-bb07-98ecaf56cf92',	'Full Scope Disabled',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'anonymous'),
        ('b73fdf13-c7e1-47d7-be39-f0bdc91dcc91',	'Max Clients Limit',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'anonymous'),
        ('6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'Allowed Protocol Mapper Types',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'anonymous'),
        ('a317dd75-9a40-4808-8a90-31fe1b25f537',	'Allowed Client Scopes',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'anonymous'),
        ('df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'Allowed Protocol Mapper Types',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'authenticated'),
        ('02236132-fbe4-433b-980b-c9078c9b8392',	'Allowed Client Scopes',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'authenticated');

        CREATE TABLE IF NOT EXISTS "public"."component_config" (
            "id" character varying(36) NOT NULL,
            "component_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(4000),
            CONSTRAINT "constr_component_config_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_compo_config_compo ON public.component_config USING btree (component_id);

        INSERT INTO "component_config" ("id", "component_id", "name", "value") VALUES
        ('90e0e26a-16c6-429f-a1e0-842cb0191e4c',	'f8b0a269-4ad2-481e-a439-55f24570b991',	'host-sending-registration-request-must-match',	'true'),
        ('e2fc8d64-4dde-44d5-a40e-0a54e4f6d07b',	'f8b0a269-4ad2-481e-a439-55f24570b991',	'client-uris-must-match',	'true'),
        ('c47d03d4-bdff-489f-962e-704c24efad62',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('7aa096f7-333d-4088-ae45-10eafb4ea9dc',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('2dad3390-6ac4-45aa-8a8a-693535ffdde8',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('55d66376-c689-49cf-a8fe-6544b333bb0f',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('6b608564-7088-4331-92b3-e6f3c9955d04',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('0399f862-cfc7-4d42-ac97-6a98ff26455e',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('29c752ee-9a2c-453b-ac28-d0e705903a10',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('04988c94-4bef-40a8-9bff-c18b19c64bd1',	'80f923e5-61b7-486a-8a1c-502c34fe3cde',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('ba8a460b-9875-4441-8b41-39292b1ce3f9',	'ba2e1ad1-40c6-47e9-94b6-078ce20ab47f',	'allow-default-scopes',	'true'),
        ('615836c3-14a0-412b-b7cd-b04166553c05',	'c95a3196-c118-4d74-9d3e-1ea47a2f166b',	'max-clients',	'200'),
        ('c4658d9f-b825-443f-aca8-b510e62cda99',	'831b654d-7166-467e-b997-07f909aa8236',	'allow-default-scopes',	'true'),
        ('cec3737a-0bf7-42f6-99f9-7d54dac6ae28',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('da0095e1-291a-4439-9401-8ea9f36ccd0b',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('5e1aa7c0-27a5-414a-869f-a0a82a64192a',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('d359f291-2164-49c1-932d-0e02e2ed3aa2',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('1cf56b9a-8d07-4e6e-99d2-4a08a4aed8df',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('37c59703-9212-4247-9053-8d13fc011a9f',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('a8264180-79de-4463-b7ca-340a83ab7b3e',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('74a90428-e4b9-4d87-ab68-640830d246f4',	'42a0af72-9c6f-4c44-a549-f21c2efac54b',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('a603f88f-0625-4c3d-8af1-e7ef53e47ed3',	'38076f95-a7f2-4f37-bc49-5bd0c46dbe92',	'certificate',	'MIICmzCCAYMCBgGWeF0fXDANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNDI3MTc0NjMwWhcNMzUwNDI3MTc0ODEwWjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC5TyTeBghIQqduteCpTAjTkM56Q16Trsmv2tkN0o+wWlLEz9S2Cqq+QW2sIrjV08CTa3FRl0yHFwSY4e3CABI8wG8CBwpsmmtXM848eglMyMwepNB5nMQf344uT5vcfYtwN9bs+dTXR3QV5TTbii4wFs7Vwa0dsqNSzTOhmf1RiGKn0FeMzPBuPSgdUua7KB/afTLrttVvsc/d2zk9YqSmjsESS3o/TpZ9DmmS4yiGDW0Ra/dntJAqx5dGW4VVkRR3qsh/v7Tzeka0kNS5/nORCG9BTpblMLZymzqzhNboMokMzoPeWhCLvrD/Ru0hA0y04nFfbhQIoLeHY6CPnjGLAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAAdn++1RfGCyzzr/4SkDJ5oPwx5whnBB5quhLzy7SOuAkC1JV1qH/yDtc99rAaFibOX5do1WHL0RwaxpSIKyj2Oay3kjRjsA+6fBzeE2veLRBqGvbMF1r+xxEPZ6L3GMgqZgFUJvGSrk7OkvriWjvAKaERFTLVQsOIXftceWSf1z10up3KYFJsN2BLIOt1k6cq34JAdfZqjRX2B+6DI09vpLUS+V3UT4p0Uob6IRQJOehw7j0JCKQmyZjDpE4jNAsdxo+bYyI9CEa6gUNDSiGbhvGcbKxTy8tYQv4BKHBhyV24o+Z0e2nScuwoDa4sdiQN6asr9nwHz+zIpHJCvFPJE='),
        ('20864dee-e7d2-4429-9e1a-2e49688ea6a0',	'38076f95-a7f2-4f37-bc49-5bd0c46dbe92',	'privateKey',	'MIIEogIBAAKCAQEAuU8k3gYISEKnbrXgqUwI05DOekNek67Jr9rZDdKPsFpSxM/UtgqqvkFtrCK41dPAk2txUZdMhxcEmOHtwgASPMBvAgcKbJprVzPOPHoJTMjMHqTQeZzEH9+OLk+b3H2LcDfW7PnU10d0FeU024ouMBbO1cGtHbKjUs0zoZn9UYhip9BXjMzwbj0oHVLmuygf2n0y67bVb7HP3ds5PWKkpo7BEkt6P06WfQ5pkuMohg1tEWv3Z7SQKseXRluFVZEUd6rIf7+083pGtJDUuf5zkQhvQU6W5TC2cps6s4TW6DKJDM6D3loQi76w/0btIQNMtOJxX24UCKC3h2Ogj54xiwIDAQABAoIBACdZIt7yMPgHDz526EmKl6U07mMPwxlg1/q7YbOJrnEn/MNFzkkJYtAbXIpcpRriZ9Xlzp4gnmNhA9zivHWhRj4YLnojvJbrAzf1DCnTT/459P3cyVfJbevGsI4s4U+kaONfOAgB1KwRxBiSHvMvGDel4C2LfpL7x6phYEFETYHuMl2B2Sck/jePdKrLeRoQE086p+X45wzRNtBGz4Un6SUDEYTNzcCsmQmDa+Y06nIePnu6a4Y5AUiOZZoMgEXCGjS+uukLh91M7YFjskNF3hSM7obM/9bUAVEm2SoW6F8Z+KzQvvFftrLwOnuqOgmoFayO5W2n4LoC8Cy/FAaSCMECgYEA/NJB1Iu17bKq7Y1bdx1cdz0BfY4szODmhY/DZaPopn9hqzCHimfLPMHXpIgW4PCazT+Cfvbw95lxDeGjWuRTccPLQJpoiYq6FEFu2X5tgbmvjjAV5PLs3QMev2AGy2l/dksquLSfKgBo1cZMsdb2uX2nyMJjLgQgkTjwJa6SBGkCgYEAu6OWxyAHiGzg9bg28PMO47dZSXqp80KhIhm+pWOfjVTLZHjrjURNFlzez3YRqF1v9tWAQjJtqJSU5YNKtADoR0IrCOS0+Q/rfJSEl0AgQxuBJZRIsBwjLNvfe9LwbF5aVaq6m/TY/sdAtiNUoq1sLGgUIzHhNLfU88KB42mCN9MCgYA/yHPfQZgWbdr9r4oB1SB83KFBodrfnWXLEGgFfoK1brGfVND9NA5sN6NF2SecfFcxrPIpQfetH8ML838Y8T20F+dxVwNEoAEuFwv2RxUtg0EtKCh96GOlpqHdOka8jtMfbvtKOhdlq1DHjg3PWCJKzc7EsXYAJ/5nZOwBNbeU8QKBgAN+NBxY2EsN29I3L66kl65dh2f6xJcmsmE0IXxqslmrLm4cYiYrE1RLPkqWZBXCR4dMyipxTeFJgswS+Z0IN0q8TJjJySSpyzRyf1VheDhHlsvgSsoce/slzK89agRabUwS9Y5ZrIBxrR624ah1mgHpnhZZo1ub4Hb5M/nLyQTJAoGAD3mioCfZntZTJ3K5J53MRjDFU1VcF2CPS27KfiPCGxeKTYwNsndSpBnZm6rBsnWzPrequBuAmeBcTShyns7acMbTe69pSinIvzJtijfD7EFN9s+zY+EE2RAynE1iknjF1EFgn7ojSVjOC+sPKt2wl7Z2ZJ1KtPB4HHAHQcdVuWI='),
        ('593a6b1b-2477-46ab-88c5-f695335ba70b',	'38076f95-a7f2-4f37-bc49-5bd0c46dbe92',	'priority',	'100'),
        ('15bbae3e-9c0a-4ebd-ba99-d11b0fc58b38',	'38076f95-a7f2-4f37-bc49-5bd0c46dbe92',	'keyUse',	'SIG'),
        ('c13ed421-dd5c-406a-a502-9440d1f02b1c',	'1cb32a32-7192-4c87-a669-b12410ac8968',	'secret',	'u9Uhk3B_0UZQUCupclpY7g'),
        ('64884a1d-e04d-4fc4-936f-bbec1d2f3ef8',	'1cb32a32-7192-4c87-a669-b12410ac8968',	'kid',	'04663887-5471-4705-97e0-8fb67bb2309a'),
        ('71c6f1ba-96b3-4d50-902c-bdee45a99b71',	'1cb32a32-7192-4c87-a669-b12410ac8968',	'priority',	'100'),
        ('5c2682c5-e881-44ed-8acd-07679dbcea07',	'f4ef2a92-de5a-4c87-9a93-01d7e46b3efc',	'certificate',	'MIICmzCCAYMCBgGWeF0f1jANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNDI3MTc0NjMxWhcNMzUwNDI3MTc0ODExWjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCdvSl01H/FjqQjl0ZFPz3LbCc76WwFfq1rQISoGX9VYQtJb4GeC73EVv4tUmg1N3fhCr3hEcgbIiFlZM/rHdqsnwLDrALLIKbfpOhhVNA9lnD8qAlQvhuIBzrPwwxZq8yYnuYy1SL/zioj21QvnerJOvtfPPqF+93HseEBaVZeRA2GTQCDiVq5sH/6tJTE2RbfaS0YjRlEaH+M00b3HnV/2LW81xi3lxpWHwQEeI+OBwEvRcZQ1V2txSWjQdUvK9teVlk4OOorn0TRFK7VTS6epXltJq/UkBPYrcA7lofpAWmof635EY3XuorLSO+sNZE8aW5fBc0R93eOFYbKSYnVAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAJP7YajfIxroaszKur8esNlSRCCWTyOsxRuEk63yI6l+StuMKqefaemvGLGI9zeOZdO/awu/m8vOMd5dGubUYA/Zi107gISVpLmykt0RlepIQ4SAMwZv06St0dv0a5A9r4LAUTkzMJm8bct671mlGwQvBh3M9QuKRtoiTmSAKAvHikY09qWp3w/+1YKsqPaQjGr/D2hQJ+X8UT/Px0yRpJWyC36o0rTtpt1RIeEldPO6bP0XQT43ZubKys9YNRQsPIerpmf6MoB1JfWBrOsyOxCm6CNCe/JBLCpmkGe12vNhdE2IKNuQaYcZ6YhNvvkiTDmK7ZFFW6vk2pxdC2G2b5o='),
        ('b6a5b022-e0f0-4dc2-a2b8-c0e7aea44d7e',	'e797bd57-3010-47e7-8434-b3cd81121beb',	'host-sending-registration-request-must-match',	'true'),
        ('41e67266-ec64-4f71-9e41-684a6218e0aa',	'f4ef2a92-de5a-4c87-9a93-01d7e46b3efc',	'privateKey',	'MIIEogIBAAKCAQEAnb0pdNR/xY6kI5dGRT89y2wnO+lsBX6ta0CEqBl/VWELSW+Bngu9xFb+LVJoNTd34Qq94RHIGyIhZWTP6x3arJ8Cw6wCyyCm36ToYVTQPZZw/KgJUL4biAc6z8MMWavMmJ7mMtUi/84qI9tUL53qyTr7Xzz6hfvdx7HhAWlWXkQNhk0Ag4laubB/+rSUxNkW32ktGI0ZRGh/jNNG9x51f9i1vNcYt5caVh8EBHiPjgcBL0XGUNVdrcUlo0HVLyvbXlZZODjqK59E0RSu1U0unqV5bSav1JAT2K3AO5aH6QFpqH+t+RGN17qKy0jvrDWRPGluXwXNEfd3jhWGykmJ1QIDAQABAoIBAAxZ0OKsdtq1qgV9ZUurwX9plwLaapJdkl+Y/CobYPyC7jrQR9JjsfarJxjOYIl2L+VHjYmPdl8lg37ob11GB6bHhpRipg9YnahaRbXFSc7gtjUaRSx0zjwO8NxutctZp9n1aynkxWpJLsSC+msGDum1vBP/dtBB4eIue+M7577WrshixuQjgrJP7crE+xwZ4MgWL38I95D3eCmc1o/qXSSi08+rSGClj4HhtisQtGXe1PnUB+X77yOEoP/WsRwCrvUPIVjpICsBPaM682tEhl/MCt8Bkau6CKn4HUtpSIbmF5nGDxf17f7PoQhvNPIXospkrYrla3luac2t34A7P6sCgYEA0NMXxdtqbLyWQWATBBM1MRFeFw+7AYBdEhXvDwqzeAUUYomNOrigW2bW6R9XgR4U1KpwgeFBiTjQx6aU+3Xy8dM4WNutPcwyIo083gZIOpP59nK3Ta8irDaQqwL9xibXpjz0GKSnOkjE/n5d9YQMW+mMUnpg7+djeT7BCWK9AjMCgYEAwV+kg+vfoUt4YL4QGRVd6SYsCG3SFnjWNzUKEx5VopvRspYWQXndq2Gp7s4jGogHyHXqjuSSs4h/Td/0hGFOHcOWAvQhqFNIe+pDymmAC3KwxDRO32AJFeqpZ49SsTyIRq6WMO3k+mWLxLh23nRCzoDP5FB0J6GqjlTvAkA5i9cCgYAEvpcOrsKHP+2mBJnKAOm3eK6LlZbpHNKAg7EPxW14b50b9AYabHFwQjDl0Ql67IUKo+i0erqzdHAB6T3/TJm9dtAT7MHN4qLM+CqG3NutQQlv2QFjKXR/NyooSIQdiWpi44WkPbFy+I5JPfPCjUJ+oBrJEPC84qP28D8QiIbE0wKBgHKZMs83n01gg5OZr8KdxgzhIWFRbBsDO7h2B2VzKb73ZxvWFJRzG2pws5uhsG4NxccIEpziWwpoz0EggzFc1UWQXrubYML3sFI3cDtpsIeYTafJdCCvMlM5wXFtH82HmR+CYeHXakb2nRFVXKTS87Cb6BhfkabXCVPWg9qqoy/7AoGAQTpj9R5qFXZ3GzwBSjEu82z2vOs1Pv42woxjzmaVZJuJaIygWQgyBEZ5JNYebaS4xVMEUvH8MfcXtm1QD7Cekb1QeKa+g2n3XxqlKpsbrYAzvsn1wMBkaoIImqVBbfTLrsd3sVBXGVfSYnSY3gtxOm0CbM38dIlQQ2SpzKiJtAU='),
        ('3e2e5b09-4faf-4ab7-8f05-19a125dfbbe0',	'f4ef2a92-de5a-4c87-9a93-01d7e46b3efc',	'algorithm',	'RSA-OAEP'),
        ('876bbb14-62cb-458d-9a43-ef6da17e5dec',	'f4ef2a92-de5a-4c87-9a93-01d7e46b3efc',	'priority',	'100'),
        ('65ded0b6-f074-4ef5-aa9d-efcf4a33261d',	'f4ef2a92-de5a-4c87-9a93-01d7e46b3efc',	'keyUse',	'ENC'),
        ('859a4dc4-2b75-452a-ad62-ea6808ca7af9',	'8084148a-19ef-4492-87dd-85c7388b26fc',	'kid',	'5cb616ea-8562-48dc-8b72-267f0ea43a20'),
        ('9ae7bda7-ce2f-4f69-ad23-1c01f4e26fdc',	'8084148a-19ef-4492-87dd-85c7388b26fc',	'algorithm',	'HS256'),
        ('a7a11ab1-d8fc-4763-adb2-c3172000f43a',	'8084148a-19ef-4492-87dd-85c7388b26fc',	'priority',	'100'),
        ('6f7e3e6f-4fd8-411e-878a-461b4e9a9be9',	'8084148a-19ef-4492-87dd-85c7388b26fc',	'secret',	'lHA2RNw_sgAvkbqeY60twTD0hXzmYWDDqjBZ195zZkXLo_C1PpnmtsMJetfv-rTOZH9Pm7bkarpnhV8AtIi-Ow'),
        ('20f080d7-f6e1-49fe-aa3f-5517cd58e2c5',	'45c7f8a1-81e5-448c-b093-9a7799f2a384',	'keyUse',	'ENC'),
        ('52c1bd70-e9d5-4880-b6a1-365ed7b72170',	'45c7f8a1-81e5-448c-b093-9a7799f2a384',	'priority',	'100'),
        ('c22fe12a-5c66-4a04-afc9-de34aa11b698',	'45c7f8a1-81e5-448c-b093-9a7799f2a384',	'certificate',	'MIICqzCCAZMCBgGWeF19STANBgkqhkiG9w0BAQsFADAZMRcwFQYDVQQDDA50ZW5hbnQtbWFuYWdlcjAeFw0yNTA0MjcxNzQ2NTVaFw0zNTA0MjcxNzQ4MzVaMBkxFzAVBgNVBAMMDnRlbmFudC1tYW5hZ2VyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjgLYDGXZxJBnQaX3sSPnDJOCK1KpErHqCW3sAzatrA98K9qIyU20oGO6MtrjAfNVBKa9RosAr1oQ3ZixWk4XDiipNnfF5mxtoir5NcBxkrALaUJ4Tu07HUYNFgLOZy2pvSNukablXH2K1JNNfoS/jabZNdzhgksoepAOgjtGzb8Kt6zx7+767ZXDxAYcAxfHGN0oaOR/I+U4lG4dwv3hWzXRVws4kLSmH47xtK0gT5duM4dHN7gouZj9zZgt69W4SLb7s7T6q3RmVEHbU/iHcX6Iwvaj+SHiZaAtLMCVvax+98CVYBM3kOSV8+awqUx3y07j6Sy2WZps2NEp7cZmkwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQAxTb+YcAKmxox4DbGtPvuHG8vU16FB6MfTV+L2REk1XujQV4cWgwGTZjAuaMD9Sai53RvABdU8KFZnvCYCJ3dOAy4hPveEyVjtOXiL0XBEg8LUmYDROorjg68ZV3o9DLePEipfYv0IOXjHdmpPXQ+WBFSWWU0kOZd8s0axxd19V2hLlWErkO8yBDo1RETlBRdUGk9GH6kDWE+N+1Y4PH2FYobEtdyJh3Z6DG7bSx5ImS64bB8/HrVpyvAo9EMuqoMP3KigrZSxoP+m21c2f8mMK+9hZMY96xSzjst51lGn97lTyDI1iFizgV/JrAdw6Y/zdjTds9lf+M1sN4qeFPi1'),
        ('b007850a-7950-450f-b8e9-ddbc9f0c453d',	'45c7f8a1-81e5-448c-b093-9a7799f2a384',	'algorithm',	'RSA-OAEP'),
        ('4f975ca0-3ec8-4e51-9ac4-f3e4e48e955e',	'45c7f8a1-81e5-448c-b093-9a7799f2a384',	'privateKey',	'MIIEpAIBAAKCAQEAjgLYDGXZxJBnQaX3sSPnDJOCK1KpErHqCW3sAzatrA98K9qIyU20oGO6MtrjAfNVBKa9RosAr1oQ3ZixWk4XDiipNnfF5mxtoir5NcBxkrALaUJ4Tu07HUYNFgLOZy2pvSNukablXH2K1JNNfoS/jabZNdzhgksoepAOgjtGzb8Kt6zx7+767ZXDxAYcAxfHGN0oaOR/I+U4lG4dwv3hWzXRVws4kLSmH47xtK0gT5duM4dHN7gouZj9zZgt69W4SLb7s7T6q3RmVEHbU/iHcX6Iwvaj+SHiZaAtLMCVvax+98CVYBM3kOSV8+awqUx3y07j6Sy2WZps2NEp7cZmkwIDAQABAoIBAC5IUP9hZmLAqVZk79/kkpWvsXnLG91C7MKb95JojJuKBG8KbxAkahznw7R1UAy/sFKuwPsvrE557Qs7i3mh/7OWnudoi6/4YJa8qLHTAXDMPTvV9P3MJTr6LKOi0IAiCrLgzFVd7lyBcfCr/VKhkwhoe1wpm7lqI0wmjQP2q7G7+EqnPM1YxscOUc0/9imT76+dmWxmkIfzpMZJqSy+v+Fn9DuHRIwQWzzBKWoGb6uvUBuIzsD/21pKUUKf57TluXjjHW0nFGm3cmpczPBYErFEOg5JVsQZUP2Oee2cL87sscNkeU+g/biph3pAFYBfTYFPxXVuv2qFiCzjse2DT0kCgYEAvySr7eFgZs5pLtPijrZy6OBoic8OReyp4BflL4qVtGGlQJLzSL07RY1HDg3VZ6DMNHsGkzceuLGsGXOC1/b6rbrsDr16leu9bvfXTw2fYcZVNhWeq7uwIlCqSlRBxCY81LSyAK1JHTy8l+cJoA0TiOokRYRtYpjL5Tb6CNUiWPUCgYEAvjJk04hAaDuK+E28suE0P5WzLw0XHT9Itn19Dy0CclJR5bClmwAJpOA68Vxp/C8iuQJoJzjXIpozildDxqKhH1p/1QwhtV3IP3TgTXsJZQhEMNXvHoRHM1ARWevf674kwkq/tRH5rnBOrh79sU6ztwHlUiOLGId35tQfFLZwrGcCgYA/+lZR2Q4bWxt5wRIkEo2DxRk4h0WPwhdaGqVrtEdHiSd25bjor+4W6aLO4XJ8rSWsq0EYJWrzBwVDv5YcE170p4w7otBVPgK6EdrPaw/d+jSrVbE2aiwaUEWve2RH80SJLbIm59mBH8NAVOSz3aycN0LMwfaa+enFXxFxw2UrwQKBgQCAc35e0mVVP+liOvYXBTuJOewS+c9DhGryPJwKdMZzOR9wdbXSkdrxjDKisVwu4hKcSoDXBG9fQi9O3hK7Azi3+SHeW9wkJhtIqAGYsmCs6EFNjh55SLLhd9DQHxpSl08yHNOOUX6EDftFY07IwbNkBJbAu5qETXFv5pC7J1+/PwKBgQC2vnf6oaZFicHQj0HdL+JcAWi2ri7hEzHcW9/jQ8jEfGS8ZpnI5UcpugclNfg9h+r5pEA+PJA1hL0a0kDuFIE5Rlh0Z+14E9wjcv8brK6jk8IsEUP2xyyx++g7soYYV50womtjAz5e1jcJAm0Ec/kLlHjyoK+Ri32fNoFGw3fvLA=='),
        ('8f1f8313-8a1b-4be2-9a0f-a0cdf15c664f',	'cfba7b9e-3bf2-4473-91a3-950bfcdf8bdf',	'keyUse',	'SIG'),
        ('422a579b-2254-4696-9e64-ffbe3027705a',	'cfba7b9e-3bf2-4473-91a3-950bfcdf8bdf',	'priority',	'100'),
        ('28b039ad-07fb-4139-aa42-4961ea084bba',	'cfba7b9e-3bf2-4473-91a3-950bfcdf8bdf',	'certificate',	'MIICqzCCAZMCBgGWeF19KjANBgkqhkiG9w0BAQsFADAZMRcwFQYDVQQDDA50ZW5hbnQtbWFuYWdlcjAeFw0yNTA0MjcxNzQ2NTRaFw0zNTA0MjcxNzQ4MzRaMBkxFzAVBgNVBAMMDnRlbmFudC1tYW5hZ2VyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsb5LqPwQeVvaSVOKvDWBMoqYq/qoHYNwNI51PpHxqticHQc01AMQ89MAGUIU+he5h1xPTMOV5vwgLIr6J5h9v54u9HSv+NG2m+yawOEp/U2llRs3Bw59AIw8HrkdeBm7qO1XQ7vYFJF+ph92xNC+jmw0oDPXwM3jaTWGFtNVWQq1eMFwDYWIhz4XizHKkuYiHo0FYtdBkzGTborLMfD5qAlaQw3y6gpwm0PNH6s5omzT/hO66qhAkf1vZlB03oFPkcSkO+4+jBIWNQvXz2al520rSf1jY8tZ6bl1vRrj5SyVPEj0yhp9uXZup26UgoswoutcVk8fqG6VH/nmcGdW3QIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQAteUby1B59DNdVJVMGAX+zjX8joW8n6vZSia7b1H6dYDm8u3om5MQTZAtYW9Oh75fAqkR6u+xbGcEOyRnDC96uEpLVYJqtQM1dfj8MTf5oNRvTIja3k4f9XQq4hvdIjDirOd/8HbXwFaieiVHuin/hX62UQ6l8wFFmrrpHaA8HIdVKjF+3YZYVxTyriqlBUOUhRpKbDOhAUfy6f13VhiOZLOActstGj83ySXCnWhJkVEnbxtZ5Se/PHKlZsAOGyc1Wsr1ygazb4KDqMJNT1TwMitmwIG8FfIEGNtftFmjEsUKcwOmZRGXgF+LlXJP0ttkHNUCQzxllqqtM2QA1RfdY'),
        ('14309b56-2b49-4f59-9fb3-0370c6fff038',	'cfba7b9e-3bf2-4473-91a3-950bfcdf8bdf',	'privateKey',	'MIIEowIBAAKCAQEAsb5LqPwQeVvaSVOKvDWBMoqYq/qoHYNwNI51PpHxqticHQc01AMQ89MAGUIU+he5h1xPTMOV5vwgLIr6J5h9v54u9HSv+NG2m+yawOEp/U2llRs3Bw59AIw8HrkdeBm7qO1XQ7vYFJF+ph92xNC+jmw0oDPXwM3jaTWGFtNVWQq1eMFwDYWIhz4XizHKkuYiHo0FYtdBkzGTborLMfD5qAlaQw3y6gpwm0PNH6s5omzT/hO66qhAkf1vZlB03oFPkcSkO+4+jBIWNQvXz2al520rSf1jY8tZ6bl1vRrj5SyVPEj0yhp9uXZup26UgoswoutcVk8fqG6VH/nmcGdW3QIDAQABAoIBAAqE1whwpm7wMypswcSiq/s4PZHL+0AH48+QcLrbu0AfopmLMCKt7cZoQdS81u34EnhCy8SgvJHXcPYB4Y0PVr3M2D2KXFGu4441wfDRQoFlo3uACVEnF9m88t/kl6xgZX05UpVFHCQXejXNneNG8UgPekvS1Z1o8eDrlHxsVFw5D0WR6CQnOpxTH9rJScN6A1XA0w/1BoatZB1LTYBuxxZiO8gNdsbx/jdsGi3puKeUKkJwWcXgXExcRQie+UV1fjsx+u+icz5+7OX3qoiyQuHAaHYL4XNL7h1ZhIPdDOWphENk6iXQBTK6IOfDuNkflhZhBwbiRrAtd1wZIdT+CqcCgYEA1vwMkEl2Dh9icDG2bauOWLtH6/i689zdRkEGznl9DLqIkZbOEA6bjioI2SiQ47/ki5pzSMwKm18gpPtu8WP4qcQI5PWPGWcxrkWWaY99ktGDA1Eg7GPzAqGqIbTbzK3hEqmoT/IA09nCNLupcG9xajYGsBTDSZHeADfrVXNWHlMCgYEA06dd7oOVDf02Kuc3zbi5psApSlaqlAbtP6Jy31UyLuwHnvjMjchn6QxjAuGf9Tvhw1l0aBJlZclQ7uzo+rAcwAc1rrurfN49etpVOF2gTIrB2JquwbSKl9DYpvE017z+WvFVMpMdMbIStBpk5fllZx3YRSmIkHS+WoVY3AckMA8CgYEAzRqo7dt4Kx8cLkfvcRA0fUkn4+RokJIN9aHCWaikeklYU/YZYE5fcDGAQZYnLXH9HIddJ32rPPJz1ZMgFGK5cVXa2n3AaY7/YfF3//vq5PDH2plDHOR6iu8rvn6rGMpnKgP81VDgIz3kv6Sukdtwy9EBbmpQqlblAzZWai7gYYsCgYAhRu890PJegLFDjZb4gVKvJQdY6Mdo/q3Ok3v8ISkCt5l7JSMEJ74upgPrMA42QHP+gtIKitnmZwCSkC0RPR9IEdoe3uLXfxmrdyhu9El/v1E6/Rb83aXsuKlXlhT41n4nTkpW6UptxdGq/3tdrRiEXezi8uTF0ZUcJlsSPQ0zgQKBgGBUVTfuXoDbPBOKi++78usqQFzYS2sP11CP97skxi9JHKTrnIoQv/6u5jg6XrBdIVi14PimIohmB/ZvGZFvRgtLPxZqrfvtrnnjBn17llCQ1f2E+ROB7wA+XSyYspcynX7hL5yM5jFN1cy5RS2tO0zqUHoPB9q+xUDu0yHLe6jW'),
        ('28a5bf83-1229-45c7-ab6c-f72af73263e1',	'98260899-2625-4667-97db-6a3900469453',	'secret',	'UOxA1cTrrEgX67BBc_Xt1w'),
        ('37be0b34-4741-4467-91dc-02102ead750b',	'98260899-2625-4667-97db-6a3900469453',	'kid',	'ff01e088-3abb-46e8-a657-f0cde6ed19d2'),
        ('0405ee53-8837-4daa-ae30-eeb7fcf53c66',	'98260899-2625-4667-97db-6a3900469453',	'priority',	'100'),
        ('c56b6c20-8e6a-4bcb-b491-a042b13e8b1f',	'97d7ca09-f545-4bda-b8ce-da04797c94e6',	'kid',	'9dcf1e74-87b9-49d0-a5b6-cc2d04f57522'),
        ('c16a5d09-6b89-4642-bcaa-9c9e41b4a3eb',	'97d7ca09-f545-4bda-b8ce-da04797c94e6',	'priority',	'100'),
        ('ff234d2f-88cc-417a-bd91-4913581d98ef',	'97d7ca09-f545-4bda-b8ce-da04797c94e6',	'secret',	'yL3j-F46YPAZimSe86fEXSU3aaSmlXgEABgCLp6b7D0lI0Y91Bxn9cPL_ZJIV9mEXp4MTHhgqa-ew-ZeAt4XoA'),
        ('89d0848d-f406-403a-af7e-8718338fa9d6',	'97d7ca09-f545-4bda-b8ce-da04797c94e6',	'algorithm',	'HS256'),
        ('75c8f6b0-ce26-4b62-93ba-999c9e0acb5c',	'c4213884-2a98-4dc4-9891-aae511b0c5c8',	'host-sending-registration-request-must-match',	'true'),
        ('6cf0790c-a7e6-4feb-90b6-b6cec6f92cc7',	'c4213884-2a98-4dc4-9891-aae511b0c5c8',	'client-uris-must-match',	'true'),
        ('08f03a46-bca7-4bce-8210-3106cb509523',	'6cf63e89-b6cd-44cd-9f4b-5ca2c2171229',	'max-clients',	'200'),
        ('c4e9d966-ea40-4717-a7b2-61f293a5a265',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('327c1cce-6880-41eb-bf5c-6723a59370f2',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('21686a1c-0171-42a3-923d-46d312e6506d',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('ffcd0de8-dc9d-4351-926c-163851cf8972',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('c015199b-07d9-4798-96ef-d7f14bb81494',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('88417f97-a885-4adc-80a6-8a8228db10e8',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('e03f8f06-b3f8-4fb8-b88b-e375f5fa41de',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('90780900-47bb-4058-a1bd-eb0b727208d5',	'8f73b2a3-9414-4552-add5-4f3aa67b4cce',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('d455720f-e22c-4097-9d01-c0eb1e29ae13',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('9bbb9931-160f-451c-ab50-b6b9c007acd8',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('59f7e255-d63f-45f4-b2cf-233823c9cc81',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('6d48e3d9-0440-4139-93ca-34614a4c7718',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('7b48e3ae-ba46-436b-8d58-d95e2d1b7298',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('ed70c0f5-b6fa-4750-a2a6-8cbb0fcce017',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('0a72417c-086d-452d-96cb-4974fdd91686',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('201cb492-fc65-4f6e-a36a-f5eb1d17ea4e',	'818c52bd-c7bd-4dea-9921-6735ed99780b',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('e62e2fdd-db22-4e3c-a727-6f7392286855',	'befbaf64-a5fd-4817-8eee-d7be429b802f',	'allow-default-scopes',	'true'),
        ('24f07557-2f3a-40b2-93f3-0eb78e51beb1',	'c515e23b-a04c-42ec-bf69-e10c5fe8ec84',	'allow-default-scopes',	'true'),
        ('81249d3f-b5a8-458b-9d33-c8ed8eeb5c05',	'd0d871fd-50f8-4585-85ad-97f22231c8ac',	'priority',	'100'),
        ('2f078db4-116f-4ce1-b436-281aa5b97146',	'd0d871fd-50f8-4585-85ad-97f22231c8ac',	'kid',	'aed6ebaf-d1ef-4312-a402-cc375bce7070'),
        ('b52ae5f4-152e-45cf-a221-1bb9c0b3a94d',	'd0d871fd-50f8-4585-85ad-97f22231c8ac',	'secret',	'FpzAg6wFiwN6kfPgRacBOHyfjpS1ZgltqPHxoWU0cluZ949cV3FW-sFcadEtcjndyWMvDpFewBohpIjaMHIQUw'),
        ('e779c2f4-9def-4516-acaa-fe70a3b2eda8',	'd0d871fd-50f8-4585-85ad-97f22231c8ac',	'algorithm',	'HS256'),
        ('d3a0f643-7a5f-4f6f-a1f9-42671d4c4ece',	'08bb2a76-373b-4e2a-a2e6-a2e7a387b261',	'kid',	'e755ded8-c920-439e-a388-9a6cc2be7499'),
        ('930e12ef-1fae-4291-8619-cda6344dfa8d',	'08bb2a76-373b-4e2a-a2e6-a2e7a387b261',	'priority',	'100'),
        ('3ddd2f52-bb40-4e8b-a78b-a8d69a079ab4',	'08bb2a76-373b-4e2a-a2e6-a2e7a387b261',	'secret',	'1RV6Q0YOjA4x6YM548hCtw'),
        ('03576e87-fc0d-4410-b785-3d8172158570',	'f34eafa4-6647-4d37-a205-f50eaa65f36d',	'certificate',	'MIICmzCCAYMCBgGWeHq0czANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZvcGVuazkwHhcNMjUwNDI3MTgxODQ5WhcNMzUwNDI3MTgyMDI5WjARMQ8wDQYDVQQDDAZvcGVuazkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDdwgNgN8lDPKZaq17pwWxnupbiCp5pvchK60+6SOLVMFrZhC7xo49DdhVobk+SDBs8HTUWuahtbp4mvQE0JLrNsXJzIgf/xlTsoSPPCfSthFSiXZvnxNxa/AiAY/G0VWlYJIrRtsyjOEu0SUk4MZrUBmK30LC4GpJXrIg9DXMxvsbkUxysOQgHupvFMYxhvC4dQkZVaFce2ZJ1Y38dtP32cIlgVAZUnmkrzccV9ouk7k0bu7UdI2af/tOuh6vKKqOv52ADgv3w328e2ZSctzppbaYRKW+5nZpJMlQvEBYGI60J1bQ5/SB2SkIV/vd+T29YCR6Cvjxm2fvRFr3l/Ru/AgMBAAEwDQYJKoZIhvcNAQELBQADggEBAFgEz8WB+I60KHZMxEO59BigUT+OB+BPF2HBENFrtlWN2Waaz6KH8EKGOlvXcqMCaj1seUT0JflN9RQ9bp0l9izP2NmR/jkAqhe+Vu0FDmDhVjlgn+O3Ql3N7aRxVtctgnrjKACpKAhS+a3zttNV7CvA3Xzmd0zXJ/zJkrUuYb2ditRGumKjUPrXyRenQb2gDzeLtJ4O+iUOTkjyEZQyaOdBqz4DiEnh9w6S9zFSzSM4KLWVZKyFoO7RxbBnQIDahlPovWiQUiK2JYoEjwUbQEG3e0pZhadSnJbFRjvz06ObKZZs3fgIGFEX+SHxrw/wvhqr7T5WchPUpgkA/nK0vfs='),
        ('3b9d7bbe-577d-43cf-8b88-39aba191bac1',	'f34eafa4-6647-4d37-a205-f50eaa65f36d',	'privateKey',	'MIIEpAIBAAKCAQEA3cIDYDfJQzymWqte6cFsZ7qW4gqeab3ISutPukji1TBa2YQu8aOPQ3YVaG5PkgwbPB01FrmobW6eJr0BNCS6zbFycyIH/8ZU7KEjzwn0rYRUol2b58TcWvwIgGPxtFVpWCSK0bbMozhLtElJODGa1AZit9CwuBqSV6yIPQ1zMb7G5FMcrDkIB7qbxTGMYbwuHUJGVWhXHtmSdWN/HbT99nCJYFQGVJ5pK83HFfaLpO5NG7u1HSNmn/7Troeryiqjr+dgA4L98N9vHtmUnLc6aW2mESlvuZ2aSTJULxAWBiOtCdW0Of0gdkpCFf73fk9vWAkegr48Ztn70Ra95f0bvwIDAQABAoIBAGiOBpBxjQOWD2sVfJEjAM0hZO1bi1gYwlxMeRFANy7D9zmyQH3um2f3v103EW4vJhIgRT0bOQWrj4Z9O5mNrcO4o9kT1QirxrOwwBToQUQkbdEF6LGMmEaPqSR7uuTHFn2X8RmyoeZc7xioKw0DGlhYUGgRTZjn8lLDBpMeic8MHq8zG0msbRl4MHuwG/7wVoeR5VU/N9j0SdkVZKqIzt2FBnPbx/H97y1nqrWhp2d6uivrwOWsgo4g3yAOQkH16MllPpxSS8umRpljpfK6acCP8IcLcdEYZuSb03s64YwGCGaHIVCfpslJHvlNYZXrhh6PzkRzkHT5gnK0dHovkmkCgYEA9Qc/L0qs4R3UkCC9JzM/NSK9T1CY4x3IIf+6QFUeLrkA/eyRjCD9NxWFDCSK3q6KtjlG1xTv8jsqbJlcaRqkn1dky13YGv7o90W/seNM1sdzG7Y5YUQQ9O5CxFIpEpbbaAU6G5K3GzHumP6OusP7/0UitHeDejsAnfu4vpYeKskCgYEA57AEk04Wq2YUNZ+FLdEziQeBsoOC4yHv+3YC36X7siUO6NyMsohirAz9XZC6eBdTHAOI4Yyxu0hdAavzj/+ss42DOyDEBJHqQQ3pS8DXC88AYn0BbD6CxkeH2sywn+79VlU+pRhjSr2jPocBIQhxjZvfBXeG/SOPb8j9hJLmTkcCgYEAxRBC/OHuEE9ReWkbN7+7ghyibHvBukdlghKN1NegIquf5JiypiSSAg9Ipe9t0JJH9S4zjx/DqulywUzGSbGn3I8I/ZZIC84fx19NQOAyGDrolM0FcPixOou1HveFGPJDG3G4vhxNL44E+v8gA41DypXrx4CnOVLiuCyO9Jj/QiECgYBKbGRL3T2y+Stsvj1NpnHjhqHG8i/NPk2UfSgXJ35ej0Dm1FHt15m6osGQmLkNcpCj4w7JRSSigHA/5Thr/TlAxChviertfOG+2/Ug/GUXyfrZqEYYu0vO3ZNqWW4zMdX3MZLZ/aeHjrstjrhyIAA5+OP/IZIfJpkg03N8bqPEaQKBgQCwNOTWU3BZJkwzuu30pu+l8tB0HEB/eL04kuoU3NTou6FjF5Hb2xKLEaNP/4Qk76anMZb3FqmPF/GQk76/nu0Zsv/77pUa4tmVNRgyQ7Z3MYmuM4oAYHYx+JiaGaS0Ga6AyaaeKBvrE/WoYBZW4O9I88lNyk/KxJZkOYGAiih5vg=='),
        ('078e7247-551e-48d7-a7dc-979db9944ba7',	'f34eafa4-6647-4d37-a205-f50eaa65f36d',	'priority',	'100'),
        ('18e77115-3dc4-44ec-9c65-c30817e179ad',	'f34eafa4-6647-4d37-a205-f50eaa65f36d',	'keyUse',	'SIG'),
        ('7548dfa7-dbc7-4b20-83ac-139053253a28',	'975ec143-0a21-4b05-8524-e5b1038201ff',	'keyUse',	'ENC'),
        ('2a84246a-5abb-4813-920e-e9f6f7f2df10',	'975ec143-0a21-4b05-8524-e5b1038201ff',	'certificate',	'MIICmzCCAYMCBgGWeHq1KDANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZvcGVuazkwHhcNMjUwNDI3MTgxODQ5WhcNMzUwNDI3MTgyMDI5WjARMQ8wDQYDVQQDDAZvcGVuazkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCOXN0LmI/tLNXa9BvHT3ot72VvXiuBrLh8NHqtAQjPgQ4mDfdK7z4V6wzZQ5a6qdnIO1F6gz+NYKDiZCKTa5Yk7eqzgNt2ARu4gOXPk5O29tZ8u64EQx2kI+BedC+rDwzdWk2w7LQSEZjfikv3TDkSd/s27NM9VhmZRRiX3iHiXMJiJkvSgEv6zzKFA+hDL0XmXBHROITHzNWmpNUQQZshCJS72n1a3x3K9kpBdwU8fXv+uPvZRa+LySCqpTVSz0YhhryapN4K2djTAS48nHWVVmv5nwtNQYoZVTpYZLezE2wIGYPs29bNke7J0RfFAm8vgYhLS9lm47X3MKKgS8txAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAIR7Y6J7WCjPm7cV2iYy7RNl0D+fQpNcXnLHoKLiH46KWTrUWXywyUDqJkKAPEihMnOaEDI9+51uUlw0HmCiLZL7ZTLjIxP8QvMhJGpoOACsh66jMr93/6i3rNgG1pRpWzcZuMIBVDGt+45VB6b6HgeuD/WuoSeKe6cYzYOaNJ2ol7QAPthIMkDDEwBO09Vfuv4mEOXQQ8NCwkbFZ8g5dcl+C4V/mvu0EHPcGEmDppBxslgBhPDD3YTeKYD4q7x2D5tlVK8hXiaYVICpk8nuAty5Ee8IDmnEMfuAnkpM9gIC4kFXNTe63279xVQI9M9fJlGDZBMJu+exeDUhCS5GeLg='),
        ('902a5efb-b84b-44ee-a627-b2910a48ef7a',	'975ec143-0a21-4b05-8524-e5b1038201ff',	'algorithm',	'RSA-OAEP'),
        ('7e847661-90e3-4692-b1f1-563b9ec02599',	'975ec143-0a21-4b05-8524-e5b1038201ff',	'priority',	'100'),
        ('7358047b-8063-46b7-9bbd-256c5dc7c689',	'975ec143-0a21-4b05-8524-e5b1038201ff',	'privateKey',	'MIIEogIBAAKCAQEAjlzdC5iP7SzV2vQbx096Le9lb14rgay4fDR6rQEIz4EOJg33Su8+FesM2UOWuqnZyDtReoM/jWCg4mQik2uWJO3qs4DbdgEbuIDlz5OTtvbWfLuuBEMdpCPgXnQvqw8M3VpNsOy0EhGY34pL90w5Enf7NuzTPVYZmUUYl94h4lzCYiZL0oBL+s8yhQPoQy9F5lwR0TiEx8zVpqTVEEGbIQiUu9p9Wt8dyvZKQXcFPH17/rj72UWvi8kgqqU1Us9GIYa8mqTeCtnY0wEuPJx1lVZr+Z8LTUGKGVU6WGS3sxNsCBmD7NvWzZHuydEXxQJvL4GIS0vZZuO19zCioEvLcQIDAQABAoIBAB8GuBoN3tg23EjCyMcQWDVopje2U08IoJBZM58+yXfHzZiXlJydNW+7cWpsY4489DzCSMilkBU0yPtuDWcJnIhh3bPPUoUOSsdUOEGcDSZj92zpzkwZgH1EYGLVeYoTVp9rTeFQRRJnCWU2k8XKs+mFgpd+167Xc+7Gc0yt2b6mW2Uj+mQ0Cl9IAOa73OuQi2FG24iaik5fm8jZFue5005do+v7mSSa7ralLQuJYl2xk7BXHJhxvdhSLOPCgj9y+Y3krkBq/sNYJisjIQ5JypjiFHZpfGBzEbMlGnxkFGgTNUXmLA56D2AjRF9z+cdsSF6TI1noj8Bh069+aWRMP1ECgYEAxkwbOYlBpX28i97GxeBTeiFfukfZVLp8szQ8CxrDgI/ExepqW+S5yIdcf6xlQvrbYyQzi1gTu3Vjy5Mi6ScB2BcSsuFB1uXkC1lDZcGT3dj1zKe6VUROOeoeE9KrczMom7/lMme6qPQu2xHa4weUDoOXU+ekbEIYV57s9Xnx70UCgYEAt8n80BLkqh8KOUR9IIhK0IW60T8GMxm8SBPv9CdqAtVbWqU2Lp0Xj+rBo/+UmfaSLJtPGMuklh0rpq4qfZrHuuqrvcfrFMvBx5wGAtBVc0kU+rhvIydgW0Zym+lr5VdvZ5uBHvt5y4kmfJyDx2HS0vi+T8HqLeAHpMvC07nnKD0CgYAguGItpkZcxlicQ9BMym0709H17PEhl/wHnfDibDIiPpbbiYivTRiSzDMpJgs3eClHPKv8rDXlUsN6zfaOvk8Blx0QwzaX/SsZ7ErAuaZqjuhQMsT+WN0HlWgNIVAFHEXxHUL2hEM6qcApidKyb4ewtWn9AFYTPvzBSIoVH2AaeQKBgGBX9zpQHSR8/wwENh++spFa+RLFmgdhn0ydfvUbUDxKAgijgKKw4PAlnPrOeOS41+mgRLd30UCL7B0FQer0H22BpSPoT/hn055C0PfapGC519CSrFfeRWmzpVLL6y6fRhwm7WoDY9ZpgA5kscPsKOtFT+ZZfdwsi3U9B1WcJ2v1AoGACrb1wT8UDM22fB12nbyC8aIWBau00lHttECEonfX0SVmPIPKKRbUVlAjVtrZbAF1t2o0blouCYvlm+0mplSmHI9yc0fbsGaNgTXM5e79MDMvnlpRrLKlK0tAyyuxzfVpA2bcfpIVKLEXH75RVmd7V+oHWdMxwm+3jF/t1mlXnxY='),
        ('8ee8ff01-7dab-49de-a1da-80128c2a5219',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('c9d093c0-d190-41af-9298-ff0c39e0ffd4',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('8d50347f-32c0-4637-bc3b-a7ba87b2448a',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('08c68f36-0213-4fa6-9f6f-8f248a4a2185',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('8065ac21-8f8d-4fa8-8936-c80a91bd9d13',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('97bc2a72-c81c-41ef-ba2e-26052202dfd0',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('bd28f6d6-85d5-4e4e-b64c-36ba32668194',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('0a6a4fd5-8f8c-4681-8514-3836e282f914',	'df5d4a21-1483-4b69-a6ee-c28f7d904ca0',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('29d90d60-b764-4605-a0fa-cd7f027e01b5',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('deef7775-099d-488d-bd9c-c5ee625c4137',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('767e113d-6a19-44a8-a144-11414bd38c4c',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('8729d5a8-3ba4-440a-88b6-3b881ac9a341',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('20b761bb-8342-4d5f-8c87-5c4b318939b7',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('fb6b27b6-0478-4747-814d-696d6a96ff6d',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('b89aede1-4648-4d53-87cf-d71246ae06a9',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('3c674d50-76a2-4fca-a4fe-454ad3243ae4',	'6070f9e1-b261-4dc3-83ff-9c85c0bc1463',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('23632981-d759-4b7f-932a-61279cd53a46',	'02236132-fbe4-433b-980b-c9078c9b8392',	'allow-default-scopes',	'true'),
        ('acbec817-ba24-4415-8f07-e9086d0aaf64',	'b73fdf13-c7e1-47d7-be39-f0bdc91dcc91',	'max-clients',	'200'),
        ('acd3ed4c-0905-4d88-b988-674efbc8d8a2',	'a317dd75-9a40-4808-8a90-31fe1b25f537',	'allow-default-scopes',	'true'),
        ('835c1094-900f-47d1-b395-6170d41025ff',	'e797bd57-3010-47e7-8434-b3cd81121beb',	'client-uris-must-match',	'true');

        CREATE TABLE IF NOT EXISTS "public"."composite_role" (
            "composite" character varying(36) NOT NULL,
            "child_role" character varying(36) NOT NULL,
            CONSTRAINT "constraint_composite_role" PRIMARY KEY ("composite", "child_role")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_composite ON public.composite_role USING btree (composite);

        CREATE INDEX IF NOT EXISTS idx_composite_child ON public.composite_role USING btree (child_role);

        INSERT INTO "composite_role" ("composite", "child_role") VALUES
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'e125e84c-897d-46ff-b5ed-33ec793d902d'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'a6ba66ca-d9d5-4302-82d9-0c067a108565'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'5c00def1-c865-41bf-81eb-2212799fdb30'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'66d6b367-fdff-4a66-ac59-ebdac1f900c4'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'47456962-7657-4cc8-8c4c-68b128e3946c'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'a9c6d599-eaf5-4893-b2a0-6d9f7a752a7c'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'b1d68eb5-5a6d-47b9-9d92-59a02a4d3830'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'b7b28cd9-7bc5-4f5a-a64c-4e9552f481a7'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'1bf18d6f-7da7-4af8-95e5-bb12f75624c9'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'b4c84aa6-124c-41e8-ad7c-87198a066b12'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2f1b03eb-76cb-4af1-bf58-69adf89d64f6'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'fda0eb40-ae17-4f0e-a537-16a1eb749e86'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'bd764018-ee73-43d3-94ac-80bfd3f3e77e'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'c4c9e69d-6057-489b-92f0-901366a20bf6'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'10699d5c-3908-47ad-9a8a-01b060d1123c'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'bf40eff7-b00c-4738-ad8e-88159580d140'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'd8859617-a768-42f8-accb-a15f8c0d7b9e'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'3ffc9916-dd66-4021-96d1-ff410971d43e'),
        ('0eb7f97a-bb13-4666-8ef2-fe28d37dff01',	'be1d9e4e-cee0-4d0a-9e31-6207618d1148'),
        ('47456962-7657-4cc8-8c4c-68b128e3946c',	'bf40eff7-b00c-4738-ad8e-88159580d140'),
        ('66d6b367-fdff-4a66-ac59-ebdac1f900c4',	'3ffc9916-dd66-4021-96d1-ff410971d43e'),
        ('66d6b367-fdff-4a66-ac59-ebdac1f900c4',	'10699d5c-3908-47ad-9a8a-01b060d1123c'),
        ('0eb7f97a-bb13-4666-8ef2-fe28d37dff01',	'676508a8-16d1-4519-a907-6e46d58a2559'),
        ('676508a8-16d1-4519-a907-6e46d58a2559',	'fa45ec91-9633-4f9b-8fb9-02763b089cc9'),
        ('5cf941ad-d7f2-4edd-a6be-902a740b4a44',	'1b5e021c-10c0-4910-a813-9b29a2c37405'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'b527daac-764b-4c74-9abd-744050454400'),
        ('0eb7f97a-bb13-4666-8ef2-fe28d37dff01',	'd2ad7d1b-7432-4d63-af55-e8dcbfdfb525'),
        ('0eb7f97a-bb13-4666-8ef2-fe28d37dff01',	'0fb27c2d-d533-4f92-9ae6-9c90e08b8d66'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'999b4793-f9cb-4f87-85df-268540465280'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'a9a5d436-92cd-49c9-a1a2-2dc1aeb2286d'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'324f0087-cfc8-4735-8047-a6d095e9b29b'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'212de463-3a92-4524-9460-ab727300ff8b'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'6d765ab0-6165-4363-9550-0f987614cb2b'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'da91fc7d-4291-4122-99ec-545d59ddef2e'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'c82615ec-4565-4d5f-90b8-94cd738bd52f'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'518b47ba-5111-4c11-bebe-bcbca103b716'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'f23e7e75-ecd4-4dba-9bba-42383fa7c775'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2fa9ae60-e0f9-4c97-82e4-d4cacd65b10c'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'4d30f7f1-ba8a-4a17-b714-eeece44888a1'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'14b94361-4613-412f-b03d-24836f38920a'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'baed571a-2c21-4a14-b504-61f0f2d4f015'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'52f26c30-0d74-4476-a0a5-84ab09ebac02'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'f376db7d-a9c1-407c-a73c-2ea9a9a6c15c'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'd909e0e4-def4-414f-a03e-39e777a41e0d'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'1ea09efe-60c8-4b18-a22c-d2d6d322fadd'),
        ('212de463-3a92-4524-9460-ab727300ff8b',	'f376db7d-a9c1-407c-a73c-2ea9a9a6c15c'),
        ('324f0087-cfc8-4735-8047-a6d095e9b29b',	'1ea09efe-60c8-4b18-a22c-d2d6d322fadd'),
        ('324f0087-cfc8-4735-8047-a6d095e9b29b',	'52f26c30-0d74-4476-a0a5-84ab09ebac02'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'ad11d1be-3942-4c84-b527-a2b90ceef718'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'6bcf9972-083a-4efc-80ab-8a83999bdca1'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'a50978fc-1613-4c85-990c-6557a8c9cf02'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'688e7e16-4e9a-446a-8522-b62e1ee22de7'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'94ecb7ae-c6ad-4610-8d08-2b4cd6486b21'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'27c6c26d-8e5d-4114-ac16-7dbc409cd965'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'c1eb3d87-b95a-4660-9855-97056b9831e4'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'87e75ce2-7a44-4af9-91ea-269ac58bd962'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'a4b47ba4-2270-4260-bbe1-5b7e21d35034'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'bb0c4911-333f-47c4-b61d-f7eb6f096640'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'54e66818-e3ba-4a21-bc05-7d362eaa2adf'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'54dd9e72-9c27-4604-af1a-3acb3ad72f3a'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'328c67f2-1ac3-44e2-92ba-b2c4769310ea'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'b6f9187e-4ccc-4d14-8b63-a38d0c5854df'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'ee13eb37-882f-4f0f-85ce-fc19b5352c1b'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'07425b79-6744-424a-8032-b77c42a63d28'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'd375b2e2-e95c-4ce9-bc35-8e92a234fc89'),
        ('688e7e16-4e9a-446a-8522-b62e1ee22de7',	'ee13eb37-882f-4f0f-85ce-fc19b5352c1b'),
        ('6899cefd-5fbb-41e2-82c4-919f0a34f170',	'3bb8c51e-1865-4bfc-bdd9-31b359c435a1'),
        ('a50978fc-1613-4c85-990c-6557a8c9cf02',	'd375b2e2-e95c-4ce9-bc35-8e92a234fc89'),
        ('a50978fc-1613-4c85-990c-6557a8c9cf02',	'b6f9187e-4ccc-4d14-8b63-a38d0c5854df'),
        ('6899cefd-5fbb-41e2-82c4-919f0a34f170',	'0b456db2-a080-48c6-86c7-98bbee5426a8'),
        ('0b456db2-a080-48c6-86c7-98bbee5426a8',	'9bfb3589-4488-4c0f-90d4-00306c5d301e'),
        ('5732fe4c-ea34-495e-8c03-c962ac389036',	'eea61099-44d6-4651-b997-463a869c7fd8'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2233e0a8-104e-4032-82ff-9153aab813ca'),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'befa7bcc-0949-4f13-81b5-23ca31bf5d99'),
        ('6899cefd-5fbb-41e2-82c4-919f0a34f170',	'd8af271a-f459-408d-8c70-f793a831b551'),
        ('6899cefd-5fbb-41e2-82c4-919f0a34f170',	'4456adca-b603-436d-8664-8ec24a0ca788'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'e22a2f5f-b2e2-4f3c-9470-6d20b583a734'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'c821717f-ac1a-4460-a2d8-b0120c2aeaab'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'7a50bc1e-7913-4785-8155-bafb0301f7bf'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'1b7c75ab-36aa-456f-90d1-86a7128c3fe9'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'bbcd6f9e-3fcc-4914-a4c9-d89226874f84'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2862ecff-b7e7-41b7-b28e-8f560cfd39f4'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2d3e81af-f0ee-4686-85ec-0aa19949c09a'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'6251e530-7e1f-41ef-b7ef-f7f7331c47c5'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'd177f479-d57f-44f0-aa00-772631313f70'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2315d12c-fe68-4ee5-b603-a7b2df33cb12'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'9e5daa7b-20b9-4043-ac83-4bef3d0c1b96'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'e90a49c9-9f13-40ec-902f-f1d5f61a3768'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'2ce2e238-e733-4cdb-951e-e053786fc242'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'dbaeb2c9-a2d0-42a6-92fa-59368a3ba5f4'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'ccbb8eca-4683-4112-8db8-ca7e4a0512d6'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'6536f0e5-d16d-4880-aa23-3e70af704ba1'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'7cb7caf1-2b3f-4adb-adac-154a76092738'),
        ('1b7c75ab-36aa-456f-90d1-86a7128c3fe9',	'ccbb8eca-4683-4112-8db8-ca7e4a0512d6'),
        ('7a50bc1e-7913-4785-8155-bafb0301f7bf',	'7cb7caf1-2b3f-4adb-adac-154a76092738'),
        ('7a50bc1e-7913-4785-8155-bafb0301f7bf',	'dbaeb2c9-a2d0-42a6-92fa-59368a3ba5f4'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'f0c698c9-3bcd-49c7-8e92-5d8a4ef604d1'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'b107c5b1-95a9-4c10-85ca-ad3a7382e870'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'94e5cad9-1bbe-43f7-a6fd-b91b630bc386'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'781e59ea-8349-4703-9ed2-113f9d0d29d9'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'c604374a-40b8-4626-97d7-3b8d57568883'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'ab795987-83dc-49bf-b479-dcd0dbf7265c'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'd9a55160-5001-4e8b-a62f-1e3bd8fa51a5'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'0eadf679-cf48-4539-ab84-805c52a24584'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'd9ac6cef-508b-422e-ab6a-ad3bb1323b2c'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'bda818f6-e636-4de8-bf71-7ebfd16be794'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'eddccae8-6d80-4403-83f5-bdf09734d810'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'a87730cf-8933-4fcb-ab14-94fa6253a6e3'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'7c449f1b-8471-4243-97e3-421906393359'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'6e82f7ae-2a57-4848-986a-ee215f54329e'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'4394521e-5dfc-4f68-9085-8a86eb431a86'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'2c100cbc-5a4d-4294-ad62-119611edcfcd'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'4ac18c0b-be91-4d1b-89db-656ac74c7d11'),
        ('781e59ea-8349-4703-9ed2-113f9d0d29d9',	'4394521e-5dfc-4f68-9085-8a86eb431a86'),
        ('94e5cad9-1bbe-43f7-a6fd-b91b630bc386',	'6e82f7ae-2a57-4848-986a-ee215f54329e'),
        ('94e5cad9-1bbe-43f7-a6fd-b91b630bc386',	'4ac18c0b-be91-4d1b-89db-656ac74c7d11'),
        ('f749b549-08fa-48ce-87f8-9df49f4c2bfa',	'f28b4a92-c2d6-496a-895f-c67ff64fb799'),
        ('f749b549-08fa-48ce-87f8-9df49f4c2bfa',	'1ed0867f-b4e2-4d27-9aca-d0b002079bd2'),
        ('1ed0867f-b4e2-4d27-9aca-d0b002079bd2',	'364acfb8-77f8-4c33-8489-60a2204c0a12'),
        ('03e240bc-ffd1-4ba1-ab44-b2ad9a4ea6dd',	'672b4e51-e3cf-4adc-80ad-0223deb8fd85'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'3653fa78-5e9e-429d-b31a-0fbc240f9f09'),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'30e7a1f6-bca0-4b4e-a280-f684d9747120'),
        ('f749b549-08fa-48ce-87f8-9df49f4c2bfa',	'95ec85b4-d1bf-4a2f-8c35-35c464da6f05'),
        ('f749b549-08fa-48ce-87f8-9df49f4c2bfa',	'fc98bf35-cc28-4be1-9fb3-879724b168da');

        CREATE TABLE IF NOT EXISTS "public"."credential" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_user_credential ON public.credential USING btree (user_id);

        INSERT INTO "credential" ("id", "salt", "type", "user_id", "created_date", "user_label", "secret_data", "credential_data", "priority") VALUES
        ('9a7ce342-7021-4f7e-983d-cc6dafa136e2',	NULL,	'password',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3',	1745776091399,	NULL,	'{"value":"DBz3fR3+04ZoZAE1JreJX7ATWw0JCSXn8devkXprvmAndpEdlug/jQKxzpv0/54pgDFCaUrqUyGUv0uorEn14w==","salt":"dyZnZwe260BpDneFxEXtzA==","additionalParameters":{}}',	'{"hashIterations":27500,"algorithm":"pbkdf2-sha256","additionalParameters":{}}',	10),
        ('a38873df-397d-4f87-821b-9142cbae681b',	NULL,	'password',	'56e6c86d-b39a-4eca-9f06-2e2df17e82c9',	1745776156996,	'My password',	'{"value":"WRbBcK3dwYLP1JJckXBHAn8R6NXU+/IN+4jBRfHfkiUblaXafmSg9y8yF0v5v2peaSUWMYSk0rG8WFBL1R5nFA==","salt":"P0hjgIhjcqA3EJzCVKeRUQ==","additionalParameters":{}}',	'{"hashIterations":27500,"algorithm":"pbkdf2-sha256","additionalParameters":{}}',	10),
        ('b5ff3e8a-b8a6-4977-92af-812fa4d8b020',	NULL,	'password',	'9edce24d-8235-4740-8bd4-c33679eacf88',	1745778098417,	'My password',	'{"value":"mP1UbsdGVC6n3hO0g+u7Ku732PjZcofn6ygeBgOjNyM3gnmodUJYnpL5qDoxwsxJADi26R/9IkEcFNdG6wnPzQ==","salt":"lIU2hcff/LBllK6zVTOB+w==","additionalParameters":{}}',	'{"hashIterations":27500,"algorithm":"pbkdf2-sha256","additionalParameters":{}}',	10);

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
        ('1.0.0.Final-KEYCLOAK-5461',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.0.0.Final.xml',	'2025-04-27 17:48:09.019746',	1,	'EXECUTED',	'8:bda77d94bf90182a1e30c24f1c155ec7',	'createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.0.0.Final-KEYCLOAK-5461',	'sthorger@redhat.com',	'META-INF/db2-jpa-changelog-1.0.0.Final.xml',	'2025-04-27 17:48:09.024348',	2,	'MARK_RAN',	'8:1ecb330f30986693d1cba9ab579fa219',	'createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.1.0.Beta1',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.1.0.Beta1.xml',	'2025-04-27 17:48:09.051605',	3,	'EXECUTED',	'8:cb7ace19bc6d959f305605d255d4c843',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=CLIENT_ATTRIBUTES; createTable tableName=CLIENT_SESSION_NOTE; createTable tableName=APP_NODE_REGISTRATIONS; addColumn table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.1.0.Final',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.1.0.Final.xml',	'2025-04-27 17:48:09.054317',	4,	'EXECUTED',	'8:80230013e961310e6872e871be424a63',	'renameColumn newColumnName=EVENT_TIME, oldColumnName=TIME, tableName=EVENT_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.2.0.Beta1',	'psilva@redhat.com',	'META-INF/jpa-changelog-1.2.0.Beta1.xml',	'2025-04-27 17:48:09.117025',	5,	'EXECUTED',	'8:67f4c20929126adc0c8e9bf48279d244',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.2.0.Beta1',	'psilva@redhat.com',	'META-INF/db2-jpa-changelog-1.2.0.Beta1.xml',	'2025-04-27 17:48:09.118897',	6,	'MARK_RAN',	'8:7311018b0b8179ce14628ab412bb6783',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.2.0.RC1',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.2.0.CR1.xml',	'2025-04-27 17:48:09.181514',	7,	'EXECUTED',	'8:037ba1216c3640f8785ee6b8e7c8e3c1',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.2.0.RC1',	'bburke@redhat.com',	'META-INF/db2-jpa-changelog-1.2.0.CR1.xml',	'2025-04-27 17:48:09.183607',	8,	'MARK_RAN',	'8:7fe6ffe4af4df289b3157de32c624263',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.2.0.Final',	'keycloak',	'META-INF/jpa-changelog-1.2.0.Final.xml',	'2025-04-27 17:48:09.186739',	9,	'EXECUTED',	'8:9c136bc3187083a98745c7d03bc8a303',	'update tableName=CLIENT; update tableName=CLIENT; update tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.3.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.3.0.xml',	'2025-04-27 17:48:09.25268',	10,	'EXECUTED',	'8:b5f09474dca81fb56a97cf5b6553d331',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=ADMI...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.4.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.4.0.xml',	'2025-04-27 17:48:09.294873',	11,	'EXECUTED',	'8:ca924f31bd2a3b219fdcfe78c82dacf4',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.4.0',	'bburke@redhat.com',	'META-INF/db2-jpa-changelog-1.4.0.xml',	'2025-04-27 17:48:09.297221',	12,	'MARK_RAN',	'8:8acad7483e106416bcfa6f3b824a16cd',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.5.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.5.0.xml',	'2025-04-27 17:48:09.308957',	13,	'EXECUTED',	'8:9b1266d17f4f87c78226f5055408fd5e',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.6.1_from15',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-04-27 17:48:09.32574',	14,	'EXECUTED',	'8:d80ec4ab6dbfe573550ff72396c7e910',	'addColumn tableName=REALM; addColumn tableName=KEYCLOAK_ROLE; addColumn tableName=CLIENT; createTable tableName=OFFLINE_USER_SESSION; createTable tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_US_SES_PK2, tableName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.6.1_from16-pre',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-04-27 17:48:09.327696',	15,	'MARK_RAN',	'8:d86eb172171e7c20b9c849b584d147b2',	'delete tableName=OFFLINE_CLIENT_SESSION; delete tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.6.1_from16',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-04-27 17:48:09.329242',	16,	'MARK_RAN',	'8:5735f46f0fa60689deb0ecdc2a0dea22',	'dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_US_SES_PK, tableName=OFFLINE_USER_SESSION; dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_CL_SES_PK, tableName=OFFLINE_CLIENT_SESSION; addColumn tableName=OFFLINE_USER_SESSION; update tableName=OF...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.6.1',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-04-27 17:48:09.330808',	17,	'EXECUTED',	'8:d41d8cd98f00b204e9800998ecf8427e',	'empty',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.7.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.7.0.xml',	'2025-04-27 17:48:09.366431',	18,	'EXECUTED',	'8:5c1a8fd2014ac7fc43b90a700f117b23',	'createTable tableName=KEYCLOAK_GROUP; createTable tableName=GROUP_ROLE_MAPPING; createTable tableName=GROUP_ATTRIBUTE; createTable tableName=USER_GROUP_MEMBERSHIP; createTable tableName=REALM_DEFAULT_GROUPS; addColumn tableName=IDENTITY_PROVIDER; ...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.8.0',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.8.0.xml',	'2025-04-27 17:48:09.406777',	19,	'EXECUTED',	'8:1f6c2c2dfc362aff4ed75b3f0ef6b331',	'addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.8.0-2',	'keycloak',	'META-INF/jpa-changelog-1.8.0.xml',	'2025-04-27 17:48:09.411463',	20,	'EXECUTED',	'8:dee9246280915712591f83a127665107',	'dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part1',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-04-27 17:48:09.760042',	45,	'EXECUTED',	'8:a164ae073c56ffdbc98a615493609a52',	'addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_RESOURCE; addColumn tableName=RESOURCE_SERVER_SCOPE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.8.0',	'mposolda@redhat.com',	'META-INF/db2-jpa-changelog-1.8.0.xml',	'2025-04-27 17:48:09.413424',	21,	'MARK_RAN',	'8:9eb2ee1fa8ad1c5e426421a6f8fdfa6a',	'addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.8.0-2',	'keycloak',	'META-INF/db2-jpa-changelog-1.8.0.xml',	'2025-04-27 17:48:09.415158',	22,	'MARK_RAN',	'8:dee9246280915712591f83a127665107',	'dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.9.0',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.9.0.xml',	'2025-04-27 17:48:09.429134',	23,	'EXECUTED',	'8:d9fa18ffa355320395b86270680dd4fe',	'update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=REALM; update tableName=REALM; customChange; dr...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.9.1',	'keycloak',	'META-INF/jpa-changelog-1.9.1.xml',	'2025-04-27 17:48:09.43335',	24,	'EXECUTED',	'8:90cff506fedb06141ffc1c71c4a1214c',	'modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=PUBLIC_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.9.1',	'keycloak',	'META-INF/db2-jpa-changelog-1.9.1.xml',	'2025-04-27 17:48:09.435097',	25,	'MARK_RAN',	'8:11a788aed4961d6d29c427c063af828c',	'modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('1.9.2',	'keycloak',	'META-INF/jpa-changelog-1.9.2.xml',	'2025-04-27 17:48:09.462309',	26,	'EXECUTED',	'8:a4218e51e1faf380518cce2af5d39b43',	'createIndex indexName=IDX_USER_EMAIL, tableName=USER_ENTITY; createIndex indexName=IDX_USER_ROLE_MAPPING, tableName=USER_ROLE_MAPPING; createIndex indexName=IDX_USER_GROUP_MAPPING, tableName=USER_GROUP_MEMBERSHIP; createIndex indexName=IDX_USER_CO...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-2.0.0',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-2.0.0.xml',	'2025-04-27 17:48:09.531242',	27,	'EXECUTED',	'8:d9e9a1bfaa644da9952456050f07bbdc',	'createTable tableName=RESOURCE_SERVER; addPrimaryKey constraintName=CONSTRAINT_FARS, tableName=RESOURCE_SERVER; addUniqueConstraint constraintName=UK_AU8TT6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER; createTable tableName=RESOURCE_SERVER_RESOU...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-2.5.1',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-2.5.1.xml',	'2025-04-27 17:48:09.533898',	28,	'EXECUTED',	'8:d1bf991a6163c0acbfe664b615314505',	'update tableName=RESOURCE_SERVER_POLICY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.1.0-KEYCLOAK-5461',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.1.0.xml',	'2025-04-27 17:48:09.580757',	29,	'EXECUTED',	'8:88a743a1e87ec5e30bf603da68058a8c',	'createTable tableName=BROKER_LINK; createTable tableName=FED_USER_ATTRIBUTE; createTable tableName=FED_USER_CONSENT; createTable tableName=FED_USER_CONSENT_ROLE; createTable tableName=FED_USER_CONSENT_PROT_MAPPER; createTable tableName=FED_USER_CR...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.2.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.2.0.xml',	'2025-04-27 17:48:09.590401',	30,	'EXECUTED',	'8:c5517863c875d325dea463d00ec26d7a',	'addColumn tableName=ADMIN_EVENT_ENTITY; createTable tableName=CREDENTIAL_ATTRIBUTE; createTable tableName=FED_CREDENTIAL_ATTRIBUTE; modifyDataType columnName=VALUE, tableName=CREDENTIAL; addForeignKeyConstraint baseTableName=FED_CREDENTIAL_ATTRIBU...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.3.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.3.0.xml',	'2025-04-27 17:48:09.602843',	31,	'EXECUTED',	'8:ada8b4833b74a498f376d7136bc7d327',	'createTable tableName=FEDERATED_USER; addPrimaryKey constraintName=CONSTR_FEDERATED_USER, tableName=FEDERATED_USER; dropDefaultValue columnName=TOTP, tableName=USER_ENTITY; dropColumn columnName=TOTP, tableName=USER_ENTITY; addColumn tableName=IDE...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.4.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.4.0.xml',	'2025-04-27 17:48:09.605272',	32,	'EXECUTED',	'8:b9b73c8ea7299457f99fcbb825c263ba',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.5.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-04-27 17:48:09.609172',	33,	'EXECUTED',	'8:07724333e625ccfcfc5adc63d57314f3',	'customChange; modifyDataType columnName=USER_ID, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.5.0-unicode-oracle',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-04-27 17:48:09.610597',	34,	'MARK_RAN',	'8:8b6fd445958882efe55deb26fc541a7b',	'modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.5.0-unicode-other-dbs',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-04-27 17:48:09.628855',	35,	'EXECUTED',	'8:29b29cfebfd12600897680147277a9d7',	'modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.5.0-duplicate-email-support',	'slawomir@dabek.name',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-04-27 17:48:09.632906',	36,	'EXECUTED',	'8:73ad77ca8fd0410c7f9f15a471fa52bc',	'addColumn tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.5.0-unique-group-names',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-04-27 17:48:09.637184',	37,	'EXECUTED',	'8:64f27a6fdcad57f6f9153210f2ec1bdb',	'addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('2.5.1',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.5.1.xml',	'2025-04-27 17:48:09.640358',	38,	'EXECUTED',	'8:27180251182e6c31846c2ddab4bc5781',	'addColumn tableName=FED_USER_CONSENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.0.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-3.0.0.xml',	'2025-04-27 17:48:09.643242',	39,	'EXECUTED',	'8:d56f201bfcfa7a1413eb3e9bc02978f9',	'addColumn tableName=IDENTITY_PROVIDER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.2.0-fix',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-04-27 17:48:09.644726',	40,	'MARK_RAN',	'8:91f5522bf6afdc2077dfab57fbd3455c',	'addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.2.0-fix-with-keycloak-5416',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-04-27 17:48:09.646138',	41,	'MARK_RAN',	'8:0f01b554f256c22caeb7d8aee3a1cdc8',	'dropIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS; addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS; createIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.2.0-fix-offline-sessions',	'hmlnarik',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-04-27 17:48:09.648722',	42,	'EXECUTED',	'8:ab91cf9cee415867ade0e2df9651a947',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.2.0-fixed',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-04-27 17:48:09.752496',	43,	'EXECUTED',	'8:ceac9b1889e97d602caf373eadb0d4b7',	'addColumn tableName=REALM; dropPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_PK2, tableName=OFFLINE_CLIENT_SESSION; dropColumn columnName=CLIENT_SESSION_ID, tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_P...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.3.0',	'keycloak',	'META-INF/jpa-changelog-3.3.0.xml',	'2025-04-27 17:48:09.755645',	44,	'EXECUTED',	'8:84b986e628fe8f7fd8fd3c275c5259f2',	'addColumn tableName=USER_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part2-KEYCLOAK-6095',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-04-27 17:48:09.762908',	46,	'EXECUTED',	'8:70a2b4f1f4bd4dbf487114bdb1810e64',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part3-fixed',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-04-27 17:48:09.764141',	47,	'MARK_RAN',	'8:7be68b71d2f5b94b8df2e824f2860fa2',	'dropIndex indexName=IDX_RES_SERV_POL_RES_SERV, tableName=RESOURCE_SERVER_POLICY; dropIndex indexName=IDX_RES_SRV_RES_RES_SRV, tableName=RESOURCE_SERVER_RESOURCE; dropIndex indexName=IDX_RES_SRV_SCOPE_RES_SRV, tableName=RESOURCE_SERVER_SCOPE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part3-fixed-nodropindex',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-04-27 17:48:09.791373',	48,	'EXECUTED',	'8:bab7c631093c3861d6cf6144cd944982',	'addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_POLICY; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_RESOURCE; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, ...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authn-3.4.0.CR1-refresh-token-max-reuse',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-04-27 17:48:09.794103',	49,	'EXECUTED',	'8:fa809ac11877d74d76fe40869916daad',	'addColumn tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.4.0',	'keycloak',	'META-INF/jpa-changelog-3.4.0.xml',	'2025-04-27 17:48:09.829606',	50,	'EXECUTED',	'8:fac23540a40208f5f5e326f6ceb4d291',	'addPrimaryKey constraintName=CONSTRAINT_REALM_DEFAULT_ROLES, tableName=REALM_DEFAULT_ROLES; addPrimaryKey constraintName=CONSTRAINT_COMPOSITE_ROLE, tableName=COMPOSITE_ROLE; addPrimaryKey constraintName=CONSTR_REALM_DEFAULT_GROUPS, tableName=REALM...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.4.0-KEYCLOAK-5230',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-3.4.0.xml',	'2025-04-27 17:48:09.854741',	51,	'EXECUTED',	'8:2612d1b8a97e2b5588c346e817307593',	'createIndex indexName=IDX_FU_ATTRIBUTE, tableName=FED_USER_ATTRIBUTE; createIndex indexName=IDX_FU_CONSENT, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CONSENT_RU, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CREDENTIAL, t...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.4.1',	'psilva@redhat.com',	'META-INF/jpa-changelog-3.4.1.xml',	'2025-04-27 17:48:09.857974',	52,	'EXECUTED',	'8:9842f155c5db2206c88bcb5d1046e941',	'modifyDataType columnName=VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.4.2',	'keycloak',	'META-INF/jpa-changelog-3.4.2.xml',	'2025-04-27 17:48:09.860317',	53,	'EXECUTED',	'8:2e12e06e45498406db72d5b3da5bbc76',	'update tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('3.4.2-KEYCLOAK-5172',	'mkanis@redhat.com',	'META-INF/jpa-changelog-3.4.2.xml',	'2025-04-27 17:48:09.862104',	54,	'EXECUTED',	'8:33560e7c7989250c40da3abdabdc75a4',	'update tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.0.0-KEYCLOAK-6335',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-04-27 17:48:09.867851',	55,	'EXECUTED',	'8:87a8d8542046817a9107c7eb9cbad1cd',	'createTable tableName=CLIENT_AUTH_FLOW_BINDINGS; addPrimaryKey constraintName=C_CLI_FLOW_BIND, tableName=CLIENT_AUTH_FLOW_BINDINGS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.0.0-CLEANUP-UNUSED-TABLE',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-04-27 17:48:09.87099',	56,	'EXECUTED',	'8:3ea08490a70215ed0088c273d776311e',	'dropTable tableName=CLIENT_IDENTITY_PROV_MAPPING',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.0.0-KEYCLOAK-6228',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-04-27 17:48:09.886418',	57,	'EXECUTED',	'8:2d56697c8723d4592ab608ce14b6ed68',	'dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; dropNotNullConstraint columnName=CLIENT_ID, tableName=USER_CONSENT; addColumn tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHO...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.0.0-KEYCLOAK-5579-fixed',	'mposolda@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-04-27 17:48:09.952728',	58,	'EXECUTED',	'8:3e423e249f6068ea2bbe48bf907f9d86',	'dropForeignKeyConstraint baseTableName=CLIENT_TEMPLATE_ATTRIBUTES, constraintName=FK_CL_TEMPL_ATTR_TEMPL; renameTable newTableName=CLIENT_SCOPE_ATTRIBUTES, oldTableName=CLIENT_TEMPLATE_ATTRIBUTES; renameColumn newColumnName=SCOPE_ID, oldColumnName...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-4.0.0.CR1',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-4.0.0.CR1.xml',	'2025-04-27 17:48:09.970657',	59,	'EXECUTED',	'8:15cabee5e5df0ff099510a0fc03e4103',	'createTable tableName=RESOURCE_SERVER_PERM_TICKET; addPrimaryKey constraintName=CONSTRAINT_FAPMT, tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRHO213XCX4WNKOG82SSPMT...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-4.0.0.Beta3',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-4.0.0.Beta3.xml',	'2025-04-27 17:48:09.974303',	60,	'EXECUTED',	'8:4b80200af916ac54d2ffbfc47918ab0e',	'addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRPO2128CX4WNKOG82SSRFY, referencedTableName=RESOURCE_SERVER_POLICY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-4.2.0.Final',	'mhajas@redhat.com',	'META-INF/jpa-changelog-authz-4.2.0.Final.xml',	'2025-04-27 17:48:09.979242',	61,	'EXECUTED',	'8:66564cd5e168045d52252c5027485bbb',	'createTable tableName=RESOURCE_URIS; addForeignKeyConstraint baseTableName=RESOURCE_URIS, constraintName=FK_RESOURCE_SERVER_URIS, referencedTableName=RESOURCE_SERVER_RESOURCE; customChange; dropColumn columnName=URI, tableName=RESOURCE_SERVER_RESO...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-4.2.0.Final-KEYCLOAK-9944',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-authz-4.2.0.Final.xml',	'2025-04-27 17:48:09.983798',	62,	'EXECUTED',	'8:1c7064fafb030222be2bd16ccf690f6f',	'addPrimaryKey constraintName=CONSTRAINT_RESOUR_URIS_PK, tableName=RESOURCE_URIS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.2.0-KEYCLOAK-6313',	'wadahiro@gmail.com',	'META-INF/jpa-changelog-4.2.0.xml',	'2025-04-27 17:48:09.986369',	63,	'EXECUTED',	'8:2de18a0dce10cdda5c7e65c9b719b6e5',	'addColumn tableName=REQUIRED_ACTION_PROVIDER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.3.0-KEYCLOAK-7984',	'wadahiro@gmail.com',	'META-INF/jpa-changelog-4.3.0.xml',	'2025-04-27 17:48:09.988157',	64,	'EXECUTED',	'8:03e413dd182dcbd5c57e41c34d0ef682',	'update tableName=REQUIRED_ACTION_PROVIDER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.6.0-KEYCLOAK-7950',	'psilva@redhat.com',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-04-27 17:48:09.989842',	65,	'EXECUTED',	'8:d27b42bb2571c18fbe3fe4e4fb7582a7',	'update tableName=RESOURCE_SERVER_RESOURCE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.6.0-KEYCLOAK-8377',	'keycloak',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-04-27 17:48:09.999229',	66,	'EXECUTED',	'8:698baf84d9fd0027e9192717c2154fb8',	'createTable tableName=ROLE_ATTRIBUTE; addPrimaryKey constraintName=CONSTRAINT_ROLE_ATTRIBUTE_PK, tableName=ROLE_ATTRIBUTE; addForeignKeyConstraint baseTableName=ROLE_ATTRIBUTE, constraintName=FK_ROLE_ATTRIBUTE_ID, referencedTableName=KEYCLOAK_ROLE...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.6.0-KEYCLOAK-8555',	'gideonray@gmail.com',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-04-27 17:48:10.004143',	67,	'EXECUTED',	'8:ced8822edf0f75ef26eb51582f9a821a',	'createIndex indexName=IDX_COMPONENT_PROVIDER_TYPE, tableName=COMPONENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.7.0-KEYCLOAK-1267',	'sguilhen@redhat.com',	'META-INF/jpa-changelog-4.7.0.xml',	'2025-04-27 17:48:10.007169',	68,	'EXECUTED',	'8:f0abba004cf429e8afc43056df06487d',	'addColumn tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.7.0-KEYCLOAK-7275',	'keycloak',	'META-INF/jpa-changelog-4.7.0.xml',	'2025-04-27 17:48:10.014651',	69,	'EXECUTED',	'8:6662f8b0b611caa359fcf13bf63b4e24',	'renameColumn newColumnName=CREATED_ON, oldColumnName=LAST_SESSION_REFRESH, tableName=OFFLINE_USER_SESSION; addNotNullConstraint columnName=CREATED_ON, tableName=OFFLINE_USER_SESSION; addColumn tableName=OFFLINE_USER_SESSION; customChange; createIn...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('4.8.0-KEYCLOAK-8835',	'sguilhen@redhat.com',	'META-INF/jpa-changelog-4.8.0.xml',	'2025-04-27 17:48:10.019072',	70,	'EXECUTED',	'8:9e6b8009560f684250bdbdf97670d39e',	'addNotNullConstraint columnName=SSO_MAX_LIFESPAN_REMEMBER_ME, tableName=REALM; addNotNullConstraint columnName=SSO_IDLE_TIMEOUT_REMEMBER_ME, tableName=REALM',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('authz-7.0.0-KEYCLOAK-10443',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-7.0.0.xml',	'2025-04-27 17:48:10.021536',	71,	'EXECUTED',	'8:4223f561f3b8dc655846562b57bb502e',	'addColumn tableName=RESOURCE_SERVER',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('8.0.0-adding-credential-columns',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-04-27 17:48:10.025119',	72,	'EXECUTED',	'8:215a31c398b363ce383a2b301202f29e',	'addColumn tableName=CREDENTIAL; addColumn tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('8.0.0-updating-credential-data-not-oracle-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-04-27 17:48:10.028277',	73,	'EXECUTED',	'8:83f7a671792ca98b3cbd3a1a34862d3d',	'update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('8.0.0-updating-credential-data-oracle-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-04-27 17:48:10.029541',	74,	'MARK_RAN',	'8:f58ad148698cf30707a6efbdf8061aa7',	'update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('8.0.0-credential-cleanup-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-04-27 17:48:10.040214',	75,	'EXECUTED',	'8:79e4fd6c6442980e58d52ffc3ee7b19c',	'dropDefaultValue columnName=COUNTER, tableName=CREDENTIAL; dropDefaultValue columnName=DIGITS, tableName=CREDENTIAL; dropDefaultValue columnName=PERIOD, tableName=CREDENTIAL; dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; dropColumn ...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('8.0.0-resource-tag-support',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-04-27 17:48:10.044874',	76,	'EXECUTED',	'8:87af6a1e6d241ca4b15801d1f86a297d',	'addColumn tableName=MIGRATION_MODEL; createIndex indexName=IDX_UPDATE_TIME, tableName=MIGRATION_MODEL',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.0-always-display-client',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-04-27 17:48:10.047248',	77,	'EXECUTED',	'8:b44f8d9b7b6ea455305a6d72a200ed15',	'addColumn tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.0-drop-constraints-for-column-increase',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-04-27 17:48:10.048483',	78,	'MARK_RAN',	'8:2d8ed5aaaeffd0cb004c046b4a903ac5',	'dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5PMT, tableName=RESOURCE_SERVER_PERM_TICKET; dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER_RESOURCE; dropPrimaryKey constraintName=CONSTRAINT_O...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.0-increase-column-size-federated-fk',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-04-27 17:48:10.060786',	79,	'EXECUTED',	'8:e290c01fcbc275326c511633f6e2acde',	'modifyDataType columnName=CLIENT_ID, tableName=FED_USER_CONSENT; modifyDataType columnName=CLIENT_REALM_CONSTRAINT, tableName=KEYCLOAK_ROLE; modifyDataType columnName=OWNER, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=CLIENT_ID, ta...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.0-recreate-constraints-after-column-increase',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-04-27 17:48:10.062351',	80,	'MARK_RAN',	'8:c9db8784c33cea210872ac2d805439f8',	'addNotNullConstraint columnName=CLIENT_ID, tableName=OFFLINE_CLIENT_SESSION; addNotNullConstraint columnName=OWNER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNullConstraint columnName=REQUESTER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNull...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.1-add-index-to-client.client_id',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-04-27 17:48:10.067466',	81,	'EXECUTED',	'8:95b676ce8fc546a1fcfb4c92fae4add5',	'createIndex indexName=IDX_CLIENT_ID, tableName=CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.1-KEYCLOAK-12579-drop-constraints',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-04-27 17:48:10.068891',	82,	'MARK_RAN',	'8:38a6b2a41f5651018b1aca93a41401e5',	'dropUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.1-KEYCLOAK-12579-add-not-null-constraint',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-04-27 17:48:10.071642',	83,	'EXECUTED',	'8:3fb99bcad86a0229783123ac52f7609c',	'addNotNullConstraint columnName=PARENT_GROUP, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.1-KEYCLOAK-12579-recreate-constraints',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-04-27 17:48:10.073166',	84,	'MARK_RAN',	'8:64f27a6fdcad57f6f9153210f2ec1bdb',	'addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('9.0.1-add-index-to-events',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-04-27 17:48:10.078295',	85,	'EXECUTED',	'8:ab4f863f39adafd4c862f7ec01890abc',	'createIndex indexName=IDX_EVENT_TIME, tableName=EVENT_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('map-remove-ri',	'keycloak',	'META-INF/jpa-changelog-11.0.0.xml',	'2025-04-27 17:48:10.081413',	86,	'EXECUTED',	'8:13c419a0eb336e91ee3a3bf8fda6e2a7',	'dropForeignKeyConstraint baseTableName=REALM, constraintName=FK_TRAF444KK6QRKMS7N56AIWQ5Y; dropForeignKeyConstraint baseTableName=KEYCLOAK_ROLE, constraintName=FK_KJHO5LE2C0RAL09FL8CM9WFW9',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('map-remove-ri',	'keycloak',	'META-INF/jpa-changelog-12.0.0.xml',	'2025-04-27 17:48:10.085785',	87,	'EXECUTED',	'8:e3fb1e698e0471487f51af1ed80fe3ac',	'dropForeignKeyConstraint baseTableName=REALM_DEFAULT_GROUPS, constraintName=FK_DEF_GROUPS_GROUP; dropForeignKeyConstraint baseTableName=REALM_DEFAULT_ROLES, constraintName=FK_H4WPD7W4HSOOLNI3H0SW7BTJE; dropForeignKeyConstraint baseTableName=CLIENT...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('12.1.0-add-realm-localization-table',	'keycloak',	'META-INF/jpa-changelog-12.0.0.xml',	'2025-04-27 17:48:10.096906',	88,	'EXECUTED',	'8:babadb686aab7b56562817e60bf0abd0',	'createTable tableName=REALM_LOCALIZATIONS; addPrimaryKey tableName=REALM_LOCALIZATIONS',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('default-roles',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.101165',	89,	'EXECUTED',	'8:72d03345fda8e2f17093d08801947773',	'addColumn tableName=REALM; customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('default-roles-cleanup',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.105666',	90,	'EXECUTED',	'8:61c9233951bd96ffecd9ba75f7d978a4',	'dropTable tableName=REALM_DEFAULT_ROLES; dropTable tableName=CLIENT_DEFAULT_ROLES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('13.0.0-KEYCLOAK-16844',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.111493',	91,	'EXECUTED',	'8:ea82e6ad945cec250af6372767b25525',	'createIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('map-remove-ri-13.0.0',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.117461',	92,	'EXECUTED',	'8:d3f4a33f41d960ddacd7e2ef30d126b3',	'dropForeignKeyConstraint baseTableName=DEFAULT_CLIENT_SCOPE, constraintName=FK_R_DEF_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SCOPE_CLIENT, constraintName=FK_C_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SC...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('13.0.0-KEYCLOAK-17992-drop-constraints',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.118905',	93,	'MARK_RAN',	'8:1284a27fbd049d65831cb6fc07c8a783',	'dropPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CLSCOPE_CL, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CL_CLSCOPE, tableName=CLIENT_SCOPE_CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('13.0.0-increase-column-size-federated',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.124369',	94,	'EXECUTED',	'8:9d11b619db2ae27c25853b8a37cd0dea',	'modifyDataType columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; modifyDataType columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('13.0.0-KEYCLOAK-17992-recreate-constraints',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.125887',	95,	'MARK_RAN',	'8:3002bb3997451bb9e8bac5c5cd8d6327',	'addNotNullConstraint columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; addNotNullConstraint columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT; addPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; createIndex indexName=...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('json-string-accomodation-fixed',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-04-27 17:48:10.129608',	96,	'EXECUTED',	'8:dfbee0d6237a23ef4ccbb7a4e063c163',	'addColumn tableName=REALM_ATTRIBUTE; update tableName=REALM_ATTRIBUTE; dropColumn columnName=VALUE, tableName=REALM_ATTRIBUTE; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=REALM_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('14.0.0-KEYCLOAK-11019',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.138865',	97,	'EXECUTED',	'8:75f3e372df18d38c62734eebb986b960',	'createIndex indexName=IDX_OFFLINE_CSS_PRELOAD, tableName=OFFLINE_CLIENT_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USER, tableName=OFFLINE_USER_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('14.0.0-KEYCLOAK-18286',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.140674',	98,	'MARK_RAN',	'8:7fee73eddf84a6035691512c85637eef',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('14.0.0-KEYCLOAK-18286-revert',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.148376',	99,	'MARK_RAN',	'8:7a11134ab12820f999fbf3bb13c3adc8',	'dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('14.0.0-KEYCLOAK-18286-supported-dbs',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.154481',	100,	'EXECUTED',	'8:c0f6eaac1f3be773ffe54cb5b8482b70',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('14.0.0-KEYCLOAK-18286-unsupported-dbs',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.157349',	101,	'MARK_RAN',	'8:18186f0008b86e0f0f49b0c4d0e842ac',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('KEYCLOAK-17267-add-index-to-user-attributes',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.163363',	102,	'EXECUTED',	'8:09c2780bcb23b310a7019d217dc7b433',	'createIndex indexName=IDX_USER_ATTRIBUTE_NAME, tableName=USER_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('KEYCLOAK-18146-add-saml-art-binding-identifier',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-04-27 17:48:10.166149',	103,	'EXECUTED',	'8:276a44955eab693c970a42880197fff2',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('15.0.0-KEYCLOAK-18467',	'keycloak',	'META-INF/jpa-changelog-15.0.0.xml',	'2025-04-27 17:48:10.169882',	104,	'EXECUTED',	'8:ba8ee3b694d043f2bfc1a1079d0760d7',	'addColumn tableName=REALM_LOCALIZATIONS; update tableName=REALM_LOCALIZATIONS; dropColumn columnName=TEXTS, tableName=REALM_LOCALIZATIONS; renameColumn newColumnName=TEXTS, oldColumnName=TEXTS_NEW, tableName=REALM_LOCALIZATIONS; addNotNullConstrai...',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('17.0.0-9562',	'keycloak',	'META-INF/jpa-changelog-17.0.0.xml',	'2025-04-27 17:48:10.175672',	105,	'EXECUTED',	'8:5e06b1d75f5d17685485e610c2851b17',	'createIndex indexName=IDX_USER_SERVICE_ACCOUNT, tableName=USER_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('18.0.0-10625-IDX_ADMIN_EVENT_TIME',	'keycloak',	'META-INF/jpa-changelog-18.0.0.xml',	'2025-04-27 17:48:10.18069',	106,	'EXECUTED',	'8:4b80546c1dc550ac552ee7b24a4ab7c0',	'createIndex indexName=IDX_ADMIN_EVENT_TIME, tableName=ADMIN_EVENT_ENTITY',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('19.0.0-10135',	'keycloak',	'META-INF/jpa-changelog-19.0.0.xml',	'2025-04-27 17:48:10.183699',	107,	'EXECUTED',	'8:af510cd1bb2ab6339c45372f3e491696',	'customChange',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('20.0.0-12964-supported-dbs',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-04-27 17:48:10.188116',	108,	'EXECUTED',	'8:05c99fc610845ef66ee812b7921af0ef',	'createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('20.0.0-12964-unsupported-dbs',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-04-27 17:48:10.18948',	109,	'MARK_RAN',	'8:314e803baf2f1ec315b3464e398b8247',	'createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710'),
        ('client-attributes-string-accomodation-fixed',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-04-27 17:48:10.193573',	110,	'EXECUTED',	'8:56e4677e7e12556f70b604c573840100',	'addColumn tableName=CLIENT_ATTRIBUTES; update tableName=CLIENT_ATTRIBUTES; dropColumn columnName=VALUE, tableName=CLIENT_ATTRIBUTES; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.8.0',	NULL,	NULL,	'5776088710');

        CREATE TABLE IF NOT EXISTS "public"."databasechangeloglock" (
            "id" integer NOT NULL,
            "locked" boolean NOT NULL,
            "lockgranted" timestamp,
            "lockedby" character varying(255),
            CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        INSERT INTO "databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
        (1,	'0',	NULL,	NULL),
        (1000,	'0',	NULL,	NULL),
        (1001,	'0',	NULL,	NULL);

        CREATE TABLE IF NOT EXISTS "public"."default_client_scope" (
            "realm_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            "default_scope" boolean DEFAULT false NOT NULL,
            CONSTRAINT "r_def_cli_scope_bind" PRIMARY KEY ("realm_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_defcls_realm ON public.default_client_scope USING btree (realm_id);

        CREATE INDEX IF NOT EXISTS idx_defcls_scope ON public.default_client_scope USING btree (scope_id);

        INSERT INTO "default_client_scope" ("realm_id", "scope_id", "default_scope") VALUES
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'9cc4bf72-80ca-4543-a50d-e564d70ef530',	'0'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'c63e8029-e161-42e8-b6c8-391b4ce4e8bf',	'1'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6',	'1'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'180fae93-bd13-4947-b45e-71184ff6cb8e',	'1'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'cd45dcbf-b0d1-4263-93a9-a98b2276f931',	'0'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'3c29835b-9afa-408f-b312-020e2d382171',	'0'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'27a38596-04c0-449f-8aa0-dd04ba078dbd',	'1'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f',	'1'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'3b4cb516-3d86-4be4-ad28-445db543020f',	'0'),
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'e8be51d3-0671-4356-bc8f-7236c55b5eae',	'1'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'925d0d04-389b-4355-a073-763af5634342',	'0'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'3bf53c49-b355-481f-b6f0-ad5dd177529e',	'1'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'a2486c02-5694-4cd3-9372-74783e105f85',	'1'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'0986bbff-258c-4976-aff5-8a886bf9a10a',	'1'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'6c7d666e-eed3-40df-a2f1-954ac62d9270',	'0'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706',	'0'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'428550b5-ca79-4b63-85c3-4a26fe4555d5',	'1'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'dc349458-e591-48e7-8f4f-8dde93668a78',	'1'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc',	'0'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'59846590-87dd-4430-b6d7-a6c6d184379b',	'1'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'9960ffce-59b3-4df9-816e-cabef361c972',	'0'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'198e2431-2db6-47b7-a15a-6af52e68a6b2',	'1'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71',	'1'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10',	'1'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1714e6d4-8929-4921-a9d7-bfe4970becd4',	'0'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'e0587a64-8227-4bdc-b923-2cc098bc1954',	'0'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'160d040f-f71e-41f2-9a36-e0464cb8c3ca',	'1'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca',	'1'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'7113307d-79a0-4e25-a362-e95b4ef1614f',	'0'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee',	'1');

        CREATE TABLE IF NOT EXISTS "public"."event_entity" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_event_time ON public.event_entity USING btree (realm_id, event_time);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_attribute" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            "value" character varying(2024),
            CONSTRAINT "constr_fed_user_attr_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fu_attribute ON public.fed_user_attribute USING btree (user_id, realm_id, name);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_consent" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fu_consent_ru ON public.fed_user_consent USING btree (realm_id, user_id);

        CREATE INDEX IF NOT EXISTS idx_fu_cnsnt_ext ON public.fed_user_consent USING btree (user_id, client_storage_provider, external_client_id);

        CREATE INDEX IF NOT EXISTS idx_fu_consent ON public.fed_user_consent USING btree (user_id, client_id);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_consent_cl_scope" (
            "user_consent_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_fgrntcsnt_clsc_pm" PRIMARY KEY ("user_consent_id", "scope_id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_credential" (
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
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fu_credential ON public.fed_user_credential USING btree (user_id, type);

        CREATE INDEX IF NOT EXISTS idx_fu_credential_ru ON public.fed_user_credential USING btree (realm_id, user_id);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_group_membership" (
            "group_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_user_group" PRIMARY KEY ("group_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fu_group_membership ON public.fed_user_group_membership USING btree (user_id, group_id);

        CREATE INDEX IF NOT EXISTS idx_fu_group_membership_ru ON public.fed_user_group_membership USING btree (realm_id, user_id);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_required_action" (
            "required_action" character varying(255) DEFAULT ' ' NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_required_action" PRIMARY KEY ("required_action", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fu_required_action ON public.fed_user_required_action USING btree (user_id, required_action);

        CREATE INDEX IF NOT EXISTS idx_fu_required_action_ru ON public.fed_user_required_action USING btree (realm_id, user_id);


        CREATE TABLE IF NOT EXISTS "public"."fed_user_role_mapping" (
            "role_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_user_role" PRIMARY KEY ("role_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fu_role_mapping ON public.fed_user_role_mapping USING btree (user_id, role_id);

        CREATE INDEX IF NOT EXISTS idx_fu_role_mapping_ru ON public.fed_user_role_mapping USING btree (realm_id, user_id);


        CREATE TABLE IF NOT EXISTS "public"."federated_identity" (
            "identity_provider" character varying(255) NOT NULL,
            "realm_id" character varying(36),
            "federated_user_id" character varying(255),
            "federated_username" character varying(255),
            "token" text,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_40" PRIMARY KEY ("identity_provider", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_fedidentity_user ON public.federated_identity USING btree (user_id);

        CREATE INDEX IF NOT EXISTS idx_fedidentity_feduser ON public.federated_identity USING btree (federated_user_id);


        CREATE TABLE IF NOT EXISTS "public"."federated_user" (
            "id" character varying(255) NOT NULL,
            "storage_provider_id" character varying(255),
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constr_federated_user" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."group_attribute" (
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_group_attribute_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_group_attr_group ON public.group_attribute USING btree (group_id);

        CREATE INDEX IF NOT EXISTS idx_group_att_by_name_value ON public.group_attribute USING btree (name, ((value)::character varying(250)));


        CREATE TABLE IF NOT EXISTS "public"."group_role_mapping" (
            "role_id" character varying(36) NOT NULL,
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_group_role" PRIMARY KEY ("role_id", "group_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_group_role_mapp_group ON public.group_role_mapping USING btree (group_id);


        CREATE TABLE IF NOT EXISTS "public"."identity_provider" (
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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_2daelwnibji49avxsrtuf6xj33 ON public.identity_provider USING btree (provider_alias, realm_id);

        CREATE INDEX IF NOT EXISTS idx_ident_prov_realm ON public.identity_provider USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."identity_provider_config" (
            "identity_provider_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_d" PRIMARY KEY ("identity_provider_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."identity_provider_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "idp_alias" character varying(255) NOT NULL,
            "idp_mapper_name" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_idpm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_id_prov_mapp_realm ON public.identity_provider_mapper USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."idp_mapper_config" (
            "idp_mapper_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_idpmconfig" PRIMARY KEY ("idp_mapper_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."keycloak_group" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "parent_group" character varying(36) NOT NULL,
            "realm_id" character varying(36),
            CONSTRAINT "constraint_group" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX sibling_names ON public.keycloak_group USING btree (realm_id, parent_group, name);


        CREATE TABLE IF NOT EXISTS "public"."keycloak_role" (
            "id" character varying(36) NOT NULL,
            "client_realm_constraint" character varying(255),
            "client_role" boolean DEFAULT false NOT NULL,
            "description" character varying(255),
            "name" character varying(255),
            "realm_id" character varying(255),
            "client" character varying(36),
            "realm" character varying(36),
            CONSTRAINT "constraint_a" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_keycloak_role_client ON public.keycloak_role USING btree (client);

        CREATE INDEX IF NOT EXISTS idx_keycloak_role_realm ON public.keycloak_role USING btree (realm);

        CREATE UNIQUE INDEX "UK_J3RWUVD56ONTGSUHOGM184WW2-2" ON public.keycloak_role USING btree (name, client_realm_constraint);

        INSERT INTO "keycloak_role" ("id", "client_realm_constraint", "client_role", "description", "name", "realm_id", "client", "realm") VALUES
        ('0eb7f97a-bb13-4666-8ef2-fe28d37dff01',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'${role_default-roles}',	'default-roles-master',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	NULL),
        ('e125e84c-897d-46ff-b5ed-33ec793d902d',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'${role_create-realm}',	'create-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	NULL),
        ('a6ba66ca-d9d5-4302-82d9-0c067a108565',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_create-client}',	'create-client',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('5c00def1-c865-41bf-81eb-2212799fdb30',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_view-realm}',	'view-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('66d6b367-fdff-4a66-ac59-ebdac1f900c4',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_view-users}',	'view-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('47456962-7657-4cc8-8c4c-68b128e3946c',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_view-clients}',	'view-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('a9c6d599-eaf5-4893-b2a0-6d9f7a752a7c',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_view-events}',	'view-events',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('b1d68eb5-5a6d-47b9-9d92-59a02a4d3830',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('b7b28cd9-7bc5-4f5a-a64c-4e9552f481a7',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_view-authorization}',	'view-authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('1bf18d6f-7da7-4af8-95e5-bb12f75624c9',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_manage-realm}',	'manage-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('b4c84aa6-124c-41e8-ad7c-87198a066b12',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_manage-users}',	'manage-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('2f1b03eb-76cb-4af1-bf58-69adf89d64f6',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_manage-clients}',	'manage-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('fda0eb40-ae17-4f0e-a537-16a1eb749e86',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_manage-events}',	'manage-events',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('bd764018-ee73-43d3-94ac-80bfd3f3e77e',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('c4c9e69d-6057-489b-92f0-901366a20bf6',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_manage-authorization}',	'manage-authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('10699d5c-3908-47ad-9a8a-01b060d1123c',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_query-users}',	'query-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('bf40eff7-b00c-4738-ad8e-88159580d140',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_query-clients}',	'query-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('d8859617-a768-42f8-accb-a15f8c0d7b9e',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_query-realms}',	'query-realms',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('3ffc9916-dd66-4021-96d1-ff410971d43e',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_query-groups}',	'query-groups',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('be1d9e4e-cee0-4d0a-9e31-6207618d1148',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_view-profile}',	'view-profile',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('676508a8-16d1-4519-a907-6e46d58a2559',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_manage-account}',	'manage-account',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('fa45ec91-9633-4f9b-8fb9-02763b089cc9',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_manage-account-links}',	'manage-account-links',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('6761ad02-7b91-4829-975d-bbbac86a8b89',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_view-applications}',	'view-applications',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('1b5e021c-10c0-4910-a813-9b29a2c37405',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_view-consent}',	'view-consent',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('5cf941ad-d7f2-4edd-a6be-902a740b4a44',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_manage-consent}',	'manage-consent',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('61e9e17e-85c9-4002-806b-8e2931699962',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_view-groups}',	'view-groups',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('8f686399-acd9-42a6-a202-f488a51df9ff',	'df39997b-873d-441b-8c40-4c85a5648424',	'1',	'${role_delete-account}',	'delete-account',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'df39997b-873d-441b-8c40-4c85a5648424',	NULL),
        ('2625a847-ed36-4f89-a98d-2297361c5b32',	'2e8b984f-c411-416b-88e8-a19092d77a62',	'1',	'${role_read-token}',	'read-token',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'2e8b984f-c411-416b-88e8-a19092d77a62',	NULL),
        ('b527daac-764b-4c74-9abd-744050454400',	'284f063e-1ad4-4c28-8243-9e181600363f',	'1',	'${role_impersonation}',	'impersonation',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'284f063e-1ad4-4c28-8243-9e181600363f',	NULL),
        ('d2ad7d1b-7432-4d63-af55-e8dcbfdfb525',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'${role_offline-access}',	'offline_access',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	NULL),
        ('0fb27c2d-d533-4f92-9ae6-9c90e08b8d66',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'${role_uma_authorization}',	'uma_authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	NULL),
        ('6899cefd-5fbb-41e2-82c4-919f0a34f170',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0',	'${role_default-roles}',	'default-roles-tenant-manager',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL,	NULL),
        ('999b4793-f9cb-4f87-85df-268540465280',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_create-client}',	'create-client',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('a9a5d436-92cd-49c9-a1a2-2dc1aeb2286d',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_view-realm}',	'view-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('324f0087-cfc8-4735-8047-a6d095e9b29b',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_view-users}',	'view-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('212de463-3a92-4524-9460-ab727300ff8b',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_view-clients}',	'view-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('6d765ab0-6165-4363-9550-0f987614cb2b',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_view-events}',	'view-events',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('da91fc7d-4291-4122-99ec-545d59ddef2e',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('c82615ec-4565-4d5f-90b8-94cd738bd52f',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_view-authorization}',	'view-authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('518b47ba-5111-4c11-bebe-bcbca103b716',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_manage-realm}',	'manage-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('f23e7e75-ecd4-4dba-9bba-42383fa7c775',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_manage-users}',	'manage-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('2fa9ae60-e0f9-4c97-82e4-d4cacd65b10c',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_manage-clients}',	'manage-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('4d30f7f1-ba8a-4a17-b714-eeece44888a1',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_manage-events}',	'manage-events',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('14b94361-4613-412f-b03d-24836f38920a',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('baed571a-2c21-4a14-b504-61f0f2d4f015',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_manage-authorization}',	'manage-authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('52f26c30-0d74-4476-a0a5-84ab09ebac02',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_query-users}',	'query-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('f376db7d-a9c1-407c-a73c-2ea9a9a6c15c',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_query-clients}',	'query-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('d909e0e4-def4-414f-a03e-39e777a41e0d',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_query-realms}',	'query-realms',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('1ea09efe-60c8-4b18-a22c-d2d6d322fadd',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_query-groups}',	'query-groups',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('7374df8a-5f5c-4e94-be22-9fb6b2c76acf',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_realm-admin}',	'realm-admin',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('ad11d1be-3942-4c84-b527-a2b90ceef718',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_create-client}',	'create-client',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('6bcf9972-083a-4efc-80ab-8a83999bdca1',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_view-realm}',	'view-realm',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('a50978fc-1613-4c85-990c-6557a8c9cf02',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_view-users}',	'view-users',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('688e7e16-4e9a-446a-8522-b62e1ee22de7',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_view-clients}',	'view-clients',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('94ecb7ae-c6ad-4610-8d08-2b4cd6486b21',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_view-events}',	'view-events',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('27c6c26d-8e5d-4114-ac16-7dbc409cd965',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('c1eb3d87-b95a-4660-9855-97056b9831e4',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_view-authorization}',	'view-authorization',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('87e75ce2-7a44-4af9-91ea-269ac58bd962',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_manage-realm}',	'manage-realm',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('a4b47ba4-2270-4260-bbe1-5b7e21d35034',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_manage-users}',	'manage-users',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('bb0c4911-333f-47c4-b61d-f7eb6f096640',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_manage-clients}',	'manage-clients',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('54e66818-e3ba-4a21-bc05-7d362eaa2adf',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_manage-events}',	'manage-events',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('54dd9e72-9c27-4604-af1a-3acb3ad72f3a',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('328c67f2-1ac3-44e2-92ba-b2c4769310ea',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_manage-authorization}',	'manage-authorization',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('b6f9187e-4ccc-4d14-8b63-a38d0c5854df',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_query-users}',	'query-users',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('ee13eb37-882f-4f0f-85ce-fc19b5352c1b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_query-clients}',	'query-clients',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('07425b79-6744-424a-8032-b77c42a63d28',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_query-realms}',	'query-realms',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('d375b2e2-e95c-4ce9-bc35-8e92a234fc89',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_query-groups}',	'query-groups',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('3bb8c51e-1865-4bfc-bdd9-31b359c435a1',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_view-profile}',	'view-profile',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('0b456db2-a080-48c6-86c7-98bbee5426a8',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_manage-account}',	'manage-account',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('9bfb3589-4488-4c0f-90d4-00306c5d301e',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_manage-account-links}',	'manage-account-links',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('be094477-4478-4898-8a49-6444fee5522e',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_view-applications}',	'view-applications',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('eea61099-44d6-4651-b997-463a869c7fd8',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_view-consent}',	'view-consent',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('5732fe4c-ea34-495e-8c03-c962ac389036',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_manage-consent}',	'manage-consent',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('9f34a225-8156-4995-a6f1-1a3a51bcc5ef',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_view-groups}',	'view-groups',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('0b4ef589-8656-4d97-97f7-5c58330b2c4c',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'1',	'${role_delete-account}',	'delete-account',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	NULL),
        ('2233e0a8-104e-4032-82ff-9153aab813ca',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	'1',	'${role_impersonation}',	'impersonation',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	NULL),
        ('befa7bcc-0949-4f13-81b5-23ca31bf5d99',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	'1',	'${role_impersonation}',	'impersonation',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'63a798ec-a937-491a-a61c-bf30a566c1e6',	NULL),
        ('497c0a46-d3c9-40b4-9d7f-b7049d1c0104',	'ecf09fc1-99fa-478b-9e83-6c25d54390dc',	'1',	'${role_read-token}',	'read-token',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'ecf09fc1-99fa-478b-9e83-6c25d54390dc',	NULL),
        ('d8af271a-f459-408d-8c70-f793a831b551',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0',	'${role_offline-access}',	'offline_access',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL,	NULL),
        ('4456adca-b603-436d-8664-8ec24a0ca788',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0',	'${role_uma_authorization}',	'uma_authorization',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL,	NULL),
        ('52ede75a-b30d-4fa7-991b-2c88ddffda25',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0',	'',	'admin',	'8057e71d-86e9-4b84-8438-269c52eea27b',	NULL,	NULL),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'${role_admin}',	'admin',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	NULL,	NULL),
        ('f749b549-08fa-48ce-87f8-9df49f4c2bfa',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'${role_default-roles}',	'default-roles-openk9',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL,	NULL),
        ('e22a2f5f-b2e2-4f3c-9470-6d20b583a734',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_create-client}',	'create-client',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('c821717f-ac1a-4460-a2d8-b0120c2aeaab',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_view-realm}',	'view-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('7a50bc1e-7913-4785-8155-bafb0301f7bf',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_view-users}',	'view-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('1b7c75ab-36aa-456f-90d1-86a7128c3fe9',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_view-clients}',	'view-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('bbcd6f9e-3fcc-4914-a4c9-d89226874f84',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_view-events}',	'view-events',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('2862ecff-b7e7-41b7-b28e-8f560cfd39f4',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('2d3e81af-f0ee-4686-85ec-0aa19949c09a',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_view-authorization}',	'view-authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('6251e530-7e1f-41ef-b7ef-f7f7331c47c5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_manage-realm}',	'manage-realm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('d177f479-d57f-44f0-aa00-772631313f70',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_manage-users}',	'manage-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('2315d12c-fe68-4ee5-b603-a7b2df33cb12',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_manage-clients}',	'manage-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('9e5daa7b-20b9-4043-ac83-4bef3d0c1b96',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_manage-events}',	'manage-events',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('e90a49c9-9f13-40ec-902f-f1d5f61a3768',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('2ce2e238-e733-4cdb-951e-e053786fc242',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_manage-authorization}',	'manage-authorization',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('dbaeb2c9-a2d0-42a6-92fa-59368a3ba5f4',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_query-users}',	'query-users',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('ccbb8eca-4683-4112-8db8-ca7e4a0512d6',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_query-clients}',	'query-clients',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('6536f0e5-d16d-4880-aa23-3e70af704ba1',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_query-realms}',	'query-realms',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('7cb7caf1-2b3f-4adb-adac-154a76092738',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_query-groups}',	'query-groups',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('7d74754d-d6db-44f4-8a0b-aad2579a5620',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_realm-admin}',	'realm-admin',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('f0c698c9-3bcd-49c7-8e92-5d8a4ef604d1',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_create-client}',	'create-client',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('b107c5b1-95a9-4c10-85ca-ad3a7382e870',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_view-realm}',	'view-realm',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('94e5cad9-1bbe-43f7-a6fd-b91b630bc386',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_view-users}',	'view-users',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('781e59ea-8349-4703-9ed2-113f9d0d29d9',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_view-clients}',	'view-clients',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('c604374a-40b8-4626-97d7-3b8d57568883',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_view-events}',	'view-events',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('ab795987-83dc-49bf-b479-dcd0dbf7265c',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('d9a55160-5001-4e8b-a62f-1e3bd8fa51a5',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_view-authorization}',	'view-authorization',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('0eadf679-cf48-4539-ab84-805c52a24584',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_manage-realm}',	'manage-realm',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('d9ac6cef-508b-422e-ab6a-ad3bb1323b2c',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_manage-users}',	'manage-users',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('bda818f6-e636-4de8-bf71-7ebfd16be794',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_manage-clients}',	'manage-clients',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('eddccae8-6d80-4403-83f5-bdf09734d810',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_manage-events}',	'manage-events',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('a87730cf-8933-4fcb-ab14-94fa6253a6e3',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('7c449f1b-8471-4243-97e3-421906393359',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_manage-authorization}',	'manage-authorization',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('6e82f7ae-2a57-4848-986a-ee215f54329e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_query-users}',	'query-users',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('4394521e-5dfc-4f68-9085-8a86eb431a86',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_query-clients}',	'query-clients',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('2c100cbc-5a4d-4294-ad62-119611edcfcd',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_query-realms}',	'query-realms',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('4ac18c0b-be91-4d1b-89db-656ac74c7d11',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_query-groups}',	'query-groups',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('f28b4a92-c2d6-496a-895f-c67ff64fb799',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_view-profile}',	'view-profile',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('1ed0867f-b4e2-4d27-9aca-d0b002079bd2',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_manage-account}',	'manage-account',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('364acfb8-77f8-4c33-8489-60a2204c0a12',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_manage-account-links}',	'manage-account-links',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('596554b5-5c50-4353-809b-35a050b630a1',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_view-applications}',	'view-applications',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('672b4e51-e3cf-4adc-80ad-0223deb8fd85',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_view-consent}',	'view-consent',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('03e240bc-ffd1-4ba1-ab44-b2ad9a4ea6dd',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_manage-consent}',	'manage-consent',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('932ba3ac-fac4-4e03-b5dd-486527d8a6cb',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_view-groups}',	'view-groups',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('7face836-ebd9-4fdf-b86c-ad9f77f316c4',	'44e9e0f3-5502-4761-b209-998860155253',	'1',	'${role_delete-account}',	'delete-account',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'44e9e0f3-5502-4761-b209-998860155253',	NULL),
        ('3653fa78-5e9e-429d-b31a-0fbc240f9f09',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	'1',	'${role_impersonation}',	'impersonation',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	NULL),
        ('30e7a1f6-bca0-4b4e-a280-f684d9747120',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	'1',	'${role_impersonation}',	'impersonation',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'a0d9a2c0-7be3-4ac2-9681-f969d5ff08ce',	NULL),
        ('73f1734d-4fda-410c-9245-eb128b47598a',	'3576883c-04f4-4d95-8c0a-92878d1a0606',	'1',	'${role_read-token}',	'read-token',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'3576883c-04f4-4d95-8c0a-92878d1a0606',	NULL),
        ('95ec85b4-d1bf-4a2f-8c35-35c464da6f05',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'${role_offline-access}',	'offline_access',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL,	NULL),
        ('fc98bf35-cc28-4be1-9fb3-879724b168da',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'${role_uma_authorization}',	'uma_authorization',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL,	NULL),
        ('69c0a37c-4a1d-4ca5-be5e-9b5c1ff6dff8',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'',	'k9-admin',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL,	NULL),
        ('0137b641-bd6f-40d5-839b-2d916d1d10c2',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'',	'k9-write',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL,	NULL),
        ('65acabcd-a3d4-4429-a5fd-33013fb11485',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'',	'k9-read',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	NULL,	NULL);

        CREATE TABLE IF NOT EXISTS "public"."migration_model" (
            "id" character varying(36) NOT NULL,
            "version" character varying(36),
            "update_time" bigint DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_migmod" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_update_time ON public.migration_model USING btree (update_time);

        INSERT INTO "migration_model" ("id", "version", "update_time") VALUES
        ('zjkcp',	'20.0.5',	1745776090);

        CREATE TABLE IF NOT EXISTS "public"."offline_client_session" (
            "user_session_id" character varying(36) NOT NULL,
            "client_id" character varying(255) NOT NULL,
            "offline_flag" character varying(4) NOT NULL,
            "timestamp" integer,
            "data" text,
            "client_storage_provider" character varying(36) DEFAULT 'local' NOT NULL,
            "external_client_id" character varying(255) DEFAULT 'local' NOT NULL,
            CONSTRAINT "constraint_offl_cl_ses_pk3" PRIMARY KEY ("user_session_id", "client_id", "client_storage_provider", "external_client_id", "offline_flag")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_us_sess_id_on_cl_sess ON public.offline_client_session USING btree (user_session_id);

        CREATE INDEX IF NOT EXISTS idx_offline_css_preload ON public.offline_client_session USING btree (client_id, offline_flag);


        CREATE TABLE IF NOT EXISTS "public"."offline_user_session" (
            "user_session_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "created_on" integer NOT NULL,
            "offline_flag" character varying(4) NOT NULL,
            "data" text,
            "last_session_refresh" integer DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_offl_us_ses_pk2" PRIMARY KEY ("user_session_id", "offline_flag")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_offline_uss_createdon ON public.offline_user_session USING btree (created_on);

        CREATE INDEX IF NOT EXISTS idx_offline_uss_preload ON public.offline_user_session USING btree (offline_flag, created_on, user_session_id);

        CREATE INDEX IF NOT EXISTS idx_offline_uss_by_user ON public.offline_user_session USING btree (user_id, realm_id, offline_flag);

        CREATE INDEX IF NOT EXISTS idx_offline_uss_by_usersess ON public.offline_user_session USING btree (realm_id, offline_flag, user_session_id);


        CREATE TABLE IF NOT EXISTS "public"."policy_config" (
            "policy_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_dpc" PRIMARY KEY ("policy_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."protocol_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "protocol" character varying(255) NOT NULL,
            "protocol_mapper_name" character varying(255) NOT NULL,
            "client_id" character varying(36),
            "client_scope_id" character varying(36),
            CONSTRAINT "constraint_pcm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_protocol_mapper_client ON public.protocol_mapper USING btree (client_id);

        CREATE INDEX IF NOT EXISTS idx_clscope_protmap ON public.protocol_mapper USING btree (client_scope_id);

        INSERT INTO "protocol_mapper" ("id", "name", "protocol", "protocol_mapper_name", "client_id", "client_scope_id") VALUES
        ('af3de5df-0b6d-451e-980c-424f8de07a47',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'3881304e-d247-4373-a143-07d3096f04d2',	NULL),
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'9306c727-41dc-4892-873e-e3852008de32',	NULL),
        ('252de504-54b1-4d6c-957b-e44df9df604c',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'c63e8029-e161-42e8-b6c8-391b4ce4e8bf'),
        ('be765428-8f40-4413-98dc-932af63f3f2a',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'family name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'given name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'username',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'6b85ea2a-7dc6-41b3-ac1e-44c7c80d73f6'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'email',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'180fae93-bd13-4947-b45e-71184ff6cb8e'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'180fae93-bd13-4947-b45e-71184ff6cb8e'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'cd45dcbf-b0d1-4263-93a9-a98b2276f931'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c29835b-9afa-408f-b312-020e2d382171'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'3c29835b-9afa-408f-b312-020e2d382171'),
        ('7e8877f7-ad3c-4f83-a7f6-5645a4acbc3b',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'27a38596-04c0-449f-8aa0-dd04ba078dbd'),
        ('591c1e59-9926-457a-9a1d-36bc9dd68033',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'27a38596-04c0-449f-8aa0-dd04ba078dbd'),
        ('7b876413-ebfc-4378-ba9e-31d64fbd1f8e',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'27a38596-04c0-449f-8aa0-dd04ba078dbd'),
        ('8edee93b-870c-4861-838e-5d1cf59d5347',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'5b9ddad5-966d-454c-9f4d-ed0cd599c04f'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'upn',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'3b4cb516-3d86-4be4-ad28-445db543020f'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'3b4cb516-3d86-4be4-ad28-445db543020f'),
        ('b36a1a46-37ec-42bb-adbe-dc937b46dba7',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'e8be51d3-0671-4356-bc8f-7236c55b5eae'),
        ('0f159c63-1cb3-4780-9df2-91b4f62a15d4',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'23a52d4c-249a-47b3-8d98-cc8b48188a93',	NULL),
        ('46d935cf-e0a0-4c9e-86b7-ab92202ad381',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'3bf53c49-b355-481f-b6f0-ad5dd177529e'),
        ('4cbfb353-8fd8-49a7-a621-7899907a21c8',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'family name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'given name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'username',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'a2486c02-5694-4cd3-9372-74783e105f85'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'email',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'0986bbff-258c-4976-aff5-8a886bf9a10a'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'0986bbff-258c-4976-aff5-8a886bf9a10a'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'6c7d666e-eed3-40df-a2f1-954ac62d9270'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'bf8f8a4e-fae7-47ff-8e97-1729e14f8706'),
        ('fec5d967-2501-4cfc-99b0-10e350e98e05',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'428550b5-ca79-4b63-85c3-4a26fe4555d5'),
        ('f9f06e1e-ed26-4636-b324-d167a19530c4',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'428550b5-ca79-4b63-85c3-4a26fe4555d5'),
        ('48c31f73-34cd-4c05-8499-0492bcd4b4f4',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'428550b5-ca79-4b63-85c3-4a26fe4555d5'),
        ('c32f7493-e62b-4bbb-b0b6-a017569388ab',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'dc349458-e591-48e7-8f4f-8dde93668a78'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'upn',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'fa40b8e9-9dc9-4cf4-8abe-6d256f9f4abc'),
        ('0062df60-8c28-45e4-bf55-72de87dce108',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'59846590-87dd-4430-b6d7-a6c6d184379b'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'f1210412-5e15-4ef5-b552-03d8d8344fca',	NULL),
        ('0ee25c97-e975-46b1-bc05-7c5072806cf0',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'8df73544-8837-4c85-92c8-d22005f498e4',	NULL),
        ('659f176e-0549-43b8-b9c1-dc17bdd8b65a',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'198e2431-2db6-47b7-a15a-6af52e68a6b2'),
        ('fa26fa4c-6511-4b65-a744-410e2143f6d3',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'family name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'given name',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'username',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd6fe4c1b-67f9-442c-bb46-1728cd0b3e71'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'email',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'37426e8a-5bb0-41d0-83e2-188f6e5b2a10'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'1714e6d4-8929-4921-a9d7-bfe4970becd4'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'e0587a64-8227-4bdc-b923-2cc098bc1954'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'e0587a64-8227-4bdc-b923-2cc098bc1954'),
        ('afee4528-b497-4e95-aa7a-f80fa9a759dc',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'160d040f-f71e-41f2-9a36-e0464cb8c3ca'),
        ('557d2aab-d25b-41c7-a4cc-d419597849d8',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'160d040f-f71e-41f2-9a36-e0464cb8c3ca'),
        ('4799f1a8-efc3-407d-a4d0-1a845b90cbe2',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'160d040f-f71e-41f2-9a36-e0464cb8c3ca'),
        ('3ba39515-be75-4699-829e-3b473d954778',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'd3017fff-ad4d-4e42-bc6e-3a3a68ee8aca'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'upn',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'7113307d-79a0-4e25-a362-e95b4ef1614f'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'7113307d-79a0-4e25-a362-e95b4ef1614f'),
        ('f632f3b5-c24b-4060-8536-1cf5c8909dfd',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'7ac1a798-3750-4cca-8692-b26ee9e4a5ee'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'011747e2-5e34-400e-a0e5-0c8f837d7d3c',	NULL);

        CREATE TABLE IF NOT EXISTS "public"."protocol_mapper_config" (
            "protocol_mapper_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_pmconfig" PRIMARY KEY ("protocol_mapper_id", "name")
        ) WITH (oids = false);

        INSERT INTO "protocol_mapper_config" ("protocol_mapper_id", "value", "name") VALUES
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'true',	'userinfo.token.claim'),
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'locale',	'user.attribute'),
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'true',	'id.token.claim'),
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'true',	'access.token.claim'),
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'locale',	'claim.name'),
        ('e9dc38fc-3ed6-461f-8d91-2eb2c2ae89a3',	'String',	'jsonType.label'),
        ('252de504-54b1-4d6c-957b-e44df9df604c',	'false',	'single'),
        ('252de504-54b1-4d6c-957b-e44df9df604c',	'Basic',	'attribute.nameformat'),
        ('252de504-54b1-4d6c-957b-e44df9df604c',	'Role',	'attribute.name'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'true',	'userinfo.token.claim'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'profile',	'user.attribute'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'true',	'id.token.claim'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'true',	'access.token.claim'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'profile',	'claim.name'),
        ('22a8be26-c73a-40bf-abd0-1689fc4a592a',	'String',	'jsonType.label'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'true',	'userinfo.token.claim'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'lastName',	'user.attribute'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'true',	'id.token.claim'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'true',	'access.token.claim'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'family_name',	'claim.name'),
        ('24699221-edd7-4373-86c8-02a794c7378d',	'String',	'jsonType.label'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'true',	'userinfo.token.claim'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'gender',	'user.attribute'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'true',	'id.token.claim'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'true',	'access.token.claim'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'gender',	'claim.name'),
        ('61b630be-c54b-4651-91f0-9aa172b86bb4',	'String',	'jsonType.label'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'true',	'userinfo.token.claim'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'updatedAt',	'user.attribute'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'true',	'id.token.claim'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'true',	'access.token.claim'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'updated_at',	'claim.name'),
        ('6260704c-2704-4e8d-9ca9-e41462664207',	'long',	'jsonType.label'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'true',	'userinfo.token.claim'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'locale',	'user.attribute'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'true',	'id.token.claim'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'true',	'access.token.claim'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'locale',	'claim.name'),
        ('746a71d2-3e5e-4374-a956-f2ad7d8e3adc',	'String',	'jsonType.label'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'true',	'userinfo.token.claim'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'nickname',	'user.attribute'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'true',	'id.token.claim'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'true',	'access.token.claim'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'nickname',	'claim.name'),
        ('76e24e7a-6ec1-43ac-abaa-2f4d6fb2f9e6',	'String',	'jsonType.label'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'true',	'userinfo.token.claim'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'zoneinfo',	'user.attribute'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'true',	'id.token.claim'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'true',	'access.token.claim'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'zoneinfo',	'claim.name'),
        ('8e93fbf4-e017-458b-8d2a-63545100faa8',	'String',	'jsonType.label'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'true',	'userinfo.token.claim'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'birthdate',	'user.attribute'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'true',	'id.token.claim'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'true',	'access.token.claim'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'birthdate',	'claim.name'),
        ('938ebe22-c4dc-4eb5-998d-6d15e59ce098',	'String',	'jsonType.label'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'true',	'userinfo.token.claim'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'middleName',	'user.attribute'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'true',	'id.token.claim'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'true',	'access.token.claim'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'middle_name',	'claim.name'),
        ('b148a14b-35cf-498c-9642-0c75e81cee7d',	'String',	'jsonType.label'),
        ('be765428-8f40-4413-98dc-932af63f3f2a',	'true',	'userinfo.token.claim'),
        ('be765428-8f40-4413-98dc-932af63f3f2a',	'true',	'id.token.claim'),
        ('be765428-8f40-4413-98dc-932af63f3f2a',	'true',	'access.token.claim'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'true',	'userinfo.token.claim'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'username',	'user.attribute'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'true',	'id.token.claim'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'true',	'access.token.claim'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'preferred_username',	'claim.name'),
        ('e9e27a87-d9d1-4cda-8a83-939679f23503',	'String',	'jsonType.label'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'true',	'userinfo.token.claim'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'website',	'user.attribute'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'true',	'id.token.claim'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'true',	'access.token.claim'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'website',	'claim.name'),
        ('ed149125-04f9-4959-921e-83c4c67e504c',	'String',	'jsonType.label'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'true',	'userinfo.token.claim'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'picture',	'user.attribute'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'true',	'id.token.claim'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'true',	'access.token.claim'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'picture',	'claim.name'),
        ('f86a0be0-c083-4aa3-bf6f-0e810f3ab12d',	'String',	'jsonType.label'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'true',	'userinfo.token.claim'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'firstName',	'user.attribute'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'true',	'id.token.claim'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'true',	'access.token.claim'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'given_name',	'claim.name'),
        ('ff98b481-748d-4724-b2f8-781b7625e4bb',	'String',	'jsonType.label'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'true',	'userinfo.token.claim'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'email',	'user.attribute'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'true',	'id.token.claim'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'true',	'access.token.claim'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'email',	'claim.name'),
        ('4e464f91-d324-4781-86a9-d382c8ff4879',	'String',	'jsonType.label'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'true',	'userinfo.token.claim'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'emailVerified',	'user.attribute'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'true',	'id.token.claim'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'true',	'access.token.claim'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'email_verified',	'claim.name'),
        ('903a1dc7-c90c-44fc-9104-5ad1844d4463',	'boolean',	'jsonType.label'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'formatted',	'user.attribute.formatted'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'country',	'user.attribute.country'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'postal_code',	'user.attribute.postal_code'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'true',	'userinfo.token.claim'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'street',	'user.attribute.street'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'true',	'id.token.claim'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'region',	'user.attribute.region'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'true',	'access.token.claim'),
        ('4498620e-6b82-420c-9b63-736d2d2590d6',	'locality',	'user.attribute.locality'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'true',	'userinfo.token.claim'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'phoneNumberVerified',	'user.attribute'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'true',	'id.token.claim'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'true',	'access.token.claim'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'phone_number_verified',	'claim.name'),
        ('92345693-696a-4340-9fe0-307b7b033b79',	'boolean',	'jsonType.label'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'true',	'userinfo.token.claim'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'phoneNumber',	'user.attribute'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'true',	'id.token.claim'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'true',	'access.token.claim'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'phone_number',	'claim.name'),
        ('c86f3ec1-8ac8-4b53-b1e8-5c66fbc8d798',	'String',	'jsonType.label'),
        ('591c1e59-9926-457a-9a1d-36bc9dd68033',	'true',	'multivalued'),
        ('591c1e59-9926-457a-9a1d-36bc9dd68033',	'foo',	'user.attribute'),
        ('591c1e59-9926-457a-9a1d-36bc9dd68033',	'true',	'access.token.claim'),
        ('591c1e59-9926-457a-9a1d-36bc9dd68033',	'resource_access.${client_id}.roles',	'claim.name'),
        ('591c1e59-9926-457a-9a1d-36bc9dd68033',	'String',	'jsonType.label'),
        ('7e8877f7-ad3c-4f83-a7f6-5645a4acbc3b',	'true',	'multivalued'),
        ('7e8877f7-ad3c-4f83-a7f6-5645a4acbc3b',	'foo',	'user.attribute'),
        ('7e8877f7-ad3c-4f83-a7f6-5645a4acbc3b',	'true',	'access.token.claim'),
        ('7e8877f7-ad3c-4f83-a7f6-5645a4acbc3b',	'realm_access.roles',	'claim.name'),
        ('7e8877f7-ad3c-4f83-a7f6-5645a4acbc3b',	'String',	'jsonType.label'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'true',	'userinfo.token.claim'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'username',	'user.attribute'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'true',	'id.token.claim'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'true',	'access.token.claim'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'upn',	'claim.name'),
        ('1a7a6918-56be-4263-acfa-91de40a57575',	'String',	'jsonType.label'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'true',	'multivalued'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'foo',	'user.attribute'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'true',	'id.token.claim'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'true',	'access.token.claim'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'groups',	'claim.name'),
        ('df975cb1-3f82-4352-9897-f96b0e14990d',	'String',	'jsonType.label'),
        ('b36a1a46-37ec-42bb-adbe-dc937b46dba7',	'true',	'id.token.claim'),
        ('b36a1a46-37ec-42bb-adbe-dc937b46dba7',	'true',	'access.token.claim'),
        ('46d935cf-e0a0-4c9e-86b7-ab92202ad381',	'false',	'single'),
        ('46d935cf-e0a0-4c9e-86b7-ab92202ad381',	'Basic',	'attribute.nameformat'),
        ('46d935cf-e0a0-4c9e-86b7-ab92202ad381',	'Role',	'attribute.name'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'true',	'userinfo.token.claim'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'updatedAt',	'user.attribute'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'true',	'id.token.claim'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'true',	'access.token.claim'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'updated_at',	'claim.name'),
        ('012b4db0-7882-45b8-aa8f-28e714b3c532',	'long',	'jsonType.label'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'true',	'userinfo.token.claim'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'profile',	'user.attribute'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'true',	'id.token.claim'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'true',	'access.token.claim'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'profile',	'claim.name'),
        ('19cde79a-db8d-47b0-b5f6-e42de5b79716',	'String',	'jsonType.label'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'true',	'userinfo.token.claim'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'lastName',	'user.attribute'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'true',	'id.token.claim'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'true',	'access.token.claim'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'family_name',	'claim.name'),
        ('1d06a293-ffb0-49c4-a5cc-422dfe8701c2',	'String',	'jsonType.label'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'true',	'userinfo.token.claim'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'zoneinfo',	'user.attribute'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'true',	'id.token.claim'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'true',	'access.token.claim'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'zoneinfo',	'claim.name'),
        ('2199b069-0e3c-4738-b7b2-5d5d31a2f803',	'String',	'jsonType.label'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'true',	'userinfo.token.claim'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'nickname',	'user.attribute'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'true',	'id.token.claim'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'true',	'access.token.claim'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'nickname',	'claim.name'),
        ('2dcec2c5-f38a-4c00-9851-427c78281ffe',	'String',	'jsonType.label'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'true',	'userinfo.token.claim'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'firstName',	'user.attribute'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'true',	'id.token.claim'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'true',	'access.token.claim'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'given_name',	'claim.name'),
        ('4c2eeedf-c5ce-4002-8728-7d0272e5f6d2',	'String',	'jsonType.label'),
        ('4cbfb353-8fd8-49a7-a621-7899907a21c8',	'true',	'userinfo.token.claim'),
        ('4cbfb353-8fd8-49a7-a621-7899907a21c8',	'true',	'id.token.claim'),
        ('4cbfb353-8fd8-49a7-a621-7899907a21c8',	'true',	'access.token.claim'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'true',	'userinfo.token.claim'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'middleName',	'user.attribute'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'true',	'id.token.claim'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'true',	'access.token.claim'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'middle_name',	'claim.name'),
        ('4ce6c3ed-5bac-4023-93e9-dcb052d07617',	'String',	'jsonType.label'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'true',	'userinfo.token.claim'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'locale',	'user.attribute'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'true',	'id.token.claim'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'true',	'access.token.claim'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'locale',	'claim.name'),
        ('6571492a-2521-4d95-b3cd-d563f8070eb1',	'String',	'jsonType.label'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'true',	'userinfo.token.claim'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'picture',	'user.attribute'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'true',	'id.token.claim'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'true',	'access.token.claim'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'picture',	'claim.name'),
        ('8d101c9b-aebd-469c-8004-de0d5a22b142',	'String',	'jsonType.label'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'true',	'userinfo.token.claim'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'username',	'user.attribute'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'true',	'id.token.claim'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'true',	'access.token.claim'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'preferred_username',	'claim.name'),
        ('a1c15fd6-6aa6-49c3-899f-178e592f0fff',	'String',	'jsonType.label'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'true',	'userinfo.token.claim'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'gender',	'user.attribute'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'true',	'id.token.claim'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'true',	'access.token.claim'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'gender',	'claim.name'),
        ('a4c9aace-739f-4251-b69d-c02225e0fb1b',	'String',	'jsonType.label'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'true',	'userinfo.token.claim'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'birthdate',	'user.attribute'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'true',	'id.token.claim'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'true',	'access.token.claim'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'birthdate',	'claim.name'),
        ('b370acab-e540-436e-a9a6-fc51d0bd2ca3',	'String',	'jsonType.label'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'true',	'userinfo.token.claim'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'website',	'user.attribute'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'true',	'id.token.claim'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'true',	'access.token.claim'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'website',	'claim.name'),
        ('d446d42f-99b2-4038-a80e-c6395e127218',	'String',	'jsonType.label'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'true',	'userinfo.token.claim'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'email',	'user.attribute'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'true',	'id.token.claim'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'true',	'access.token.claim'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'email',	'claim.name'),
        ('e460b0a3-6b13-4888-9559-9a64f724abd8',	'String',	'jsonType.label'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'true',	'userinfo.token.claim'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'emailVerified',	'user.attribute'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'true',	'id.token.claim'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'true',	'access.token.claim'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'email_verified',	'claim.name'),
        ('f25aa3ca-6dd4-470a-b5c1-1409e1c59366',	'boolean',	'jsonType.label'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'formatted',	'user.attribute.formatted'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'country',	'user.attribute.country'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'postal_code',	'user.attribute.postal_code'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'true',	'userinfo.token.claim'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'street',	'user.attribute.street'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'true',	'id.token.claim'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'region',	'user.attribute.region'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'true',	'access.token.claim'),
        ('74558b34-2dd2-49c5-90c2-c05e7326f98d',	'locality',	'user.attribute.locality'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'true',	'userinfo.token.claim'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'phoneNumberVerified',	'user.attribute'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'true',	'id.token.claim'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'true',	'access.token.claim'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'phone_number_verified',	'claim.name'),
        ('e1041c94-8e65-4a78-976f-513a858474de',	'boolean',	'jsonType.label'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'true',	'userinfo.token.claim'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'phoneNumber',	'user.attribute'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'true',	'id.token.claim'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'true',	'access.token.claim'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'phone_number',	'claim.name'),
        ('ef78e13b-c336-4c1c-80c8-1fcee0bbb230',	'String',	'jsonType.label'),
        ('f9f06e1e-ed26-4636-b324-d167a19530c4',	'true',	'multivalued'),
        ('f9f06e1e-ed26-4636-b324-d167a19530c4',	'foo',	'user.attribute'),
        ('f9f06e1e-ed26-4636-b324-d167a19530c4',	'true',	'access.token.claim'),
        ('f9f06e1e-ed26-4636-b324-d167a19530c4',	'resource_access.${client_id}.roles',	'claim.name'),
        ('f9f06e1e-ed26-4636-b324-d167a19530c4',	'String',	'jsonType.label'),
        ('fec5d967-2501-4cfc-99b0-10e350e98e05',	'true',	'multivalued'),
        ('fec5d967-2501-4cfc-99b0-10e350e98e05',	'foo',	'user.attribute'),
        ('fec5d967-2501-4cfc-99b0-10e350e98e05',	'true',	'access.token.claim'),
        ('fec5d967-2501-4cfc-99b0-10e350e98e05',	'realm_access.roles',	'claim.name'),
        ('fec5d967-2501-4cfc-99b0-10e350e98e05',	'String',	'jsonType.label'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'true',	'userinfo.token.claim'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'username',	'user.attribute'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'true',	'id.token.claim'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'true',	'access.token.claim'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'upn',	'claim.name'),
        ('f982fae4-c941-493d-9b62-701e6c4d5bac',	'String',	'jsonType.label'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'true',	'multivalued'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'foo',	'user.attribute'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'true',	'id.token.claim'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'true',	'access.token.claim'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'groups',	'claim.name'),
        ('fb0a01bb-c9c4-4098-80e0-e68e4fdd951a',	'String',	'jsonType.label'),
        ('0062df60-8c28-45e4-bf55-72de87dce108',	'true',	'id.token.claim'),
        ('0062df60-8c28-45e4-bf55-72de87dce108',	'true',	'access.token.claim'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'true',	'userinfo.token.claim'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'locale',	'user.attribute'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'true',	'id.token.claim'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'true',	'access.token.claim'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'locale',	'claim.name'),
        ('c6e6dcfe-ec69-4afe-b41f-b9502b24e354',	'String',	'jsonType.label'),
        ('659f176e-0549-43b8-b9c1-dc17bdd8b65a',	'false',	'single'),
        ('659f176e-0549-43b8-b9c1-dc17bdd8b65a',	'Basic',	'attribute.nameformat'),
        ('659f176e-0549-43b8-b9c1-dc17bdd8b65a',	'Role',	'attribute.name'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'true',	'userinfo.token.claim'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'firstName',	'user.attribute'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'true',	'id.token.claim'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'true',	'access.token.claim'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'given_name',	'claim.name'),
        ('24ce4800-d216-4fee-9045-bdd096baac1f',	'String',	'jsonType.label'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'true',	'userinfo.token.claim'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'picture',	'user.attribute'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'true',	'id.token.claim'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'true',	'access.token.claim'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'picture',	'claim.name'),
        ('3a34b4e1-8412-41a9-85db-6061157d4f67',	'String',	'jsonType.label'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'true',	'userinfo.token.claim'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'profile',	'user.attribute'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'true',	'id.token.claim'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'true',	'access.token.claim'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'profile',	'claim.name'),
        ('3a5b0e7a-2d9d-414d-ba73-1005a40b0ce8',	'String',	'jsonType.label'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'true',	'userinfo.token.claim'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'zoneinfo',	'user.attribute'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'true',	'id.token.claim'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'true',	'access.token.claim'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'zoneinfo',	'claim.name'),
        ('48ec424b-3797-457d-80b9-2f9994daa798',	'String',	'jsonType.label'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'true',	'userinfo.token.claim'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'lastName',	'user.attribute'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'true',	'id.token.claim'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'true',	'access.token.claim'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'family_name',	'claim.name'),
        ('578f2b6b-596e-4ee9-bdcd-cd6631afd4f8',	'String',	'jsonType.label'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'true',	'userinfo.token.claim'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'middleName',	'user.attribute'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'true',	'id.token.claim'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'true',	'access.token.claim'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'middle_name',	'claim.name'),
        ('6bf0d4bc-322f-4a56-a59f-1a34ef731292',	'String',	'jsonType.label'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'true',	'userinfo.token.claim'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'locale',	'user.attribute'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'true',	'id.token.claim'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'true',	'access.token.claim'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'locale',	'claim.name'),
        ('7b37c22d-ea59-4832-a034-ee3c6b46d40f',	'String',	'jsonType.label'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'true',	'userinfo.token.claim'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'username',	'user.attribute'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'true',	'id.token.claim'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'true',	'access.token.claim'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'preferred_username',	'claim.name'),
        ('7d002e1f-18a0-4595-a598-f64fb29fe5db',	'String',	'jsonType.label'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'true',	'userinfo.token.claim'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'updatedAt',	'user.attribute'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'true',	'id.token.claim'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'true',	'access.token.claim'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'updated_at',	'claim.name'),
        ('8730be16-7b32-42c8-b1fa-0d8496d18061',	'long',	'jsonType.label'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'true',	'userinfo.token.claim'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'nickname',	'user.attribute'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'true',	'id.token.claim'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'true',	'access.token.claim'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'nickname',	'claim.name'),
        ('aa001376-7ca0-499f-8ee8-f1e287fd4ac2',	'String',	'jsonType.label'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'true',	'userinfo.token.claim'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'birthdate',	'user.attribute'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'true',	'id.token.claim'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'true',	'access.token.claim'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'birthdate',	'claim.name'),
        ('d89f2c3d-e034-49ee-9d25-e18767ef762f',	'String',	'jsonType.label'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'true',	'userinfo.token.claim'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'gender',	'user.attribute'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'true',	'id.token.claim'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'true',	'access.token.claim'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'gender',	'claim.name'),
        ('e831a140-5b6f-4561-9716-7f731e0eec7f',	'String',	'jsonType.label'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'true',	'userinfo.token.claim'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'website',	'user.attribute'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'true',	'id.token.claim'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'true',	'access.token.claim'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'website',	'claim.name'),
        ('f897297f-cf13-4b45-b04b-4f4c9c47c1db',	'String',	'jsonType.label'),
        ('fa26fa4c-6511-4b65-a744-410e2143f6d3',	'true',	'userinfo.token.claim'),
        ('fa26fa4c-6511-4b65-a744-410e2143f6d3',	'true',	'id.token.claim'),
        ('fa26fa4c-6511-4b65-a744-410e2143f6d3',	'true',	'access.token.claim'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'true',	'userinfo.token.claim'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'emailVerified',	'user.attribute'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'true',	'id.token.claim'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'true',	'access.token.claim'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'email_verified',	'claim.name'),
        ('8b904dfc-88f9-47ca-becd-3d9b26fa3612',	'boolean',	'jsonType.label'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'true',	'userinfo.token.claim'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'email',	'user.attribute'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'true',	'id.token.claim'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'true',	'access.token.claim'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'email',	'claim.name'),
        ('bf272bbc-9a2f-4f09-83a2-026eb9e33db7',	'String',	'jsonType.label'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'formatted',	'user.attribute.formatted'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'country',	'user.attribute.country'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'postal_code',	'user.attribute.postal_code'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'true',	'userinfo.token.claim'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'street',	'user.attribute.street'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'true',	'id.token.claim'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'region',	'user.attribute.region'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'true',	'access.token.claim'),
        ('daad4880-3cd9-4902-afd2-517a96f5c0e5',	'locality',	'user.attribute.locality'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'true',	'userinfo.token.claim'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'phoneNumberVerified',	'user.attribute'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'true',	'id.token.claim'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'true',	'access.token.claim'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'phone_number_verified',	'claim.name'),
        ('6e25c04d-12fb-41ff-af0e-6ac9a1a34e0b',	'boolean',	'jsonType.label'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'true',	'userinfo.token.claim'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'phoneNumber',	'user.attribute'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'true',	'id.token.claim'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'true',	'access.token.claim'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'phone_number',	'claim.name'),
        ('e04eb4c4-0828-49f3-8426-f362755f1667',	'String',	'jsonType.label'),
        ('557d2aab-d25b-41c7-a4cc-d419597849d8',	'true',	'multivalued'),
        ('557d2aab-d25b-41c7-a4cc-d419597849d8',	'foo',	'user.attribute'),
        ('557d2aab-d25b-41c7-a4cc-d419597849d8',	'true',	'access.token.claim'),
        ('557d2aab-d25b-41c7-a4cc-d419597849d8',	'resource_access.${client_id}.roles',	'claim.name'),
        ('557d2aab-d25b-41c7-a4cc-d419597849d8',	'String',	'jsonType.label'),
        ('afee4528-b497-4e95-aa7a-f80fa9a759dc',	'true',	'multivalued'),
        ('afee4528-b497-4e95-aa7a-f80fa9a759dc',	'foo',	'user.attribute'),
        ('afee4528-b497-4e95-aa7a-f80fa9a759dc',	'true',	'access.token.claim'),
        ('afee4528-b497-4e95-aa7a-f80fa9a759dc',	'realm_access.roles',	'claim.name'),
        ('afee4528-b497-4e95-aa7a-f80fa9a759dc',	'String',	'jsonType.label'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'true',	'userinfo.token.claim'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'username',	'user.attribute'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'true',	'id.token.claim'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'true',	'access.token.claim'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'upn',	'claim.name'),
        ('1bf96d43-3160-4c9e-ac88-662c59825224',	'String',	'jsonType.label'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'true',	'multivalued'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'foo',	'user.attribute'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'true',	'id.token.claim'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'true',	'access.token.claim'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'groups',	'claim.name'),
        ('5ad720dd-b11d-4585-8927-649dff3ffc42',	'String',	'jsonType.label'),
        ('f632f3b5-c24b-4060-8536-1cf5c8909dfd',	'true',	'id.token.claim'),
        ('f632f3b5-c24b-4060-8536-1cf5c8909dfd',	'true',	'access.token.claim'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'true',	'userinfo.token.claim'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'locale',	'user.attribute'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'true',	'id.token.claim'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'true',	'access.token.claim'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'locale',	'claim.name'),
        ('c2ec7519-2f84-4d21-8f10-88ab8640511d',	'String',	'jsonType.label');

        CREATE TABLE IF NOT EXISTS "public"."realm" (
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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_orvsdmla56612eaefiq6wl5oi ON public.realm USING btree (name);

        CREATE INDEX IF NOT EXISTS idx_realm_master_adm_cli ON public.realm USING btree (master_admin_client);

        INSERT INTO "realm" ("id", "access_code_lifespan", "user_action_lifespan", "access_token_lifespan", "account_theme", "admin_theme", "email_theme", "enabled", "events_enabled", "events_expiration", "login_theme", "name", "not_before", "password_policy", "registration_allowed", "remember_me", "reset_password_allowed", "social", "ssl_required", "sso_idle_timeout", "sso_max_lifespan", "update_profile_on_soc_login", "verify_email", "master_admin_client", "login_lifespan", "internationalization_enabled", "default_locale", "reg_email_as_username", "admin_events_enabled", "admin_events_details_enabled", "edit_username_allowed", "otp_policy_counter", "otp_policy_window", "otp_policy_period", "otp_policy_digits", "otp_policy_alg", "otp_policy_type", "browser_flow", "registration_flow", "direct_grant_flow", "reset_credentials_flow", "client_auth_flow", "offline_session_idle_timeout", "revoke_refresh_token", "access_token_life_implicit", "login_with_email_allowed", "duplicate_emails_allowed", "docker_auth_flow", "refresh_token_max_reuse", "allow_user_managed_access", "sso_max_lifespan_remember_me", "sso_idle_timeout_remember_me", "default_role") VALUES
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	60,	300,	60,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'master',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'284f063e-1ad4-4c28-8243-9e181600363f',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'abf0b79f-cb5f-4d3d-910b-6384ae8e49a7',	'ca19c414-05fc-464a-a500-6ba676768e69',	'a7bccc73-600e-48a7-9fb3-84bb95256599',	'8afc682d-8c78-4fb6-834c-e0625a4b0d9e',	'18dca5e1-350a-4a9d-9acb-9c0d5bf919c6',	2592000,	'0',	900,	'1',	'0',	'dd38a4bf-9af2-4f76-998c-115d3197bbe9',	0,	'0',	0,	0,	'0eb7f97a-bb13-4666-8ef2-fe28d37dff01'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	60,	300,	300,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'tenant-manager',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'ce4af291-185f-4a80-a74a-bccab0accfa3',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'6cb9ed68-6ca4-41ac-ace6-596b392bbaea',	'ef1b5db8-4eaa-4e17-8f89-86487af81789',	'e4bcd017-fa57-4f51-9327-a9a74dc3cda6',	'fd4dc7bd-4022-49bb-806b-85dfb41b8f3e',	'79af612f-c9f2-4b71-93df-394b73dbea39',	2592000,	'0',	900,	'1',	'0',	'808dba35-dc5d-413d-9160-740e05897f63',	0,	'0',	0,	0,	'6899cefd-5fbb-41e2-82c4-919f0a34f170'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	60,	300,	300,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'openk9',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'25e52d96-d21a-4743-b6b7-32b947f0bfdc',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'923d001f-1282-4cb6-8f44-e46b876d2cd6',	'0566cc92-8616-4097-b667-5ddb691b79b0',	'62f0f7ac-b435-4c86-bebe-782964eba51e',	'a6b03e9a-cf71-4fbb-82a5-c5c20359ea87',	'a216a725-432f-45e8-9d25-ca1281693434',	2592000,	'0',	900,	'1',	'0',	'38e8e54c-af57-488d-ac97-b6a9b45317ba',	0,	'0',	0,	0,	'f749b549-08fa-48ce-87f8-9df49f4c2bfa');

        CREATE TABLE IF NOT EXISTS "public"."realm_attribute" (
            "name" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_9" PRIMARY KEY ("name", "realm_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_realm_attr_realm ON public.realm_attribute USING btree (realm_id);

        INSERT INTO "realm_attribute" ("name", "realm_id", "value") VALUES
        ('_browser_header.contentSecurityPolicyReportOnly',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	''),
        ('_browser_header.xContentTypeOptions',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'nosniff'),
        ('_browser_header.xRobotsTag',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'none'),
        ('_browser_header.xFrameOptions',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'false'),
        ('permanentLockout',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'false'),
        ('maxFailureWaitSeconds',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'900'),
        ('minimumQuickLoginWaitSeconds',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'60'),
        ('waitIncrementSeconds',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'60'),
        ('quickLoginCheckMilliSeconds',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1000'),
        ('maxDeltaTimeSeconds',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'43200'),
        ('failureFactor',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'30'),
        ('realmReusableOtpCode',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'false'),
        ('displayName',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'Keycloak'),
        ('displayNameHtml',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'<div class="kc-logo-text"><span>Keycloak</span></div>'),
        ('defaultSignatureAlgorithm',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'false'),
        ('offlineSessionMaxLifespan',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'5184000'),
        ('_browser_header.contentSecurityPolicyReportOnly',	'8057e71d-86e9-4b84-8438-269c52eea27b',	''),
        ('_browser_header.xContentTypeOptions',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'nosniff'),
        ('_browser_header.xRobotsTag',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'none'),
        ('_browser_header.xFrameOptions',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'false'),
        ('permanentLockout',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'false'),
        ('maxFailureWaitSeconds',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'900'),
        ('minimumQuickLoginWaitSeconds',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'60'),
        ('waitIncrementSeconds',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'60'),
        ('quickLoginCheckMilliSeconds',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1000'),
        ('maxDeltaTimeSeconds',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'43200'),
        ('failureFactor',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'30'),
        ('realmReusableOtpCode',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'false'),
        ('defaultSignatureAlgorithm',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'false'),
        ('offlineSessionMaxLifespan',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'5184000'),
        ('actionTokenGeneratedByAdminLifespan',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'43200'),
        ('actionTokenGeneratedByUserLifespan',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'300'),
        ('oauth2DeviceCodeLifespan',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'600'),
        ('oauth2DevicePollingInterval',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'5'),
        ('webAuthnPolicyRpEntityName',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithms',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'ES256'),
        ('webAuthnPolicyRpId',	'8057e71d-86e9-4b84-8438-269c52eea27b',	''),
        ('webAuthnPolicyAttestationConveyancePreference',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachment',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyRequireResidentKey',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirement',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyCreateTimeout',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegister',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'false'),
        ('webAuthnPolicyRpEntityNamePasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithmsPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'ES256'),
        ('webAuthnPolicyRpIdPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	''),
        ('webAuthnPolicyAttestationConveyancePreferencePasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachmentPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyRequireResidentKeyPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirementPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'not specified'),
        ('webAuthnPolicyCreateTimeoutPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'false'),
        ('cibaBackchannelTokenDeliveryMode',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'poll'),
        ('cibaExpiresIn',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'120'),
        ('cibaInterval',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'5'),
        ('cibaAuthRequestedUserHint',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'login_hint'),
        ('parRequestUriLifespan',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'60'),
        ('_browser_header.contentSecurityPolicyReportOnly',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	''),
        ('_browser_header.xContentTypeOptions',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'nosniff'),
        ('_browser_header.xRobotsTag',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'none'),
        ('_browser_header.xFrameOptions',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'false'),
        ('permanentLockout',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'false'),
        ('maxFailureWaitSeconds',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'900'),
        ('minimumQuickLoginWaitSeconds',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'60'),
        ('waitIncrementSeconds',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'60'),
        ('quickLoginCheckMilliSeconds',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1000'),
        ('maxDeltaTimeSeconds',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'43200'),
        ('failureFactor',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'30'),
        ('realmReusableOtpCode',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'false'),
        ('defaultSignatureAlgorithm',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'false'),
        ('offlineSessionMaxLifespan',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'5184000'),
        ('actionTokenGeneratedByAdminLifespan',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'43200'),
        ('actionTokenGeneratedByUserLifespan',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'300'),
        ('oauth2DeviceCodeLifespan',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'600'),
        ('oauth2DevicePollingInterval',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'5'),
        ('webAuthnPolicyRpEntityName',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithms',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'ES256'),
        ('webAuthnPolicyRpId',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	''),
        ('webAuthnPolicyAttestationConveyancePreference',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachment',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyRequireResidentKey',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirement',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyCreateTimeout',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegister',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'false'),
        ('webAuthnPolicyRpEntityNamePasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithmsPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'ES256'),
        ('webAuthnPolicyRpIdPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	''),
        ('webAuthnPolicyAttestationConveyancePreferencePasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachmentPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyRequireResidentKeyPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirementPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'not specified'),
        ('webAuthnPolicyCreateTimeoutPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'false'),
        ('cibaBackchannelTokenDeliveryMode',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'poll'),
        ('cibaExpiresIn',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'120'),
        ('cibaInterval',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'5'),
        ('cibaAuthRequestedUserHint',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'login_hint'),
        ('parRequestUriLifespan',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'60');

        CREATE TABLE IF NOT EXISTS "public"."realm_default_groups" (
            "realm_id" character varying(36) NOT NULL,
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constr_realm_default_groups" PRIMARY KEY ("realm_id", "group_id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX con_group_id_def_groups ON public.realm_default_groups USING btree (group_id);

        CREATE INDEX IF NOT EXISTS idx_realm_def_grp_realm ON public.realm_default_groups USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."realm_enabled_event_types" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_enabl_event_types" PRIMARY KEY ("realm_id", "value")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_realm_evt_types_realm ON public.realm_enabled_event_types USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."realm_events_listeners" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_events_listeners" PRIMARY KEY ("realm_id", "value")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_realm_evt_list_realm ON public.realm_events_listeners USING btree (realm_id);

        INSERT INTO "realm_events_listeners" ("realm_id", "value") VALUES
        ('876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'jboss-logging'),
        ('8057e71d-86e9-4b84-8438-269c52eea27b',	'jboss-logging'),
        ('5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'jboss-logging');

        CREATE TABLE IF NOT EXISTS "public"."realm_localizations" (
            "realm_id" character varying(255) NOT NULL,
            "locale" character varying(255) NOT NULL,
            "texts" text NOT NULL,
            CONSTRAINT "realm_localizations_pkey" PRIMARY KEY ("realm_id", "locale")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."realm_required_credential" (
            "type" character varying(255) NOT NULL,
            "form_label" character varying(255),
            "input" boolean DEFAULT false NOT NULL,
            "secret" boolean DEFAULT false NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_92" PRIMARY KEY ("realm_id", "type")
        ) WITH (oids = false);

        INSERT INTO "realm_required_credential" ("type", "form_label", "input", "secret", "realm_id") VALUES
        ('password',	'password',	'1',	'1',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5'),
        ('password',	'password',	'1',	'1',	'8057e71d-86e9-4b84-8438-269c52eea27b'),
        ('password',	'password',	'1',	'1',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e');

        CREATE TABLE IF NOT EXISTS "public"."realm_smtp_config" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_e" PRIMARY KEY ("realm_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."realm_supported_locales" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_supported_locales" PRIMARY KEY ("realm_id", "value")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_realm_supp_local_realm ON public.realm_supported_locales USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."redirect_uris" (
            "client_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_redirect_uris" PRIMARY KEY ("client_id", "value")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_redir_uri_client ON public.redirect_uris USING btree (client_id);

        INSERT INTO "redirect_uris" ("client_id", "value") VALUES
        ('df39997b-873d-441b-8c40-4c85a5648424',	'/realms/master/account/*'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'/realms/master/account/*'),
        ('9306c727-41dc-4892-873e-e3852008de32',	'/admin/master/console/*'),
        ('b8e4d79a-1cf3-4bc9-b127-50533f4eacb2',	'/realms/tenant-manager/account/*'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'/realms/tenant-manager/account/*'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'/admin/tenant-manager/console/*'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'http://tenant.openk9.local/*'),
        ('44e9e0f3-5502-4761-b209-998860155253',	'/realms/openk9/account/*'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'/realms/openk9/account/*'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'/admin/openk9/console/*'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'http://demo.openk9.localhost/*');

        CREATE TABLE IF NOT EXISTS "public"."required_action_config" (
            "required_action_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_req_act_cfg_pk" PRIMARY KEY ("required_action_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."required_action_provider" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "name" character varying(255),
            "realm_id" character varying(36),
            "enabled" boolean DEFAULT false NOT NULL,
            "default_action" boolean DEFAULT false NOT NULL,
            "provider_id" character varying(255),
            "priority" integer,
            CONSTRAINT "constraint_req_act_prv_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_req_act_prov_realm ON public.required_action_provider USING btree (realm_id);

        INSERT INTO "required_action_provider" ("id", "alias", "name", "realm_id", "enabled", "default_action", "provider_id", "priority") VALUES
        ('57cdc012-b2a2-4ccb-852d-2ac572b6e587',	'VERIFY_EMAIL',	'Verify Email',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('4db7c30b-5274-4c10-8ba4-638f0e199616',	'UPDATE_PROFILE',	'Update Profile',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('2769250a-2128-41fd-a376-f071fd27253b',	'CONFIGURE_TOTP',	'Configure OTP',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('24525985-f36f-47ea-a8ab-5d6e765fa6a2',	'UPDATE_PASSWORD',	'Update Password',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('5d738fcc-ae89-4ad8-8be4-69675b754625',	'terms_and_conditions',	'Terms and Conditions',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'0',	'terms_and_conditions',	20),
        ('e817d3e7-a578-4659-9e7f-48604261402c',	'delete_account',	'Delete Account',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'0',	'0',	'delete_account',	60),
        ('30a91ed2-d055-4a20-b202-5297ea93d617',	'update_user_locale',	'Update User Locale',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'update_user_locale',	1000),
        ('7387c370-bc14-475f-95c6-c011d6fa148d',	'webauthn-register',	'Webauthn Register',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'webauthn-register',	70),
        ('a546fead-202c-4dbf-9d7c-816cb1cf9e8f',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'1',	'0',	'webauthn-register-passwordless',	80),
        ('b99450ed-d6fa-457a-b01d-b6377e93ba20',	'VERIFY_EMAIL',	'Verify Email',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('caf865f9-19e5-4591-8b64-54629754ad67',	'UPDATE_PROFILE',	'Update Profile',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('eda81b0d-f07a-48ed-b3fa-94d0d317b688',	'CONFIGURE_TOTP',	'Configure OTP',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('ab4ee083-9bd0-4eac-8f48-8b813b2f56ea',	'UPDATE_PASSWORD',	'Update Password',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('bff4c166-413e-4c1b-a939-c52f865225f5',	'terms_and_conditions',	'Terms and Conditions',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0',	'0',	'terms_and_conditions',	20),
        ('2d812034-847f-470c-8101-f63f6bac32d6',	'delete_account',	'Delete Account',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'0',	'0',	'delete_account',	60),
        ('d1dcaf81-04ee-42aa-adbb-4d90c237a2c9',	'update_user_locale',	'Update User Locale',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'update_user_locale',	1000),
        ('69b0c033-5d1e-4bf5-bf34-a8b26c14cdff',	'webauthn-register',	'Webauthn Register',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'webauthn-register',	70),
        ('87fa5e79-b3cd-4bf9-bd27-9ee84f1cb484',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'1',	'0',	'webauthn-register-passwordless',	80),
        ('673ec312-846f-4776-adaf-bfcd8c4ead89',	'VERIFY_EMAIL',	'Verify Email',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('568f2b7d-9df3-43eb-8431-b8abc0238c24',	'UPDATE_PROFILE',	'Update Profile',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('16deef9d-7a9c-4c23-aee5-54397cc06a69',	'CONFIGURE_TOTP',	'Configure OTP',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('d09569bb-68bc-4c41-8674-f307bc082b04',	'UPDATE_PASSWORD',	'Update Password',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('0a249c89-b49f-4684-a22f-6a4e04983a27',	'terms_and_conditions',	'Terms and Conditions',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'0',	'terms_and_conditions',	20),
        ('9c1b7a70-3656-41fd-b1e6-c7ad1905082a',	'delete_account',	'Delete Account',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'0',	'0',	'delete_account',	60),
        ('a31b7cdf-d1fd-4c02-9cba-b99728bfc90f',	'update_user_locale',	'Update User Locale',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'update_user_locale',	1000),
        ('7187c7c3-e460-4bcc-ad77-5d8a737f5e1f',	'webauthn-register',	'Webauthn Register',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'webauthn-register',	70),
        ('6d3365b8-b63d-405e-b94c-633152a1dd6a',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'1',	'0',	'webauthn-register-passwordless',	80);

        CREATE TABLE IF NOT EXISTS "public"."resource_attribute" (
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "resource_id" character varying(36) NOT NULL,
            CONSTRAINT "res_attr_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."resource_policy" (
            "resource_id" character varying(36) NOT NULL,
            "policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrpp" PRIMARY KEY ("resource_id", "policy_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_res_policy_policy ON public.resource_policy USING btree (policy_id);


        CREATE TABLE IF NOT EXISTS "public"."resource_scope" (
            "resource_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrsp" PRIMARY KEY ("resource_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_res_scope_scope ON public.resource_scope USING btree (scope_id);


        CREATE TABLE IF NOT EXISTS "public"."resource_server" (
            "id" character varying(36) NOT NULL,
            "allow_rs_remote_mgmt" boolean DEFAULT false NOT NULL,
            "policy_enforce_mode" character varying(15) NOT NULL,
            "decision_strategy" smallint DEFAULT '1' NOT NULL,
            CONSTRAINT "pk_resource_server" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."resource_server_perm_ticket" (
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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsr6t700s9v50bu18ws5pmt ON public.resource_server_perm_ticket USING btree (owner, requester, resource_server_id, resource_id, scope_id);


        CREATE TABLE IF NOT EXISTS "public"."resource_server_policy" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "description" character varying(255),
            "type" character varying(255) NOT NULL,
            "decision_strategy" character varying(20),
            "logic" character varying(20),
            "resource_server_id" character varying(36) NOT NULL,
            "owner" character varying(255),
            CONSTRAINT "constraint_farsrp" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsrpt700s9v50bu18ws5ha6 ON public.resource_server_policy USING btree (name, resource_server_id);

        CREATE INDEX IF NOT EXISTS idx_res_serv_pol_res_serv ON public.resource_server_policy USING btree (resource_server_id);


        CREATE TABLE IF NOT EXISTS "public"."resource_server_resource" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "type" character varying(255),
            "icon_uri" character varying(255),
            "owner" character varying(255) NOT NULL,
            "resource_server_id" character varying(36) NOT NULL,
            "owner_managed_access" boolean DEFAULT false NOT NULL,
            "display_name" character varying(255),
            CONSTRAINT "constraint_farsr" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_res_srv_res_res_srv ON public.resource_server_resource USING btree (resource_server_id);

        CREATE UNIQUE INDEX uk_frsr6t700s9v50bu18ws5ha6 ON public.resource_server_resource USING btree (name, owner, resource_server_id);


        CREATE TABLE IF NOT EXISTS "public"."resource_server_scope" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "icon_uri" character varying(255),
            "resource_server_id" character varying(36) NOT NULL,
            "display_name" character varying(255),
            CONSTRAINT "constraint_farsrs" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsrst700s9v50bu18ws5ha6 ON public.resource_server_scope USING btree (name, resource_server_id);

        CREATE INDEX IF NOT EXISTS idx_res_srv_scope_res_srv ON public.resource_server_scope USING btree (resource_server_id);


        CREATE TABLE IF NOT EXISTS "public"."resource_uris" (
            "resource_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_resour_uris_pk" PRIMARY KEY ("resource_id", "value")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."role_attribute" (
            "id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "constraint_role_attribute_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_role_attribute ON public.role_attribute USING btree (role_id);


        CREATE TABLE IF NOT EXISTS "public"."scope_mapping" (
            "client_id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_81" PRIMARY KEY ("client_id", "role_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_scope_mapping_role ON public.scope_mapping USING btree (role_id);

        INSERT INTO "scope_mapping" ("client_id", "role_id") VALUES
        ('3881304e-d247-4373-a143-07d3096f04d2',	'676508a8-16d1-4519-a907-6e46d58a2559'),
        ('3881304e-d247-4373-a143-07d3096f04d2',	'61e9e17e-85c9-4002-806b-8e2931699962'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'9f34a225-8156-4995-a6f1-1a3a51bcc5ef'),
        ('23a52d4c-249a-47b3-8d98-cc8b48188a93',	'0b456db2-a080-48c6-86c7-98bbee5426a8'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'932ba3ac-fac4-4e03-b5dd-486527d8a6cb'),
        ('8df73544-8837-4c85-92c8-d22005f498e4',	'1ed0867f-b4e2-4d27-9aca-d0b002079bd2');

        CREATE TABLE IF NOT EXISTS "public"."scope_policy" (
            "scope_id" character varying(36) NOT NULL,
            "policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrsps" PRIMARY KEY ("scope_id", "policy_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_scope_policy_policy ON public.scope_policy USING btree (policy_id);


        CREATE TABLE IF NOT EXISTS "public"."user_attribute" (
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "user_id" character varying(36) NOT NULL,
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            CONSTRAINT "constraint_user_attribute_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_user_attribute ON public.user_attribute USING btree (user_id);

        CREATE INDEX IF NOT EXISTS idx_user_attribute_name ON public.user_attribute USING btree (name, value);


        CREATE TABLE IF NOT EXISTS "public"."user_consent" (
            "id" character varying(36) NOT NULL,
            "client_id" character varying(255),
            "user_id" character varying(36) NOT NULL,
            "created_date" bigint,
            "last_updated_date" bigint,
            "client_storage_provider" character varying(36),
            "external_client_id" character varying(255),
            CONSTRAINT "constraint_grntcsnt_pm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_user_consent ON public.user_consent USING btree (user_id);

        CREATE UNIQUE INDEX uk_jkuwuvd56ontgsuhogm8uewrt ON public.user_consent USING btree (client_id, client_storage_provider, external_client_id, user_id);


        CREATE TABLE IF NOT EXISTS "public"."user_consent_client_scope" (
            "user_consent_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_grntcsnt_clsc_pm" PRIMARY KEY ("user_consent_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_usconsent_clscope ON public.user_consent_client_scope USING btree (user_consent_id);


        CREATE TABLE IF NOT EXISTS "public"."user_entity" (
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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_dykn684sl8up1crfei6eckhd7 ON public.user_entity USING btree (realm_id, email_constraint);

        CREATE INDEX IF NOT EXISTS idx_user_email ON public.user_entity USING btree (email);

        CREATE UNIQUE INDEX uk_ru8tt6t700s9v50bu18ws5ha6 ON public.user_entity USING btree (realm_id, username);

        CREATE INDEX IF NOT EXISTS idx_user_service_account ON public.user_entity USING btree (realm_id, service_account_client_link);

        INSERT INTO "user_entity" ("id", "email", "email_constraint", "email_verified", "enabled", "federation_link", "first_name", "last_name", "realm_id", "username", "created_timestamp", "service_account_client_link", "not_before") VALUES
        ('9847b647-ccd1-45f4-9f8e-4f9f671fcfc3',	NULL,	'0c2e2388-d30a-4668-b2d2-272fa2500c5a',	'0',	'1',	NULL,	NULL,	NULL,	'876c6e89-09aa-4210-95fb-1ab5f9edb7f5',	'user',	1745776091297,	NULL,	0),
        ('56e6c86d-b39a-4eca-9f06-2e2df17e82c9',	NULL,	'676f3c95-d611-441a-8989-0c9aefcecc1a',	'0',	'1',	NULL,	'',	'',	'8057e71d-86e9-4b84-8438-269c52eea27b',	'tenant-manager-admin',	1745776136353,	NULL,	0),
        ('9edce24d-8235-4740-8bd4-c33679eacf88',	NULL,	'dd7b4a99-8fdb-4878-8576-3e6772140259',	'0',	'1',	NULL,	'',	'',	'5431cc9a-0641-4c2b-96d9-ed10965b5e5e',	'k9admin',	1745778086920,	NULL,	0);

        CREATE TABLE IF NOT EXISTS "public"."user_federation_config" (
            "user_federation_provider_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_f9" PRIMARY KEY ("user_federation_provider_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."user_federation_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "federation_provider_id" character varying(36) NOT NULL,
            "federation_mapper_type" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_fedmapperpm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_usr_fed_map_fed_prv ON public.user_federation_mapper USING btree (federation_provider_id);

        CREATE INDEX IF NOT EXISTS idx_usr_fed_map_realm ON public.user_federation_mapper USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."user_federation_mapper_config" (
            "user_federation_mapper_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_fedmapper_cfg_pm" PRIMARY KEY ("user_federation_mapper_id", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."user_federation_provider" (
            "id" character varying(36) NOT NULL,
            "changed_sync_period" integer,
            "display_name" character varying(255),
            "full_sync_period" integer,
            "last_sync" integer,
            "priority" integer,
            "provider_name" character varying(255),
            "realm_id" character varying(36),
            CONSTRAINT "constraint_5c" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_usr_fed_prv_realm ON public.user_federation_provider USING btree (realm_id);


        CREATE TABLE IF NOT EXISTS "public"."user_group_membership" (
            "group_id" character varying(36) NOT NULL,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_user_group" PRIMARY KEY ("group_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_user_group_mapping ON public.user_group_membership USING btree (user_id);


        CREATE TABLE IF NOT EXISTS "public"."user_required_action" (
            "user_id" character varying(36) NOT NULL,
            "required_action" character varying(255) DEFAULT ' ' NOT NULL,
            CONSTRAINT "constraint_required_action" PRIMARY KEY ("required_action", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_user_reqactions ON public.user_required_action USING btree (user_id);


        CREATE TABLE IF NOT EXISTS "public"."user_role_mapping" (
            "role_id" character varying(255) NOT NULL,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_c" PRIMARY KEY ("role_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_user_role_mapping ON public.user_role_mapping USING btree (user_id);

        INSERT INTO "user_role_mapping" ("role_id", "user_id") VALUES
        ('0eb7f97a-bb13-4666-8ef2-fe28d37dff01',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('752dbaed-7327-4edd-b751-b3479a5277bf',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('999b4793-f9cb-4f87-85df-268540465280',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('a9a5d436-92cd-49c9-a1a2-2dc1aeb2286d',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('324f0087-cfc8-4735-8047-a6d095e9b29b',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('212de463-3a92-4524-9460-ab727300ff8b',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('6d765ab0-6165-4363-9550-0f987614cb2b',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('da91fc7d-4291-4122-99ec-545d59ddef2e',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('c82615ec-4565-4d5f-90b8-94cd738bd52f',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('518b47ba-5111-4c11-bebe-bcbca103b716',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('f23e7e75-ecd4-4dba-9bba-42383fa7c775',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('2fa9ae60-e0f9-4c97-82e4-d4cacd65b10c',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('4d30f7f1-ba8a-4a17-b714-eeece44888a1',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('14b94361-4613-412f-b03d-24836f38920a',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('baed571a-2c21-4a14-b504-61f0f2d4f015',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('52f26c30-0d74-4476-a0a5-84ab09ebac02',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('f376db7d-a9c1-407c-a73c-2ea9a9a6c15c',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('d909e0e4-def4-414f-a03e-39e777a41e0d',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('1ea09efe-60c8-4b18-a22c-d2d6d322fadd',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('6899cefd-5fbb-41e2-82c4-919f0a34f170',	'56e6c86d-b39a-4eca-9f06-2e2df17e82c9'),
        ('52ede75a-b30d-4fa7-991b-2c88ddffda25',	'56e6c86d-b39a-4eca-9f06-2e2df17e82c9'),
        ('e22a2f5f-b2e2-4f3c-9470-6d20b583a734',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('c821717f-ac1a-4460-a2d8-b0120c2aeaab',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('7a50bc1e-7913-4785-8155-bafb0301f7bf',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('1b7c75ab-36aa-456f-90d1-86a7128c3fe9',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('bbcd6f9e-3fcc-4914-a4c9-d89226874f84',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('2862ecff-b7e7-41b7-b28e-8f560cfd39f4',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('2d3e81af-f0ee-4686-85ec-0aa19949c09a',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('6251e530-7e1f-41ef-b7ef-f7f7331c47c5',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('d177f479-d57f-44f0-aa00-772631313f70',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('2315d12c-fe68-4ee5-b603-a7b2df33cb12',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('9e5daa7b-20b9-4043-ac83-4bef3d0c1b96',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('e90a49c9-9f13-40ec-902f-f1d5f61a3768',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('2ce2e238-e733-4cdb-951e-e053786fc242',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('dbaeb2c9-a2d0-42a6-92fa-59368a3ba5f4',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('ccbb8eca-4683-4112-8db8-ca7e4a0512d6',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('6536f0e5-d16d-4880-aa23-3e70af704ba1',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('7cb7caf1-2b3f-4adb-adac-154a76092738',	'9847b647-ccd1-45f4-9f8e-4f9f671fcfc3'),
        ('f749b549-08fa-48ce-87f8-9df49f4c2bfa',	'9edce24d-8235-4740-8bd4-c33679eacf88'),
        ('69c0a37c-4a1d-4ca5-be5e-9b5c1ff6dff8',	'9edce24d-8235-4740-8bd4-c33679eacf88'),
        ('65acabcd-a3d4-4429-a5fd-33013fb11485',	'9edce24d-8235-4740-8bd4-c33679eacf88'),
        ('0137b641-bd6f-40d5-839b-2d916d1d10c2',	'9edce24d-8235-4740-8bd4-c33679eacf88');

        CREATE TABLE IF NOT EXISTS "public"."user_session" (
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
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."user_session_note" (
            "user_session" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(2048),
            CONSTRAINT "constraint_usn_pk" PRIMARY KEY ("user_session", "name")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."username_login_failure" (
            "realm_id" character varying(36) NOT NULL,
            "username" character varying(255) NOT NULL,
            "failed_login_not_before" integer,
            "last_failure" bigint,
            "last_ip_failure" character varying(255),
            "num_failures" integer,
            CONSTRAINT "CONSTRAINT_17-2" PRIMARY KEY ("realm_id", "username")
        ) WITH (oids = false);


        CREATE TABLE IF NOT EXISTS "public"."web_origins" (
            "client_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_web_origins" PRIMARY KEY ("client_id", "value")
        ) WITH (oids = false);

        CREATE INDEX IF NOT EXISTS idx_web_orig_client ON public.web_origins USING btree (client_id);

        INSERT INTO "web_origins" ("client_id", "value") VALUES
        ('9306c727-41dc-4892-873e-e3852008de32',	'+'),
        ('f1210412-5e15-4ef5-b552-03d8d8344fca',	'+'),
        ('499ff9a5-f24b-43e1-bca3-6122674b5998',	'+'),
        ('011747e2-5e34-400e-a0e5-0c8f837d7d3c',	'+'),
        ('41d2c21c-bf96-4ee8-9776-5039b0ae5ff4',	'+');

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