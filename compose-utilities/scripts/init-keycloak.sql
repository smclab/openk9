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
            "details_json" text,
            CONSTRAINT "constraint_admin_event_entity" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_admin_event_time ON public.admin_event_entity USING btree (realm_id, admin_event_time);


        CREATE TABLE "public"."associated_policy" (
            "policy_id" character varying(36) NOT NULL,
            "associated_policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrpap" PRIMARY KEY ("policy_id", "associated_policy_id")
        ) WITH (oids = false);

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
        ) WITH (oids = false);

        CREATE INDEX idx_auth_exec_realm_flow ON public.authentication_execution USING btree (realm_id, flow_id);

        CREATE INDEX idx_auth_exec_flow ON public.authentication_execution USING btree (flow_id);

        INSERT INTO "authentication_execution" ("id", "alias", "authenticator", "realm_id", "flow_id", "requirement", "priority", "authenticator_flow", "auth_flow_id", "auth_config") VALUES
        ('020443cc-90d1-4ddb-a16a-38254d79b445',	NULL,	'auth-cookie',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'7b3202ae-cb95-4fae-9863-3cc48379d3e1',	2,	10,	'0',	NULL,	NULL),
        ('678951a2-11d5-4bfe-9fe8-a2253439e34f',	NULL,	'auth-spnego',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'7b3202ae-cb95-4fae-9863-3cc48379d3e1',	3,	20,	'0',	NULL,	NULL),
        ('a9df7a86-3164-4620-98e6-3bd5b9e50d81',	NULL,	'identity-provider-redirector',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'7b3202ae-cb95-4fae-9863-3cc48379d3e1',	2,	25,	'0',	NULL,	NULL),
        ('19b954ff-5009-4105-9574-ea1551958dcc',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'7b3202ae-cb95-4fae-9863-3cc48379d3e1',	2,	30,	'1',	'aee5d8f0-00bf-4ec7-ba03-19580bd517e4',	NULL),
        ('8083570b-bff3-4f96-86fe-c983da2fca5e',	NULL,	'auth-username-password-form',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'aee5d8f0-00bf-4ec7-ba03-19580bd517e4',	0,	10,	'0',	NULL,	NULL),
        ('3360e0e5-cd7e-42d4-9441-6e2dbf9e2684',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'aee5d8f0-00bf-4ec7-ba03-19580bd517e4',	1,	20,	'1',	'6ca02746-0d87-4d08-8598-a158caf28ff4',	NULL),
        ('1421ae8c-0cbb-40a6-a0fb-d08e5a46610c',	NULL,	'conditional-user-configured',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'6ca02746-0d87-4d08-8598-a158caf28ff4',	0,	10,	'0',	NULL,	NULL),
        ('c945b3c9-8de2-468b-a552-aa6d549be4b0',	NULL,	'auth-otp-form',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'6ca02746-0d87-4d08-8598-a158caf28ff4',	0,	20,	'0',	NULL,	NULL),
        ('fe5ef0ff-0558-47f2-9e84-1386288bdd37',	NULL,	'direct-grant-validate-username',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c665881e-3b45-4b9f-a390-06b82530faab',	0,	10,	'0',	NULL,	NULL),
        ('9b13818a-e666-455b-9319-cc95d977d8a7',	NULL,	'direct-grant-validate-password',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c665881e-3b45-4b9f-a390-06b82530faab',	0,	20,	'0',	NULL,	NULL),
        ('dcd03dc2-e537-4a72-9aba-97ca4e11ea55',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c665881e-3b45-4b9f-a390-06b82530faab',	1,	30,	'1',	'c27578d5-5952-428b-a3bb-dc8ee4bde15d',	NULL),
        ('a762af9c-ad28-4df2-8993-295dfdea0536',	NULL,	'conditional-user-configured',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c27578d5-5952-428b-a3bb-dc8ee4bde15d',	0,	10,	'0',	NULL,	NULL),
        ('a379f4d3-429d-4c31-be92-73627267c5e6',	NULL,	'direct-grant-validate-otp',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c27578d5-5952-428b-a3bb-dc8ee4bde15d',	0,	20,	'0',	NULL,	NULL),
        ('57a4209a-edaa-4627-af66-a27b8c809229',	NULL,	'registration-page-form',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1f822451-7d52-4f8f-98ea-adc6dab77176',	0,	10,	'1',	'5d398684-cebd-4ca3-92ae-a6c2a9f7150c',	NULL),
        ('6f2dbc57-ab31-4ae7-abdb-dceb112a629a',	NULL,	'registration-user-creation',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5d398684-cebd-4ca3-92ae-a6c2a9f7150c',	0,	20,	'0',	NULL,	NULL),
        ('3983ab29-62db-48fe-87aa-2ee0921a1f37',	NULL,	'registration-password-action',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5d398684-cebd-4ca3-92ae-a6c2a9f7150c',	0,	50,	'0',	NULL,	NULL),
        ('bacf883e-1693-4040-b7ff-a76d821d8285',	NULL,	'registration-recaptcha-action',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5d398684-cebd-4ca3-92ae-a6c2a9f7150c',	3,	60,	'0',	NULL,	NULL),
        ('a4812d4c-a82c-44cf-9c65-7ecbe314ce8b',	NULL,	'registration-terms-and-conditions',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5d398684-cebd-4ca3-92ae-a6c2a9f7150c',	3,	70,	'0',	NULL,	NULL),
        ('10f5ce3a-2eaf-4c7f-83ab-46376b33e4b9',	NULL,	'reset-credentials-choose-user',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'02bc64c5-b9dc-4d3a-879e-72ddfeb45eaf',	0,	10,	'0',	NULL,	NULL),
        ('6c0e30ea-8dd3-4eb6-90c5-eee3bab2c3b3',	NULL,	'reset-credential-email',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'02bc64c5-b9dc-4d3a-879e-72ddfeb45eaf',	0,	20,	'0',	NULL,	NULL),
        ('c94f3a2c-1cab-46d3-b741-e06d8f1b45b2',	NULL,	'reset-password',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'02bc64c5-b9dc-4d3a-879e-72ddfeb45eaf',	0,	30,	'0',	NULL,	NULL),
        ('71eb603e-e7e4-4cff-b7e7-9ce155c2c6c2',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'02bc64c5-b9dc-4d3a-879e-72ddfeb45eaf',	1,	40,	'1',	'f9298688-90f6-45b2-a61d-00e28502d8ec',	NULL),
        ('cdd76e03-a976-4e16-90a0-a9d40171e226',	NULL,	'conditional-user-configured',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f9298688-90f6-45b2-a61d-00e28502d8ec',	0,	10,	'0',	NULL,	NULL),
        ('45e1260a-1294-4c6c-8ffa-893bfbfb6ec4',	NULL,	'reset-otp',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f9298688-90f6-45b2-a61d-00e28502d8ec',	0,	20,	'0',	NULL,	NULL),
        ('441ac390-6bcf-4994-a7b2-7f1a414b0a9c',	NULL,	'client-secret',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'76b4f0c5-9457-441d-ae0c-9a0d945ba5f3',	2,	10,	'0',	NULL,	NULL),
        ('522d8b23-4d3b-4fdb-8bce-0f25b7e42b41',	NULL,	'client-jwt',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'76b4f0c5-9457-441d-ae0c-9a0d945ba5f3',	2,	20,	'0',	NULL,	NULL),
        ('7d22332e-7bbe-4a88-8e42-cbaf6426d8c0',	NULL,	'client-secret-jwt',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'76b4f0c5-9457-441d-ae0c-9a0d945ba5f3',	2,	30,	'0',	NULL,	NULL),
        ('73f880d8-7199-4ebd-b7dd-c131a96f4a33',	NULL,	'client-x509',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'76b4f0c5-9457-441d-ae0c-9a0d945ba5f3',	2,	40,	'0',	NULL,	NULL),
        ('fd74afff-ec87-43ec-bfbc-537bc488d4c3',	NULL,	'idp-review-profile',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'cf18ec32-d1bc-4541-ab7f-c3d76a51c8cd',	0,	10,	'0',	NULL,	'c8e8254e-a1ce-4a5d-8ee7-e1ecaeeb3476'),
        ('dad5b257-3b51-413e-a290-79ecc5be5571',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'cf18ec32-d1bc-4541-ab7f-c3d76a51c8cd',	0,	20,	'1',	'43aa6508-aef5-4e9f-a3da-a0b78cdeb75a',	NULL),
        ('b6983108-a44c-4902-86a8-470c6532ff71',	NULL,	'idp-create-user-if-unique',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'43aa6508-aef5-4e9f-a3da-a0b78cdeb75a',	2,	10,	'0',	NULL,	'da46079b-2d11-4338-9b34-35d4b266dfa7'),
        ('4b50c129-fd80-489a-9e85-31adfdfff950',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'43aa6508-aef5-4e9f-a3da-a0b78cdeb75a',	2,	20,	'1',	'54bd65f7-23a9-42d5-a533-c67abb4469e4',	NULL),
        ('919922a0-3041-4ab5-9f4c-c5debc2e3cee',	NULL,	'idp-confirm-link',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'54bd65f7-23a9-42d5-a533-c67abb4469e4',	0,	10,	'0',	NULL,	NULL),
        ('63ce79ec-bba5-4314-9764-27be76b4b3b8',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'54bd65f7-23a9-42d5-a533-c67abb4469e4',	0,	20,	'1',	'cde56fea-c461-4851-9ba9-a6d4edf814f6',	NULL),
        ('1d9149b2-408b-4137-a466-1230c2323360',	NULL,	'idp-email-verification',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'cde56fea-c461-4851-9ba9-a6d4edf814f6',	2,	10,	'0',	NULL,	NULL),
        ('9cd655c3-1495-4a43-b83d-3822f91a5585',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'cde56fea-c461-4851-9ba9-a6d4edf814f6',	2,	20,	'1',	'c1d2118e-e634-4053-b7a1-a5d5b1fa0f00',	NULL),
        ('4e2c9ab5-453c-42ba-9417-d08694b8b3b2',	NULL,	'idp-username-password-form',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c1d2118e-e634-4053-b7a1-a5d5b1fa0f00',	0,	10,	'0',	NULL,	NULL),
        ('4eaa530b-a0bd-483e-8dbf-5d6006ff8311',	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'c1d2118e-e634-4053-b7a1-a5d5b1fa0f00',	1,	20,	'1',	'79e4cb63-9fce-4e9f-a6a5-6ee2f561d478',	NULL),
        ('5448b2e3-1108-4192-98b9-0c83affa6d23',	NULL,	'conditional-user-configured',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'79e4cb63-9fce-4e9f-a6a5-6ee2f561d478',	0,	10,	'0',	NULL,	NULL),
        ('36ea87de-7312-46ca-8413-c12b5818cefe',	NULL,	'auth-otp-form',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'79e4cb63-9fce-4e9f-a6a5-6ee2f561d478',	0,	20,	'0',	NULL,	NULL),
        ('39d42294-b26f-405c-8e50-b66f647b42d7',	NULL,	'http-basic-authenticator',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'3a6662cd-5d9b-4bb8-94a3-d469fc2c66c3',	0,	10,	'0',	NULL,	NULL),
        ('7bb5bf3e-057e-43ff-9c26-b2f4a0a98c73',	NULL,	'docker-http-basic-authenticator',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'3cbf3454-3a95-44d3-9ced-3c51e5882669',	0,	10,	'0',	NULL,	NULL),
        ('3d0542bf-0cf9-433e-bb40-09d74634aa9a',	NULL,	'auth-cookie',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8fa14c62-098d-4d87-9d56-e27a3525de8b',	2,	10,	'0',	NULL,	NULL),
        ('c04559e0-2008-4bb5-8290-19ec4ae8c522',	NULL,	'auth-spnego',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8fa14c62-098d-4d87-9d56-e27a3525de8b',	3,	20,	'0',	NULL,	NULL),
        ('2901ad18-ab21-44b3-a80c-bbe6e0631aa0',	NULL,	'identity-provider-redirector',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8fa14c62-098d-4d87-9d56-e27a3525de8b',	2,	25,	'0',	NULL,	NULL),
        ('ded4d2fb-8a7c-4ec9-945f-e4fa922908d4',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8fa14c62-098d-4d87-9d56-e27a3525de8b',	2,	30,	'1',	'57f24a37-cf99-41a4-8e0e-98bd9028bb43',	NULL),
        ('d622faab-4f20-476a-9d16-cc37658574b4',	NULL,	'auth-username-password-form',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'57f24a37-cf99-41a4-8e0e-98bd9028bb43',	0,	10,	'0',	NULL,	NULL),
        ('20ee221b-b772-4e56-9d33-4b60e6fc9d1e',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'57f24a37-cf99-41a4-8e0e-98bd9028bb43',	1,	20,	'1',	'9ab64edc-cbe6-4e24-9533-1046bee8e89f',	NULL),
        ('8f26bff1-ced5-4df6-99c7-d755d896d791',	NULL,	'conditional-user-configured',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'9ab64edc-cbe6-4e24-9533-1046bee8e89f',	0,	10,	'0',	NULL,	NULL),
        ('04b9fbb5-f6bd-4f38-9ed7-5be0126193d3',	NULL,	'auth-otp-form',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'9ab64edc-cbe6-4e24-9533-1046bee8e89f',	0,	20,	'0',	NULL,	NULL),
        ('26866c60-b549-4994-99ff-7db709f5fbd0',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8fa14c62-098d-4d87-9d56-e27a3525de8b',	2,	26,	'1',	'ebdd8a12-15d7-41a9-b65b-910ff4c0b9a3',	NULL),
        ('65fef914-4821-4256-a366-9fef476108c8',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ebdd8a12-15d7-41a9-b65b-910ff4c0b9a3',	1,	10,	'1',	'f3902a26-28cc-485d-8837-dc26c900cb26',	NULL),
        ('14c1c536-ec9c-4940-be4a-0902d82c3b73',	NULL,	'conditional-user-configured',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'f3902a26-28cc-485d-8837-dc26c900cb26',	0,	10,	'0',	NULL,	NULL),
        ('6d9416fb-279b-417e-8755-c62c099461d5',	NULL,	'organization',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'f3902a26-28cc-485d-8837-dc26c900cb26',	2,	20,	'0',	NULL,	NULL),
        ('d05a3692-04fd-40af-9ebe-08125e4e93b3',	NULL,	'direct-grant-validate-username',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3d475867-13dc-4b82-87f7-49da51efa0eb',	0,	10,	'0',	NULL,	NULL),
        ('114c54d0-8828-4a83-a4e4-c71061430f87',	NULL,	'direct-grant-validate-password',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3d475867-13dc-4b82-87f7-49da51efa0eb',	0,	20,	'0',	NULL,	NULL),
        ('d5e45d2c-9633-46bd-b3f5-c6a9b6bd4e36',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3d475867-13dc-4b82-87f7-49da51efa0eb',	1,	30,	'1',	'2b4560d6-61e3-4de2-83c4-56edcb53d0c0',	NULL),
        ('49cc3560-f20e-4eb6-abb9-14cf3ea49a28',	NULL,	'conditional-user-configured',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'2b4560d6-61e3-4de2-83c4-56edcb53d0c0',	0,	10,	'0',	NULL,	NULL),
        ('6abc5e73-8077-4af9-88a0-b5322a7db179',	NULL,	'direct-grant-validate-otp',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'2b4560d6-61e3-4de2-83c4-56edcb53d0c0',	0,	20,	'0',	NULL,	NULL),
        ('93583eb7-4340-452b-a0aa-862cd1c541e7',	NULL,	'registration-page-form',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'd0fb91e5-3652-4cc2-aeb6-0e0bf1de4662',	0,	10,	'1',	'ee81520f-5a15-4061-b569-3d5649a31b40',	NULL),
        ('cd7ed2e8-432d-417e-b333-cecf0adef5c0',	NULL,	'registration-user-creation',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ee81520f-5a15-4061-b569-3d5649a31b40',	0,	20,	'0',	NULL,	NULL),
        ('e9cc11a1-db96-4dda-92d5-ec9c742b1407',	NULL,	'registration-password-action',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ee81520f-5a15-4061-b569-3d5649a31b40',	0,	50,	'0',	NULL,	NULL),
        ('f2d743e1-1eea-44b3-ba33-3571d6367d84',	NULL,	'registration-recaptcha-action',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ee81520f-5a15-4061-b569-3d5649a31b40',	3,	60,	'0',	NULL,	NULL),
        ('a74d8bba-3cba-4513-affa-80a4e2280f62',	NULL,	'registration-terms-and-conditions',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ee81520f-5a15-4061-b569-3d5649a31b40',	3,	70,	'0',	NULL,	NULL),
        ('f8ee4b7a-6179-4e66-91d9-e372f6fde877',	NULL,	'reset-credentials-choose-user',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'4510b8e8-ef1f-4e30-8d82-5f42ec0ab318',	0,	10,	'0',	NULL,	NULL),
        ('eca7debb-6d09-4f76-8c53-cc1c49bc6185',	NULL,	'reset-credential-email',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'4510b8e8-ef1f-4e30-8d82-5f42ec0ab318',	0,	20,	'0',	NULL,	NULL),
        ('cfb6456a-110e-4241-8a44-38974deb0e00',	NULL,	'reset-password',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'4510b8e8-ef1f-4e30-8d82-5f42ec0ab318',	0,	30,	'0',	NULL,	NULL),
        ('f28714bd-43c3-46cf-b6fc-17f0a3237ac3',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'4510b8e8-ef1f-4e30-8d82-5f42ec0ab318',	1,	40,	'1',	'6edd31f6-b79d-46fe-919a-52cf2e227376',	NULL),
        ('339e825e-f20f-41cf-8414-b6459acadbb4',	NULL,	'conditional-user-configured',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'6edd31f6-b79d-46fe-919a-52cf2e227376',	0,	10,	'0',	NULL,	NULL),
        ('28607312-29c0-4ccc-b84f-06cc1f495041',	NULL,	'reset-otp',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'6edd31f6-b79d-46fe-919a-52cf2e227376',	0,	20,	'0',	NULL,	NULL),
        ('7dc17a8f-ec5e-4692-866e-7783355986a5',	NULL,	'client-secret',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'2ddb5ffb-5457-48e7-b8fa-0a53a5ce83b6',	2,	10,	'0',	NULL,	NULL),
        ('0db6ce00-fee9-4c59-a6fe-321b3d0ee82a',	NULL,	'client-jwt',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'2ddb5ffb-5457-48e7-b8fa-0a53a5ce83b6',	2,	20,	'0',	NULL,	NULL),
        ('f7c260c4-26a8-4b48-935e-c98477eceee9',	NULL,	'client-secret-jwt',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'2ddb5ffb-5457-48e7-b8fa-0a53a5ce83b6',	2,	30,	'0',	NULL,	NULL),
        ('70114640-efc9-4359-9535-0a470cebdbe2',	NULL,	'client-x509',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'2ddb5ffb-5457-48e7-b8fa-0a53a5ce83b6',	2,	40,	'0',	NULL,	NULL),
        ('f7a563f2-b489-4f8e-9793-fc777829af90',	NULL,	'idp-review-profile',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ea6ba12a-e11d-4d89-a19c-26961b4abea4',	0,	10,	'0',	NULL,	'8b0e4567-d26c-455f-a491-9c62d2ce2113'),
        ('71b57c6e-f46a-45d7-b462-465c2959cad8',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ea6ba12a-e11d-4d89-a19c-26961b4abea4',	0,	20,	'1',	'fde853cc-c6ec-4bb7-9686-ec477bbc8336',	NULL),
        ('8f24e857-ecce-43de-bc66-ccf7d5ef90f3',	NULL,	'idp-create-user-if-unique',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'fde853cc-c6ec-4bb7-9686-ec477bbc8336',	2,	10,	'0',	NULL,	'bd5b6077-eaa0-41e3-8097-d270a14fe3d4'),
        ('fe0d10b3-6327-48d3-a16d-68fe28a6014a',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'fde853cc-c6ec-4bb7-9686-ec477bbc8336',	2,	20,	'1',	'83711e30-281e-4213-93eb-1166c6f3c70c',	NULL),
        ('04332021-8415-4e34-9022-c427b1325781',	NULL,	'idp-confirm-link',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'83711e30-281e-4213-93eb-1166c6f3c70c',	0,	10,	'0',	NULL,	NULL),
        ('01290766-62ed-4d00-8932-db769a6fae09',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'83711e30-281e-4213-93eb-1166c6f3c70c',	0,	20,	'1',	'c571457c-3a8f-4c49-baa2-3527512ba5f8',	NULL),
        ('3991cc95-7d31-4f6c-9356-2e1d532b603b',	NULL,	'idp-email-verification',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'c571457c-3a8f-4c49-baa2-3527512ba5f8',	2,	10,	'0',	NULL,	NULL),
        ('61dac086-8b34-4a21-8298-c6e30cc400ce',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'c571457c-3a8f-4c49-baa2-3527512ba5f8',	2,	20,	'1',	'3d948cb2-2e6c-40a5-b71e-7a51a61f1483',	NULL),
        ('f048e5db-08ef-4af7-8d28-8e0adf6f8b6c',	NULL,	'idp-username-password-form',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3d948cb2-2e6c-40a5-b71e-7a51a61f1483',	0,	10,	'0',	NULL,	NULL),
        ('0d2e9928-78c3-4f98-aa09-89574f5d4a79',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3d948cb2-2e6c-40a5-b71e-7a51a61f1483',	1,	20,	'1',	'41e707b4-4e19-4f9f-ab0c-da442a9ced63',	NULL),
        ('4ea8a18b-b9d6-4ac4-a524-593b53df9f0e',	NULL,	'conditional-user-configured',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'41e707b4-4e19-4f9f-ab0c-da442a9ced63',	0,	10,	'0',	NULL,	NULL),
        ('622808c9-28a6-421a-a467-c658b5aadd06',	NULL,	'auth-otp-form',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'41e707b4-4e19-4f9f-ab0c-da442a9ced63',	0,	20,	'0',	NULL,	NULL),
        ('ce2b3b7d-36ff-4a14-a8cd-2af7b37e35b1',	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ea6ba12a-e11d-4d89-a19c-26961b4abea4',	1,	50,	'1',	'69ee8d26-fe0a-435f-b0c0-767237be5f29',	NULL),
        ('0b21b0c1-3ccf-4186-bef6-d97e23829a75',	NULL,	'conditional-user-configured',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'69ee8d26-fe0a-435f-b0c0-767237be5f29',	0,	10,	'0',	NULL,	NULL),
        ('9d165b00-f75e-4009-8944-6957784b91d3',	NULL,	'idp-add-organization-member',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'69ee8d26-fe0a-435f-b0c0-767237be5f29',	0,	20,	'0',	NULL,	NULL),
        ('e9c190d1-5c32-4f9a-98d8-1c9716b18532',	NULL,	'http-basic-authenticator',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'fe1ef707-b991-4f40-be62-9a4f87d43c3f',	0,	10,	'0',	NULL,	NULL),
        ('16fc4344-dcd0-4399-afd3-fc5ae9f75168',	NULL,	'docker-http-basic-authenticator',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'56d82c54-a5e3-4eb5-bfd7-8fb3c9cadeb9',	0,	10,	'0',	NULL,	NULL),
        ('e40d14fc-5920-4269-a78d-bc61337c0d00',	NULL,	'auth-cookie',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'd553321a-ffd5-439a-88de-6c0e6b889ed1',	2,	10,	'0',	NULL,	NULL),
        ('657aa68e-4387-428e-900a-0831d688f64e',	NULL,	'auth-spnego',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'd553321a-ffd5-439a-88de-6c0e6b889ed1',	3,	20,	'0',	NULL,	NULL),
        ('6117ee85-cdff-4078-9fff-410a7d37dcc3',	NULL,	'identity-provider-redirector',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'd553321a-ffd5-439a-88de-6c0e6b889ed1',	2,	25,	'0',	NULL,	NULL),
        ('09617c04-92f1-4cf3-b026-9891cbcb5a48',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'd553321a-ffd5-439a-88de-6c0e6b889ed1',	2,	30,	'1',	'cc0dd7b4-58c6-4c1c-8993-b87551128adf',	NULL),
        ('4103a734-4226-4246-9532-b110fdbbebb5',	NULL,	'auth-username-password-form',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'cc0dd7b4-58c6-4c1c-8993-b87551128adf',	0,	10,	'0',	NULL,	NULL),
        ('a2eac751-65db-4409-b598-2b4d69e15aa5',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'cc0dd7b4-58c6-4c1c-8993-b87551128adf',	1,	20,	'1',	'48b73e55-2f7d-4012-a4e9-4a16d53b573c',	NULL),
        ('c82058e2-f25c-47cc-8b92-ce732976bc4f',	NULL,	'conditional-user-configured',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'48b73e55-2f7d-4012-a4e9-4a16d53b573c',	0,	10,	'0',	NULL,	NULL),
        ('57972112-4f51-48bd-96a3-e760f551b5ce',	NULL,	'auth-otp-form',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'48b73e55-2f7d-4012-a4e9-4a16d53b573c',	0,	20,	'0',	NULL,	NULL),
        ('3e366100-10f9-4aa4-8d73-45a402b27796',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'd553321a-ffd5-439a-88de-6c0e6b889ed1',	2,	26,	'1',	'fe30742f-cbbc-4bc6-ad54-25bc1db6b9c2',	NULL),
        ('00ffba99-8e12-457c-a8ab-fe7f45b95560',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'fe30742f-cbbc-4bc6-ad54-25bc1db6b9c2',	1,	10,	'1',	'46fa4127-6f0a-4d35-996f-ae507c68b7f6',	NULL),
        ('aa89d08a-be3b-468e-b99b-fb8ff65e1db2',	NULL,	'conditional-user-configured',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'46fa4127-6f0a-4d35-996f-ae507c68b7f6',	0,	10,	'0',	NULL,	NULL),
        ('764fdfd8-8cd9-48ee-bc69-042f75d240ea',	NULL,	'organization',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'46fa4127-6f0a-4d35-996f-ae507c68b7f6',	2,	20,	'0',	NULL,	NULL),
        ('0f47e881-bd1b-42c4-bae5-8722356bc503',	NULL,	'direct-grant-validate-username',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'9cd24228-ad2b-41a5-a179-79ff571d3f4e',	0,	10,	'0',	NULL,	NULL),
        ('e90a3e6f-0afd-451a-968a-9f177b24426e',	NULL,	'direct-grant-validate-password',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'9cd24228-ad2b-41a5-a179-79ff571d3f4e',	0,	20,	'0',	NULL,	NULL),
        ('9b2c90db-233a-4096-b6f5-8665212b40ae',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'9cd24228-ad2b-41a5-a179-79ff571d3f4e',	1,	30,	'1',	'57c8f9cc-03ca-44cd-b13f-74fcc0c3e853',	NULL),
        ('98cc2fb3-6e09-4403-a82f-0833b1ce9bfb',	NULL,	'conditional-user-configured',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'57c8f9cc-03ca-44cd-b13f-74fcc0c3e853',	0,	10,	'0',	NULL,	NULL),
        ('33d33e3a-13c2-47a7-ade0-0e37e9021983',	NULL,	'direct-grant-validate-otp',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'57c8f9cc-03ca-44cd-b13f-74fcc0c3e853',	0,	20,	'0',	NULL,	NULL),
        ('c221393e-97a5-4781-916b-7a735b3a0578',	NULL,	'registration-page-form',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'aae7828a-8732-4d18-a944-e064a390a16d',	0,	10,	'1',	'135af02b-fb37-43f0-89a6-a2eb35ec19bb',	NULL),
        ('58d02163-242d-45ff-83e5-04f8560127e7',	NULL,	'registration-user-creation',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'135af02b-fb37-43f0-89a6-a2eb35ec19bb',	0,	20,	'0',	NULL,	NULL),
        ('6c2f1e7e-87c4-4388-8c55-e501028fe01e',	NULL,	'registration-password-action',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'135af02b-fb37-43f0-89a6-a2eb35ec19bb',	0,	50,	'0',	NULL,	NULL),
        ('d89ad888-e456-4edd-a130-96a0fd9acf8a',	NULL,	'registration-recaptcha-action',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'135af02b-fb37-43f0-89a6-a2eb35ec19bb',	3,	60,	'0',	NULL,	NULL),
        ('14ddcbd5-9804-4c6d-a58c-f6016026c8f0',	NULL,	'registration-terms-and-conditions',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'135af02b-fb37-43f0-89a6-a2eb35ec19bb',	3,	70,	'0',	NULL,	NULL),
        ('ac16b8ef-098a-4ffa-bcab-150ae18b3bed',	NULL,	'reset-credentials-choose-user',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'14414cbc-ef1d-481c-b5bf-a9da2366671c',	0,	10,	'0',	NULL,	NULL),
        ('2321e066-d087-4cfd-a0ed-7a034babde29',	NULL,	'reset-credential-email',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'14414cbc-ef1d-481c-b5bf-a9da2366671c',	0,	20,	'0',	NULL,	NULL),
        ('07c3b83d-404f-4e91-bba9-f43f5a00ca7b',	NULL,	'reset-password',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'14414cbc-ef1d-481c-b5bf-a9da2366671c',	0,	30,	'0',	NULL,	NULL),
        ('aeaf3096-b8a1-4527-a7fe-c721119f4247',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'14414cbc-ef1d-481c-b5bf-a9da2366671c',	1,	40,	'1',	'4b146f2d-2d9a-4bc9-9be1-2b05b960b839',	NULL),
        ('59a37b3f-319c-4822-bd51-a290031062f4',	NULL,	'conditional-user-configured',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'4b146f2d-2d9a-4bc9-9be1-2b05b960b839',	0,	10,	'0',	NULL,	NULL),
        ('91a63288-04a3-4c53-b191-c588199fc9fa',	NULL,	'reset-otp',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'4b146f2d-2d9a-4bc9-9be1-2b05b960b839',	0,	20,	'0',	NULL,	NULL),
        ('c1fdb15e-f4d7-4eb0-a526-d27ed413e80d',	NULL,	'client-secret',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'6b1855bd-dc9d-4589-acda-04fcf4e33d33',	2,	10,	'0',	NULL,	NULL),
        ('ef4809b1-c49b-40cb-acbf-1a42377947d8',	NULL,	'client-jwt',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'6b1855bd-dc9d-4589-acda-04fcf4e33d33',	2,	20,	'0',	NULL,	NULL),
        ('fe492b20-d09d-4487-b694-53d1826014fe',	NULL,	'client-secret-jwt',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'6b1855bd-dc9d-4589-acda-04fcf4e33d33',	2,	30,	'0',	NULL,	NULL),
        ('9e2fcef7-11b6-431c-b92e-2d695e66793c',	NULL,	'client-x509',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'6b1855bd-dc9d-4589-acda-04fcf4e33d33',	2,	40,	'0',	NULL,	NULL),
        ('2e535384-9573-4ac0-a530-35075abeeec4',	NULL,	'idp-review-profile',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'87568ca1-089e-423e-a47c-38de2f7b5e0a',	0,	10,	'0',	NULL,	'5b1760d7-d8c2-47d7-b817-43bddab4e632'),
        ('3a41b870-e691-4b99-87fe-4d0d00262937',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'87568ca1-089e-423e-a47c-38de2f7b5e0a',	0,	20,	'1',	'616f57b1-e520-43e7-a6e8-014b2cb4d443',	NULL),
        ('e6afdfee-632a-4f6a-8fff-4645d23b92b4',	NULL,	'idp-create-user-if-unique',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'616f57b1-e520-43e7-a6e8-014b2cb4d443',	2,	10,	'0',	NULL,	'48e29fef-1775-42e5-a5e7-c1888b7f79f2'),
        ('0a8495b9-6fb2-415b-a40a-f752e8599cf6',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'616f57b1-e520-43e7-a6e8-014b2cb4d443',	2,	20,	'1',	'e3357b4d-8494-4986-896a-969abd5507ee',	NULL),
        ('d8db192b-1efc-4d89-867a-cf19e352465f',	NULL,	'idp-confirm-link',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'e3357b4d-8494-4986-896a-969abd5507ee',	0,	10,	'0',	NULL,	NULL),
        ('ce550174-bc91-45d5-99e7-33bbc23cf562',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'e3357b4d-8494-4986-896a-969abd5507ee',	0,	20,	'1',	'ab7254b9-18ec-4991-bff1-3cd7569dcf55',	NULL),
        ('0ea9debd-5cc6-4f9b-888a-fabeeff977d4',	NULL,	'idp-email-verification',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'ab7254b9-18ec-4991-bff1-3cd7569dcf55',	2,	10,	'0',	NULL,	NULL),
        ('578e1e2f-1d62-4e8d-8fd1-f7c73f4e4c32',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'ab7254b9-18ec-4991-bff1-3cd7569dcf55',	2,	20,	'1',	'cf706f9e-b131-4917-ac4a-c7ddc7283e29',	NULL),
        ('069a4f9a-1541-4a9a-b204-cd75222cc380',	NULL,	'idp-username-password-form',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'cf706f9e-b131-4917-ac4a-c7ddc7283e29',	0,	10,	'0',	NULL,	NULL),
        ('733a364c-063f-49e1-b4fa-a8c82f593067',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'cf706f9e-b131-4917-ac4a-c7ddc7283e29',	1,	20,	'1',	'a5bd1904-b771-4334-a8d2-ca0b4a381d0c',	NULL),
        ('1bf00621-9c61-4bba-b893-2549543ae6e4',	NULL,	'conditional-user-configured',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'a5bd1904-b771-4334-a8d2-ca0b4a381d0c',	0,	10,	'0',	NULL,	NULL),
        ('c4e5847e-4ed5-4a87-81cb-28ccfcb5122a',	NULL,	'auth-otp-form',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'a5bd1904-b771-4334-a8d2-ca0b4a381d0c',	0,	20,	'0',	NULL,	NULL),
        ('c00ece24-d192-4cbb-a9a4-5214f441cfef',	NULL,	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'87568ca1-089e-423e-a47c-38de2f7b5e0a',	1,	50,	'1',	'7dff29d4-171a-48a6-a43e-c586093bcfdf',	NULL),
        ('bdc7258a-0c0f-4863-813e-d77cd9a2bccd',	NULL,	'conditional-user-configured',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'7dff29d4-171a-48a6-a43e-c586093bcfdf',	0,	10,	'0',	NULL,	NULL),
        ('0c6d53cc-1fd5-4028-9a38-159d37047718',	NULL,	'idp-add-organization-member',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'7dff29d4-171a-48a6-a43e-c586093bcfdf',	0,	20,	'0',	NULL,	NULL),
        ('e81e2a69-60bd-49d4-a7bf-12ca1b647b22',	NULL,	'http-basic-authenticator',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'dce61608-f431-4fd3-9832-ecd12b65e5d8',	0,	10,	'0',	NULL,	NULL),
        ('56c92727-2386-472d-a652-1b7820c64bf4',	NULL,	'docker-http-basic-authenticator',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'a56e9328-65ce-4869-9ba2-16807cd25c7b',	0,	10,	'0',	NULL,	NULL);

        CREATE TABLE "public"."authentication_flow" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "description" character varying(255),
            "realm_id" character varying(36),
            "provider_id" character varying(36) DEFAULT 'basic-flow' NOT NULL,
            "top_level" boolean DEFAULT false NOT NULL,
            "built_in" boolean DEFAULT false NOT NULL,
            CONSTRAINT "constraint_auth_flow_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_auth_flow_realm ON public.authentication_flow USING btree (realm_id);

        INSERT INTO "authentication_flow" ("id", "alias", "description", "realm_id", "provider_id", "top_level", "built_in") VALUES
        ('7b3202ae-cb95-4fae-9863-3cc48379d3e1',	'browser',	'Browser based authentication',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('aee5d8f0-00bf-4ec7-ba03-19580bd517e4',	'forms',	'Username, password, otp and other auth forms.',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('6ca02746-0d87-4d08-8598-a158caf28ff4',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('c665881e-3b45-4b9f-a390-06b82530faab',	'direct grant',	'OpenID Connect Resource Owner Grant',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('c27578d5-5952-428b-a3bb-dc8ee4bde15d',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('1f822451-7d52-4f8f-98ea-adc6dab77176',	'registration',	'Registration flow',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('5d398684-cebd-4ca3-92ae-a6c2a9f7150c',	'registration form',	'Registration form',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'form-flow',	'0',	'1'),
        ('02bc64c5-b9dc-4d3a-879e-72ddfeb45eaf',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('f9298688-90f6-45b2-a61d-00e28502d8ec',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('76b4f0c5-9457-441d-ae0c-9a0d945ba5f3',	'clients',	'Base authentication for clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'client-flow',	'1',	'1'),
        ('cf18ec32-d1bc-4541-ab7f-c3d76a51c8cd',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('43aa6508-aef5-4e9f-a3da-a0b78cdeb75a',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('54bd65f7-23a9-42d5-a533-c67abb4469e4',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('cde56fea-c461-4851-9ba9-a6d4edf814f6',	'Account verification options',	'Method with which to verity the existing account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('c1d2118e-e634-4053-b7a1-a5d5b1fa0f00',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('79e4cb63-9fce-4e9f-a6a5-6ee2f561d478',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'0',	'1'),
        ('3a6662cd-5d9b-4bb8-94a3-d469fc2c66c3',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('3cbf3454-3a95-44d3-9ced-3c51e5882669',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'basic-flow',	'1',	'1'),
        ('8fa14c62-098d-4d87-9d56-e27a3525de8b',	'browser',	'Browser based authentication',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('57f24a37-cf99-41a4-8e0e-98bd9028bb43',	'forms',	'Username, password, otp and other auth forms.',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('9ab64edc-cbe6-4e24-9533-1046bee8e89f',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('ebdd8a12-15d7-41a9-b65b-910ff4c0b9a3',	'Organization',	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('f3902a26-28cc-485d-8837-dc26c900cb26',	'Browser - Conditional Organization',	'Flow to determine if the organization identity-first login is to be used',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('3d475867-13dc-4b82-87f7-49da51efa0eb',	'direct grant',	'OpenID Connect Resource Owner Grant',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('2b4560d6-61e3-4de2-83c4-56edcb53d0c0',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('d0fb91e5-3652-4cc2-aeb6-0e0bf1de4662',	'registration',	'Registration flow',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('ee81520f-5a15-4061-b569-3d5649a31b40',	'registration form',	'Registration form',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'form-flow',	'0',	'1'),
        ('4510b8e8-ef1f-4e30-8d82-5f42ec0ab318',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('6edd31f6-b79d-46fe-919a-52cf2e227376',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('2ddb5ffb-5457-48e7-b8fa-0a53a5ce83b6',	'clients',	'Base authentication for clients',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'client-flow',	'1',	'1'),
        ('ea6ba12a-e11d-4d89-a19c-26961b4abea4',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('fde853cc-c6ec-4bb7-9686-ec477bbc8336',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('83711e30-281e-4213-93eb-1166c6f3c70c',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('c571457c-3a8f-4c49-baa2-3527512ba5f8',	'Account verification options',	'Method with which to verity the existing account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('3d948cb2-2e6c-40a5-b71e-7a51a61f1483',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('41e707b4-4e19-4f9f-ab0c-da442a9ced63',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('69ee8d26-fe0a-435f-b0c0-767237be5f29',	'First Broker Login - Conditional Organization',	'Flow to determine if the authenticator that adds organization members is to be used',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'0',	'1'),
        ('fe1ef707-b991-4f40-be62-9a4f87d43c3f',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('56d82c54-a5e3-4eb5-bfd7-8fb3c9cadeb9',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'basic-flow',	'1',	'1'),
        ('d553321a-ffd5-439a-88de-6c0e6b889ed1',	'browser',	'Browser based authentication',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1'),
        ('cc0dd7b4-58c6-4c1c-8993-b87551128adf',	'forms',	'Username, password, otp and other auth forms.',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('48b73e55-2f7d-4012-a4e9-4a16d53b573c',	'Browser - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('fe30742f-cbbc-4bc6-ad54-25bc1db6b9c2',	'Organization',	NULL,	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('46fa4127-6f0a-4d35-996f-ae507c68b7f6',	'Browser - Conditional Organization',	'Flow to determine if the organization identity-first login is to be used',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('9cd24228-ad2b-41a5-a179-79ff571d3f4e',	'direct grant',	'OpenID Connect Resource Owner Grant',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1'),
        ('57c8f9cc-03ca-44cd-b13f-74fcc0c3e853',	'Direct Grant - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('aae7828a-8732-4d18-a944-e064a390a16d',	'registration',	'Registration flow',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1'),
        ('135af02b-fb37-43f0-89a6-a2eb35ec19bb',	'registration form',	'Registration form',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'form-flow',	'0',	'1'),
        ('14414cbc-ef1d-481c-b5bf-a9da2366671c',	'reset credentials',	'Reset credentials for a user if they forgot their password or something',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1'),
        ('4b146f2d-2d9a-4bc9-9be1-2b05b960b839',	'Reset - Conditional OTP',	'Flow to determine if the OTP should be reset or not. Set to REQUIRED to force.',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('6b1855bd-dc9d-4589-acda-04fcf4e33d33',	'clients',	'Base authentication for clients',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'client-flow',	'1',	'1'),
        ('87568ca1-089e-423e-a47c-38de2f7b5e0a',	'first broker login',	'Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1'),
        ('616f57b1-e520-43e7-a6e8-014b2cb4d443',	'User creation or linking',	'Flow for the existing/non-existing user alternatives',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('e3357b4d-8494-4986-896a-969abd5507ee',	'Handle Existing Account',	'Handle what to do if there is existing account with same email/username like authenticated identity provider',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('ab7254b9-18ec-4991-bff1-3cd7569dcf55',	'Account verification options',	'Method with which to verity the existing account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('cf706f9e-b131-4917-ac4a-c7ddc7283e29',	'Verify Existing Account by Re-authentication',	'Reauthentication of existing account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('a5bd1904-b771-4334-a8d2-ca0b4a381d0c',	'First broker login - Conditional OTP',	'Flow to determine if the OTP is required for the authentication',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('7dff29d4-171a-48a6-a43e-c586093bcfdf',	'First Broker Login - Conditional Organization',	'Flow to determine if the authenticator that adds organization members is to be used',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'0',	'1'),
        ('dce61608-f431-4fd3-9832-ecd12b65e5d8',	'saml ecp',	'SAML ECP Profile Authentication Flow',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1'),
        ('a56e9328-65ce-4869-9ba2-16807cd25c7b',	'docker auth',	'Used by Docker clients to authenticate against the IDP',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'basic-flow',	'1',	'1');

        CREATE TABLE "public"."authenticator_config" (
            "id" character varying(36) NOT NULL,
            "alias" character varying(255),
            "realm_id" character varying(36),
            CONSTRAINT "constraint_auth_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_auth_config_realm ON public.authenticator_config USING btree (realm_id);

        INSERT INTO "authenticator_config" ("id", "alias", "realm_id") VALUES
        ('c8e8254e-a1ce-4a5d-8ee7-e1ecaeeb3476',	'review profile config',	'60cc6974-2944-452e-a1b4-9ca8d732ed55'),
        ('da46079b-2d11-4338-9b34-35d4b266dfa7',	'create unique user config',	'60cc6974-2944-452e-a1b4-9ca8d732ed55'),
        ('8b0e4567-d26c-455f-a491-9c62d2ce2113',	'review profile config',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f'),
        ('bd5b6077-eaa0-41e3-8097-d270a14fe3d4',	'create unique user config',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f'),
        ('5b1760d7-d8c2-47d7-b817-43bddab4e632',	'review profile config',	'46e0cc9f-90b8-49f5-87c0-130927b60639'),
        ('48e29fef-1775-42e5-a5e7-c1888b7f79f2',	'create unique user config',	'46e0cc9f-90b8-49f5-87c0-130927b60639');

        CREATE TABLE "public"."authenticator_config_entry" (
            "authenticator_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_auth_cfg_pk" PRIMARY KEY ("authenticator_id", "name")
        ) WITH (oids = false);

        INSERT INTO "authenticator_config_entry" ("authenticator_id", "value", "name") VALUES
        ('c8e8254e-a1ce-4a5d-8ee7-e1ecaeeb3476',	'missing',	'update.profile.on.first.login'),
        ('da46079b-2d11-4338-9b34-35d4b266dfa7',	'false',	'require.password.update.after.registration'),
        ('8b0e4567-d26c-455f-a491-9c62d2ce2113',	'missing',	'update.profile.on.first.login'),
        ('bd5b6077-eaa0-41e3-8097-d270a14fe3d4',	'false',	'require.password.update.after.registration'),
        ('48e29fef-1775-42e5-a5e7-c1888b7f79f2',	'false',	'require.password.update.after.registration'),
        ('5b1760d7-d8c2-47d7-b817-43bddab4e632',	'missing',	'update.profile.on.first.login');

        CREATE TABLE "public"."broker_link" (
            "identity_provider" character varying(255) NOT NULL,
            "storage_provider_id" character varying(255),
            "realm_id" character varying(36) NOT NULL,
            "broker_user_id" character varying(255),
            "broker_username" character varying(255),
            "token" text,
            "user_id" character varying(255) NOT NULL,
            CONSTRAINT "constr_broker_link_pk" PRIMARY KEY ("identity_provider", "user_id")
        ) WITH (oids = false);


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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_b71cjlbenv945rb6gcon438at ON public.client USING btree (realm_id, client_id);

        CREATE INDEX idx_client_id ON public.client USING btree (client_id);

        INSERT INTO "client" ("id", "enabled", "full_scope_allowed", "client_id", "not_before", "public_client", "secret", "base_url", "bearer_only", "management_url", "surrogate_auth_required", "realm_id", "protocol", "node_rereg_timeout", "frontchannel_logout", "consent_required", "name", "service_accounts_enabled", "client_authenticator_type", "root_url", "description", "registration_token", "standard_flow_enabled", "implicit_flow_enabled", "direct_access_grants_enabled", "always_display_in_console") VALUES
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'0',	'master-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	0,	'0',	'0',	'master Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/master/account/',	'0',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/master/account/',	'0',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'1',	'1',	'security-admin-console',	0,	'1',	NULL,	'/admin/master/console/',	'0',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'1',	'1',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'0',	'tenant-manager-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	0,	'0',	'0',	'tenant-manager Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'0',	'realm-management',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	0,	'0',	'0',	'${client_realm-management}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/tenant-manager/account/',	'0',	NULL,	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/tenant-manager/account/',	'0',	NULL,	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'1',	'1',	'security-admin-console',	0,	'1',	NULL,	'/admin/tenant-manager/console/',	'0',	NULL,	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'1',	'1',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'1',	'1',	'tenant-manager',	0,	'1',	NULL,	'',	'0',	'',	'0',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'openid-connect',	-1,	'1',	'0',	'',	'0',	'client-secret',	'',	'',	NULL,	'1',	'0',	'1',	'0'),
        ('5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'0',	'openk9-realm',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	0,	'0',	'0',	'openk9 Realm',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'0',	'realm-management',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	0,	'0',	'0',	'${client_realm-management}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'0',	'account',	0,	'1',	NULL,	'/realms/openk9/account/',	'0',	NULL,	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	0,	'0',	'0',	'${client_account}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'1',	'0',	'account-console',	0,	'1',	NULL,	'/realms/openk9/account/',	'0',	NULL,	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	0,	'0',	'0',	'${client_account-console}',	'0',	'client-secret',	'${authBaseUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'1',	'0',	'broker',	0,	'0',	NULL,	NULL,	'1',	NULL,	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	0,	'0',	'0',	'${client_broker}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'1',	'1',	'security-admin-console',	0,	'1',	NULL,	'/admin/openk9/console/',	'0',	NULL,	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	0,	'0',	'0',	'${client_security-admin-console}',	'0',	'client-secret',	'${authAdminUrl}',	NULL,	NULL,	'1',	'0',	'0',	'0'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'1',	'1',	'admin-cli',	0,	'1',	NULL,	NULL,	'0',	NULL,	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	0,	'0',	'0',	'${client_admin-cli}',	'0',	'client-secret',	NULL,	NULL,	NULL,	'0',	'0',	'1',	'0'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'1',	'1',	'openk9',	0,	'1',	NULL,	'',	'0',	'',	'0',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'openid-connect',	-1,	'1',	'0',	'',	'0',	'client-secret',	'',	'',	NULL,	'1',	'0',	'1',	'0');

        CREATE TABLE "public"."client_attributes" (
            "client_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_3c" PRIMARY KEY ("client_id", "name")
        ) WITH (oids = false);

        CREATE INDEX idx_client_att_by_name_value ON public.client_attributes USING btree (name, substr(value, 1, 255));

        INSERT INTO "client_attributes" ("client_id", "name", "value") VALUES
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'post.logout.redirect.uris',	'+'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'post.logout.redirect.uris',	'+'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'pkce.code.challenge.method',	'S256'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'post.logout.redirect.uris',	'+'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'pkce.code.challenge.method',	'S256'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'client.use.lightweight.access.token.enabled',	'true'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'client.use.lightweight.access.token.enabled',	'true'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'post.logout.redirect.uris',	'+'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'post.logout.redirect.uris',	'+'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'pkce.code.challenge.method',	'S256'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'post.logout.redirect.uris',	'+'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'pkce.code.challenge.method',	'S256'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'client.use.lightweight.access.token.enabled',	'true'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'client.use.lightweight.access.token.enabled',	'true'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'oauth2.device.authorization.grant.enabled',	'false'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'oidc.ciba.grant.enabled',	'false'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'post.logout.redirect.uris',	'+'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'backchannel.logout.session.required',	'true'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'backchannel.logout.revoke.offline.tokens',	'false'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'post.logout.redirect.uris',	'+'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'post.logout.redirect.uris',	'+'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'pkce.code.challenge.method',	'S256'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'post.logout.redirect.uris',	'+'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'pkce.code.challenge.method',	'S256'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'client.use.lightweight.access.token.enabled',	'true'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'client.use.lightweight.access.token.enabled',	'true'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'oauth2.device.authorization.grant.enabled',	'false'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'oidc.ciba.grant.enabled',	'false'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'post.logout.redirect.uris',	'+'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'backchannel.logout.session.required',	'true'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'backchannel.logout.revoke.offline.tokens',	'false');

        CREATE TABLE "public"."client_auth_flow_bindings" (
            "client_id" character varying(36) NOT NULL,
            "flow_id" character varying(36),
            "binding_name" character varying(255) NOT NULL,
            CONSTRAINT "c_cli_flow_bind" PRIMARY KEY ("client_id", "binding_name")
        ) WITH (oids = false);


        CREATE TABLE "public"."client_initial_access" (
            "id" character varying(36) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "timestamp" integer,
            "expiration" integer,
            "count" integer,
            "remaining_count" integer,
            CONSTRAINT "cnstr_client_init_acc_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_client_init_acc_realm ON public.client_initial_access USING btree (realm_id);


        CREATE TABLE "public"."client_node_registrations" (
            "client_id" character varying(36) NOT NULL,
            "value" integer,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_84" PRIMARY KEY ("client_id", "name")
        ) WITH (oids = false);


        CREATE TABLE "public"."client_scope" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "realm_id" character varying(36),
            "description" character varying(255),
            "protocol" character varying(255),
            CONSTRAINT "pk_cli_template" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_cli_scope ON public.client_scope USING btree (realm_id, name);

        CREATE INDEX idx_realm_clscope ON public.client_scope USING btree (realm_id);

        INSERT INTO "client_scope" ("id", "name", "realm_id", "description", "protocol") VALUES
        ('5cd8523c-5f60-463a-b532-2cfaf55ded80',	'offline_access',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('bc89c033-c280-43dc-8cdb-bc269d05e524',	'role_list',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'SAML role list',	'saml'),
        ('bea1a009-6cf9-4d99-b259-d3d8c3fda3f1',	'saml_organization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'Organization Membership',	'saml'),
        ('10591155-81d6-49f0-a8f5-e8087a63db62',	'profile',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('d345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'email',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'address',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('25a3c139-2ee5-4355-8438-1f297d538aee',	'phone',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'roles',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('a2c56a67-31c9-4df1-8118-3ac8734653db',	'web-origins',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'microprofile-jwt',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('d5c86a99-148a-4768-bfca-b576dc02c70a',	'acr',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect'),
        ('3380a789-01be-4463-ab25-474b6c8fd663',	'basic',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'OpenID Connect scope for add all basic claims to the token',	'openid-connect'),
        ('f9f30390-aaf7-4a28-ade5-ee2a1921d4b9',	'service_account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'Specific scope for a client enabled for service accounts',	'openid-connect'),
        ('b3104f32-05c6-4937-ab4d-cf157a3353dd',	'organization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'Additional claims about the organization a subject belongs to',	'openid-connect'),
        ('fe1baebc-39a9-429e-a86e-36c4be33b83b',	'offline_access',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('51feec57-d3d7-4687-b90a-70518e0d0c46',	'role_list',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'SAML role list',	'saml'),
        ('57ac9c43-a988-4e5f-8f0a-bc0e3a09f280',	'saml_organization',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'Organization Membership',	'saml'),
        ('8335dfdb-9837-4d49-9666-7a6b93a46509',	'profile',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'email',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('52af6a76-82be-45a7-918d-48ff410082d1',	'address',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'phone',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('d8c80810-aa75-4c40-921f-25c3b5c721d3',	'roles',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('dc95d831-220d-45e3-8086-09384d30d4f9',	'web-origins',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'microprofile-jwt',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('7ca1d277-d673-46fb-89ad-f31679e2a29a',	'acr',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect'),
        ('731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'basic',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'OpenID Connect scope for add all basic claims to the token',	'openid-connect'),
        ('042672bb-0c5b-43d6-bbcc-c5aaffd1747f',	'service_account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'Specific scope for a client enabled for service accounts',	'openid-connect'),
        ('e18b0b07-aa7f-419d-812b-ebf112027b72',	'organization',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'Additional claims about the organization a subject belongs to',	'openid-connect'),
        ('744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'offline_access',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect built-in scope: offline_access',	'openid-connect'),
        ('3128e6da-8350-4d17-9d92-825fe80b7303',	'role_list',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'SAML role list',	'saml'),
        ('af2e4cec-ac0b-4121-b053-9ff6ad8afa5e',	'saml_organization',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'Organization Membership',	'saml'),
        ('43547928-98c8-48cd-989b-b2eecabb8f12',	'profile',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect built-in scope: profile',	'openid-connect'),
        ('f775ff97-aa91-4817-b493-2c7a70236ec8',	'email',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect built-in scope: email',	'openid-connect'),
        ('f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'address',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect built-in scope: address',	'openid-connect'),
        ('4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'phone',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect built-in scope: phone',	'openid-connect'),
        ('1424086c-fade-4ad5-91eb-b4717a3e148f',	'roles',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect scope for add user roles to the access token',	'openid-connect'),
        ('5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'web-origins',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect scope for add allowed web origins to the access token',	'openid-connect'),
        ('c691f15d-a84d-4563-b053-588cd21daf1e',	'microprofile-jwt',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'Microprofile - JWT built-in scope',	'openid-connect'),
        ('a401b408-9edd-4ad9-a067-55228409b062',	'acr',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect scope for add acr (authentication context class reference) to the token',	'openid-connect'),
        ('58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'basic',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'OpenID Connect scope for add all basic claims to the token',	'openid-connect'),
        ('53bf40a1-985c-4ea5-a3be-9fde41c60825',	'service_account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'Specific scope for a client enabled for service accounts',	'openid-connect'),
        ('174806e1-084a-42cd-b3ad-800599d4a2dd',	'organization',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'Additional claims about the organization a subject belongs to',	'openid-connect');

        CREATE TABLE "public"."client_scope_attributes" (
            "scope_id" character varying(36) NOT NULL,
            "value" character varying(2048),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "pk_cl_tmpl_attr" PRIMARY KEY ("scope_id", "name")
        ) WITH (oids = false);

        CREATE INDEX idx_clscope_attrs ON public.client_scope_attributes USING btree (scope_id);

        INSERT INTO "client_scope_attributes" ("scope_id", "value", "name") VALUES
        ('5cd8523c-5f60-463a-b532-2cfaf55ded80',	'true',	'display.on.consent.screen'),
        ('5cd8523c-5f60-463a-b532-2cfaf55ded80',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('bc89c033-c280-43dc-8cdb-bc269d05e524',	'true',	'display.on.consent.screen'),
        ('bc89c033-c280-43dc-8cdb-bc269d05e524',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('bea1a009-6cf9-4d99-b259-d3d8c3fda3f1',	'false',	'display.on.consent.screen'),
        ('10591155-81d6-49f0-a8f5-e8087a63db62',	'true',	'display.on.consent.screen'),
        ('10591155-81d6-49f0-a8f5-e8087a63db62',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('10591155-81d6-49f0-a8f5-e8087a63db62',	'true',	'include.in.token.scope'),
        ('d345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'true',	'display.on.consent.screen'),
        ('d345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('d345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'true',	'include.in.token.scope'),
        ('9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'true',	'display.on.consent.screen'),
        ('9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'true',	'include.in.token.scope'),
        ('25a3c139-2ee5-4355-8438-1f297d538aee',	'true',	'display.on.consent.screen'),
        ('25a3c139-2ee5-4355-8438-1f297d538aee',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('25a3c139-2ee5-4355-8438-1f297d538aee',	'true',	'include.in.token.scope'),
        ('eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'true',	'display.on.consent.screen'),
        ('eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'false',	'include.in.token.scope'),
        ('a2c56a67-31c9-4df1-8118-3ac8734653db',	'false',	'display.on.consent.screen'),
        ('a2c56a67-31c9-4df1-8118-3ac8734653db',	'',	'consent.screen.text'),
        ('a2c56a67-31c9-4df1-8118-3ac8734653db',	'false',	'include.in.token.scope'),
        ('9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'false',	'display.on.consent.screen'),
        ('9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'true',	'include.in.token.scope'),
        ('d5c86a99-148a-4768-bfca-b576dc02c70a',	'false',	'display.on.consent.screen'),
        ('d5c86a99-148a-4768-bfca-b576dc02c70a',	'false',	'include.in.token.scope'),
        ('3380a789-01be-4463-ab25-474b6c8fd663',	'false',	'display.on.consent.screen'),
        ('3380a789-01be-4463-ab25-474b6c8fd663',	'false',	'include.in.token.scope'),
        ('f9f30390-aaf7-4a28-ade5-ee2a1921d4b9',	'false',	'display.on.consent.screen'),
        ('f9f30390-aaf7-4a28-ade5-ee2a1921d4b9',	'false',	'include.in.token.scope'),
        ('b3104f32-05c6-4937-ab4d-cf157a3353dd',	'true',	'display.on.consent.screen'),
        ('b3104f32-05c6-4937-ab4d-cf157a3353dd',	'${organizationScopeConsentText}',	'consent.screen.text'),
        ('b3104f32-05c6-4937-ab4d-cf157a3353dd',	'true',	'include.in.token.scope'),
        ('fe1baebc-39a9-429e-a86e-36c4be33b83b',	'true',	'display.on.consent.screen'),
        ('fe1baebc-39a9-429e-a86e-36c4be33b83b',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('51feec57-d3d7-4687-b90a-70518e0d0c46',	'true',	'display.on.consent.screen'),
        ('51feec57-d3d7-4687-b90a-70518e0d0c46',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('57ac9c43-a988-4e5f-8f0a-bc0e3a09f280',	'false',	'display.on.consent.screen'),
        ('8335dfdb-9837-4d49-9666-7a6b93a46509',	'true',	'display.on.consent.screen'),
        ('8335dfdb-9837-4d49-9666-7a6b93a46509',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('8335dfdb-9837-4d49-9666-7a6b93a46509',	'true',	'include.in.token.scope'),
        ('fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'true',	'display.on.consent.screen'),
        ('fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'true',	'include.in.token.scope'),
        ('52af6a76-82be-45a7-918d-48ff410082d1',	'true',	'display.on.consent.screen'),
        ('52af6a76-82be-45a7-918d-48ff410082d1',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('52af6a76-82be-45a7-918d-48ff410082d1',	'true',	'include.in.token.scope'),
        ('8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'true',	'display.on.consent.screen'),
        ('8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'true',	'include.in.token.scope'),
        ('d8c80810-aa75-4c40-921f-25c3b5c721d3',	'true',	'display.on.consent.screen'),
        ('d8c80810-aa75-4c40-921f-25c3b5c721d3',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('d8c80810-aa75-4c40-921f-25c3b5c721d3',	'false',	'include.in.token.scope'),
        ('dc95d831-220d-45e3-8086-09384d30d4f9',	'false',	'display.on.consent.screen'),
        ('dc95d831-220d-45e3-8086-09384d30d4f9',	'',	'consent.screen.text'),
        ('dc95d831-220d-45e3-8086-09384d30d4f9',	'false',	'include.in.token.scope'),
        ('25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'false',	'display.on.consent.screen'),
        ('25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'true',	'include.in.token.scope'),
        ('7ca1d277-d673-46fb-89ad-f31679e2a29a',	'false',	'display.on.consent.screen'),
        ('7ca1d277-d673-46fb-89ad-f31679e2a29a',	'false',	'include.in.token.scope'),
        ('731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'false',	'display.on.consent.screen'),
        ('731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'false',	'include.in.token.scope'),
        ('042672bb-0c5b-43d6-bbcc-c5aaffd1747f',	'false',	'display.on.consent.screen'),
        ('042672bb-0c5b-43d6-bbcc-c5aaffd1747f',	'false',	'include.in.token.scope'),
        ('e18b0b07-aa7f-419d-812b-ebf112027b72',	'true',	'display.on.consent.screen'),
        ('e18b0b07-aa7f-419d-812b-ebf112027b72',	'${organizationScopeConsentText}',	'consent.screen.text'),
        ('e18b0b07-aa7f-419d-812b-ebf112027b72',	'true',	'include.in.token.scope'),
        ('744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'true',	'display.on.consent.screen'),
        ('744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'${offlineAccessScopeConsentText}',	'consent.screen.text'),
        ('3128e6da-8350-4d17-9d92-825fe80b7303',	'true',	'display.on.consent.screen'),
        ('3128e6da-8350-4d17-9d92-825fe80b7303',	'${samlRoleListScopeConsentText}',	'consent.screen.text'),
        ('af2e4cec-ac0b-4121-b053-9ff6ad8afa5e',	'false',	'display.on.consent.screen'),
        ('43547928-98c8-48cd-989b-b2eecabb8f12',	'true',	'display.on.consent.screen'),
        ('43547928-98c8-48cd-989b-b2eecabb8f12',	'${profileScopeConsentText}',	'consent.screen.text'),
        ('43547928-98c8-48cd-989b-b2eecabb8f12',	'true',	'include.in.token.scope'),
        ('f775ff97-aa91-4817-b493-2c7a70236ec8',	'true',	'display.on.consent.screen'),
        ('f775ff97-aa91-4817-b493-2c7a70236ec8',	'${emailScopeConsentText}',	'consent.screen.text'),
        ('f775ff97-aa91-4817-b493-2c7a70236ec8',	'true',	'include.in.token.scope'),
        ('f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'true',	'display.on.consent.screen'),
        ('f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'${addressScopeConsentText}',	'consent.screen.text'),
        ('f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'true',	'include.in.token.scope'),
        ('4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'true',	'display.on.consent.screen'),
        ('4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'${phoneScopeConsentText}',	'consent.screen.text'),
        ('4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'true',	'include.in.token.scope'),
        ('1424086c-fade-4ad5-91eb-b4717a3e148f',	'true',	'display.on.consent.screen'),
        ('1424086c-fade-4ad5-91eb-b4717a3e148f',	'${rolesScopeConsentText}',	'consent.screen.text'),
        ('1424086c-fade-4ad5-91eb-b4717a3e148f',	'false',	'include.in.token.scope'),
        ('5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'false',	'display.on.consent.screen'),
        ('5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'',	'consent.screen.text'),
        ('5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'false',	'include.in.token.scope'),
        ('c691f15d-a84d-4563-b053-588cd21daf1e',	'false',	'display.on.consent.screen'),
        ('c691f15d-a84d-4563-b053-588cd21daf1e',	'true',	'include.in.token.scope'),
        ('a401b408-9edd-4ad9-a067-55228409b062',	'false',	'display.on.consent.screen'),
        ('a401b408-9edd-4ad9-a067-55228409b062',	'false',	'include.in.token.scope'),
        ('58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'false',	'display.on.consent.screen'),
        ('58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'false',	'include.in.token.scope'),
        ('53bf40a1-985c-4ea5-a3be-9fde41c60825',	'false',	'display.on.consent.screen'),
        ('53bf40a1-985c-4ea5-a3be-9fde41c60825',	'false',	'include.in.token.scope'),
        ('174806e1-084a-42cd-b3ad-800599d4a2dd',	'true',	'display.on.consent.screen'),
        ('174806e1-084a-42cd-b3ad-800599d4a2dd',	'${organizationScopeConsentText}',	'consent.screen.text'),
        ('174806e1-084a-42cd-b3ad-800599d4a2dd',	'true',	'include.in.token.scope');

        CREATE TABLE "public"."client_scope_client" (
            "client_id" character varying(255) NOT NULL,
            "scope_id" character varying(255) NOT NULL,
            "default_scope" boolean DEFAULT false NOT NULL,
            CONSTRAINT "c_cli_scope_bind" PRIMARY KEY ("client_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX idx_clscope_cl ON public.client_scope_client USING btree (client_id);

        CREATE INDEX idx_cl_clscope ON public.client_scope_client USING btree (scope_id);

        INSERT INTO "client_scope_client" ("client_id", "scope_id", "default_scope") VALUES
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('d325aa12-7b59-46f1-aeec-357f177e939a',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('4b422d32-a6b3-4919-aedf-183726855a3d',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('5911cec8-88d4-4ac6-ac6c-4070628dc0e3',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('401f9b61-d1ea-4579-8def-0047bcea9776',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('315b3003-d0f9-4a6f-896a-7dd913fed925',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('212c7998-eb96-4635-8ab2-c70006bb1e83',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('2355c5ca-f94a-4858-9b94-e81179e44e89',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('37eb8750-21cd-4452-8c27-10873468e17e',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0');

        CREATE TABLE "public"."client_scope_role_mapping" (
            "scope_id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            CONSTRAINT "pk_template_scope" PRIMARY KEY ("scope_id", "role_id")
        ) WITH (oids = false);

        CREATE INDEX idx_clscope_role ON public.client_scope_role_mapping USING btree (scope_id);

        CREATE INDEX idx_role_clscope ON public.client_scope_role_mapping USING btree (role_id);

        INSERT INTO "client_scope_role_mapping" ("scope_id", "role_id") VALUES
        ('5cd8523c-5f60-463a-b532-2cfaf55ded80',	'6586e32c-9de2-4ebf-85be-92b235951148'),
        ('fe1baebc-39a9-429e-a86e-36c4be33b83b',	'675b804d-7f39-41f9-bbd7-805d095b590e'),
        ('744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'6edf8d44-92b6-4db4-81fc-5deb7a693f50');

        CREATE TABLE "public"."component" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "parent_id" character varying(36),
            "provider_id" character varying(36),
            "provider_type" character varying(255),
            "realm_id" character varying(36),
            "sub_type" character varying(255),
            CONSTRAINT "constr_component_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_component_realm ON public.component USING btree (realm_id);

        CREATE INDEX idx_component_provider_type ON public.component USING btree (provider_type);

        INSERT INTO "component" ("id", "name", "parent_id", "provider_id", "provider_type", "realm_id", "sub_type") VALUES
        ('050e659c-f073-4912-a42a-60968e584244',	'Trusted Hosts',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'anonymous'),
        ('2aea972d-39c0-4e77-9156-edd6511c12e0',	'Consent Required',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'anonymous'),
        ('0829372c-090e-4f1c-9cdf-ddafb82c9cc0',	'Full Scope Disabled',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'anonymous'),
        ('598ccbfd-27ae-43fd-8518-716f9278f66e',	'Max Clients Limit',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'anonymous'),
        ('cac925bf-d035-4b18-9da8-278d88c60ddc',	'Allowed Protocol Mapper Types',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'anonymous'),
        ('c35ecf65-a31c-4087-b216-435a606d3081',	'Allowed Client Scopes',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'anonymous'),
        ('36e2afe3-b6cf-4261-a2ae-6952f601823e',	'Allowed Protocol Mapper Types',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'authenticated'),
        ('76abba3e-9da2-44cd-b127-6390975f44d7',	'Allowed Client Scopes',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'authenticated'),
        ('0d4cb60a-b0c8-486b-ab0c-b00859fd0617',	'rsa-generated',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL),
        ('bd3a45cc-aa3a-4061-bfa3-f006590e2eda',	'rsa-enc-generated',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL),
        ('5ccf77f1-da63-4659-820b-9007b9ee9008',	'hmac-generated-hs512',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL),
        ('aea88527-c8f1-4b71-87b9-5451e23d734c',	'aes-generated',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL),
        ('d53c54d9-6f44-4545-92ca-1d790ae58a96',	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'declarative-user-profile',	'org.keycloak.userprofile.UserProfileProvider',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL),
        ('9f9d2182-48d6-4d93-8bf6-189b459f8a15',	'rsa-generated',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL),
        ('46291381-6c1e-4692-babd-8f84d449ceb5',	'rsa-enc-generated',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL),
        ('2d37501e-42dd-4b21-a75d-50c791731d48',	'hmac-generated-hs512',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL),
        ('f057ba75-2e5b-49e6-99e5-ffcf55a82977',	'aes-generated',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL),
        ('bce0a59e-e434-49ab-b224-8af720b29b39',	'Trusted Hosts',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'anonymous'),
        ('07b3f8be-ecf0-4a20-bca4-583884737d0d',	'Consent Required',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'anonymous'),
        ('1542f8cb-9cd8-4bfa-9351-87fdb794df7b',	'Full Scope Disabled',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'anonymous'),
        ('8d7a1ab5-e802-47db-94c2-e3c1183a2a23',	'Max Clients Limit',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'anonymous'),
        ('af27259e-dce9-4ccc-85ff-d352b870ce16',	'Allowed Protocol Mapper Types',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'anonymous'),
        ('c1004821-f903-4e4f-8bf9-5f29e50174fa',	'Allowed Client Scopes',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'anonymous'),
        ('9b252e48-aa01-4fc7-b105-995df3e41391',	'Allowed Protocol Mapper Types',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'authenticated'),
        ('790676e0-4631-4c51-8a58-3d1ecccfebe7',	'Allowed Client Scopes',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'authenticated'),
        ('ebb3953c-3a57-418d-8c5d-30421c42a201',	'rsa-generated',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'rsa-generated',	'org.keycloak.keys.KeyProvider',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL),
        ('89d14d92-0748-4252-b983-f4462cf4c66e',	'rsa-enc-generated',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'rsa-enc-generated',	'org.keycloak.keys.KeyProvider',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL),
        ('c8f83698-cb68-4a7c-835e-0f4c948b446b',	'hmac-generated-hs512',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'hmac-generated',	'org.keycloak.keys.KeyProvider',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL),
        ('53fe87fd-7ed5-43fb-bdf0-2fd64d2863a1',	'aes-generated',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'aes-generated',	'org.keycloak.keys.KeyProvider',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL),
        ('9167bf6d-f224-46c9-99eb-0ab7f226d4f7',	'Trusted Hosts',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'trusted-hosts',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'anonymous'),
        ('4bccaa7a-00be-415b-a9c3-ef0b62780381',	'Consent Required',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'consent-required',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'anonymous'),
        ('f0743118-8d83-4aba-8e38-159b41b3cd07',	'Full Scope Disabled',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'scope',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'anonymous'),
        ('1a889efc-87f2-4d85-83fe-5cee79afc1b9',	'Max Clients Limit',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'max-clients',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'anonymous'),
        ('3dd98894-e6bc-4218-8cb9-db91218ebe38',	'Allowed Protocol Mapper Types',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'anonymous'),
        ('e49c7732-70c2-44ac-88f6-a7a1751de3a0',	'Allowed Client Scopes',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'anonymous'),
        ('2683551d-ff74-4699-ae31-583028ea8555',	'Allowed Protocol Mapper Types',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'allowed-protocol-mappers',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'authenticated'),
        ('f7af03c5-8fea-436f-9e9c-270f4c9cea78',	'Allowed Client Scopes',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'allowed-client-templates',	'org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'authenticated');

        CREATE TABLE "public"."component_config" (
            "id" character varying(36) NOT NULL,
            "component_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constr_component_config_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_compo_config_compo ON public.component_config USING btree (component_id);

        INSERT INTO "component_config" ("id", "component_id", "name", "value") VALUES
        ('e4857ab0-e854-4bf4-9f44-62559bfa0899',	'050e659c-f073-4912-a42a-60968e584244',	'host-sending-registration-request-must-match',	'true'),
        ('cbf94141-c7a0-4cfe-b6e9-0bf0d0cc750a',	'050e659c-f073-4912-a42a-60968e584244',	'client-uris-must-match',	'true'),
        ('8306e1ee-c0e3-43f6-83ba-4f7723457fd7',	'598ccbfd-27ae-43fd-8518-716f9278f66e',	'max-clients',	'200'),
        ('576f4785-d461-47c5-8d3f-5d66929c633f',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('934888ef-1ad4-4a39-a54d-b3364bbd6b5d',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('012267ca-dcd7-4be6-936f-0250ad223c77',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('77c9054c-1ae7-45f0-a9d1-fe1238a704c8',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('4b26bbc7-26ee-4770-a73b-ce3254b28c48',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('6e1e5cf6-5aa3-4bce-beb3-d58ee528c9c0',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('3ae135c6-f834-4338-81b3-79695cf07991',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('d6725fd1-31e3-4328-bf1b-4e32e58a5bee',	'36e2afe3-b6cf-4261-a2ae-6952f601823e',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('86d68f21-77f1-4f58-9480-2747189314f6',	'76abba3e-9da2-44cd-b127-6390975f44d7',	'allow-default-scopes',	'true'),
        ('26983e87-e1e3-4ef5-8724-58f950a8c7b7',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('85ef01c9-cd09-48f6-87e8-ed584ab65205',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('097cc672-299e-4689-91ec-3314eab24d19',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('79474ef9-0bf6-4198-a31d-e09c795d5a72',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('9c11e97d-dd7d-45ad-b4fd-454dbfea2d70',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('4e52288c-f1b0-467f-acb4-144adbe01756',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('373aa3c7-73b1-45c3-8edf-f3084bbf79f6',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('e4b049f5-4e57-4b12-b3a8-4564264cc9c9',	'cac925bf-d035-4b18-9da8-278d88c60ddc',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('c0176d07-bf26-4534-bd8d-760485641e2f',	'c35ecf65-a31c-4087-b216-435a606d3081',	'allow-default-scopes',	'true'),
        ('e23c70d1-6563-4232-bc69-8f7ce79885f3',	'5ccf77f1-da63-4659-820b-9007b9ee9008',	'kid',	'66849304-2199-4306-8fe0-0042cb90c01b'),
        ('f932f368-d2bd-411a-9394-292ebb156f82',	'5ccf77f1-da63-4659-820b-9007b9ee9008',	'algorithm',	'HS512'),
        ('7e98d7b4-111e-4f83-b23e-9c0e05f26959',	'5ccf77f1-da63-4659-820b-9007b9ee9008',	'priority',	'100'),
        ('2104cc85-51ab-4dff-9594-232b7e74d129',	'5ccf77f1-da63-4659-820b-9007b9ee9008',	'secret',	'JWD-p63RxPdy_4NxGA8PaOhrScYFgaIPY74ljIuQVFRKPiVVwfZ3FFzTw73N6ZjzIcCUYhXnqKswOCT1f5h6GrKdNFTYtAvaT08HZ9q3t4cTAUMVVtp9iYmxXjnERmtcrpjymlmyqOEI5IyBiihmMcS648gbeXCvF3lDmNknk_w'),
        ('a1867e51-37ae-49f6-b37a-83e771982c7d',	'aea88527-c8f1-4b71-87b9-5451e23d734c',	'priority',	'100'),
        ('9062fc07-bc72-4318-8250-2e48f71b61bd',	'aea88527-c8f1-4b71-87b9-5451e23d734c',	'kid',	'54fecb2c-cfaf-48b1-bef1-71e8370d0a42'),
        ('a5889e89-8070-430b-bbe2-2d5a3e5c50b4',	'aea88527-c8f1-4b71-87b9-5451e23d734c',	'secret',	'Pu07aT30TEPED_UbLWLP0Q'),
        ('2c100431-6fe9-498d-985a-12e8abb57b0b',	'd53c54d9-6f44-4545-92ca-1d790ae58a96',	'kc.user.profile.config',	'{"attributes":[{"name":"username","displayName":"${username}","validations":{"length":{"min":3,"max":255},"username-prohibited-characters":{},"up-username-not-idn-homograph":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"email","displayName":"${email}","validations":{"email":{},"length":{"max":255}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"firstName","displayName":"${firstName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false},{"name":"lastName","displayName":"${lastName}","validations":{"length":{"max":255},"person-name-prohibited-characters":{}},"permissions":{"view":["admin","user"],"edit":["admin","user"]},"multivalued":false}],"groups":[{"name":"user-metadata","displayHeader":"User metadata","displayDescription":"Attributes, which refer to user metadata"}]}'),
        ('606e2181-4576-4389-814d-825df8d70716',	'0d4cb60a-b0c8-486b-ab0c-b00859fd0617',	'keyUse',	'SIG'),
        ('68f0674d-2d60-4ac8-bbfe-8937153eca2f',	'0d4cb60a-b0c8-486b-ab0c-b00859fd0617',	'privateKey',	'MIIEpAIBAAKCAQEAoMPxmA2EcDby5tOTvVsdM6U0Ai3fO6aV3eSg4+ucSAqVTAQ7eygDPexd0VM+gxPMtvopxI1GnciiLScw4ss+GBL6WxJp96rZG3jzk4zGzMYUz/jvZo9R10qgAa93JkStsh0jz/TnbpbXh7xWBClyxiILNbsxc7HXn67r44wk0ztDX6iPu/oC1EwWsRn4Uoci52swQA6gc7WIUfQ3M6C/jryaFfVtDEAv+xgjyGa/EGmVtWbO+D1c+FtRHFVaihbCkwPJ6v/EO8TsQ4Sz+lYWJF8y0ucMF5/eXwxWKfDVkxKfIuEDQY1I71rh7gLALcHC3HSoyufhWMT3Q72q1yTcvQIDAQABAoIBACQujVptGmQ9/bOVJTBOCBe2Q+sEo3PzpVGcEdjSD2mIVjsWTcPFSNuALco9e7l49I8u5MUbjavXuqZCZoZ4exhkdwuVrJtEzJmaAmnwGlL+drMq4ch5k5AKd9IBSWXnuZb3Z3a3dfnGAaqHKO3FXVM52L+GSpYxDage8HCVOzl9xuhXE71mG6Sl9SaRc7G9qpJ8czk/NX2CdFkpt5eiBeXGWjUw13tm3nwb6AZcSoSNAZrm+W6f2hhY1BBrVwaeaRKZ6+2/mEiTuKV/A8elqYDxjXSKguG7z8J9KbHGHc7M5ZjYrHq9Gk1jLKK+nErOikyXPLJrXobM6uZx/OoMejkCgYEA0ff18JxuZ/zsDoUwj2XVrTfAwDm457oYT3n2chMRo1oRemVG1gbENXvYm1WWAnvE+ejDyag1TnVJ8yuQaKWISx7R5G0NKa1pViI522Q4OZSeavdUsK3XWjiU5f2U0HU34QD99fAtVIGTw9LmRFESRBbhq2LK7LZkEAOjiGxqh3UCgYEAxAKPFnBjkVElCG1VktqX0+tPX+AtqMeRy6HIRhG2jlbmNp8PatUkH42QinYmpdIYvaDUC9mQ4pEgsaz4CK2K74YooLF7c+NEx+QS/A0QcDEXWdkU6Fmm0bf8oYox4K29BZ5ScEn3dOMCxl8gRFCcVQPHPF0akNMJpU/oKN02HykCgYEAtlUcdnBxk83X85gHnk7xajVjJco6XsiEueeQcmlKxM6/JMlz5QIFOuisJeABn3B5kgdmleSDPfmuWQN4qNcubwV5gWKNrqaPNaQNGA6EoMQFeaAEJFUiEI/YBYzawNc5yiOBZiPGSX8ooUGwgpzgPs6b86ebzkloxVtTlHGJZM0CgYB3/TUIwH5DPBBWi0/CWWxuP3NUttBcrObUaFJIipldo1bEgzSV6qp1YlzkVhWsluOsWeLU6jfman9AJSmlbk9J96+xR1TSiYLWdwkkIP86HuMzjk9dOVyEr4PIg+eqqdC3usaulKkWdc2CEEOZblt1M53olvonQ/l4qGTvdxOlmQKBgQDKuHCcFnITiQwfJtMEelFvCb7tWi80oNJJROVd4XeG0G8smEwvlvkYJrXO33gLfUllzfBNHCaluauzj8ykWUQ1urs8tKO3jEAg+GLyD1kK5EK/MO2P9B9aF9HEhJJlUTcGYOrZj0f0pZb5q/JCuG0rE1HFxDmKtXEsWzre4MFafQ=='),
        ('769f2f1b-5d06-40a9-8449-0528af5929b8',	'f057ba75-2e5b-49e6-99e5-ffcf55a82977',	'kid',	'ba3f9936-0925-4000-b05f-237a831f607c'),
        ('b679e1aa-82ef-404d-9e7f-70bc0ac48363',	'f057ba75-2e5b-49e6-99e5-ffcf55a82977',	'priority',	'100'),
        ('55245620-531e-4c3b-be6d-8738e3ab6ae0',	'9f9d2182-48d6-4d93-8bf6-189b459f8a15',	'priority',	'100'),
        ('7600589e-d3b6-4e50-bfeb-49b1fc48c3e4',	'9167bf6d-f224-46c9-99eb-0ab7f226d4f7',	'client-uris-must-match',	'true'),
        ('7047725f-0c17-4930-a911-0c66bcdc35ca',	'1a889efc-87f2-4d85-83fe-5cee79afc1b9',	'max-clients',	'200'),
        ('2323d9aa-b48f-4168-9e75-ec6728709f09',	'0d4cb60a-b0c8-486b-ab0c-b00859fd0617',	'certificate',	'MIICmzCCAYMCBgGW49YkBzANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNTE4MTQzODA0WhcNMzUwNTE4MTQzOTQ0WjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCgw/GYDYRwNvLm05O9Wx0zpTQCLd87ppXd5KDj65xICpVMBDt7KAM97F3RUz6DE8y2+inEjUadyKItJzDiyz4YEvpbEmn3qtkbePOTjMbMxhTP+O9mj1HXSqABr3cmRK2yHSPP9OdulteHvFYEKXLGIgs1uzFzsdefruvjjCTTO0NfqI+7+gLUTBaxGfhShyLnazBADqBztYhR9DczoL+OvJoV9W0MQC/7GCPIZr8QaZW1Zs74PVz4W1EcVVqKFsKTA8nq/8Q7xOxDhLP6VhYkXzLS5wwXn95fDFYp8NWTEp8i4QNBjUjvWuHuAsAtwcLcdKjK5+FYxPdDvarXJNy9AgMBAAEwDQYJKoZIhvcNAQELBQADggEBADug7R/lWLfNOEUvNNPWG70Xn0Smy6vQrk06USBvCV1LgNbZV3qL41USHiP4vopTzoHqtooIGGs7OcJ0hfoWO/xAuimrabQ2YhjoWqgFpaLMXlZGrChuqEPsHGux0IfxJbDikWC/NZCAUtDFnPtgB/B3uii7WqJ7gqZQ9vHhZ5/owxOnva/E8HI3CVqyVIqXUHoW0fxeOu6bZOVfhXR723zuuB0/itG9jcVf/nBjm2/z4tSfINEKj4ulUMOCYkZcxqgmL4xqmxOvC3uMUh9cLNSd1s78/bsZvFbDD/yM/9xtjplQoK0UYW8vs+UyAJAReuD2MfQNnAlNJOf9d5EeKj8='),
        ('ce3d9d21-ef6e-48ca-8557-ca092ddfb426',	'0d4cb60a-b0c8-486b-ab0c-b00859fd0617',	'priority',	'100'),
        ('3f04615f-6635-410a-8361-d7e0c3ad25a8',	'bd3a45cc-aa3a-4061-bfa3-f006590e2eda',	'privateKey',	'MIIEowIBAAKCAQEAt626P2AgEyM0gOTfY3mniToSVhDV0p6Y0T/dYWlIaUNV684Ak2CUoSkicmHJC670KP9mgkQ2LP7Ll0NyESpUJQ0p3ahNCRQ0yKCWlN8vNYx9ni7i4zgoa4CwMx6KHcCNtKWzbchmMvroveYkfuT6bE2eXy+nN+Zxh4eDzQgq4oLub+UQSi07hue58KavHGO1Uzr1sbnWDAf8ALGL4ByFwk5GuK+3uXf6zZmnsp5yhTF5cwiuFkNIOFU40tl83pAv+mhXRopn1/cQv01SCRLaoYSq25ZbmEB4ljF2W6LLejBLlTET10hxNAcAdqhy11Sg0qDximbFSAGO8o3UHRZKqQIDAQABAoIBAAVBpABXTGOv0LtVkW0D2L8NGeQ7pBrcbgwi8z9r52YXxrgyU4LkMyyvhCxYzwuWDKe1s2XL9O7xtbrU88k4+GJ9D3Mn81hsqxwIuHZ3Mp8qL5gcyGJvQ3zcGG7GE3bJgWSnj0x3fTE9S/bPRMEa5W2/BOa9cuAfKKWTw7pU3wFPaIAqxLjC7MCQ1o8Keb/PCQ0T/a3jYEft3HMi4P7aTiBbMBH+q3utVnpeIKgJD7lCAXQdnu/ADFsrsGoMeTf6oaIbXyLXNV18VT942JmaYGdcuCpErUCKDvFe/WpbUypc+DcSWjSbIocpm2AQq3m2KirLert2DULRCgSyU23u3E8CgYEA3kSDtEmmN4I2eHn0sxjAcQTiyK09RP46lZDBU3l3PDG6xZChcdICtl2/pO3m2Pqft074Dw84dKm4dPeGm+5cos316E6IKw9WYwVT+VeVCh8+HqyT2q5g5dYnkZ3Q2Z39BYkEyomGNW6AshiJqR5mVN8bNvxs2q5vZocVq5x0jMsCgYEA0432beELe+6ixSGnKVxSaVl2lFy5a9EcLOd2/ji93BJ6d6UfVpPgzkVRgrZJeskwriCkcLRTxP2Ci8rSq+prtGJLXLer8XqpLYOiqTvEHAcAhVxHtGljKKInrdKI6nayRflCSvKIRbH8tkld2BIcylpaUaoxPE+vZAlq5pt7a9sCgYEAkijL0sp7mxTtbNwFpaApLCjiWMRS/gOP0rqV5qaBWfv5reElyQso7XNIitRGcXidfpVXWc/QeCcDPSdoRTHBiO2XP2Qk9uvnCsZ+KgEF6NoSp2kk8TCqV3k0G/WDWRcjQ3iOxgEZWBmKV5L7M1LiR/1OkCH+Eu+SIgHCpk8D9XECgYApaLQ/tbklvQQnSfsKYYPQEy7ew3eX3wqAz52/DSQ8m3FVUgDcVX2/YloeF8gyPIdGziTh2qo6+NRAwKaT+AK1ADh4IYuKbNQIkKeaMJSmV8iGUhXG/onmPjc+EuOx4SHsCGoteWOrILeyhpHT5Ve1VBIxwfp5L6/JCP7fvLdf2QKBgH/6gERT4WcEU6rIMbLbiLpxjnbsplpM1ujxaxDSHR9NarXqKYNDZDDm1d0CfrKjJuZiSpQPkPDicjfyFueqotp0bLFEQu+YTLpJjP8UPnTNU2ZKg9p08Rf8f6fr6RheKky93uat6s11q5Z7+PKIQWhkms0uPI4ouCqfLR5lj+VU'),
        ('649c29b3-a7df-436e-9c08-3b723531c021',	'bd3a45cc-aa3a-4061-bfa3-f006590e2eda',	'keyUse',	'ENC'),
        ('12bccbc2-a0c7-433f-bd0d-e824e4a547d2',	'bd3a45cc-aa3a-4061-bfa3-f006590e2eda',	'certificate',	'MIICmzCCAYMCBgGW49YkiTANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMjUwNTE4MTQzODA0WhcNMzUwNTE4MTQzOTQ0WjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC3rbo/YCATIzSA5N9jeaeJOhJWENXSnpjRP91haUhpQ1XrzgCTYJShKSJyYckLrvQo/2aCRDYs/suXQ3IRKlQlDSndqE0JFDTIoJaU3y81jH2eLuLjOChrgLAzHoodwI20pbNtyGYy+ui95iR+5PpsTZ5fL6c35nGHh4PNCCrigu5v5RBKLTuG57nwpq8cY7VTOvWxudYMB/wAsYvgHIXCTka4r7e5d/rNmaeynnKFMXlzCK4WQ0g4VTjS2XzekC/6aFdGimfX9xC/TVIJEtqhhKrblluYQHiWMXZbost6MEuVMRPXSHE0BwB2qHLXVKDSoPGKZsVIAY7yjdQdFkqpAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAF5/LCGEDYaFgjr2sGYAdLiFhSF7dYYHBKkp6TCvNv4Use2WlkTStw9IygW58+W28e1H9NQRj1hsWmON/s480WK6w76ew4+yFZrtIVpdIHG80Y7Nhxg1OvWsq8mk4Wz0In64v1piee4gKUfTZ7RiWVVWkqzJmu709KUQQ751cxNzNTnV2dG6stEsUioYs6VUDLWKt9cW9W1baF98AJ0Wu0qaAxuxbw33FdZZLPLgyjqm4ue+m0IwUPhSCC5hiJK9h5Srj7hKFo4pKMFk3/YQ+XoSVRh7eEvN5h7Do87e3jhdzTjnxsNXSQBgaxMcY/emlj8pfEBCpCjMrtzn96llxlA='),
        ('16d65de3-c55c-4d03-af28-0b215fbf25da',	'bd3a45cc-aa3a-4061-bfa3-f006590e2eda',	'algorithm',	'RSA-OAEP'),
        ('fb7452a5-04e0-4590-93ca-dc0506339b85',	'bd3a45cc-aa3a-4061-bfa3-f006590e2eda',	'priority',	'100'),
        ('02099840-ed0c-44d2-8569-182d7cfdb629',	'2d37501e-42dd-4b21-a75d-50c791731d48',	'kid',	'8185a792-7576-494c-b40d-11e937ae3372'),
        ('b5c6dc1d-7cf9-4571-aae5-415544badc49',	'2d37501e-42dd-4b21-a75d-50c791731d48',	'secret',	'Gyowl6g8cd8AUTyXGe8BXRh_tt5-3rTqBwUc_bX9sskSN2V8RPha2Yor3LxDc--xATZa_kQ_GepQ1LskCxrkUGczp8h4Qn-DpwOrpEEWel0-FhTpbSIPk2rHpEBKr96mpsUchToAIgUYqVfPXX1Zx647midV7L8eO_tOVD-NdQE'),
        ('95b86ad3-f13a-421d-a2e0-b00d52e208a2',	'2d37501e-42dd-4b21-a75d-50c791731d48',	'priority',	'100'),
        ('db08bc7e-3afd-43ff-a721-2da942a17b8e',	'2d37501e-42dd-4b21-a75d-50c791731d48',	'algorithm',	'HS512'),
        ('91fbf9a2-1966-4ae7-8625-1ee182218b2a',	'46291381-6c1e-4692-babd-8f84d449ceb5',	'privateKey',	'MIIEpAIBAAKCAQEA5r9A6ZOG7CaOxvCAqShwnMElRFR8foFfDrv413ZxIt6gb9b4Qb/mpy8mQhn/6KZ1wTB0HDDZOqRluacwNEvwj8UyGGxjmyuaL95k6CahYtcTfFjGilCGOjg1C8VAyAKizIBHGgcDOuuXyBD4fWZTGVaChlyFuF+2oA2/QAFC2GBQn7O78EHn+sg7Msj+yHVEz9yYHb4jfDBiytZagAXlABa7QKdzh1UkcNpl0IsRhkOmAt/xKRCIFcZOXOHp7vMXgre7YzgHtV7G24XgCcWx/mGvBRV6T7BVxwXF5DSPgBAj9gPxE6SxJ/ROdLHQVu8jI4DQ5WSEYGfyYuBg53DxowIDAQABAoIBAEza8d96nEtmslKPw4LULeIAMQ33x/mI1KT/NexqDlfLaAuoQcsAhG419Zu5tjOC0iDVJy9Gk+wE2r8B8TvR+V+hkkRxQaHq3XHJNLngOAvIUDywAv0JwhN9Kinv+tpDin7r0QLZyBklZEomYOW/obkER/hBs+ZLtvPLcGJ8qQuRMvXniYIxrRyw60b7JwRoFrD0s6s4h7x1VhXG6Yg0nifwfRX3yQblmed/c/hnrT+uFyPWBUBlUEW9C5nvLjZ1Q+MkcOikbWfFh3rAV770mbI7+Wo6c9/+rzEj4Rz9QPXHD6zvlmN/ZK48e/BJpXwt9rz7Cjh++xM5b0Yg8f+f2DkCgYEA/BYS/OpSXmoeKk8ww2EECdjjf2ZKWHpGEsTbfgSM2uu6BdUhaiUnliPajEoRnYeD6Y5ncbQnvo72lZWPiOY6HsD4Yzc2kYcdH/C1uWYtSoxvkPSgV6l59IhwhpCSnnziIXIK12RFnAI54Nq3wmPzj39q5vV7kqVtVQfRHyEZB1sCgYEA6lRdvxFANw/JjLHRH6HJbBGGp8RnQCmPanZbCYcBXWMfz9TdNBgOjMfnBbkegJep4i8iVrKew+uMpAbKDbNeM1CTiOBdCBnx12uKE9L9NuBcYxR20qwX3Kn0tuYdPHx5X4XULW2CvqXGsX8N/RAsfQlLC1Bi3xGusGIffhiImVkCgYBByEjgMw2Xkb2ZcNzav2BcaXrEbh8Mg6vxjelxMyLGpij/CKUVm+h+p5CKd3GtWAW2VvDrnQWaFpiGZGb2ZI3aJKUszuWwiD+zbA8f+GftQkSC4TNObO1kQI6MrwFz1kb0T1xA5Ou9UF9mK+00Dv0p/ygYaQr8M6saO1X495webQKBgQDid4rqn9d5344gpbHAanHqOb3z4gMPpZrvf1dYi9urz6LtD4KBWEanlIMXquNp0CnlaTw8ogCCxOF1nQIl35ZI7bTIcl42+Cwcz9fwdNc38/oV368+yAd/wdm58geLb5k10ndPOClF651t6acxTjJQ3WxjkNsdT0+PNCXPjhJmcQKBgQDbzF5nJz8oEijb68NER1WK2hGKUgO5dDTXUqRsjHvpg5RC6bMxdScbf5Z1q0ZX1kzrnAOjkyHExWF2WW5iFuh9aWvNpzDuntRJPElBwu1NxMmAgrGmQNsRKkJ8g0tUMNCN8I315jLVhMGF9akQ9+ktOua++8iqyJK+9Oohgq0QVw=='),
        ('d63b06fd-05ee-4001-b039-f2020b43aa47',	'46291381-6c1e-4692-babd-8f84d449ceb5',	'certificate',	'MIICqzCCAZMCBgGW49ak+zANBgkqhkiG9w0BAQsFADAZMRcwFQYDVQQDDA50ZW5hbnQtbWFuYWdlcjAeFw0yNTA1MTgxNDM4MzdaFw0zNTA1MTgxNDQwMTdaMBkxFzAVBgNVBAMMDnRlbmFudC1tYW5hZ2VyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5r9A6ZOG7CaOxvCAqShwnMElRFR8foFfDrv413ZxIt6gb9b4Qb/mpy8mQhn/6KZ1wTB0HDDZOqRluacwNEvwj8UyGGxjmyuaL95k6CahYtcTfFjGilCGOjg1C8VAyAKizIBHGgcDOuuXyBD4fWZTGVaChlyFuF+2oA2/QAFC2GBQn7O78EHn+sg7Msj+yHVEz9yYHb4jfDBiytZagAXlABa7QKdzh1UkcNpl0IsRhkOmAt/xKRCIFcZOXOHp7vMXgre7YzgHtV7G24XgCcWx/mGvBRV6T7BVxwXF5DSPgBAj9gPxE6SxJ/ROdLHQVu8jI4DQ5WSEYGfyYuBg53DxowIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQB9jCwSyM7w31KZnFvah1OV/HRKluSrQoppfDOqhT8vpJjFPOIDoxIrPc+9gJUlqkHwE/VNeQ3vB1s6ML3OGQoi+jVHIhS0o6kvfmAZJRTwZqIDKzxJT/mdCfMY0T/iUCpW2TGlstNO3YJxyzl0tBqEgRNUIBZU0w8ikTM2WlGh6grXjmmFch1uKWxpKSxAhmqFVPJ96ikwDJRCHqt2dgOSC7lbg9TFO4c0zelaQZbCHBnJmgX0Luuq8zng8ys+9viHzelpKkMd4GmlLQSuyCfucRYe+q6YiSu8LeVSh+5VgfLMzqQGrrWsXUOHwaWSlEhpvtYfAY9uHD+PjGrO4qvp'),
        ('6410fb8b-8a16-425a-9c68-8fd80c0d0b49',	'46291381-6c1e-4692-babd-8f84d449ceb5',	'algorithm',	'RSA-OAEP'),
        ('41d637e7-1a7d-43a7-b4c3-d4603fc853ec',	'46291381-6c1e-4692-babd-8f84d449ceb5',	'keyUse',	'ENC'),
        ('4d5d65fb-b99a-402a-951a-ac25e9c6ef31',	'46291381-6c1e-4692-babd-8f84d449ceb5',	'priority',	'100'),
        ('2720168a-7518-4480-bec4-ed49bc8f5f87',	'f057ba75-2e5b-49e6-99e5-ffcf55a82977',	'secret',	'poWMNsdUQDv5CV3Vetb19A'),
        ('5a4f29e4-8ed9-4052-b004-f15f6933803e',	'9f9d2182-48d6-4d93-8bf6-189b459f8a15',	'privateKey',	'MIIEpAIBAAKCAQEAoMH2PBTA3bX9hm1Y7iUDoyM7a/eZ/bQFeBQ/JJBvNFnNN/R1oXXV1jQ7mPm/TnbbyvXonojUYvzjMS5aVoukuMcLK3eFLYlsTEcyBzbdYz56JRKI5UPl3bsxrQq69hGKMwLJLAjDvKyNSA++ycmaGZFMNdM1O6VodoQOc2ODU5+QWwNDD8erna+sBKVT+lfD1TPHw324gdYFgQ7tN/UkRP4mUnyzTnCK+S0EcmEKy0HrAM2R4yegGyadbDT/fp4yxttSbvPPV2YMA49CLIMqEPECSSAYtgJVJHU7iZajc3SFVFkxDY6CSDUbMyN7XjczbxwXUjRa6SK3sRJD23LStwIDAQABAoIBAA+Wn1z2Gjmlkx787XmyRycDnJh8mwcZ9MnPFMxj4RSvfJHnd5yfg4vQ5tKPrThbmDaXH+Z3mDi9Ev7K4uGvitqMGo4tedGPmNvQr445iMA957Q+lt28dsbIhymjd7uyubb2lzFY7DoYdV6+gB6rGPFPwJcbkvbktDuB1LDlvm9WS1j7Jgpjdf4LBo4e9Y0xuS/1asC7Tc8mlNBQX4272WAjX5RhgUszSw2M7HmjXaRpo6BQL0QbqOvMw4+nU44RNKXkLlzx35tp6qIaqdh+TGfvqkvTwCpgOWF6m8I8js+Tpuj5kc0x7jGNRYcs1ChYiDIBX/WKuV/sXKvg83FNnzECgYEA0ZPBBKqeUhx2uMjFwSjO6hwmE9LWyhlI7afYZ+kO5RC5a1KeSmTxDPN7Hd0ouiqu/2ZWkFMj5UGwwaoa8u6WeZ325mMNZgwy/xACFhsc/PlQus1gipFzTTAQLhl9XUfxpLEzjQkNYx+qSOKQV4mgZpD58URuPSBQi+6l+whC928CgYEAxF3bmeWlFZtyNXQg2HZILjWqYEQJParhxRxl299hEOtTXVnRXwOokiuP5o9s9C+mDaUG1Y+8TngRdULFXYQdsGoT0z9AbA/NAuE167BkKoE0og+BW7P93C3CLYJ0g4QTUEb1rB+/JGqB6m0pV7+dLvndloco+YsyvwZ6AhPMdTkCgYBKJf+NehwnqwBqIkatL5X9iFYEf30U5dtCkjbjlLZ7Xu5rRUDRy2LplR+DEmM9cqqVnAyWZYP2scYqztdYCgfdIYFgQ71S1JAaY/yI+7DG5CKm5ND6Vbm4nn+q/8O2BCU/gp5dkM1Wu7ZQsTAmeH/yqep0bSSfpdY5nAbHiUXAKwKBgQCjm7/4fcoKMrX5q4QiVorVVYlbI9S/OL1bScnkZD2vpyKSSN+VVqsA/qKq6x1QZtLn03PaRCQVDR0eoj9nJ0jFT9ysyaqXXqLln1cZxKHweC6uSFTzXwhIhyeeW29QlJ6y6CyQ+PJ9GPCdrc4gelowZacmX/7XzA6/8ll9mZlkoQKBgQCa2IaSgKSOl3y7FNEFKvfh69chC3y7UkYZoDE9wTJRAezv5K/2zoMqGTHG0ffgx90qiDLC+LvSDOiG98oBCKQdjg/Y2JSTJeYIy7OIK5mF85bb1QXsfmYcsivFZrWrCQyD6TJwD7lLN1U+9MT3yV6f8ijRJIOADlUeqjK3H5U7Sw=='),
        ('192efb7b-e3b9-4452-91a6-8c635005d10e',	'9f9d2182-48d6-4d93-8bf6-189b459f8a15',	'keyUse',	'SIG'),
        ('4829c00d-6a7d-4283-9695-28bc2406c386',	'9f9d2182-48d6-4d93-8bf6-189b459f8a15',	'certificate',	'MIICqzCCAZMCBgGW49akQjANBgkqhkiG9w0BAQsFADAZMRcwFQYDVQQDDA50ZW5hbnQtbWFuYWdlcjAeFw0yNTA1MTgxNDM4MzZaFw0zNTA1MTgxNDQwMTZaMBkxFzAVBgNVBAMMDnRlbmFudC1tYW5hZ2VyMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoMH2PBTA3bX9hm1Y7iUDoyM7a/eZ/bQFeBQ/JJBvNFnNN/R1oXXV1jQ7mPm/TnbbyvXonojUYvzjMS5aVoukuMcLK3eFLYlsTEcyBzbdYz56JRKI5UPl3bsxrQq69hGKMwLJLAjDvKyNSA++ycmaGZFMNdM1O6VodoQOc2ODU5+QWwNDD8erna+sBKVT+lfD1TPHw324gdYFgQ7tN/UkRP4mUnyzTnCK+S0EcmEKy0HrAM2R4yegGyadbDT/fp4yxttSbvPPV2YMA49CLIMqEPECSSAYtgJVJHU7iZajc3SFVFkxDY6CSDUbMyN7XjczbxwXUjRa6SK3sRJD23LStwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBgJ6agkE7AU1R7HdwMdSbfKqu6NRVij0YLpfki6Hvth0LxnIktk3jSPuqw3GGq1kKFNeVHWhcrnNJHNvuc2ifXOaMK3oYiN0pfGyuPDbf5v1cWU076uvMVYOkTkiwxZXICt7C/f3qu/d/hxpjJdCibBDPAQpWn8IY4UeVNbXehm2+YlhoPzBgmZum2soBBJH3qfP7IY1Xy8KzFcYPUhpAy2CIUvdlMj5BYgGHzHJP2/hhv7SRFstmKwjt0fITgA0Ig7o1BMXpLhtTEndVNDIVJwhH7cdWbza1XJgSgMYmOgigzcT+q1whP6DCGMnNqa0an0TJ6hNU9O4FFsOsE0v0A'),
        ('7e4f8014-fc0a-4870-a43d-caaeb5301862',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('0d6997d3-5116-4145-8cd5-a080ed3b1497',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('5906d408-e9a3-4108-9dff-8166243e40a3',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('02c92d67-42e5-41b8-a29d-acc1d812e094',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('5f99d513-43e1-4246-b59d-9c0c50f1164d',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('255cca21-55b6-4150-9c85-98ad43c212d3',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('cd01ffe3-3bad-4c9f-8716-96957b81cdd0',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('4ea5a9c2-287f-4156-aeb3-dd90e5230590',	'af27259e-dce9-4ccc-85ff-d352b870ce16',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('61b86d8f-3090-4214-a9ca-b7e990182768',	'790676e0-4631-4c51-8a58-3d1ecccfebe7',	'allow-default-scopes',	'true'),
        ('b0871797-0f88-43ec-a80b-042a38ffcd1b',	'bce0a59e-e434-49ab-b224-8af720b29b39',	'host-sending-registration-request-must-match',	'true'),
        ('eb2f97d5-e8fa-4139-98d6-d484abab1300',	'bce0a59e-e434-49ab-b224-8af720b29b39',	'client-uris-must-match',	'true'),
        ('b0b02fa2-0387-4974-b7bb-e21111475b39',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('023a5c28-0729-4e89-be75-f648b52db292',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('5523a973-6ed3-4d45-83f0-35a669213fae',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('aba6b08c-c19d-40a1-8691-2da4e2a0879a',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('845593a2-8a12-48f6-876d-95c56b784b8a',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('59056c48-e451-464e-b1b8-2693924e9314',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('774ecec3-d5ff-48dc-a599-b76cbefe4c8b',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('6dee3b17-09c1-448f-a46b-84b906b7f19a',	'9b252e48-aa01-4fc7-b105-995df3e41391',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('c21b17b3-af65-4a6d-b83b-722479a3a820',	'c1004821-f903-4e4f-8bf9-5f29e50174fa',	'allow-default-scopes',	'true'),
        ('92a6d422-f87f-4992-844c-0c0bc3dc52df',	'8d7a1ab5-e802-47db-94c2-e3c1183a2a23',	'max-clients',	'200'),
        ('2865d04d-9cb0-4716-b25a-1b2039c70d7d',	'53fe87fd-7ed5-43fb-bdf0-2fd64d2863a1',	'secret',	'D3nDGg89TEoyh3zMNn-_Dg'),
        ('7dc9fbae-f549-4035-be03-5d3398a71876',	'53fe87fd-7ed5-43fb-bdf0-2fd64d2863a1',	'kid',	'667ae9cc-53fe-4008-a8e0-f664098c8bba'),
        ('ce68128c-3bd3-46f7-a1c9-2eeb635c67b9',	'53fe87fd-7ed5-43fb-bdf0-2fd64d2863a1',	'priority',	'100'),
        ('a67e5975-619d-407b-8112-a9804d2513e2',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('8f36ce55-8278-4793-b6bf-61f2f4e3ae79',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('cdeae8e3-59a2-4fab-bde5-8df679279f87',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('0b1ba42d-e4eb-41bf-a5f5-7a9cc1874aa0',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper'),
        ('d22a3ca0-3e07-49c7-af34-0092aed3768e',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'oidc-usermodel-attribute-mapper'),
        ('0032bc04-4bc5-40f3-ae6c-56ca4b050e85',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('af2defe1-bd25-4627-9651-e0f80249f92d',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('e74edfa0-2eb6-4013-80fb-77ddb79458a3',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('ff818384-2567-47f6-aa50-42b281acc99b',	'3dd98894-e6bc-4218-8cb9-db91218ebe38',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('734d6b5b-e13b-4ce6-be61-00bc959e0a5c',	'9167bf6d-f224-46c9-99eb-0ab7f226d4f7',	'host-sending-registration-request-must-match',	'true'),
        ('000da610-86b5-4c55-b56e-ff91c0709ab5',	'89d14d92-0748-4252-b983-f4462cf4c66e',	'privateKey',	'MIIEpQIBAAKCAQEAstHuDErX0YS09VJzOQX8yRmbBoUDbLrJtofH6q6kUBjf0bvg1GDKJFTab/Lgq3w2YvrgfCElJw0S307oQ1QmcrqTKbsGnLcEvRLBKyAC27VUFh2CqjwSSSfscDFsXEwP5O2pSXaAcHaeQ8RNgLjpVVuOiDcAlNPYZe6ROUPly3nG0XXLIgsTVDKP/L+TZ0fhkzW6YSVwbWd6fgZ3KfV2ahMlP9hAyNCJsdE7E3XlIfZtj+uSTk+uS1ebyanNON5/1TJceK3YAf8okVQ1jRMjWEH4k1oGIUzboVCWU7oJ7PZsCmlEnE/154kgrBS9Yk474EczvHhoJo9r+7UERHUxcQIDAQABAoIBAAOyIAvj/hrn9b9+QUBj9coIO/KCDESShbm+i6y3iFDZesLAcdCh4Xunv04CBI+2xP79tWcLXlO0yvePj3BYzGSho4oAwg26IKBxw0lLY5O75y1/1aQEEtKtPw9Ajj9IGSn/mXiZuOTaJsI6y+lFqRjpCASbsmjhCf9tU1YOJ7ExXat51H3J4M+h3LS15TWMNMpiUBPEVBKtLmo9KLnK7emBQAJ4pwrycjOBi8vs6yLK/3kIi1LszUscAtiJigrHOtCHf1WfHooYg9fKy/0ov9Je7PnEyU5S7TPWK3PRjQMxKoiLSoGNA4DHV5Rz/Pi30nzDVu4tXxtxOZaMwZAFNVkCgYEA+2WaT/WuJ9lePpkjTDm8XYGCQYzvqufn3Q44c5XeT50n0R6D9wyRNwl3/Z/G4ri0DrJlkUL9WbJAom+7joifahzzBwzDKCyvVIrVx81XjaXVhjf79IVfVLeLXRhvT77foqMhEFu6eN0Kn+GqQHxKfMMwJXObOG0LlRYVM2kIKEUCgYEAthghas1qUepmolyZLLqOAOLndEcjwJtHp1/R/VnBNmfGF8wnfhHFlD/hKwehM2qmxg2e4m6d+8jxj4CaUGT5Iy8p6HDW5+ztLpe2so2aUw1JIf7NeE8+mlSUUAIecFRsE9ReNozXcGrRmFwfL48E76ug5Pwq5/Cj3n/gnxkVRT0CgYEA9+b/Pm40aJNO/kegqSAeAc8eFWnXRR8eY7wMKt7UtIlypCKFW28HG9C4hF+0jgo5dGyy/4Whs3u1hPjNaQaMvOUDOiZ5o/o8gw/NNBPnjITFhG9cg4mpsi60vP7FdUvx170pc6ogveKrAXJUovZttY38wlG39PKDA2cdTO773kkCgYEAtGY9nRC6l0LwwUOZoL2Fd41UHldoLSXpHAGLCUMFbr/qeaMRMQ2sP431pmqdolGzvZy63rsWGt7dqfyPZLxUMi5Eh3pa/lQ0OieloUk2LbeIz3we1WU0sTK/6G/g9go9NHv9hm+L9FxvH1GMt9Ip4sOW16SXYUd/ISxxPU1dTEUCgYEA0fK/scYbOK/LNjFj6RwmmnUcKFvPI4JsQ+G+gLHJXbToGWt3uT7tvWTyleo7DxhuVZZe0QOPh78CPhn3IMi76F6RruwBPURR014QGoei7uJRvcdk2ExaLL7/VmevNxxNU6VwKoytxi/BxessFIGE1iy4sGX2/YDj/k8ohwboabg='),
        ('a925fa1c-c4e4-4373-917a-d753b6d7fdea',	'89d14d92-0748-4252-b983-f4462cf4c66e',	'algorithm',	'RSA-OAEP'),
        ('e901db37-e087-45d4-8112-18f1c6aeef33',	'89d14d92-0748-4252-b983-f4462cf4c66e',	'keyUse',	'ENC'),
        ('2a555107-5d68-4489-80b3-37742278acc3',	'89d14d92-0748-4252-b983-f4462cf4c66e',	'certificate',	'MIICmzCCAYMCBgGW49fU9DANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZvcGVuazkwHhcNMjUwNTE4MTQzOTU0WhcNMzUwNTE4MTQ0MTM0WjARMQ8wDQYDVQQDDAZvcGVuazkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCy0e4MStfRhLT1UnM5BfzJGZsGhQNsusm2h8fqrqRQGN/Ru+DUYMokVNpv8uCrfDZi+uB8ISUnDRLfTuhDVCZyupMpuwactwS9EsErIALbtVQWHYKqPBJJJ+xwMWxcTA/k7alJdoBwdp5DxE2AuOlVW46INwCU09hl7pE5Q+XLecbRdcsiCxNUMo/8v5NnR+GTNbphJXBtZ3p+Bncp9XZqEyU/2EDI0Imx0TsTdeUh9m2P65JOT65LV5vJqc043n/VMlx4rdgB/yiRVDWNEyNYQfiTWgYhTNuhUJZTugns9mwKaUScT/XniSCsFL1iTjvgRzO8eGgmj2v7tQREdTFxAgMBAAEwDQYJKoZIhvcNAQELBQADggEBABzaylV71Y2jB3+G5yi7iLF9cImnjYQU+vCEoQ5Sdz4aPBohIoQlqHjOBmmOVNxgIeQnL3ZdpPjbv42+hfat0+48Ay8FezgkwmyOrgpzfAlEjVD7CqhatnMoXRVx5k/sBs6Z3bNkR45KrP5rX9+uLDooEG0wWdZKvnXQMa2NTa6n/jDMZH3XHlr6GWFqREpnhh4TVKoEZPsdU8MmrH9gk92ol7bOVI6pV9JgNpDjw27IpYeY3gegHmJLgqnDZUMDC+Cimnh3Xl6fYyVWMujDfUFbtaMv8GUvEps0g2EAAz/b1n05cd1JRsbHywflB+bAuHqxBZH7qwdRYXNHoIr5CG4='),
        ('379e551f-f1fc-461c-8f1f-e3b7ca873b78',	'89d14d92-0748-4252-b983-f4462cf4c66e',	'priority',	'100'),
        ('f1a53a4f-6a61-4000-a193-253713198343',	'ebb3953c-3a57-418d-8c5d-30421c42a201',	'certificate',	'MIICmzCCAYMCBgGW49fUwTANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZvcGVuazkwHhcNMjUwNTE4MTQzOTU0WhcNMzUwNTE4MTQ0MTM0WjARMQ8wDQYDVQQDDAZvcGVuazkwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDBXGOsf2GNo2FQA0vSWvmpzizsKpEs2Kn0ulA0YtDtg5MOauXw90RN0cO2jTu+dEiII8yB+60qC5pbJPA7Ya/HpX2+ytgra7TadL4DAs3lL6bDBrnRSpsqttDmWGSIrrOnZ7yJvSTb4u+fAbbqR5g2QLhliRkqUNRUXe3DiEpbvMo8JZcstM5XFWhZueBYGIoXvewlcuhpcLFOJsrXyxxSvKxlwMixwxjg7Cj7oJ9pD/fKNtB8N0FIbAfkx8jCpayd5ZqEUmFc4hnEULmt3AQjopZHu6m4YpwGPwsEn7pPY8kIcc3pd9sNGdj7WP5IJ4qWCTuDDP4OPvOipMlYs5FhAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAKAGuG4V8W4nG2+/cxnFPI21TeVOHrbWomXvf6BkkY5LI47tHjeWhkhDr2VJgp9O+zOEPTj9VyjoLEaFmDZYBx6DQR8EZBVK07IrjzOW5s4rDx+WSLrc/pymyoQ+vbbavTCBiTIWk8LviW1DFPlX3clRz414Z9j4z1+hj3KK3UCxtl0Iw/gnLEmze8xObdrVFt5yKykwG9sNzmhtyTONZWv8KQc0LzT+2urjHhDsQaLQV/uRlg8BX19EnG+dzktrxUizPHpj6+/RnRnH2NzO38DYlFSQ1Dej/MGjW5H1vCrcucOWvkpheYQjYqxABD/2QyER2MfaNGV8j53gZudYkQk='),
        ('8a11e0e7-fddf-41c6-8aa7-7901d01cdbd4',	'ebb3953c-3a57-418d-8c5d-30421c42a201',	'keyUse',	'SIG'),
        ('7dd5eb5a-dea0-45ed-abd4-f670dcacd6bf',	'ebb3953c-3a57-418d-8c5d-30421c42a201',	'privateKey',	'MIIEowIBAAKCAQEAwVxjrH9hjaNhUANL0lr5qc4s7CqRLNip9LpQNGLQ7YOTDmrl8PdETdHDto07vnRIiCPMgfutKguaWyTwO2Gvx6V9vsrYK2u02nS+AwLN5S+mwwa50UqbKrbQ5lhkiK6zp2e8ib0k2+LvnwG26keYNkC4ZYkZKlDUVF3tw4hKW7zKPCWXLLTOVxVoWbngWBiKF73sJXLoaXCxTibK18scUrysZcDIscMY4Owo+6CfaQ/3yjbQfDdBSGwH5MfIwqWsneWahFJhXOIZxFC5rdwEI6KWR7upuGKcBj8LBJ+6T2PJCHHN6XfbDRnY+1j+SCeKlgk7gwz+Dj7zoqTJWLORYQIDAQABAoIBAAGxGKeI76gqABEvjrJ98JfV56BnOtEBiirjpY2QSOV/mn4AhR+Uo8OyKm4critrZDox9T1Ux8wNqP+Ltyj/WMbIf70vZFjcqNrllSckx1s2RY1Yt+/tARacje9KgiJspCJyjMCYI7gzbBMcLFLwXnxoyH49rt9XEMUGU30DwnoLDODjGhoznTyKwzxMNhDw1wRIu65eaKQUKdqPtAndc1rC10jCKkJkL34ZQv+3UpjPWakMCVdOulGTh8jckEBDFJvzygVGSz0QBjkcl8m861qPjbMV/SNlKrc8XIEUZKz6tEqLHQ0AZOvZv+kZ2uERMMEfXQ1CwvPbbQ7rzzGlPUECgYEA9UGcHcdBKP6SDpmmevzCNkphAy85Vrjiu/btCOGoitwb6VPp+fE0K5t1F4fN5R5adQVkB5G+a4O7W8fdJFx4R09W9v5OuERVIwTy818mn++DIrzdTAG/W0XH9h+F6AifBCGTuPZbl6xUIUs78fktupYoYVQq30H8UHPRm9QP0SECgYEAydTObaOUZ3vd+4kLzBXNTHM4kT5mGUAGCwFJFn4Hkj9yBHkI31RcBunM6U8uklSqjtrkDRyclCvODAX98WSQe4RtpB90UJSeFKQhiLdkshO2axE8uBavQfecGZhXqr0FaYh4jhU4D8JNXXWJiBEqo7PfYRi2sOcvfdi7b7moeEECgYAUgPqzRqA89CNz0wanC5CVB1za7kzF4kkaW8dvUaZMjaBLvnc2LSMmbiW45OKiwAnh9KRrOZUI2geE4c2sJ4rqs0ha9+HO5++SMOy6yf6pqlV1RwLdQtwjnk7cVcBVKLtalhAKi5YWtRKHggmt7h4TllAj6ux2o8abV0Nz2qPD4QKBgBH0UmDCeoMyK68io+AF2HuCjHevO1nnriHwfVX45CeIaVYohjeHw6AB0G640oXF7l5C7qKT9wHfW7cKGWsN7SRvxWkiOzCn5+AiMbF9zwFga4vevq4JppGRu1sziCjyI7cdosvQ62nLeSJlQFTQYiCp8acrkaTvC/SAATyRIYGBAoGBAOesgujYf9EG2gyXmNPANJPvBSktZiuCS3W909gq1m2z9BC+H3JozMKylJOD4oi9bHyoTzVxXKc4OWn6xwRzC7SRJDKwt+1I56OsMh8bUxQpl0McZCsG1MfeiInnNqiaPUWdrCLYzn7H0n0eYVlpnxcRQoLsqz8Jsz6HRr6HLSCi'),
        ('089086ac-599b-4266-89bb-ac5b722e0854',	'ebb3953c-3a57-418d-8c5d-30421c42a201',	'priority',	'100'),
        ('b8a01103-cf77-4933-b92a-0053ae319cfb',	'c8f83698-cb68-4a7c-835e-0f4c948b446b',	'kid',	'c6402bfe-2f97-4990-8442-32dbb0a71fff'),
        ('fda2213f-95d4-4f73-9040-4ac5c6c52334',	'c8f83698-cb68-4a7c-835e-0f4c948b446b',	'algorithm',	'HS512'),
        ('ee959718-1042-40d6-b1d6-11a842264812',	'c8f83698-cb68-4a7c-835e-0f4c948b446b',	'priority',	'100'),
        ('6865cabc-afb0-4c79-9b7e-10668533f28d',	'c8f83698-cb68-4a7c-835e-0f4c948b446b',	'secret',	'ma0TX7OHoZp6slNJWY5Y9CqczaScQzHjXAtgMvNaBy1zBk3rLsIq7SftoyUhDvEpS9hVZlP_OLm9t_s7KRC3cl_nKUROXj8fBCByWJqYBqeX8VyW500adRz5t81v4mGkFAWKooPQg8JlGizVzUWjrIlWbI6m4Y_QdgRFSHrWP64'),
        ('929eb8c0-63ee-460f-9122-468b4b45536a',	'f7af03c5-8fea-436f-9e9c-270f4c9cea78',	'allow-default-scopes',	'true'),
        ('91a73e5c-e366-4b1a-900c-89a0086255a9',	'e49c7732-70c2-44ac-88f6-a7a1751de3a0',	'allow-default-scopes',	'true'),
        ('3abe8533-c0cf-4e0e-afdd-4077caed7a16',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'saml-user-property-mapper'),
        ('d4af6756-94c0-47d4-9e21-b13c309b683a',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'oidc-address-mapper'),
        ('a328a654-f513-4d17-a82e-f006dc994e68',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'saml-role-list-mapper'),
        ('121b8420-ff97-40c5-a375-a5e8eaf1fb48',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'oidc-full-name-mapper'),
        ('a1e9cbef-e1a8-4064-a8b0-cd92c7a7eb32',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'saml-user-attribute-mapper'),
        ('e082789b-4451-4dca-accd-6885aeb52e8c',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'oidc-sha256-pairwise-sub-mapper'),
        ('c4da7f91-83bf-4406-be4d-750ddbd65823',	'2683551d-ff74-4699-ae31-583028ea8555',	'allowed-protocol-mapper-types',	'oidc-usermodel-property-mapper');

        CREATE TABLE "public"."composite_role" (
            "composite" character varying(36) NOT NULL,
            "child_role" character varying(36) NOT NULL,
            CONSTRAINT "constraint_composite_role" PRIMARY KEY ("composite", "child_role")
        ) WITH (oids = false);

        CREATE INDEX idx_composite ON public.composite_role USING btree (composite);

        CREATE INDEX idx_composite_child ON public.composite_role USING btree (child_role);

        INSERT INTO "composite_role" ("composite", "child_role") VALUES
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'd55eee94-3000-40b0-bf22-d9ad29a29ca5'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'163f2f38-ee04-4a12-baf2-f39bfb0ecc35'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'5da7175e-9163-4949-a605-d1903d112ecc'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'96f8af5f-4009-45c1-87f4-06bcf453b339'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'53a6d29b-3648-4477-9a42-61d168813266'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'bb9e6836-245f-4bd1-bb57-e09186cd36f9'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'55fc48dd-82c4-41cd-a4ee-8f67d8cda72e'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'2949e57b-dcf0-476c-bf00-ae3ac8f79cd3'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'9576afd4-b749-4478-acee-646782ce42f3'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'796a9a82-94bb-4833-bab0-666909435ebd'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'3039d5f7-03bc-4fa6-94bb-07210f8a3f58'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'6cc16c41-179e-4e49-a9aa-09814923225b'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'dc3cebed-be2d-45b0-aabd-d98054a6f2fc'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'f530218a-9873-4dc9-a944-31a169616bab'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'7e9d4b4b-ce22-40ca-9032-bfefc81209b3'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'8bb53be6-16a3-47ac-a63b-0d9a3ce36725'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'590de40e-f9e0-414c-ae48-200250188db7'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'0b3174a0-4387-4dee-99b8-41614632f2f8'),
        ('1a685a27-f87d-48c9-882f-0c81f6a7e412',	'e9ecf1a2-f35a-418c-9f51-72c6ff36a854'),
        ('53a6d29b-3648-4477-9a42-61d168813266',	'8bb53be6-16a3-47ac-a63b-0d9a3ce36725'),
        ('96f8af5f-4009-45c1-87f4-06bcf453b339',	'7e9d4b4b-ce22-40ca-9032-bfefc81209b3'),
        ('96f8af5f-4009-45c1-87f4-06bcf453b339',	'0b3174a0-4387-4dee-99b8-41614632f2f8'),
        ('1a685a27-f87d-48c9-882f-0c81f6a7e412',	'9c8aae56-a38e-4c53-a6aa-37220b4fb170'),
        ('9c8aae56-a38e-4c53-a6aa-37220b4fb170',	'5e60edab-8f04-458f-8b30-4a7607e8ba67'),
        ('fd0bd273-e782-4869-a5ea-46b574748132',	'fb02b3d3-f28c-4cea-92e4-f22068f9bbc4'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'a631c878-91f2-4cf5-bc94-e1359ae2b97d'),
        ('1a685a27-f87d-48c9-882f-0c81f6a7e412',	'6586e32c-9de2-4ebf-85be-92b235951148'),
        ('1a685a27-f87d-48c9-882f-0c81f6a7e412',	'1b499e6b-2ec8-4a0b-9929-1ac7e6472743'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'60257692-5b91-46e8-8070-3a6f3f9995f5'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'0951e280-22ec-48ec-b00c-d11737387d4b'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'ff8baf22-dfc3-452e-b147-e23669bf3e00'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'959a88bb-f1af-4703-8158-452573141104'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'7736efaa-98d5-4dba-9e35-05924be102be'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'66879d50-d7e1-4f48-ae98-c5507a2f3bce'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'87093347-1934-46d8-a640-6580a5e88691'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'64fc0715-91d1-431c-9d70-28ac14687f93'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'd233e0b6-0e95-44af-a4f3-53cc491fd5dd'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'63f88d0d-17d5-45c2-98b3-2725ca0cbfd0'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'df2e4981-ceb5-4683-af11-d49c63371dc5'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'766ef62e-e36f-4af3-9fb2-fee4a9021a44'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'7ab556a8-376b-44a5-b6fb-9eb26ed9928e'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'9d89f38f-fcf7-4548-b3f6-baa4db8e333c'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'82d3d127-32bc-44ef-8cab-3467d0b00386'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'b1cc705a-8ed6-4075-b30f-fe315f38e59a'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'67ef4ad7-c106-40a5-9331-fcf1ceda9474'),
        ('959a88bb-f1af-4703-8158-452573141104',	'82d3d127-32bc-44ef-8cab-3467d0b00386'),
        ('ff8baf22-dfc3-452e-b147-e23669bf3e00',	'9d89f38f-fcf7-4548-b3f6-baa4db8e333c'),
        ('ff8baf22-dfc3-452e-b147-e23669bf3e00',	'67ef4ad7-c106-40a5-9331-fcf1ceda9474'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'44755452-e042-4376-8217-083d6f8ad6ef'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'82b88724-7da1-4bf5-a1e0-956faac79c58'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'1a39e8b3-5a27-4f96-b94f-9227f91c65be'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'a1e70ee5-f06c-470b-902f-860bb04967f1'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'4f34fd7b-b879-43f1-908f-e5d542bef0d7'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'f91735d4-9e8a-4e3e-af6e-2dae75c81d61'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'05d27c53-c738-4bc8-b7cf-8b1d57fb957f'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'c7506d5d-1d8b-4db1-aff2-75d90b6254c9'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'37dfce7d-774d-4ca5-a6b6-58f14c08bc17'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'b105e32f-fe77-4f72-a542-da668c9266f3'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'611b97cb-d036-4186-8408-853da3d6a3da'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'7d199ddb-d219-4a08-aaa9-4a42f3a445a7'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'1a3e5603-2049-4c8e-ba00-dd5946838630'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'ff4ab908-027b-4806-a4f7-d78fe32f59f6'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'75de19b6-b43b-45c4-8da7-bde5ee6d3708'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'4122e121-07b8-44a0-af1a-d80048e43cb4'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'f80e2418-9c3a-4a81-8248-0f730d3fb3f8'),
        ('1a39e8b3-5a27-4f96-b94f-9227f91c65be',	'f80e2418-9c3a-4a81-8248-0f730d3fb3f8'),
        ('1a39e8b3-5a27-4f96-b94f-9227f91c65be',	'ff4ab908-027b-4806-a4f7-d78fe32f59f6'),
        ('88c38d21-732e-40b0-942d-7392520f0ef4',	'4abcec7b-c748-4121-8237-8661ae756192'),
        ('a1e70ee5-f06c-470b-902f-860bb04967f1',	'75de19b6-b43b-45c4-8da7-bde5ee6d3708'),
        ('88c38d21-732e-40b0-942d-7392520f0ef4',	'359987c6-da37-4cb7-a1b8-956b217d8ead'),
        ('359987c6-da37-4cb7-a1b8-956b217d8ead',	'68992d4e-4c35-4d7c-9e29-26600cfc86ec'),
        ('7c2f32d1-590f-402e-9f78-cec4102b879e',	'efc9df94-2a33-4109-9f9a-b256f34d5851'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'48be0d61-c06a-4883-b2ab-0944d6d0dd40'),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'c7a75ebe-5049-4ef9-8577-3b54253e79ac'),
        ('88c38d21-732e-40b0-942d-7392520f0ef4',	'675b804d-7f39-41f9-bbd7-805d095b590e'),
        ('88c38d21-732e-40b0-942d-7392520f0ef4',	'4b5d71dd-c6ec-4bb1-bc2a-42308bec1113'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'6a23a8ab-ce09-4894-b5e9-ff2b879cde0c'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'714d8436-5511-4634-a5ca-c5ad01a4ac7f'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'1b7a1969-47ea-4432-bf00-a7ba40def179'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'a1e3fa29-86b1-4077-bfa6-de7cee15b0fa'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'b0cfb90e-4efd-4104-98bd-d51e100863e9'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'f30d7175-7a6b-4763-95b7-b9ef179f5fb3'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'dd0eeceb-e290-4708-bd5c-bb1146a55fb9'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'f294eaef-b0a3-4bb0-b368-4f5f213ffbeb'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'3f2a371a-96dd-4e08-8120-db48dd5ddcda'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'55cd6bee-117b-4d29-a809-3f1771ac63dc'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'39349575-4c48-4dac-8904-e575357f0e33'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'fe5f5bc9-9a6d-4d7d-9371-fee59396ff26'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'1e0fd2c2-817c-406c-b47c-357fcf98ce2b'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'a41bedc5-bc98-4fe5-9d6c-9a4da8ea81cb'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'7debbdcf-c6b0-4ad4-afc1-11657d4e6e82'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'c3de7d36-1b29-4596-acc3-ec1ec696ae87'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'd5d3d265-0cf4-42ee-80d1-54859a661769'),
        ('1b7a1969-47ea-4432-bf00-a7ba40def179',	'd5d3d265-0cf4-42ee-80d1-54859a661769'),
        ('1b7a1969-47ea-4432-bf00-a7ba40def179',	'a41bedc5-bc98-4fe5-9d6c-9a4da8ea81cb'),
        ('a1e3fa29-86b1-4077-bfa6-de7cee15b0fa',	'7debbdcf-c6b0-4ad4-afc1-11657d4e6e82'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'012fbf74-66ed-4125-af5b-4a4d899d908f'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'1a6bb9a6-3492-489f-8850-e8b376621721'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'd00af4d8-2f59-491f-ac89-32df2dca4183'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'1e3267e5-445b-4310-bdfa-dbbb2835d40d'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'f9a154d1-8720-484a-954d-f0fd10f0b8cd'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'47b9d3fe-331d-43bc-a625-4cee28fc2d45'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'3c264d99-5a81-4921-879b-a99f613aa247'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'704fe125-6687-41f7-951c-10865ea96e50'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'745e6d51-cf3e-4bf7-aff2-403437e74455'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'7cc4fa65-e7f5-4926-89a7-8132ebe09b3d'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'c859daeb-04d1-4058-820a-d24046672404'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'694fc92d-d192-4db9-84e4-dbee7b76b165'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'a9bd49ad-72b4-4ab1-8f9c-a309cc564b6b'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'828cf0da-dd49-4202-ba1f-94cf922248a0'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'fc1c387d-de06-407c-a423-9610e7f85432'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'2f2d8a1f-2f36-4bac-b0f8-946e2414542e'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'6107b5f9-afe2-4a6b-9533-3a925b96588f'),
        ('08181cbd-ccaf-4ff4-98ed-daa5aa59e956',	'ebd941fd-d2b9-455f-84c1-8661c6d072fa'),
        ('1e3267e5-445b-4310-bdfa-dbbb2835d40d',	'fc1c387d-de06-407c-a423-9610e7f85432'),
        ('d00af4d8-2f59-491f-ac89-32df2dca4183',	'6107b5f9-afe2-4a6b-9533-3a925b96588f'),
        ('d00af4d8-2f59-491f-ac89-32df2dca4183',	'828cf0da-dd49-4202-ba1f-94cf922248a0'),
        ('08181cbd-ccaf-4ff4-98ed-daa5aa59e956',	'7da37ec1-399e-4ddd-8f7b-99ade12ffaed'),
        ('7da37ec1-399e-4ddd-8f7b-99ade12ffaed',	'5f9cf033-05e4-453c-bb15-9825750ea647'),
        ('c3ea1519-4c28-4a21-b100-08e87c189c08',	'664e2b7e-fb5b-4fc9-a6be-07d7d6f6a397'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'd78413d4-313e-4e6f-9555-5d8cb23dd0e3'),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'7848e509-842b-4381-a750-98f06c6d5ff0'),
        ('08181cbd-ccaf-4ff4-98ed-daa5aa59e956',	'6edf8d44-92b6-4db4-81fc-5deb7a693f50'),
        ('08181cbd-ccaf-4ff4-98ed-daa5aa59e956',	'7f2bec20-9ef5-4ca0-bc5c-c64e42a63a8e');

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
        ) WITH (oids = false);

        CREATE INDEX idx_user_credential ON public.credential USING btree (user_id);

        INSERT INTO "credential" ("id", "salt", "type", "user_id", "created_date", "user_label", "secret_data", "credential_data", "priority") VALUES
        ('9473896c-6516-45d6-ad64-f2d023025095',	NULL,	'password',	'819823be-1c8c-4da1-a247-71524f8c5539',	1747579184406,	NULL,	'{"value":"nsIJbr4QGAgyyZULdH3aGrnJ0m6l0jaeSGCih00qXxc=","salt":"ANZhMDJXbP6E4b6WrbIGXA==","additionalParameters":{}}',	'{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}',	10),
        ('ff9e6646-7b28-45b0-91d9-4c2721b50a8d',	NULL,	'password',	'b27ac118-d0fb-430b-9a82-6bf684d74000',	1747579285449,	'My password',	'{"value":"Gs8VqFx87/chqfxw5c/uZyM31NYjSemw3ismXhPqJQU=","salt":"zCZpkQ3mGkb7I77PG1ZaQg==","additionalParameters":{}}',	'{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}',	10),
        ('d8db4803-6708-417f-aa07-8afd6c11ce0d',	NULL,	'password',	'5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73',	1747579366599,	'My password',	'{"value":"rYkzV81xgWPyqy0SRjhlU7hZK8SZKQh5M/IM8xJc+MI=","salt":"9391qTLpfVNE/6AzUT1yNQ==","additionalParameters":{}}',	'{"hashIterations":5,"algorithm":"argon2","additionalParameters":{"hashLength":["32"],"memory":["7168"],"type":["id"],"version":["1.3"],"parallelism":["1"]}}',	10);

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
        ) WITH (oids = false);

        INSERT INTO "databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES
        ('1.0.0.Final-KEYCLOAK-5461',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.0.0.Final.xml',	'2025-05-18 14:39:38.603512',	1,	'EXECUTED',	'9:6f1016664e21e16d26517a4418f5e3df',	'createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.0.0.Final-KEYCLOAK-5461',	'sthorger@redhat.com',	'META-INF/db2-jpa-changelog-1.0.0.Final.xml',	'2025-05-18 14:39:38.613854',	2,	'MARK_RAN',	'9:828775b1596a07d1200ba1d49e5e3941',	'createTable tableName=APPLICATION_DEFAULT_ROLES; createTable tableName=CLIENT; createTable tableName=CLIENT_SESSION; createTable tableName=CLIENT_SESSION_ROLE; createTable tableName=COMPOSITE_ROLE; createTable tableName=CREDENTIAL; createTable tab...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.1.0.Beta1',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.1.0.Beta1.xml',	'2025-05-18 14:39:38.644463',	3,	'EXECUTED',	'9:5f090e44a7d595883c1fb61f4b41fd38',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=CLIENT_ATTRIBUTES; createTable tableName=CLIENT_SESSION_NOTE; createTable tableName=APP_NODE_REGISTRATIONS; addColumn table...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.1.0.Final',	'sthorger@redhat.com',	'META-INF/jpa-changelog-1.1.0.Final.xml',	'2025-05-18 14:39:38.648449',	4,	'EXECUTED',	'9:c07e577387a3d2c04d1adc9aaad8730e',	'renameColumn newColumnName=EVENT_TIME, oldColumnName=TIME, tableName=EVENT_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.2.0.Beta1',	'psilva@redhat.com',	'META-INF/jpa-changelog-1.2.0.Beta1.xml',	'2025-05-18 14:39:38.718561',	5,	'EXECUTED',	'9:b68ce996c655922dbcd2fe6b6ae72686',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.2.0.Beta1',	'psilva@redhat.com',	'META-INF/db2-jpa-changelog-1.2.0.Beta1.xml',	'2025-05-18 14:39:38.723767',	6,	'MARK_RAN',	'9:543b5c9989f024fe35c6f6c5a97de88e',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION; createTable tableName=PROTOCOL_MAPPER; createTable tableName=PROTOCOL_MAPPER_CONFIG; createTable tableName=...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.2.0.RC1',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.2.0.CR1.xml',	'2025-05-18 14:39:38.788816',	7,	'EXECUTED',	'9:765afebbe21cf5bbca048e632df38336',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.2.0.RC1',	'bburke@redhat.com',	'META-INF/db2-jpa-changelog-1.2.0.CR1.xml',	'2025-05-18 14:39:38.792969',	8,	'MARK_RAN',	'9:db4a145ba11a6fdaefb397f6dbf829a1',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=MIGRATION_MODEL; createTable tableName=IDENTITY_P...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.2.0.Final',	'keycloak',	'META-INF/jpa-changelog-1.2.0.Final.xml',	'2025-05-18 14:39:38.798493',	9,	'EXECUTED',	'9:9d05c7be10cdb873f8bcb41bc3a8ab23',	'update tableName=CLIENT; update tableName=CLIENT; update tableName=CLIENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.3.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.3.0.xml',	'2025-05-18 14:39:38.867051',	10,	'EXECUTED',	'9:18593702353128d53111f9b1ff0b82b8',	'delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete tableName=USER_SESSION; createTable tableName=ADMI...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.4.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.4.0.xml',	'2025-05-18 14:39:38.904598',	11,	'EXECUTED',	'9:6122efe5f090e41a85c0f1c9e52cbb62',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.4.0',	'bburke@redhat.com',	'META-INF/db2-jpa-changelog-1.4.0.xml',	'2025-05-18 14:39:38.908315',	12,	'MARK_RAN',	'9:e1ff28bf7568451453f844c5d54bb0b5',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.5.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.5.0.xml',	'2025-05-18 14:39:38.920407',	13,	'EXECUTED',	'9:7af32cd8957fbc069f796b61217483fd',	'delete tableName=CLIENT_SESSION_AUTH_STATUS; delete tableName=CLIENT_SESSION_ROLE; delete tableName=CLIENT_SESSION_PROT_MAPPER; delete tableName=CLIENT_SESSION_NOTE; delete tableName=CLIENT_SESSION; delete tableName=USER_SESSION_NOTE; delete table...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.6.1_from15',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-18 14:39:38.937369',	14,	'EXECUTED',	'9:6005e15e84714cd83226bf7879f54190',	'addColumn tableName=REALM; addColumn tableName=KEYCLOAK_ROLE; addColumn tableName=CLIENT; createTable tableName=OFFLINE_USER_SESSION; createTable tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_US_SES_PK2, tableName=...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.6.1_from16-pre',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-18 14:39:38.938893',	15,	'MARK_RAN',	'9:bf656f5a2b055d07f314431cae76f06c',	'delete tableName=OFFLINE_CLIENT_SESSION; delete tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.6.1_from16',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-18 14:39:38.940982',	16,	'MARK_RAN',	'9:f8dadc9284440469dcf71e25ca6ab99b',	'dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_US_SES_PK, tableName=OFFLINE_USER_SESSION; dropPrimaryKey constraintName=CONSTRAINT_OFFLINE_CL_SES_PK, tableName=OFFLINE_CLIENT_SESSION; addColumn tableName=OFFLINE_USER_SESSION; update tableName=OF...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.6.1',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.6.1.xml',	'2025-05-18 14:39:38.942857',	17,	'EXECUTED',	'9:d41d8cd98f00b204e9800998ecf8427e',	'empty',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.7.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-1.7.0.xml',	'2025-05-18 14:39:38.989456',	18,	'EXECUTED',	'9:3368ff0be4c2855ee2dd9ca813b38d8e',	'createTable tableName=KEYCLOAK_GROUP; createTable tableName=GROUP_ROLE_MAPPING; createTable tableName=GROUP_ATTRIBUTE; createTable tableName=USER_GROUP_MEMBERSHIP; createTable tableName=REALM_DEFAULT_GROUPS; addColumn tableName=IDENTITY_PROVIDER; ...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.8.0',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.8.0.xml',	'2025-05-18 14:39:39.020428',	19,	'EXECUTED',	'9:8ac2fb5dd030b24c0570a763ed75ed20',	'addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.8.0-2',	'keycloak',	'META-INF/jpa-changelog-1.8.0.xml',	'2025-05-18 14:39:39.024108',	20,	'EXECUTED',	'9:f91ddca9b19743db60e3057679810e6c',	'dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.8.0',	'mposolda@redhat.com',	'META-INF/db2-jpa-changelog-1.8.0.xml',	'2025-05-18 14:39:39.026073',	21,	'MARK_RAN',	'9:831e82914316dc8a57dc09d755f23c51',	'addColumn tableName=IDENTITY_PROVIDER; createTable tableName=CLIENT_TEMPLATE; createTable tableName=CLIENT_TEMPLATE_ATTRIBUTES; createTable tableName=TEMPLATE_SCOPE_MAPPING; dropNotNullConstraint columnName=CLIENT_ID, tableName=PROTOCOL_MAPPER; ad...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.8.0-2',	'keycloak',	'META-INF/db2-jpa-changelog-1.8.0.xml',	'2025-05-18 14:39:39.030447',	22,	'MARK_RAN',	'9:f91ddca9b19743db60e3057679810e6c',	'dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; update tableName=CREDENTIAL',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.9.0',	'mposolda@redhat.com',	'META-INF/jpa-changelog-1.9.0.xml',	'2025-05-18 14:39:39.079162',	23,	'EXECUTED',	'9:bc3d0f9e823a69dc21e23e94c7a94bb1',	'update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=REALM; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=REALM; update tableName=REALM; customChange; dr...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.9.1',	'keycloak',	'META-INF/jpa-changelog-1.9.1.xml',	'2025-05-18 14:39:39.08313',	24,	'EXECUTED',	'9:c9999da42f543575ab790e76439a2679',	'modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=PUBLIC_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.9.1',	'keycloak',	'META-INF/db2-jpa-changelog-1.9.1.xml',	'2025-05-18 14:39:39.084294',	25,	'MARK_RAN',	'9:0d6c65c6f58732d81569e77b10ba301d',	'modifyDataType columnName=PRIVATE_KEY, tableName=REALM; modifyDataType columnName=CERTIFICATE, tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('1.9.2',	'keycloak',	'META-INF/jpa-changelog-1.9.2.xml',	'2025-05-18 14:39:39.379176',	26,	'EXECUTED',	'9:fc576660fc016ae53d2d4778d84d86d0',	'createIndex indexName=IDX_USER_EMAIL, tableName=USER_ENTITY; createIndex indexName=IDX_USER_ROLE_MAPPING, tableName=USER_ROLE_MAPPING; createIndex indexName=IDX_USER_GROUP_MAPPING, tableName=USER_GROUP_MEMBERSHIP; createIndex indexName=IDX_USER_CO...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-2.0.0',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-2.0.0.xml',	'2025-05-18 14:39:39.433976',	27,	'EXECUTED',	'9:43ed6b0da89ff77206289e87eaa9c024',	'createTable tableName=RESOURCE_SERVER; addPrimaryKey constraintName=CONSTRAINT_FARS, tableName=RESOURCE_SERVER; addUniqueConstraint constraintName=UK_AU8TT6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER; createTable tableName=RESOURCE_SERVER_RESOU...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-2.5.1',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-2.5.1.xml',	'2025-05-18 14:39:39.436103',	28,	'EXECUTED',	'9:44bae577f551b3738740281eceb4ea70',	'update tableName=RESOURCE_SERVER_POLICY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.1.0-KEYCLOAK-5461',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.1.0.xml',	'2025-05-18 14:39:39.483168',	29,	'EXECUTED',	'9:bd88e1f833df0420b01e114533aee5e8',	'createTable tableName=BROKER_LINK; createTable tableName=FED_USER_ATTRIBUTE; createTable tableName=FED_USER_CONSENT; createTable tableName=FED_USER_CONSENT_ROLE; createTable tableName=FED_USER_CONSENT_PROT_MAPPER; createTable tableName=FED_USER_CR...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.2.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.2.0.xml',	'2025-05-18 14:39:39.492865',	30,	'EXECUTED',	'9:a7022af5267f019d020edfe316ef4371',	'addColumn tableName=ADMIN_EVENT_ENTITY; createTable tableName=CREDENTIAL_ATTRIBUTE; createTable tableName=FED_CREDENTIAL_ATTRIBUTE; modifyDataType columnName=VALUE, tableName=CREDENTIAL; addForeignKeyConstraint baseTableName=FED_CREDENTIAL_ATTRIBU...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.3.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.3.0.xml',	'2025-05-18 14:39:39.508722',	31,	'EXECUTED',	'9:fc155c394040654d6a79227e56f5e25a',	'createTable tableName=FEDERATED_USER; addPrimaryKey constraintName=CONSTR_FEDERATED_USER, tableName=FEDERATED_USER; dropDefaultValue columnName=TOTP, tableName=USER_ENTITY; dropColumn columnName=TOTP, tableName=USER_ENTITY; addColumn tableName=IDE...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.4.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.4.0.xml',	'2025-05-18 14:39:39.51196',	32,	'EXECUTED',	'9:eac4ffb2a14795e5dc7b426063e54d88',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.5.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-18 14:39:39.515989',	33,	'EXECUTED',	'9:54937c05672568c4c64fc9524c1e9462',	'customChange; modifyDataType columnName=USER_ID, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.5.0-unicode-oracle',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-18 14:39:39.51762',	34,	'MARK_RAN',	'9:3a32bace77c84d7678d035a7f5a8084e',	'modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.5.0-unicode-other-dbs',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-18 14:39:39.535358',	35,	'EXECUTED',	'9:33d72168746f81f98ae3a1e8e0ca3554',	'modifyDataType columnName=DESCRIPTION, tableName=AUTHENTICATION_FLOW; modifyDataType columnName=DESCRIPTION, tableName=CLIENT_TEMPLATE; modifyDataType columnName=DESCRIPTION, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=DESCRIPTION,...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.5.0-duplicate-email-support',	'slawomir@dabek.name',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-18 14:39:39.541026',	36,	'EXECUTED',	'9:61b6d3d7a4c0e0024b0c839da283da0c',	'addColumn tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.5.0-unique-group-names',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-2.5.0.xml',	'2025-05-18 14:39:39.545636',	37,	'EXECUTED',	'9:8dcac7bdf7378e7d823cdfddebf72fda',	'addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('2.5.1',	'bburke@redhat.com',	'META-INF/jpa-changelog-2.5.1.xml',	'2025-05-18 14:39:39.548827',	38,	'EXECUTED',	'9:a2b870802540cb3faa72098db5388af3',	'addColumn tableName=FED_USER_CONSENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.0.0',	'bburke@redhat.com',	'META-INF/jpa-changelog-3.0.0.xml',	'2025-05-18 14:39:39.551967',	39,	'EXECUTED',	'9:132a67499ba24bcc54fb5cbdcfe7e4c0',	'addColumn tableName=IDENTITY_PROVIDER',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.2.0-fix',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-18 14:39:39.553117',	40,	'MARK_RAN',	'9:938f894c032f5430f2b0fafb1a243462',	'addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.2.0-fix-with-keycloak-5416',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-18 14:39:39.554688',	41,	'MARK_RAN',	'9:845c332ff1874dc5d35974b0babf3006',	'dropIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS; addNotNullConstraint columnName=REALM_ID, tableName=CLIENT_INITIAL_ACCESS; createIndex indexName=IDX_CLIENT_INIT_ACC_REALM, tableName=CLIENT_INITIAL_ACCESS',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.2.0-fix-offline-sessions',	'hmlnarik',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-18 14:39:39.557607',	42,	'EXECUTED',	'9:fc86359c079781adc577c5a217e4d04c',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.2.0-fixed',	'keycloak',	'META-INF/jpa-changelog-3.2.0.xml',	'2025-05-18 14:39:40.581915',	43,	'EXECUTED',	'9:59a64800e3c0d09b825f8a3b444fa8f4',	'addColumn tableName=REALM; dropPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_PK2, tableName=OFFLINE_CLIENT_SESSION; dropColumn columnName=CLIENT_SESSION_ID, tableName=OFFLINE_CLIENT_SESSION; addPrimaryKey constraintName=CONSTRAINT_OFFL_CL_SES_P...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.3.0',	'keycloak',	'META-INF/jpa-changelog-3.3.0.xml',	'2025-05-18 14:39:40.584832',	44,	'EXECUTED',	'9:d48d6da5c6ccf667807f633fe489ce88',	'addColumn tableName=USER_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part1',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-18 14:39:40.587502',	45,	'EXECUTED',	'9:dde36f7973e80d71fceee683bc5d2951',	'addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_RESOURCE; addColumn tableName=RESOURCE_SERVER_SCOPE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part2-KEYCLOAK-6095',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-18 14:39:40.59036',	46,	'EXECUTED',	'9:b855e9b0a406b34fa323235a0cf4f640',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part3-fixed',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-18 14:39:40.5918',	47,	'MARK_RAN',	'9:51abbacd7b416c50c4421a8cabf7927e',	'dropIndex indexName=IDX_RES_SERV_POL_RES_SERV, tableName=RESOURCE_SERVER_POLICY; dropIndex indexName=IDX_RES_SRV_RES_RES_SRV, tableName=RESOURCE_SERVER_RESOURCE; dropIndex indexName=IDX_RES_SRV_SCOPE_RES_SRV, tableName=RESOURCE_SERVER_SCOPE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-3.4.0.CR1-resource-server-pk-change-part3-fixed-nodropindex',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-18 14:39:40.670356',	48,	'EXECUTED',	'9:bdc99e567b3398bac83263d375aad143',	'addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_POLICY; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, tableName=RESOURCE_SERVER_RESOURCE; addNotNullConstraint columnName=RESOURCE_SERVER_CLIENT_ID, ...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authn-3.4.0.CR1-refresh-token-max-reuse',	'glavoie@gmail.com',	'META-INF/jpa-changelog-authz-3.4.0.CR1.xml',	'2025-05-18 14:39:40.673694',	49,	'EXECUTED',	'9:d198654156881c46bfba39abd7769e69',	'addColumn tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.4.0',	'keycloak',	'META-INF/jpa-changelog-3.4.0.xml',	'2025-05-18 14:39:40.708427',	50,	'EXECUTED',	'9:cfdd8736332ccdd72c5256ccb42335db',	'addPrimaryKey constraintName=CONSTRAINT_REALM_DEFAULT_ROLES, tableName=REALM_DEFAULT_ROLES; addPrimaryKey constraintName=CONSTRAINT_COMPOSITE_ROLE, tableName=COMPOSITE_ROLE; addPrimaryKey constraintName=CONSTR_REALM_DEFAULT_GROUPS, tableName=REALM...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.4.0-KEYCLOAK-5230',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-3.4.0.xml',	'2025-05-18 14:39:40.909135',	51,	'EXECUTED',	'9:7c84de3d9bd84d7f077607c1a4dcb714',	'createIndex indexName=IDX_FU_ATTRIBUTE, tableName=FED_USER_ATTRIBUTE; createIndex indexName=IDX_FU_CONSENT, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CONSENT_RU, tableName=FED_USER_CONSENT; createIndex indexName=IDX_FU_CREDENTIAL, t...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.4.1',	'psilva@redhat.com',	'META-INF/jpa-changelog-3.4.1.xml',	'2025-05-18 14:39:40.911904',	52,	'EXECUTED',	'9:5a6bb36cbefb6a9d6928452c0852af2d',	'modifyDataType columnName=VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.4.2',	'keycloak',	'META-INF/jpa-changelog-3.4.2.xml',	'2025-05-18 14:39:40.913711',	53,	'EXECUTED',	'9:8f23e334dbc59f82e0a328373ca6ced0',	'update tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('3.4.2-KEYCLOAK-5172',	'mkanis@redhat.com',	'META-INF/jpa-changelog-3.4.2.xml',	'2025-05-18 14:39:40.915238',	54,	'EXECUTED',	'9:9156214268f09d970cdf0e1564d866af',	'update tableName=CLIENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.0.0-KEYCLOAK-6335',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-18 14:39:40.921075',	55,	'EXECUTED',	'9:db806613b1ed154826c02610b7dbdf74',	'createTable tableName=CLIENT_AUTH_FLOW_BINDINGS; addPrimaryKey constraintName=C_CLI_FLOW_BIND, tableName=CLIENT_AUTH_FLOW_BINDINGS',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.0.0-CLEANUP-UNUSED-TABLE',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-18 14:39:40.924096',	56,	'EXECUTED',	'9:229a041fb72d5beac76bb94a5fa709de',	'dropTable tableName=CLIENT_IDENTITY_PROV_MAPPING',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.0.0-KEYCLOAK-6228',	'bburke@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-18 14:39:40.955337',	57,	'EXECUTED',	'9:079899dade9c1e683f26b2aa9ca6ff04',	'dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; dropNotNullConstraint columnName=CLIENT_ID, tableName=USER_CONSENT; addColumn tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHO...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.0.0-KEYCLOAK-5579-fixed',	'mposolda@redhat.com',	'META-INF/jpa-changelog-4.0.0.xml',	'2025-05-18 14:39:41.168019',	58,	'EXECUTED',	'9:139b79bcbbfe903bb1c2d2a4dbf001d9',	'dropForeignKeyConstraint baseTableName=CLIENT_TEMPLATE_ATTRIBUTES, constraintName=FK_CL_TEMPL_ATTR_TEMPL; renameTable newTableName=CLIENT_SCOPE_ATTRIBUTES, oldTableName=CLIENT_TEMPLATE_ATTRIBUTES; renameColumn newColumnName=SCOPE_ID, oldColumnName...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-4.0.0.CR1',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-4.0.0.CR1.xml',	'2025-05-18 14:39:41.184258',	59,	'EXECUTED',	'9:b55738ad889860c625ba2bf483495a04',	'createTable tableName=RESOURCE_SERVER_PERM_TICKET; addPrimaryKey constraintName=CONSTRAINT_FAPMT, tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRHO213XCX4WNKOG82SSPMT...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-4.0.0.Beta3',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-4.0.0.Beta3.xml',	'2025-05-18 14:39:41.187812',	60,	'EXECUTED',	'9:e0057eac39aa8fc8e09ac6cfa4ae15fe',	'addColumn tableName=RESOURCE_SERVER_POLICY; addColumn tableName=RESOURCE_SERVER_PERM_TICKET; addForeignKeyConstraint baseTableName=RESOURCE_SERVER_PERM_TICKET, constraintName=FK_FRSRPO2128CX4WNKOG82SSRFY, referencedTableName=RESOURCE_SERVER_POLICY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-4.2.0.Final',	'mhajas@redhat.com',	'META-INF/jpa-changelog-authz-4.2.0.Final.xml',	'2025-05-18 14:39:41.19252',	61,	'EXECUTED',	'9:42a33806f3a0443fe0e7feeec821326c',	'createTable tableName=RESOURCE_URIS; addForeignKeyConstraint baseTableName=RESOURCE_URIS, constraintName=FK_RESOURCE_SERVER_URIS, referencedTableName=RESOURCE_SERVER_RESOURCE; customChange; dropColumn columnName=URI, tableName=RESOURCE_SERVER_RESO...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-4.2.0.Final-KEYCLOAK-9944',	'hmlnarik@redhat.com',	'META-INF/jpa-changelog-authz-4.2.0.Final.xml',	'2025-05-18 14:39:41.196769',	62,	'EXECUTED',	'9:9968206fca46eecc1f51db9c024bfe56',	'addPrimaryKey constraintName=CONSTRAINT_RESOUR_URIS_PK, tableName=RESOURCE_URIS',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.2.0-KEYCLOAK-6313',	'wadahiro@gmail.com',	'META-INF/jpa-changelog-4.2.0.xml',	'2025-05-18 14:39:41.200487',	63,	'EXECUTED',	'9:92143a6daea0a3f3b8f598c97ce55c3d',	'addColumn tableName=REQUIRED_ACTION_PROVIDER',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.3.0-KEYCLOAK-7984',	'wadahiro@gmail.com',	'META-INF/jpa-changelog-4.3.0.xml',	'2025-05-18 14:39:41.202435',	64,	'EXECUTED',	'9:82bab26a27195d889fb0429003b18f40',	'update tableName=REQUIRED_ACTION_PROVIDER',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.6.0-KEYCLOAK-7950',	'psilva@redhat.com',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-05-18 14:39:41.204144',	65,	'EXECUTED',	'9:e590c88ddc0b38b0ae4249bbfcb5abc3',	'update tableName=RESOURCE_SERVER_RESOURCE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.6.0-KEYCLOAK-8377',	'keycloak',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-05-18 14:39:41.2298',	66,	'EXECUTED',	'9:5c1f475536118dbdc38d5d7977950cc0',	'createTable tableName=ROLE_ATTRIBUTE; addPrimaryKey constraintName=CONSTRAINT_ROLE_ATTRIBUTE_PK, tableName=ROLE_ATTRIBUTE; addForeignKeyConstraint baseTableName=ROLE_ATTRIBUTE, constraintName=FK_ROLE_ATTRIBUTE_ID, referencedTableName=KEYCLOAK_ROLE...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.6.0-KEYCLOAK-8555',	'gideonray@gmail.com',	'META-INF/jpa-changelog-4.6.0.xml',	'2025-05-18 14:39:41.252153',	67,	'EXECUTED',	'9:e7c9f5f9c4d67ccbbcc215440c718a17',	'createIndex indexName=IDX_COMPONENT_PROVIDER_TYPE, tableName=COMPONENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.7.0-KEYCLOAK-1267',	'sguilhen@redhat.com',	'META-INF/jpa-changelog-4.7.0.xml',	'2025-05-18 14:39:41.255249',	68,	'EXECUTED',	'9:88e0bfdda924690d6f4e430c53447dd5',	'addColumn tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.7.0-KEYCLOAK-7275',	'keycloak',	'META-INF/jpa-changelog-4.7.0.xml',	'2025-05-18 14:39:41.278688',	69,	'EXECUTED',	'9:f53177f137e1c46b6a88c59ec1cb5218',	'renameColumn newColumnName=CREATED_ON, oldColumnName=LAST_SESSION_REFRESH, tableName=OFFLINE_USER_SESSION; addNotNullConstraint columnName=CREATED_ON, tableName=OFFLINE_USER_SESSION; addColumn tableName=OFFLINE_USER_SESSION; customChange; createIn...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('4.8.0-KEYCLOAK-8835',	'sguilhen@redhat.com',	'META-INF/jpa-changelog-4.8.0.xml',	'2025-05-18 14:39:41.281926',	70,	'EXECUTED',	'9:a74d33da4dc42a37ec27121580d1459f',	'addNotNullConstraint columnName=SSO_MAX_LIFESPAN_REMEMBER_ME, tableName=REALM; addNotNullConstraint columnName=SSO_IDLE_TIMEOUT_REMEMBER_ME, tableName=REALM',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('authz-7.0.0-KEYCLOAK-10443',	'psilva@redhat.com',	'META-INF/jpa-changelog-authz-7.0.0.xml',	'2025-05-18 14:39:41.284458',	71,	'EXECUTED',	'9:fd4ade7b90c3b67fae0bfcfcb42dfb5f',	'addColumn tableName=RESOURCE_SERVER',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('8.0.0-adding-credential-columns',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-18 14:39:41.287857',	72,	'EXECUTED',	'9:aa072ad090bbba210d8f18781b8cebf4',	'addColumn tableName=CREDENTIAL; addColumn tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('8.0.0-updating-credential-data-not-oracle-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-18 14:39:41.290824',	73,	'EXECUTED',	'9:1ae6be29bab7c2aa376f6983b932be37',	'update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('8.0.0-updating-credential-data-oracle-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-18 14:39:41.292125',	74,	'MARK_RAN',	'9:14706f286953fc9a25286dbd8fb30d97',	'update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL; update tableName=FED_USER_CREDENTIAL',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('8.0.0-credential-cleanup-fixed',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-18 14:39:41.299409',	75,	'EXECUTED',	'9:2b9cc12779be32c5b40e2e67711a218b',	'dropDefaultValue columnName=COUNTER, tableName=CREDENTIAL; dropDefaultValue columnName=DIGITS, tableName=CREDENTIAL; dropDefaultValue columnName=PERIOD, tableName=CREDENTIAL; dropDefaultValue columnName=ALGORITHM, tableName=CREDENTIAL; dropColumn ...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('8.0.0-resource-tag-support',	'keycloak',	'META-INF/jpa-changelog-8.0.0.xml',	'2025-05-18 14:39:41.320961',	76,	'EXECUTED',	'9:91fa186ce7a5af127a2d7a91ee083cc5',	'addColumn tableName=MIGRATION_MODEL; createIndex indexName=IDX_UPDATE_TIME, tableName=MIGRATION_MODEL',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.0-always-display-client',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-18 14:39:41.323441',	77,	'EXECUTED',	'9:6335e5c94e83a2639ccd68dd24e2e5ad',	'addColumn tableName=CLIENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.0-drop-constraints-for-column-increase',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-18 14:39:41.324491',	78,	'MARK_RAN',	'9:6bdb5658951e028bfe16fa0a8228b530',	'dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5PMT, tableName=RESOURCE_SERVER_PERM_TICKET; dropUniqueConstraint constraintName=UK_FRSR6T700S9V50BU18WS5HA6, tableName=RESOURCE_SERVER_RESOURCE; dropPrimaryKey constraintName=CONSTRAINT_O...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.0-increase-column-size-federated-fk',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-18 14:39:41.335945',	79,	'EXECUTED',	'9:d5bc15a64117ccad481ce8792d4c608f',	'modifyDataType columnName=CLIENT_ID, tableName=FED_USER_CONSENT; modifyDataType columnName=CLIENT_REALM_CONSTRAINT, tableName=KEYCLOAK_ROLE; modifyDataType columnName=OWNER, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=CLIENT_ID, ta...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.0-recreate-constraints-after-column-increase',	'keycloak',	'META-INF/jpa-changelog-9.0.0.xml',	'2025-05-18 14:39:41.337367',	80,	'MARK_RAN',	'9:077cba51999515f4d3e7ad5619ab592c',	'addNotNullConstraint columnName=CLIENT_ID, tableName=OFFLINE_CLIENT_SESSION; addNotNullConstraint columnName=OWNER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNullConstraint columnName=REQUESTER, tableName=RESOURCE_SERVER_PERM_TICKET; addNotNull...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.1-add-index-to-client.client_id',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-18 14:39:41.358804',	81,	'EXECUTED',	'9:be969f08a163bf47c6b9e9ead8ac2afb',	'createIndex indexName=IDX_CLIENT_ID, tableName=CLIENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.1-KEYCLOAK-12579-drop-constraints',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-18 14:39:41.359928',	82,	'MARK_RAN',	'9:6d3bb4408ba5a72f39bd8a0b301ec6e3',	'dropUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.1-KEYCLOAK-12579-add-not-null-constraint',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-18 14:39:41.362812',	83,	'EXECUTED',	'9:966bda61e46bebf3cc39518fbed52fa7',	'addNotNullConstraint columnName=PARENT_GROUP, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.1-KEYCLOAK-12579-recreate-constraints',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-18 14:39:41.363945',	84,	'MARK_RAN',	'9:8dcac7bdf7378e7d823cdfddebf72fda',	'addUniqueConstraint constraintName=SIBLING_NAMES, tableName=KEYCLOAK_GROUP',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('9.0.1-add-index-to-events',	'keycloak',	'META-INF/jpa-changelog-9.0.1.xml',	'2025-05-18 14:39:41.383559',	85,	'EXECUTED',	'9:7d93d602352a30c0c317e6a609b56599',	'createIndex indexName=IDX_EVENT_TIME, tableName=EVENT_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('map-remove-ri',	'keycloak',	'META-INF/jpa-changelog-11.0.0.xml',	'2025-05-18 14:39:41.387905',	86,	'EXECUTED',	'9:71c5969e6cdd8d7b6f47cebc86d37627',	'dropForeignKeyConstraint baseTableName=REALM, constraintName=FK_TRAF444KK6QRKMS7N56AIWQ5Y; dropForeignKeyConstraint baseTableName=KEYCLOAK_ROLE, constraintName=FK_KJHO5LE2C0RAL09FL8CM9WFW9',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('map-remove-ri',	'keycloak',	'META-INF/jpa-changelog-12.0.0.xml',	'2025-05-18 14:39:41.391248',	87,	'EXECUTED',	'9:a9ba7d47f065f041b7da856a81762021',	'dropForeignKeyConstraint baseTableName=REALM_DEFAULT_GROUPS, constraintName=FK_DEF_GROUPS_GROUP; dropForeignKeyConstraint baseTableName=REALM_DEFAULT_ROLES, constraintName=FK_H4WPD7W4HSOOLNI3H0SW7BTJE; dropForeignKeyConstraint baseTableName=CLIENT...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('12.1.0-add-realm-localization-table',	'keycloak',	'META-INF/jpa-changelog-12.0.0.xml',	'2025-05-18 14:39:41.39784',	88,	'EXECUTED',	'9:fffabce2bc01e1a8f5110d5278500065',	'createTable tableName=REALM_LOCALIZATIONS; addPrimaryKey tableName=REALM_LOCALIZATIONS',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('default-roles',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.400992',	89,	'EXECUTED',	'9:fa8a5b5445e3857f4b010bafb5009957',	'addColumn tableName=REALM; customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('default-roles-cleanup',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.404225',	90,	'EXECUTED',	'9:67ac3241df9a8582d591c5ed87125f39',	'dropTable tableName=REALM_DEFAULT_ROLES; dropTable tableName=CLIENT_DEFAULT_ROLES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('13.0.0-KEYCLOAK-16844',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.425066',	91,	'EXECUTED',	'9:ad1194d66c937e3ffc82386c050ba089',	'createIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('map-remove-ri-13.0.0',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.429088',	92,	'EXECUTED',	'9:d9be619d94af5a2f5d07b9f003543b91',	'dropForeignKeyConstraint baseTableName=DEFAULT_CLIENT_SCOPE, constraintName=FK_R_DEF_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SCOPE_CLIENT, constraintName=FK_C_CLI_SCOPE_SCOPE; dropForeignKeyConstraint baseTableName=CLIENT_SC...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('13.0.0-KEYCLOAK-17992-drop-constraints',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.43008',	93,	'MARK_RAN',	'9:544d201116a0fcc5a5da0925fbbc3bde',	'dropPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CLSCOPE_CL, tableName=CLIENT_SCOPE_CLIENT; dropIndex indexName=IDX_CL_CLSCOPE, tableName=CLIENT_SCOPE_CLIENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('13.0.0-increase-column-size-federated',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.435095',	94,	'EXECUTED',	'9:43c0c1055b6761b4b3e89de76d612ccf',	'modifyDataType columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; modifyDataType columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('13.0.0-KEYCLOAK-17992-recreate-constraints',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.436354',	95,	'MARK_RAN',	'9:8bd711fd0330f4fe980494ca43ab1139',	'addNotNullConstraint columnName=CLIENT_ID, tableName=CLIENT_SCOPE_CLIENT; addNotNullConstraint columnName=SCOPE_ID, tableName=CLIENT_SCOPE_CLIENT; addPrimaryKey constraintName=C_CLI_SCOPE_BIND, tableName=CLIENT_SCOPE_CLIENT; createIndex indexName=...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('json-string-accomodation-fixed',	'keycloak',	'META-INF/jpa-changelog-13.0.0.xml',	'2025-05-18 14:39:41.439503',	96,	'EXECUTED',	'9:e07d2bc0970c348bb06fb63b1f82ddbf',	'addColumn tableName=REALM_ATTRIBUTE; update tableName=REALM_ATTRIBUTE; dropColumn columnName=VALUE, tableName=REALM_ATTRIBUTE; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=REALM_ATTRIBUTE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('14.0.0-KEYCLOAK-11019',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.494266',	97,	'EXECUTED',	'9:24fb8611e97f29989bea412aa38d12b7',	'createIndex indexName=IDX_OFFLINE_CSS_PRELOAD, tableName=OFFLINE_CLIENT_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USER, tableName=OFFLINE_USER_SESSION; createIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('14.0.0-KEYCLOAK-18286',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.495448',	98,	'MARK_RAN',	'9:259f89014ce2506ee84740cbf7163aa7',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('14.0.0-KEYCLOAK-18286-revert',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.501516',	99,	'MARK_RAN',	'9:04baaf56c116ed19951cbc2cca584022',	'dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('14.0.0-KEYCLOAK-18286-supported-dbs',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.524023',	100,	'EXECUTED',	'9:60ca84a0f8c94ec8c3504a5a3bc88ee8',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('14.0.0-KEYCLOAK-18286-unsupported-dbs',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.525327',	101,	'MARK_RAN',	'9:d3d977031d431db16e2c181ce49d73e9',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('KEYCLOAK-17267-add-index-to-user-attributes',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.546851',	102,	'EXECUTED',	'9:0b305d8d1277f3a89a0a53a659ad274c',	'createIndex indexName=IDX_USER_ATTRIBUTE_NAME, tableName=USER_ATTRIBUTE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('KEYCLOAK-18146-add-saml-art-binding-identifier',	'keycloak',	'META-INF/jpa-changelog-14.0.0.xml',	'2025-05-18 14:39:41.549319',	103,	'EXECUTED',	'9:2c374ad2cdfe20e2905a84c8fac48460',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('15.0.0-KEYCLOAK-18467',	'keycloak',	'META-INF/jpa-changelog-15.0.0.xml',	'2025-05-18 14:39:41.552659',	104,	'EXECUTED',	'9:47a760639ac597360a8219f5b768b4de',	'addColumn tableName=REALM_LOCALIZATIONS; update tableName=REALM_LOCALIZATIONS; dropColumn columnName=TEXTS, tableName=REALM_LOCALIZATIONS; renameColumn newColumnName=TEXTS, oldColumnName=TEXTS_NEW, tableName=REALM_LOCALIZATIONS; addNotNullConstrai...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('17.0.0-9562',	'keycloak',	'META-INF/jpa-changelog-17.0.0.xml',	'2025-05-18 14:39:41.5758',	105,	'EXECUTED',	'9:a6272f0576727dd8cad2522335f5d99e',	'createIndex indexName=IDX_USER_SERVICE_ACCOUNT, tableName=USER_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('18.0.0-10625-IDX_ADMIN_EVENT_TIME',	'keycloak',	'META-INF/jpa-changelog-18.0.0.xml',	'2025-05-18 14:39:41.599352',	106,	'EXECUTED',	'9:015479dbd691d9cc8669282f4828c41d',	'createIndex indexName=IDX_ADMIN_EVENT_TIME, tableName=ADMIN_EVENT_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('18.0.15-30992-index-consent',	'keycloak',	'META-INF/jpa-changelog-18.0.15.xml',	'2025-05-18 14:39:41.623343',	107,	'EXECUTED',	'9:80071ede7a05604b1f4906f3bf3b00f0',	'createIndex indexName=IDX_USCONSENT_SCOPE_ID, tableName=USER_CONSENT_CLIENT_SCOPE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('19.0.0-10135',	'keycloak',	'META-INF/jpa-changelog-19.0.0.xml',	'2025-05-18 14:39:41.626006',	108,	'EXECUTED',	'9:9518e495fdd22f78ad6425cc30630221',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('20.0.0-12964-supported-dbs',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-05-18 14:39:41.648883',	109,	'EXECUTED',	'9:e5f243877199fd96bcc842f27a1656ac',	'createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('20.0.0-12964-unsupported-dbs',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-05-18 14:39:41.650525',	110,	'MARK_RAN',	'9:1a6fcaa85e20bdeae0a9ce49b41946a5',	'createIndex indexName=IDX_GROUP_ATT_BY_NAME_VALUE, tableName=GROUP_ATTRIBUTE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('client-attributes-string-accomodation-fixed',	'keycloak',	'META-INF/jpa-changelog-20.0.0.xml',	'2025-05-18 14:39:41.655064',	111,	'EXECUTED',	'9:3f332e13e90739ed0c35b0b25b7822ca',	'addColumn tableName=CLIENT_ATTRIBUTES; update tableName=CLIENT_ATTRIBUTES; dropColumn columnName=VALUE, tableName=CLIENT_ATTRIBUTES; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('21.0.2-17277',	'keycloak',	'META-INF/jpa-changelog-21.0.2.xml',	'2025-05-18 14:39:41.657215',	112,	'EXECUTED',	'9:7ee1f7a3fb8f5588f171fb9a6ab623c0',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('21.1.0-19404',	'keycloak',	'META-INF/jpa-changelog-21.1.0.xml',	'2025-05-18 14:39:41.680337',	113,	'EXECUTED',	'9:3d7e830b52f33676b9d64f7f2b2ea634',	'modifyDataType columnName=DECISION_STRATEGY, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=LOGIC, tableName=RESOURCE_SERVER_POLICY; modifyDataType columnName=POLICY_ENFORCE_MODE, tableName=RESOURCE_SERVER',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('21.1.0-19404-2',	'keycloak',	'META-INF/jpa-changelog-21.1.0.xml',	'2025-05-18 14:39:41.682582',	114,	'MARK_RAN',	'9:627d032e3ef2c06c0e1f73d2ae25c26c',	'addColumn tableName=RESOURCE_SERVER_POLICY; update tableName=RESOURCE_SERVER_POLICY; dropColumn columnName=DECISION_STRATEGY, tableName=RESOURCE_SERVER_POLICY; renameColumn newColumnName=DECISION_STRATEGY, oldColumnName=DECISION_STRATEGY_NEW, tabl...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('22.0.0-17484-updated',	'keycloak',	'META-INF/jpa-changelog-22.0.0.xml',	'2025-05-18 14:39:41.684976',	115,	'EXECUTED',	'9:90af0bfd30cafc17b9f4d6eccd92b8b3',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('22.0.5-24031',	'keycloak',	'META-INF/jpa-changelog-22.0.0.xml',	'2025-05-18 14:39:41.68605',	116,	'MARK_RAN',	'9:a60d2d7b315ec2d3eba9e2f145f9df28',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('23.0.0-12062',	'keycloak',	'META-INF/jpa-changelog-23.0.0.xml',	'2025-05-18 14:39:41.690649',	117,	'EXECUTED',	'9:2168fbe728fec46ae9baf15bf80927b8',	'addColumn tableName=COMPONENT_CONFIG; update tableName=COMPONENT_CONFIG; dropColumn columnName=VALUE, tableName=COMPONENT_CONFIG; renameColumn newColumnName=VALUE, oldColumnName=VALUE_NEW, tableName=COMPONENT_CONFIG',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('23.0.0-17258',	'keycloak',	'META-INF/jpa-changelog-23.0.0.xml',	'2025-05-18 14:39:41.693212',	118,	'EXECUTED',	'9:36506d679a83bbfda85a27ea1864dca8',	'addColumn tableName=EVENT_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.0-9758',	'keycloak',	'META-INF/jpa-changelog-24.0.0.xml',	'2025-05-18 14:39:41.781429',	119,	'EXECUTED',	'9:502c557a5189f600f0f445a9b49ebbce',	'addColumn tableName=USER_ATTRIBUTE; addColumn tableName=FED_USER_ATTRIBUTE; createIndex indexName=USER_ATTR_LONG_VALUES, tableName=USER_ATTRIBUTE; createIndex indexName=FED_USER_ATTR_LONG_VALUES, tableName=FED_USER_ATTRIBUTE; createIndex indexName...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.0-9758-2',	'keycloak',	'META-INF/jpa-changelog-24.0.0.xml',	'2025-05-18 14:39:41.784027',	120,	'EXECUTED',	'9:bf0fdee10afdf597a987adbf291db7b2',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.0-26618-drop-index-if-present',	'keycloak',	'META-INF/jpa-changelog-24.0.0.xml',	'2025-05-18 14:39:41.786794',	121,	'MARK_RAN',	'9:04baaf56c116ed19951cbc2cca584022',	'dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.0-26618-reindex',	'keycloak',	'META-INF/jpa-changelog-24.0.0.xml',	'2025-05-18 14:39:41.809684',	122,	'EXECUTED',	'9:08707c0f0db1cef6b352db03a60edc7f',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.2-27228',	'keycloak',	'META-INF/jpa-changelog-24.0.2.xml',	'2025-05-18 14:39:41.811879',	123,	'EXECUTED',	'9:eaee11f6b8aa25d2cc6a84fb86fc6238',	'customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.2-27967-drop-index-if-present',	'keycloak',	'META-INF/jpa-changelog-24.0.2.xml',	'2025-05-18 14:39:41.812988',	124,	'MARK_RAN',	'9:04baaf56c116ed19951cbc2cca584022',	'dropIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('24.0.2-27967-reindex',	'keycloak',	'META-INF/jpa-changelog-24.0.2.xml',	'2025-05-18 14:39:41.814239',	125,	'MARK_RAN',	'9:d3d977031d431db16e2c181ce49d73e9',	'createIndex indexName=IDX_CLIENT_ATT_BY_NAME_VALUE, tableName=CLIENT_ATTRIBUTES',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-tables',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.817761',	126,	'EXECUTED',	'9:deda2df035df23388af95bbd36c17cef',	'addColumn tableName=OFFLINE_USER_SESSION; addColumn tableName=OFFLINE_CLIENT_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-creation',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.839643',	127,	'EXECUTED',	'9:3e96709818458ae49f3c679ae58d263a',	'createIndex indexName=IDX_OFFLINE_USS_BY_LAST_SESSION_REFRESH, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-cleanup-uss-createdon',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.891815',	128,	'EXECUTED',	'9:78ab4fc129ed5e8265dbcc3485fba92f',	'dropIndex indexName=IDX_OFFLINE_USS_CREATEDON, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-cleanup-uss-preload',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.927135',	129,	'EXECUTED',	'9:de5f7c1f7e10994ed8b62e621d20eaab',	'dropIndex indexName=IDX_OFFLINE_USS_PRELOAD, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-cleanup-uss-by-usersess',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.963445',	130,	'EXECUTED',	'9:6eee220d024e38e89c799417ec33667f',	'dropIndex indexName=IDX_OFFLINE_USS_BY_USERSESS, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-cleanup-css-preload',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.99725',	131,	'EXECUTED',	'9:5411d2fb2891d3e8d63ddb55dfa3c0c9',	'dropIndex indexName=IDX_OFFLINE_CSS_PRELOAD, tableName=OFFLINE_CLIENT_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-2-mysql',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:41.998666',	132,	'MARK_RAN',	'9:b7ef76036d3126bb83c2423bf4d449d6',	'createIndex indexName=IDX_OFFLINE_USS_BY_BROKER_SESSION_ID, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28265-index-2-not-mysql',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:42.037867',	133,	'EXECUTED',	'9:23396cf51ab8bc1ae6f0cac7f9f6fcf7',	'createIndex indexName=IDX_OFFLINE_USS_BY_BROKER_SESSION_ID, tableName=OFFLINE_USER_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-org',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:42.056251',	134,	'EXECUTED',	'9:5c859965c2c9b9c72136c360649af157',	'createTable tableName=ORG; addUniqueConstraint constraintName=UK_ORG_NAME, tableName=ORG; addUniqueConstraint constraintName=UK_ORG_GROUP, tableName=ORG; createTable tableName=ORG_DOMAIN',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('unique-consentuser',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:42.065172',	135,	'EXECUTED',	'9:5857626a2ea8767e9a6c66bf3a2cb32f',	'customChange; dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_LOCAL_CONSENT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_EXTERNAL_CONSENT, tableName=...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('unique-consentuser-mysql',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:42.066804',	136,	'MARK_RAN',	'9:b79478aad5adaa1bc428e31563f55e8e',	'customChange; dropUniqueConstraint constraintName=UK_JKUWUVD56ONTGSUHOGM8UEWRT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_LOCAL_CONSENT, tableName=USER_CONSENT; addUniqueConstraint constraintName=UK_EXTERNAL_CONSENT, tableName=...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('25.0.0-28861-index-creation',	'keycloak',	'META-INF/jpa-changelog-25.0.0.xml',	'2025-05-18 14:39:42.127247',	137,	'EXECUTED',	'9:b9acb58ac958d9ada0fe12a5d4794ab1',	'createIndex indexName=IDX_PERM_TICKET_REQUESTER, tableName=RESOURCE_SERVER_PERM_TICKET; createIndex indexName=IDX_PERM_TICKET_OWNER, tableName=RESOURCE_SERVER_PERM_TICKET',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-org-alias',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.133133',	138,	'EXECUTED',	'9:6ef7d63e4412b3c2d66ed179159886a4',	'addColumn tableName=ORG; update tableName=ORG; addNotNullConstraint columnName=ALIAS, tableName=ORG; addUniqueConstraint constraintName=UK_ORG_ALIAS, tableName=ORG',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-org-group',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.138933',	139,	'EXECUTED',	'9:da8e8087d80ef2ace4f89d8c5b9ca223',	'addColumn tableName=KEYCLOAK_GROUP; update tableName=KEYCLOAK_GROUP; addNotNullConstraint columnName=TYPE, tableName=KEYCLOAK_GROUP; customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-org-indexes',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.161419',	140,	'EXECUTED',	'9:79b05dcd610a8c7f25ec05135eec0857',	'createIndex indexName=IDX_ORG_DOMAIN_ORG_ID, tableName=ORG_DOMAIN',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-org-group-membership',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.167144',	141,	'EXECUTED',	'9:a6ace2ce583a421d89b01ba2a28dc2d4',	'addColumn tableName=USER_GROUP_MEMBERSHIP; update tableName=USER_GROUP_MEMBERSHIP; addNotNullConstraint columnName=MEMBERSHIP_TYPE, tableName=USER_GROUP_MEMBERSHIP',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('31296-persist-revoked-access-tokens',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.172572',	142,	'EXECUTED',	'9:64ef94489d42a358e8304b0e245f0ed4',	'createTable tableName=REVOKED_TOKEN; addPrimaryKey constraintName=CONSTRAINT_RT, tableName=REVOKED_TOKEN',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('31725-index-persist-revoked-access-tokens',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.195356',	143,	'EXECUTED',	'9:b994246ec2bf7c94da881e1d28782c7b',	'createIndex indexName=IDX_REV_TOKEN_ON_EXPIRE, tableName=REVOKED_TOKEN',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-idps-for-login',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.239037',	144,	'EXECUTED',	'9:51f5fffadf986983d4bd59582c6c1604',	'addColumn tableName=IDENTITY_PROVIDER; createIndex indexName=IDX_IDP_REALM_ORG, tableName=IDENTITY_PROVIDER; createIndex indexName=IDX_IDP_FOR_LOGIN, tableName=IDENTITY_PROVIDER; customChange',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-32583-drop-redundant-index-on-client-session',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.26996',	145,	'EXECUTED',	'9:24972d83bf27317a055d234187bb4af9',	'dropIndex indexName=IDX_US_SESS_ID_ON_CL_SESS, tableName=OFFLINE_CLIENT_SESSION',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0.32582-remove-tables-user-session-user-session-note-and-client-session',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.276036',	146,	'EXECUTED',	'9:febdc0f47f2ed241c59e60f58c3ceea5',	'dropTable tableName=CLIENT_SESSION_ROLE; dropTable tableName=CLIENT_SESSION_NOTE; dropTable tableName=CLIENT_SESSION_PROT_MAPPER; dropTable tableName=CLIENT_SESSION_AUTH_STATUS; dropTable tableName=CLIENT_USER_SESSION_NOTE; dropTable tableName=CLI...',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.0.0-33201-org-redirect-url',	'keycloak',	'META-INF/jpa-changelog-26.0.0.xml',	'2025-05-18 14:39:42.27824',	147,	'EXECUTED',	'9:4d0e22b0ac68ebe9794fa9cb752ea660',	'addColumn tableName=ORG',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('29399-jdbc-ping-default',	'keycloak',	'META-INF/jpa-changelog-26.1.0.xml',	'2025-05-18 14:39:42.286156',	148,	'EXECUTED',	'9:007dbe99d7203fca403b89d4edfdf21e',	'createTable tableName=JGROUPS_PING; addPrimaryKey constraintName=CONSTRAINT_JGROUPS_PING, tableName=JGROUPS_PING',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.1.0-34013',	'keycloak',	'META-INF/jpa-changelog-26.1.0.xml',	'2025-05-18 14:39:42.290029',	149,	'EXECUTED',	'9:e6b686a15759aef99a6d758a5c4c6a26',	'addColumn tableName=ADMIN_EVENT_ENTITY',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254'),
        ('26.1.0-34380',	'keycloak',	'META-INF/jpa-changelog-26.1.0.xml',	'2025-05-18 14:39:42.292414',	150,	'EXECUTED',	'9:ac8b9edb7c2b6c17a1c7a11fcf5ccf01',	'dropTable tableName=USERNAME_LOGIN_FAILURE',	'',	NULL,	'4.29.1',	NULL,	NULL,	'7579178254');

        CREATE TABLE "public"."databasechangeloglock" (
            "id" integer NOT NULL,
            "locked" boolean NOT NULL,
            "lockgranted" timestamp,
            "lockedby" character varying(255),
            CONSTRAINT "databasechangeloglock_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        INSERT INTO "databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES
        (1,	'0',	NULL,	NULL),
        (1000,	'0',	NULL,	NULL);

        CREATE TABLE "public"."default_client_scope" (
            "realm_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            "default_scope" boolean DEFAULT false NOT NULL,
            CONSTRAINT "r_def_cli_scope_bind" PRIMARY KEY ("realm_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX idx_defcls_realm ON public.default_client_scope USING btree (realm_id);

        CREATE INDEX idx_defcls_scope ON public.default_client_scope USING btree (scope_id);

        INSERT INTO "default_client_scope" ("realm_id", "scope_id", "default_scope") VALUES
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5cd8523c-5f60-463a-b532-2cfaf55ded80',	'0'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'bc89c033-c280-43dc-8cdb-bc269d05e524',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'bea1a009-6cf9-4d99-b259-d3d8c3fda3f1',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'10591155-81d6-49f0-a8f5-e8087a63db62',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'9798e065-09c6-43fc-97bf-c50bdfdc49e6',	'0'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'25a3c139-2ee5-4355-8438-1f297d538aee',	'0'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'a2c56a67-31c9-4df1-8118-3ac8734653db',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770',	'0'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd5c86a99-148a-4768-bfca-b576dc02c70a',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'3380a789-01be-4463-ab25-474b6c8fd663',	'1'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'b3104f32-05c6-4937-ab4d-cf157a3353dd',	'0'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'fe1baebc-39a9-429e-a86e-36c4be33b83b',	'0'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'51feec57-d3d7-4687-b90a-70518e0d0c46',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'57ac9c43-a988-4e5f-8f0a-bc0e3a09f280',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8335dfdb-9837-4d49-9666-7a6b93a46509',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'52af6a76-82be-45a7-918d-48ff410082d1',	'0'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab',	'0'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'd8c80810-aa75-4c40-921f-25c3b5c721d3',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'dc95d831-220d-45e3-8086-09384d30d4f9',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a',	'0'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'7ca1d277-d673-46fb-89ad-f31679e2a29a',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'731c813d-9c7c-4cee-bd14-b0ddae633e6d',	'1'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'e18b0b07-aa7f-419d-812b-ebf112027b72',	'0'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'744a67ca-2640-4380-8ad8-1ab450b0ba0b',	'0'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'3128e6da-8350-4d17-9d92-825fe80b7303',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'af2e4cec-ac0b-4121-b053-9ff6ad8afa5e',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'43547928-98c8-48cd-989b-b2eecabb8f12',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'f775ff97-aa91-4817-b493-2c7a70236ec8',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'f62d7212-60ac-4ef9-96e5-1cc521ffe633',	'0'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00',	'0'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'1424086c-fade-4ad5-91eb-b4717a3e148f',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'5dcd7251-dacb-49cc-9ae4-91f9019674ed',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'c691f15d-a84d-4563-b053-588cd21daf1e',	'0'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'a401b408-9edd-4ad9-a067-55228409b062',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'58f52437-bd45-449b-8fb2-e7f7281c2ec5',	'1'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'174806e1-084a-42cd-b3ad-800599d4a2dd',	'0');

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
            "details_json_long_value" text,
            CONSTRAINT "constraint_4" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_event_time ON public.event_entity USING btree (realm_id, event_time);


        CREATE TABLE "public"."fed_user_attribute" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            "value" character varying(2024),
            "long_value_hash" bytea,
            "long_value_hash_lower_case" bytea,
            "long_value" text,
            CONSTRAINT "constr_fed_user_attr_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_fu_attribute ON public.fed_user_attribute USING btree (user_id, realm_id, name);

        CREATE INDEX fed_user_attr_long_values ON public.fed_user_attribute USING btree (long_value_hash, name);

        CREATE INDEX fed_user_attr_long_values_lower_case ON public.fed_user_attribute USING btree (long_value_hash_lower_case, name);


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
        ) WITH (oids = false);

        CREATE INDEX idx_fu_consent_ru ON public.fed_user_consent USING btree (realm_id, user_id);

        CREATE INDEX idx_fu_cnsnt_ext ON public.fed_user_consent USING btree (user_id, client_storage_provider, external_client_id);

        CREATE INDEX idx_fu_consent ON public.fed_user_consent USING btree (user_id, client_id);


        CREATE TABLE "public"."fed_user_consent_cl_scope" (
            "user_consent_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_fgrntcsnt_clsc_pm" PRIMARY KEY ("user_consent_id", "scope_id")
        ) WITH (oids = false);


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
        ) WITH (oids = false);

        CREATE INDEX idx_fu_credential ON public.fed_user_credential USING btree (user_id, type);

        CREATE INDEX idx_fu_credential_ru ON public.fed_user_credential USING btree (realm_id, user_id);


        CREATE TABLE "public"."fed_user_group_membership" (
            "group_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_user_group" PRIMARY KEY ("group_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX idx_fu_group_membership ON public.fed_user_group_membership USING btree (user_id, group_id);

        CREATE INDEX idx_fu_group_membership_ru ON public.fed_user_group_membership USING btree (realm_id, user_id);


        CREATE TABLE "public"."fed_user_required_action" (
            "required_action" character varying(255) DEFAULT ' ' NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_required_action" PRIMARY KEY ("required_action", "user_id")
        ) WITH (oids = false);

        CREATE INDEX idx_fu_required_action ON public.fed_user_required_action USING btree (user_id, required_action);

        CREATE INDEX idx_fu_required_action_ru ON public.fed_user_required_action USING btree (realm_id, user_id);


        CREATE TABLE "public"."fed_user_role_mapping" (
            "role_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "storage_provider_id" character varying(36),
            CONSTRAINT "constr_fed_user_role" PRIMARY KEY ("role_id", "user_id")
        ) WITH (oids = false);

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
        ) WITH (oids = false);

        CREATE INDEX idx_fedidentity_user ON public.federated_identity USING btree (user_id);

        CREATE INDEX idx_fedidentity_feduser ON public.federated_identity USING btree (federated_user_id);


        CREATE TABLE "public"."federated_user" (
            "id" character varying(255) NOT NULL,
            "storage_provider_id" character varying(255),
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constr_federated_user" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "public"."group_attribute" (
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_group_attribute_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_group_attr_group ON public.group_attribute USING btree (group_id);

        CREATE INDEX idx_group_att_by_name_value ON public.group_attribute USING btree (name, ((value)::character varying(250)));


        CREATE TABLE "public"."group_role_mapping" (
            "role_id" character varying(36) NOT NULL,
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_group_role" PRIMARY KEY ("role_id", "group_id")
        ) WITH (oids = false);

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
            "organization_id" character varying(255),
            "hide_on_login" boolean DEFAULT false,
            CONSTRAINT "constraint_2b" PRIMARY KEY ("internal_id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_2daelwnibji49avxsrtuf6xj33 ON public.identity_provider USING btree (provider_alias, realm_id);

        CREATE INDEX idx_ident_prov_realm ON public.identity_provider USING btree (realm_id);

        CREATE INDEX idx_idp_realm_org ON public.identity_provider USING btree (realm_id, organization_id);

        CREATE INDEX idx_idp_for_login ON public.identity_provider USING btree (realm_id, enabled, link_only, hide_on_login, organization_id);


        CREATE TABLE "public"."identity_provider_config" (
            "identity_provider_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_d" PRIMARY KEY ("identity_provider_id", "name")
        ) WITH (oids = false);


        CREATE TABLE "public"."identity_provider_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "idp_alias" character varying(255) NOT NULL,
            "idp_mapper_name" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_idpm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_id_prov_mapp_realm ON public.identity_provider_mapper USING btree (realm_id);


        CREATE TABLE "public"."idp_mapper_config" (
            "idp_mapper_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_idpmconfig" PRIMARY KEY ("idp_mapper_id", "name")
        ) WITH (oids = false);


        CREATE TABLE "public"."jgroups_ping" (
            "address" character varying(200) NOT NULL,
            "name" character varying(200),
            "cluster_name" character varying(200) NOT NULL,
            "ip" character varying(200) NOT NULL,
            "coord" boolean,
            CONSTRAINT "constraint_jgroups_ping" PRIMARY KEY ("address")
        ) WITH (oids = false);

        INSERT INTO "jgroups_ping" ("address", "name", "cluster_name", "ip", "coord") VALUES
        ('uuid://54192226-9368-48d4-8ad3-aeb7ab589700',	'3.0.0-keycloak.openk9.localhost-44507',	'ISPN',	'172.18.0.18:7800',	'1');

        CREATE TABLE "public"."keycloak_group" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255),
            "parent_group" character varying(36) NOT NULL,
            "realm_id" character varying(36),
            "type" integer DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_group" PRIMARY KEY ("id")
        ) WITH (oids = false);

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
        ) WITH (oids = false);

        CREATE INDEX idx_keycloak_role_client ON public.keycloak_role USING btree (client);

        CREATE INDEX idx_keycloak_role_realm ON public.keycloak_role USING btree (realm);

        CREATE UNIQUE INDEX "UK_J3RWUVD56ONTGSUHOGM184WW2-2" ON public.keycloak_role USING btree (name, client_realm_constraint);

        INSERT INTO "keycloak_role" ("id", "client_realm_constraint", "client_role", "description", "name", "realm_id", "client", "realm") VALUES
        ('1a685a27-f87d-48c9-882f-0c81f6a7e412',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'${role_default-roles}',	'default-roles-master',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	NULL),
        ('d55eee94-3000-40b0-bf22-d9ad29a29ca5',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'${role_create-realm}',	'create-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	NULL),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'${role_admin}',	'admin',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	NULL),
        ('163f2f38-ee04-4a12-baf2-f39bfb0ecc35',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_create-client}',	'create-client',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('5da7175e-9163-4949-a605-d1903d112ecc',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_view-realm}',	'view-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('96f8af5f-4009-45c1-87f4-06bcf453b339',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_view-users}',	'view-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('53a6d29b-3648-4477-9a42-61d168813266',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_view-clients}',	'view-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('bb9e6836-245f-4bd1-bb57-e09186cd36f9',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_view-events}',	'view-events',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('55fc48dd-82c4-41cd-a4ee-8f67d8cda72e',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('2949e57b-dcf0-476c-bf00-ae3ac8f79cd3',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_view-authorization}',	'view-authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('9576afd4-b749-4478-acee-646782ce42f3',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_manage-realm}',	'manage-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('796a9a82-94bb-4833-bab0-666909435ebd',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_manage-users}',	'manage-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('3039d5f7-03bc-4fa6-94bb-07210f8a3f58',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_manage-clients}',	'manage-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('6cc16c41-179e-4e49-a9aa-09814923225b',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_manage-events}',	'manage-events',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('dc3cebed-be2d-45b0-aabd-d98054a6f2fc',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('f530218a-9873-4dc9-a944-31a169616bab',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_manage-authorization}',	'manage-authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('7e9d4b4b-ce22-40ca-9032-bfefc81209b3',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_query-users}',	'query-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('8bb53be6-16a3-47ac-a63b-0d9a3ce36725',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_query-clients}',	'query-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('590de40e-f9e0-414c-ae48-200250188db7',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_query-realms}',	'query-realms',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('0b3174a0-4387-4dee-99b8-41614632f2f8',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_query-groups}',	'query-groups',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('e9ecf1a2-f35a-418c-9f51-72c6ff36a854',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_view-profile}',	'view-profile',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('9c8aae56-a38e-4c53-a6aa-37220b4fb170',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_manage-account}',	'manage-account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('5e60edab-8f04-458f-8b30-4a7607e8ba67',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_manage-account-links}',	'manage-account-links',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('04243598-d764-4c01-9dce-5616b6a7d447',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_view-applications}',	'view-applications',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('fb02b3d3-f28c-4cea-92e4-f22068f9bbc4',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_view-consent}',	'view-consent',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('fd0bd273-e782-4869-a5ea-46b574748132',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_manage-consent}',	'manage-consent',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('78053aa1-ce91-463e-8bc0-0cefef09c7ba',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_view-groups}',	'view-groups',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('c6c9d985-92eb-46bb-8ba8-80cee42c6cea',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	'1',	'${role_delete-account}',	'delete-account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'd12747b4-3958-40b8-b46d-a81db3d115c2',	NULL),
        ('b8eee19c-8153-46f5-af71-6a05730c0bbb',	'4b422d32-a6b3-4919-aedf-183726855a3d',	'1',	'${role_read-token}',	'read-token',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4b422d32-a6b3-4919-aedf-183726855a3d',	NULL),
        ('a631c878-91f2-4cf5-bc94-e1359ae2b97d',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	'1',	'${role_impersonation}',	'impersonation',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	NULL),
        ('6586e32c-9de2-4ebf-85be-92b235951148',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'${role_offline-access}',	'offline_access',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	NULL),
        ('1b499e6b-2ec8-4a0b-9929-1ac7e6472743',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'${role_uma_authorization}',	'uma_authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	NULL,	NULL),
        ('88c38d21-732e-40b0-942d-7392520f0ef4',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0',	'${role_default-roles}',	'default-roles-tenant-manager',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL,	NULL),
        ('60257692-5b91-46e8-8070-3a6f3f9995f5',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_create-client}',	'create-client',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('0951e280-22ec-48ec-b00c-d11737387d4b',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_view-realm}',	'view-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('ff8baf22-dfc3-452e-b147-e23669bf3e00',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_view-users}',	'view-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('959a88bb-f1af-4703-8158-452573141104',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_view-clients}',	'view-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('7736efaa-98d5-4dba-9e35-05924be102be',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_view-events}',	'view-events',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('66879d50-d7e1-4f48-ae98-c5507a2f3bce',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('87093347-1934-46d8-a640-6580a5e88691',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_view-authorization}',	'view-authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('64fc0715-91d1-431c-9d70-28ac14687f93',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_manage-realm}',	'manage-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('d233e0b6-0e95-44af-a4f3-53cc491fd5dd',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_manage-users}',	'manage-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('63f88d0d-17d5-45c2-98b3-2725ca0cbfd0',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_manage-clients}',	'manage-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('df2e4981-ceb5-4683-af11-d49c63371dc5',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_manage-events}',	'manage-events',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('766ef62e-e36f-4af3-9fb2-fee4a9021a44',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('7ab556a8-376b-44a5-b6fb-9eb26ed9928e',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_manage-authorization}',	'manage-authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('9d89f38f-fcf7-4548-b3f6-baa4db8e333c',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_query-users}',	'query-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('82d3d127-32bc-44ef-8cab-3467d0b00386',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_query-clients}',	'query-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('b1cc705a-8ed6-4075-b30f-fe315f38e59a',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_query-realms}',	'query-realms',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('67ef4ad7-c106-40a5-9331-fcf1ceda9474',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_query-groups}',	'query-groups',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('92a6d6b1-b1ed-40c0-99fd-7dd18053d406',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_realm-admin}',	'realm-admin',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('44755452-e042-4376-8217-083d6f8ad6ef',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_create-client}',	'create-client',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('82b88724-7da1-4bf5-a1e0-956faac79c58',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_view-realm}',	'view-realm',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('1a39e8b3-5a27-4f96-b94f-9227f91c65be',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_view-users}',	'view-users',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('a1e70ee5-f06c-470b-902f-860bb04967f1',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_view-clients}',	'view-clients',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('4f34fd7b-b879-43f1-908f-e5d542bef0d7',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_view-events}',	'view-events',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('f91735d4-9e8a-4e3e-af6e-2dae75c81d61',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('05d27c53-c738-4bc8-b7cf-8b1d57fb957f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_view-authorization}',	'view-authorization',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('c7506d5d-1d8b-4db1-aff2-75d90b6254c9',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_manage-realm}',	'manage-realm',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('37dfce7d-774d-4ca5-a6b6-58f14c08bc17',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_manage-users}',	'manage-users',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('b105e32f-fe77-4f72-a542-da668c9266f3',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_manage-clients}',	'manage-clients',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('611b97cb-d036-4186-8408-853da3d6a3da',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_manage-events}',	'manage-events',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('7d199ddb-d219-4a08-aaa9-4a42f3a445a7',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('1a3e5603-2049-4c8e-ba00-dd5946838630',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_manage-authorization}',	'manage-authorization',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('ff4ab908-027b-4806-a4f7-d78fe32f59f6',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_query-users}',	'query-users',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('75de19b6-b43b-45c4-8da7-bde5ee6d3708',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_query-clients}',	'query-clients',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('4122e121-07b8-44a0-af1a-d80048e43cb4',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_query-realms}',	'query-realms',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('f80e2418-9c3a-4a81-8248-0f730d3fb3f8',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_query-groups}',	'query-groups',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('4abcec7b-c748-4121-8237-8661ae756192',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_view-profile}',	'view-profile',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('359987c6-da37-4cb7-a1b8-956b217d8ead',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_manage-account}',	'manage-account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('68992d4e-4c35-4d7c-9e29-26600cfc86ec',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_manage-account-links}',	'manage-account-links',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('239dad97-8dca-4552-b2ce-71680dec3e34',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_view-applications}',	'view-applications',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('efc9df94-2a33-4109-9f9a-b256f34d5851',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_view-consent}',	'view-consent',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('7c2f32d1-590f-402e-9f78-cec4102b879e',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_manage-consent}',	'manage-consent',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('45b02373-9d28-4b72-9985-6de40a31e3b9',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_view-groups}',	'view-groups',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('63ae9a52-55d7-49fa-8804-f5c1ecf54db7',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'1',	'${role_delete-account}',	'delete-account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'3ed63266-c37d-4a89-b2b6-226108e1ae4f',	NULL),
        ('48be0d61-c06a-4883-b2ab-0944d6d0dd40',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	'1',	'${role_impersonation}',	'impersonation',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	NULL),
        ('c7a75ebe-5049-4ef9-8577-3b54253e79ac',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	'1',	'${role_impersonation}',	'impersonation',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'315b3003-d0f9-4a6f-896a-7dd913fed925',	NULL),
        ('2a185074-a8c4-48bc-aedb-58f1e3d41643',	'401f9b61-d1ea-4579-8def-0047bcea9776',	'1',	'${role_read-token}',	'read-token',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'401f9b61-d1ea-4579-8def-0047bcea9776',	NULL),
        ('675b804d-7f39-41f9-bbd7-805d095b590e',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0',	'${role_offline-access}',	'offline_access',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL,	NULL),
        ('4b5d71dd-c6ec-4bb1-bc2a-42308bec1113',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0',	'${role_uma_authorization}',	'uma_authorization',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL,	NULL),
        ('7c0ebbca-d2a5-42ba-af2a-4207ecd79101',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0',	'',	'admin',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	NULL,	NULL),
        ('08181cbd-ccaf-4ff4-98ed-daa5aa59e956',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'${role_default-roles}',	'default-roles-openk9',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL,	NULL),
        ('6a23a8ab-ce09-4894-b5e9-ff2b879cde0c',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_create-client}',	'create-client',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('714d8436-5511-4634-a5ca-c5ad01a4ac7f',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_view-realm}',	'view-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('1b7a1969-47ea-4432-bf00-a7ba40def179',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_view-users}',	'view-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('a1e3fa29-86b1-4077-bfa6-de7cee15b0fa',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_view-clients}',	'view-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('b0cfb90e-4efd-4104-98bd-d51e100863e9',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_view-events}',	'view-events',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('f30d7175-7a6b-4763-95b7-b9ef179f5fb3',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('dd0eeceb-e290-4708-bd5c-bb1146a55fb9',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_view-authorization}',	'view-authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('f294eaef-b0a3-4bb0-b368-4f5f213ffbeb',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_manage-realm}',	'manage-realm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('3f2a371a-96dd-4e08-8120-db48dd5ddcda',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_manage-users}',	'manage-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('55cd6bee-117b-4d29-a809-3f1771ac63dc',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_manage-clients}',	'manage-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('39349575-4c48-4dac-8904-e575357f0e33',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_manage-events}',	'manage-events',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('fe5f5bc9-9a6d-4d7d-9371-fee59396ff26',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('1e0fd2c2-817c-406c-b47c-357fcf98ce2b',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_manage-authorization}',	'manage-authorization',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('a41bedc5-bc98-4fe5-9d6c-9a4da8ea81cb',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_query-users}',	'query-users',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('7debbdcf-c6b0-4ad4-afc1-11657d4e6e82',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_query-clients}',	'query-clients',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('c3de7d36-1b29-4596-acc3-ec1ec696ae87',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_query-realms}',	'query-realms',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('d5d3d265-0cf4-42ee-80d1-54859a661769',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_query-groups}',	'query-groups',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('9a60c381-e8e2-4f07-9dbe-984931c27f06',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_realm-admin}',	'realm-admin',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('012fbf74-66ed-4125-af5b-4a4d899d908f',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_create-client}',	'create-client',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('1a6bb9a6-3492-489f-8850-e8b376621721',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_view-realm}',	'view-realm',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('d00af4d8-2f59-491f-ac89-32df2dca4183',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_view-users}',	'view-users',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('1e3267e5-445b-4310-bdfa-dbbb2835d40d',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_view-clients}',	'view-clients',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('f9a154d1-8720-484a-954d-f0fd10f0b8cd',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_view-events}',	'view-events',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('47b9d3fe-331d-43bc-a625-4cee28fc2d45',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_view-identity-providers}',	'view-identity-providers',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('3c264d99-5a81-4921-879b-a99f613aa247',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_view-authorization}',	'view-authorization',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('704fe125-6687-41f7-951c-10865ea96e50',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_manage-realm}',	'manage-realm',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('745e6d51-cf3e-4bf7-aff2-403437e74455',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_manage-users}',	'manage-users',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('7cc4fa65-e7f5-4926-89a7-8132ebe09b3d',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_manage-clients}',	'manage-clients',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('c859daeb-04d1-4058-820a-d24046672404',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_manage-events}',	'manage-events',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('694fc92d-d192-4db9-84e4-dbee7b76b165',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_manage-identity-providers}',	'manage-identity-providers',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('a9bd49ad-72b4-4ab1-8f9c-a309cc564b6b',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_manage-authorization}',	'manage-authorization',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('828cf0da-dd49-4202-ba1f-94cf922248a0',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_query-users}',	'query-users',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('fc1c387d-de06-407c-a423-9610e7f85432',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_query-clients}',	'query-clients',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('2f2d8a1f-2f36-4bac-b0f8-946e2414542e',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_query-realms}',	'query-realms',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('6107b5f9-afe2-4a6b-9533-3a925b96588f',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_query-groups}',	'query-groups',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('ebd941fd-d2b9-455f-84c1-8661c6d072fa',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_view-profile}',	'view-profile',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('7da37ec1-399e-4ddd-8f7b-99ade12ffaed',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_manage-account}',	'manage-account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('5f9cf033-05e4-453c-bb15-9825750ea647',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_manage-account-links}',	'manage-account-links',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('0fdc179e-91b0-4cca-ad24-5b166a49267a',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_view-applications}',	'view-applications',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('664e2b7e-fb5b-4fc9-a6be-07d7d6f6a397',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_view-consent}',	'view-consent',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('c3ea1519-4c28-4a21-b100-08e87c189c08',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_manage-consent}',	'manage-consent',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('e2aacd9e-d560-44e6-8db3-6c80bd391dd5',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_view-groups}',	'view-groups',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('beeb28d5-0dbf-4941-b618-e6147770aa7c',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'1',	'${role_delete-account}',	'delete-account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'eb44f382-bd55-49ee-8103-4ab4e8881d0b',	NULL),
        ('d78413d4-313e-4e6f-9555-5d8cb23dd0e3',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	'1',	'${role_impersonation}',	'impersonation',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	NULL),
        ('7848e509-842b-4381-a750-98f06c6d5ff0',	'37eb8750-21cd-4452-8c27-10873468e17e',	'1',	'${role_impersonation}',	'impersonation',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'37eb8750-21cd-4452-8c27-10873468e17e',	NULL),
        ('87584cee-8393-4ff0-ae18-6f7f37cdbaac',	'2355c5ca-f94a-4858-9b94-e81179e44e89',	'1',	'${role_read-token}',	'read-token',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'2355c5ca-f94a-4858-9b94-e81179e44e89',	NULL),
        ('6edf8d44-92b6-4db4-81fc-5deb7a693f50',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'${role_offline-access}',	'offline_access',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL,	NULL),
        ('7f2bec20-9ef5-4ca0-bc5c-c64e42a63a8e',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'${role_uma_authorization}',	'uma_authorization',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL,	NULL),
        ('ffbcebb9-7783-4e9d-ab0a-4d03ea836c1f',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'',	'k9-admin',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL,	NULL),
        ('ffbdb4bb-a64c-4c1b-b7f9-ad85cc013f9a',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'',	'k9-read',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL,	NULL),
        ('490ea4a2-4d12-47e6-bd54-0a3035b4e869',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'',	'k9-write',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	NULL,	NULL);

        CREATE TABLE "public"."migration_model" (
            "id" character varying(36) NOT NULL,
            "version" character varying(36),
            "update_time" bigint DEFAULT '0' NOT NULL,
            CONSTRAINT "constraint_migmod" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_update_time ON public.migration_model USING btree (update_time);

        INSERT INTO "migration_model" ("id", "version", "update_time") VALUES
        ('mrmsa',	'26.1.3',	1747579183);

        CREATE TABLE "public"."offline_client_session" (
            "user_session_id" character varying(36) NOT NULL,
            "client_id" character varying(255) NOT NULL,
            "offline_flag" character varying(4) NOT NULL,
            "timestamp" integer,
            "data" text,
            "client_storage_provider" character varying(36) DEFAULT 'local' NOT NULL,
            "external_client_id" character varying(255) DEFAULT 'local' NOT NULL,
            "version" integer DEFAULT '0',
            CONSTRAINT "constraint_offl_cl_ses_pk3" PRIMARY KEY ("user_session_id", "client_id", "client_storage_provider", "external_client_id", "offline_flag")
        ) WITH (oids = false);

        INSERT INTO "offline_client_session" ("user_session_id", "client_id", "offline_flag", "timestamp", "data", "client_storage_provider", "external_client_id", "version") VALUES
        ('fefc982b-20f7-4a80-825d-608a2b92ba8d',	'46305075-83e2-4080-91ca-4f953b91868d',	'0',	1747579380,	'{"authMethod":"openid-connect","redirectUri":"http://keycloak.openk9.localhost:8081/admin/master/console/","notes":{"clientId":"46305075-83e2-4080-91ca-4f953b91868d","iss":"http://keycloak.openk9.localhost:8081/realms/master","startedAt":"1747579212","response_type":"code","level-of-authentication":"-1","code_challenge_method":"S256","nonce":"7e206691-c450-4d7a-829b-0171d1ccafef","response_mode":"query","scope":"openid","userSessionStartedAt":"1747579212","redirect_uri":"http://keycloak.openk9.localhost:8081/admin/master/console/","state":"cac15d09-592a-4208-8ead-a93e9cd261ed","code_challenge":"PtmKns1vZufkKF1Z1npsjmd8gBEKnzIsTthUzHsIL90"}}',	'local',	'local',	3),
        ('b47853cc-d92e-4c3d-866a-8268769b2f19',	'437140a9-297d-4dff-8873-0e5eb6a7e515',	'0',	1747580664,	'{"authMethod":"openid-connect","redirectUri":"http://demo.openk9.localhost/admin/document-type-templates","notes":{"clientId":"437140a9-297d-4dff-8873-0e5eb6a7e515","iss":"http://3.0.0-keycloak.openk9.localhost:8081/realms/openk9","startedAt":"1747579819","response_type":"code","level-of-authentication":"-1","code_challenge_method":"S256","nonce":"cebbea13-c619-43ec-8e55-177e28c08a57","response_mode":"fragment","scope":"openid","userSessionStartedAt":"1747579819","redirect_uri":"http://demo.openk9.localhost/admin/document-type-templates","state":"ab363571-954b-49bb-b315-92edec1fb479","code_challenge":"F4123_jquIzcGf06AUmiqg92UqgmIvRsRJ8rxUMPAyo","SSO_AUTH":"true"}}',	'local',	'local',	4);

        CREATE TABLE "public"."offline_user_session" (
            "user_session_id" character varying(36) NOT NULL,
            "user_id" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "created_on" integer NOT NULL,
            "offline_flag" character varying(4) NOT NULL,
            "data" text,
            "last_session_refresh" integer DEFAULT '0' NOT NULL,
            "broker_session_id" character varying(1024),
            "version" integer DEFAULT '0',
            CONSTRAINT "constraint_offl_us_ses_pk2" PRIMARY KEY ("user_session_id", "offline_flag")
        ) WITH (oids = false);

        CREATE INDEX idx_offline_uss_by_user ON public.offline_user_session USING btree (user_id, realm_id, offline_flag);

        CREATE INDEX idx_offline_uss_by_last_session_refresh ON public.offline_user_session USING btree (realm_id, offline_flag, last_session_refresh);

        CREATE INDEX idx_offline_uss_by_broker_session_id ON public.offline_user_session USING btree (broker_session_id, realm_id);

        INSERT INTO "offline_user_session" ("user_session_id", "user_id", "realm_id", "created_on", "offline_flag", "data", "last_session_refresh", "broker_session_id", "version") VALUES
        ('fefc982b-20f7-4a80-825d-608a2b92ba8d',	'819823be-1c8c-4da1-a247-71524f8c5539',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	1747579212,	'0',	'{"ipAddress":"172.18.0.1","authMethod":"openid-connect","rememberMe":false,"started":0,"notes":{"KC_DEVICE_NOTE":"eyJpcEFkZHJlc3MiOiIxNzIuMTguMC4xIiwib3MiOiJMaW51eCIsIm9zVmVyc2lvbiI6IlVua25vd24iLCJicm93c2VyIjoiQ2hyb21lLzEzNS4wLjAiLCJkZXZpY2UiOiJPdGhlciIsImxhc3RBY2Nlc3MiOjAsIm1vYmlsZSI6ZmFsc2V9","AUTH_TIME":"1747579212","authenticators-completed":"{\"8083570b-bff3-4f96-86fe-c983da2fca5e\":1747579212}"},"state":"LOGGED_IN"}',	1747579380,	NULL,	3),
        ('b47853cc-d92e-4c3d-866a-8268769b2f19',	'5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	1747579819,	'0',	'{"ipAddress":"172.18.0.1","authMethod":"openid-connect","rememberMe":false,"started":0,"notes":{"KC_DEVICE_NOTE":"eyJpcEFkZHJlc3MiOiIxNzIuMTguMC4xIiwib3MiOiJMaW51eCIsIm9zVmVyc2lvbiI6IlVua25vd24iLCJicm93c2VyIjoiQ2hyb21lLzEzNS4wLjAiLCJkZXZpY2UiOiJPdGhlciIsImxhc3RBY2Nlc3MiOjAsIm1vYmlsZSI6ZmFsc2V9","AUTH_TIME":"1747579819","authenticators-completed":"{\"4103a734-4226-4246-9532-b110fdbbebb5\":1747579807,\"e40d14fc-5920-4269-a78d-bc61337c0d00\":1747580374}"},"state":"LOGGED_IN"}',	1747580664,	NULL,	4);

        CREATE TABLE "public"."org" (
            "id" character varying(255) NOT NULL,
            "enabled" boolean NOT NULL,
            "realm_id" character varying(255) NOT NULL,
            "group_id" character varying(255) NOT NULL,
            "name" character varying(255) NOT NULL,
            "description" character varying(4000),
            "alias" character varying(255) NOT NULL,
            "redirect_url" character varying(2048),
            CONSTRAINT "ORG_pkey" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_org_name ON public.org USING btree (realm_id, name);

        CREATE UNIQUE INDEX uk_org_group ON public.org USING btree (group_id);

        CREATE UNIQUE INDEX uk_org_alias ON public.org USING btree (realm_id, alias);


        CREATE TABLE "public"."org_domain" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "verified" boolean NOT NULL,
            "org_id" character varying(255) NOT NULL,
            CONSTRAINT "ORG_DOMAIN_pkey" PRIMARY KEY ("id", "name")
        ) WITH (oids = false);

        CREATE INDEX idx_org_domain_org_id ON public.org_domain USING btree (org_id);


        CREATE TABLE "public"."policy_config" (
            "policy_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_dpc" PRIMARY KEY ("policy_id", "name")
        ) WITH (oids = false);


        CREATE TABLE "public"."protocol_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "protocol" character varying(255) NOT NULL,
            "protocol_mapper_name" character varying(255) NOT NULL,
            "client_id" character varying(36),
            "client_scope_id" character varying(36),
            CONSTRAINT "constraint_pcm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_protocol_mapper_client ON public.protocol_mapper USING btree (client_id);

        CREATE INDEX idx_clscope_protmap ON public.protocol_mapper USING btree (client_scope_id);

        INSERT INTO "protocol_mapper" ("id", "name", "protocol", "protocol_mapper_name", "client_id", "client_scope_id") VALUES
        ('0c2aef71-9103-44cf-899c-d485db12fb70',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'6e144c2e-55a1-4978-926d-e1b2979e109a',	NULL),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'46305075-83e2-4080-91ca-4f953b91868d',	NULL),
        ('53d04079-f033-458d-86f4-2c3e6b26dde3',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'bc89c033-c280-43dc-8cdb-bc269d05e524'),
        ('01499b25-dc2e-4821-8093-4c36ede6ac07',	'organization',	'saml',	'saml-organization-membership-mapper',	NULL,	'bea1a009-6cf9-4d99-b259-d3d8c3fda3f1'),
        ('eb3b9ca4-1c4b-4e1c-8c94-160631f0fc32',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'family name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'given name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'username',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'10591155-81d6-49f0-a8f5-e8087a63db62'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'email',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'd345ea8b-958d-4e7d-b2b7-acc4e463ae16'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'9798e065-09c6-43fc-97bf-c50bdfdc49e6'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'25a3c139-2ee5-4355-8438-1f297d538aee'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'25a3c139-2ee5-4355-8438-1f297d538aee'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7'),
        ('91931a3c-9151-4a62-ba9b-2d55f195efb8',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'eac7afc4-1492-4139-83ca-aa6fe3ae06c7'),
        ('0c12a259-9808-404f-a310-ebfdc3b59907',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'a2c56a67-31c9-4df1-8118-3ac8734653db'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'upn',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'9c2b9a4e-a1d0-4a21-b883-63e7c498c770'),
        ('14113fa6-4ca3-4686-919d-0b0cec7ba268',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'd5c86a99-148a-4768-bfca-b576dc02c70a'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'auth_time',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'3380a789-01be-4463-ab25-474b6c8fd663'),
        ('9d9371d2-fe5c-4632-9c23-83e9aba583b0',	'sub',	'openid-connect',	'oidc-sub-mapper',	NULL,	'3380a789-01be-4463-ab25-474b6c8fd663'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'Client ID',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'f9f30390-aaf7-4a28-ade5-ee2a1921d4b9'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'Client Host',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'f9f30390-aaf7-4a28-ade5-ee2a1921d4b9'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'Client IP Address',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'f9f30390-aaf7-4a28-ade5-ee2a1921d4b9'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'organization',	'openid-connect',	'oidc-organization-membership-mapper',	NULL,	'b3104f32-05c6-4937-ab4d-cf157a3353dd'),
        ('8a3ad5e7-05ca-4bee-a7bf-5862507339c3',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'307570d2-8840-42af-8226-8f49db789dd7',	NULL),
        ('62fb0f67-c013-489c-9aee-4e61e15c531f',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'51feec57-d3d7-4687-b90a-70518e0d0c46'),
        ('549ea64f-6a4c-4529-9db6-c4a8641670c1',	'organization',	'saml',	'saml-organization-membership-mapper',	NULL,	'57ac9c43-a988-4e5f-8f0a-bc0e3a09f280'),
        ('1bb5c512-fd61-4bff-83ae-6f25ad39ce9b',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'family name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'given name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'username',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8335dfdb-9837-4d49-9666-7a6b93a46509'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'email',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'fa1d6ea8-9cb8-49e3-a23c-d0c6adf86f56'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'52af6a76-82be-45a7-918d-48ff410082d1'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'8a4ffae4-0635-4eaf-b2da-07e9bbbde2ab'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'd8c80810-aa75-4c40-921f-25c3b5c721d3'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'd8c80810-aa75-4c40-921f-25c3b5c721d3'),
        ('467add52-362e-44e1-a127-ee5307d35e77',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'd8c80810-aa75-4c40-921f-25c3b5c721d3'),
        ('f53b4db8-14c6-47df-a20e-3e00ad74432d',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'dc95d831-220d-45e3-8086-09384d30d4f9'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'upn',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'25a8c52b-b8f3-419f-bc63-4e884fb77f2a'),
        ('3f5cd5f5-fa60-4201-9ba3-b15f6320cbc9',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'7ca1d277-d673-46fb-89ad-f31679e2a29a'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'auth_time',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'731c813d-9c7c-4cee-bd14-b0ddae633e6d'),
        ('6e28ebb5-e6df-41e2-a1e3-4b6add4f1e18',	'sub',	'openid-connect',	'oidc-sub-mapper',	NULL,	'731c813d-9c7c-4cee-bd14-b0ddae633e6d'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'Client ID',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'042672bb-0c5b-43d6-bbcc-c5aaffd1747f'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'Client Host',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'042672bb-0c5b-43d6-bbcc-c5aaffd1747f'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'Client IP Address',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'042672bb-0c5b-43d6-bbcc-c5aaffd1747f'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'organization',	'openid-connect',	'oidc-organization-membership-mapper',	NULL,	'e18b0b07-aa7f-419d-812b-ebf112027b72'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	NULL),
        ('c87e1574-5794-414c-8b85-f2a50d4ce39c',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	'86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	NULL),
        ('96175ba8-6a55-40aa-a7f7-d08e2b6dae3e',	'role list',	'saml',	'saml-role-list-mapper',	NULL,	'3128e6da-8350-4d17-9d92-825fe80b7303'),
        ('f3b90fa3-da15-4879-b86c-96df7523c8d1',	'organization',	'saml',	'saml-organization-membership-mapper',	NULL,	'af2e4cec-ac0b-4121-b053-9ff6ad8afa5e'),
        ('6b6a8179-3e23-44a7-b9e9-9b1d17070c2d',	'full name',	'openid-connect',	'oidc-full-name-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'family name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'given name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'middle name',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'nickname',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'username',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'profile',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'picture',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'website',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'gender',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'birthdate',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'zoneinfo',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'updated at',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'43547928-98c8-48cd-989b-b2eecabb8f12'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'email',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'f775ff97-aa91-4817-b493-2c7a70236ec8'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'email verified',	'openid-connect',	'oidc-usermodel-property-mapper',	NULL,	'f775ff97-aa91-4817-b493-2c7a70236ec8'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'address',	'openid-connect',	'oidc-address-mapper',	NULL,	'f62d7212-60ac-4ef9-96e5-1cc521ffe633'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'phone number',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'phone number verified',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'4a241dc2-baaa-4a8c-b605-06c0f2d83f00'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'realm roles',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'1424086c-fade-4ad5-91eb-b4717a3e148f'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'client roles',	'openid-connect',	'oidc-usermodel-client-role-mapper',	NULL,	'1424086c-fade-4ad5-91eb-b4717a3e148f'),
        ('95e65d6d-4a4d-48fa-8829-8d9c4268651f',	'audience resolve',	'openid-connect',	'oidc-audience-resolve-mapper',	NULL,	'1424086c-fade-4ad5-91eb-b4717a3e148f'),
        ('f4677fb5-3bb6-4b2d-a490-4ef63150ba88',	'allowed web origins',	'openid-connect',	'oidc-allowed-origins-mapper',	NULL,	'5dcd7251-dacb-49cc-9ae4-91f9019674ed'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'upn',	'openid-connect',	'oidc-usermodel-attribute-mapper',	NULL,	'c691f15d-a84d-4563-b053-588cd21daf1e'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'groups',	'openid-connect',	'oidc-usermodel-realm-role-mapper',	NULL,	'c691f15d-a84d-4563-b053-588cd21daf1e'),
        ('42c0423f-4faf-414e-b9f3-b136b0f34650',	'acr loa level',	'openid-connect',	'oidc-acr-mapper',	NULL,	'a401b408-9edd-4ad9-a067-55228409b062'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'auth_time',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'58f52437-bd45-449b-8fb2-e7f7281c2ec5'),
        ('42dba870-cb0e-4571-90fc-50634e141c71',	'sub',	'openid-connect',	'oidc-sub-mapper',	NULL,	'58f52437-bd45-449b-8fb2-e7f7281c2ec5'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'Client ID',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'53bf40a1-985c-4ea5-a3be-9fde41c60825'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'Client Host',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'53bf40a1-985c-4ea5-a3be-9fde41c60825'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'Client IP Address',	'openid-connect',	'oidc-usersessionmodel-note-mapper',	NULL,	'53bf40a1-985c-4ea5-a3be-9fde41c60825'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'organization',	'openid-connect',	'oidc-organization-membership-mapper',	NULL,	'174806e1-084a-42cd-b3ad-800599d4a2dd'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'locale',	'openid-connect',	'oidc-usermodel-attribute-mapper',	'a013b77f-af95-4fcd-a627-175070130683',	NULL);

        CREATE TABLE "public"."protocol_mapper_config" (
            "protocol_mapper_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_pmconfig" PRIMARY KEY ("protocol_mapper_id", "name")
        ) WITH (oids = false);

        INSERT INTO "protocol_mapper_config" ("protocol_mapper_id", "value", "name") VALUES
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'true',	'introspection.token.claim'),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'true',	'userinfo.token.claim'),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'locale',	'user.attribute'),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'true',	'id.token.claim'),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'true',	'access.token.claim'),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'locale',	'claim.name'),
        ('5b664609-6545-4b32-8f45-f9202cf47cde',	'String',	'jsonType.label'),
        ('53d04079-f033-458d-86f4-2c3e6b26dde3',	'false',	'single'),
        ('53d04079-f033-458d-86f4-2c3e6b26dde3',	'Basic',	'attribute.nameformat'),
        ('53d04079-f033-458d-86f4-2c3e6b26dde3',	'Role',	'attribute.name'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'true',	'introspection.token.claim'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'true',	'userinfo.token.claim'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'zoneinfo',	'user.attribute'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'true',	'id.token.claim'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'true',	'access.token.claim'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'zoneinfo',	'claim.name'),
        ('225aa9ff-7f8b-4f57-ab31-62a76ae0f0d6',	'String',	'jsonType.label'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'true',	'introspection.token.claim'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'true',	'userinfo.token.claim'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'birthdate',	'user.attribute'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'true',	'id.token.claim'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'true',	'access.token.claim'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'birthdate',	'claim.name'),
        ('296f2dbd-b7de-4464-a7b9-268a9ff94d35',	'String',	'jsonType.label'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'true',	'introspection.token.claim'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'true',	'userinfo.token.claim'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'firstName',	'user.attribute'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'true',	'id.token.claim'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'true',	'access.token.claim'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'given_name',	'claim.name'),
        ('2c9a395a-e833-49f0-8fa0-f19e1d02346e',	'String',	'jsonType.label'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'true',	'introspection.token.claim'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'true',	'userinfo.token.claim'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'locale',	'user.attribute'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'true',	'id.token.claim'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'true',	'access.token.claim'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'locale',	'claim.name'),
        ('5851e3ef-9e23-4190-aaf6-054e3a269bfe',	'String',	'jsonType.label'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'true',	'introspection.token.claim'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'true',	'userinfo.token.claim'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'picture',	'user.attribute'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'true',	'id.token.claim'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'true',	'access.token.claim'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'picture',	'claim.name'),
        ('6bca1142-97ed-4a37-9d59-8d057398bfda',	'String',	'jsonType.label'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'true',	'introspection.token.claim'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'true',	'userinfo.token.claim'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'profile',	'user.attribute'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'true',	'id.token.claim'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'true',	'access.token.claim'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'profile',	'claim.name'),
        ('73546ec0-3162-42ce-b288-ef64d0747256',	'String',	'jsonType.label'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'true',	'introspection.token.claim'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'true',	'userinfo.token.claim'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'lastName',	'user.attribute'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'true',	'id.token.claim'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'true',	'access.token.claim'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'family_name',	'claim.name'),
        ('7e4a6cd7-6e29-403e-ba79-da1c9014b0a8',	'String',	'jsonType.label'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'true',	'introspection.token.claim'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'true',	'userinfo.token.claim'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'nickname',	'user.attribute'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'true',	'id.token.claim'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'true',	'access.token.claim'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'nickname',	'claim.name'),
        ('a2d13125-2fd0-4b54-945f-68983411aafe',	'String',	'jsonType.label'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'true',	'introspection.token.claim'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'true',	'userinfo.token.claim'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'updatedAt',	'user.attribute'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'true',	'id.token.claim'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'true',	'access.token.claim'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'updated_at',	'claim.name'),
        ('b72ca06d-5c12-47c9-9c31-4775a29414a4',	'long',	'jsonType.label'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'true',	'introspection.token.claim'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'true',	'userinfo.token.claim'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'middleName',	'user.attribute'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'true',	'id.token.claim'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'true',	'access.token.claim'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'middle_name',	'claim.name'),
        ('bdb5ee6a-dd09-4607-bc03-46c551f0d0fe',	'String',	'jsonType.label'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'true',	'introspection.token.claim'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'true',	'userinfo.token.claim'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'gender',	'user.attribute'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'true',	'id.token.claim'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'true',	'access.token.claim'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'gender',	'claim.name'),
        ('c2052223-1234-47a0-bb86-9f21aa6528b5',	'String',	'jsonType.label'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'true',	'introspection.token.claim'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'true',	'userinfo.token.claim'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'website',	'user.attribute'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'true',	'id.token.claim'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'true',	'access.token.claim'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'website',	'claim.name'),
        ('d708af12-9e3c-4afb-af67-8741338e69cc',	'String',	'jsonType.label'),
        ('eb3b9ca4-1c4b-4e1c-8c94-160631f0fc32',	'true',	'introspection.token.claim'),
        ('eb3b9ca4-1c4b-4e1c-8c94-160631f0fc32',	'true',	'userinfo.token.claim'),
        ('eb3b9ca4-1c4b-4e1c-8c94-160631f0fc32',	'true',	'id.token.claim'),
        ('eb3b9ca4-1c4b-4e1c-8c94-160631f0fc32',	'true',	'access.token.claim'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'true',	'introspection.token.claim'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'true',	'userinfo.token.claim'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'username',	'user.attribute'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'true',	'id.token.claim'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'true',	'access.token.claim'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'preferred_username',	'claim.name'),
        ('fa412c61-750f-4cf0-adf1-252bc9b00876',	'String',	'jsonType.label'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'true',	'introspection.token.claim'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'true',	'userinfo.token.claim'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'email',	'user.attribute'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'true',	'id.token.claim'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'true',	'access.token.claim'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'email',	'claim.name'),
        ('443d4783-4956-4e50-9f19-2015f704b093',	'String',	'jsonType.label'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'true',	'introspection.token.claim'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'true',	'userinfo.token.claim'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'emailVerified',	'user.attribute'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'true',	'id.token.claim'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'true',	'access.token.claim'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'email_verified',	'claim.name'),
        ('a6553310-eaa6-4de0-8815-8ede4d876da3',	'boolean',	'jsonType.label'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'formatted',	'user.attribute.formatted'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'country',	'user.attribute.country'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'true',	'introspection.token.claim'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'postal_code',	'user.attribute.postal_code'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'true',	'userinfo.token.claim'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'street',	'user.attribute.street'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'true',	'id.token.claim'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'region',	'user.attribute.region'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'true',	'access.token.claim'),
        ('10c67689-acc6-44e3-b21b-0aec47505e4b',	'locality',	'user.attribute.locality'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'true',	'introspection.token.claim'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'true',	'userinfo.token.claim'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'phoneNumberVerified',	'user.attribute'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'true',	'id.token.claim'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'true',	'access.token.claim'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'phone_number_verified',	'claim.name'),
        ('1ee82473-8bda-4a6e-9abb-ddc8928b5883',	'boolean',	'jsonType.label'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'true',	'introspection.token.claim'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'true',	'userinfo.token.claim'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'phoneNumber',	'user.attribute'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'true',	'id.token.claim'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'true',	'access.token.claim'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'phone_number',	'claim.name'),
        ('dd67b963-ad5e-4d1e-9a31-1001be0cf361',	'String',	'jsonType.label'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'true',	'introspection.token.claim'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'true',	'multivalued'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'foo',	'user.attribute'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'true',	'access.token.claim'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'resource_access.${client_id}.roles',	'claim.name'),
        ('07007eeb-b33e-40d7-84ae-d94fd642eb0d',	'String',	'jsonType.label'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'true',	'introspection.token.claim'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'true',	'multivalued'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'foo',	'user.attribute'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'true',	'access.token.claim'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'realm_access.roles',	'claim.name'),
        ('56bfdd81-d792-42f3-b03c-efe7cf7aa8d6',	'String',	'jsonType.label'),
        ('91931a3c-9151-4a62-ba9b-2d55f195efb8',	'true',	'introspection.token.claim'),
        ('91931a3c-9151-4a62-ba9b-2d55f195efb8',	'true',	'access.token.claim'),
        ('0c12a259-9808-404f-a310-ebfdc3b59907',	'true',	'introspection.token.claim'),
        ('0c12a259-9808-404f-a310-ebfdc3b59907',	'true',	'access.token.claim'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'true',	'introspection.token.claim'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'true',	'multivalued'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'foo',	'user.attribute'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'true',	'id.token.claim'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'true',	'access.token.claim'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'groups',	'claim.name'),
        ('31b3218e-976f-4598-adad-808c5ad1c865',	'String',	'jsonType.label'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'true',	'introspection.token.claim'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'true',	'userinfo.token.claim'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'username',	'user.attribute'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'true',	'id.token.claim'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'true',	'access.token.claim'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'upn',	'claim.name'),
        ('f1e137f3-dea0-47bf-abbd-ef2e27827da4',	'String',	'jsonType.label'),
        ('14113fa6-4ca3-4686-919d-0b0cec7ba268',	'true',	'introspection.token.claim'),
        ('14113fa6-4ca3-4686-919d-0b0cec7ba268',	'true',	'id.token.claim'),
        ('14113fa6-4ca3-4686-919d-0b0cec7ba268',	'true',	'access.token.claim'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'AUTH_TIME',	'user.session.note'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'true',	'introspection.token.claim'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'true',	'id.token.claim'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'true',	'access.token.claim'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'auth_time',	'claim.name'),
        ('226eab1f-145a-4e5f-a550-846ac261e6d6',	'long',	'jsonType.label'),
        ('9d9371d2-fe5c-4632-9c23-83e9aba583b0',	'true',	'introspection.token.claim'),
        ('9d9371d2-fe5c-4632-9c23-83e9aba583b0',	'true',	'access.token.claim'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'clientAddress',	'user.session.note'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'true',	'introspection.token.claim'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'true',	'id.token.claim'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'true',	'access.token.claim'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'clientAddress',	'claim.name'),
        ('6caea699-2358-4851-a228-1999c9ad71ac',	'String',	'jsonType.label'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'client_id',	'user.session.note'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'true',	'introspection.token.claim'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'true',	'id.token.claim'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'true',	'access.token.claim'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'client_id',	'claim.name'),
        ('93837a46-6b8e-446e-9d65-359542321412',	'String',	'jsonType.label'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'clientHost',	'user.session.note'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'true',	'introspection.token.claim'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'true',	'id.token.claim'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'true',	'access.token.claim'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'clientHost',	'claim.name'),
        ('f45c5abd-7424-484f-85f1-fb2f5d653e86',	'String',	'jsonType.label'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'true',	'introspection.token.claim'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'true',	'multivalued'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'true',	'id.token.claim'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'true',	'access.token.claim'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'organization',	'claim.name'),
        ('ae4d8367-e661-43f2-8779-bbca867aac1e',	'String',	'jsonType.label'),
        ('62fb0f67-c013-489c-9aee-4e61e15c531f',	'false',	'single'),
        ('62fb0f67-c013-489c-9aee-4e61e15c531f',	'Basic',	'attribute.nameformat'),
        ('62fb0f67-c013-489c-9aee-4e61e15c531f',	'Role',	'attribute.name'),
        ('1bb5c512-fd61-4bff-83ae-6f25ad39ce9b',	'true',	'introspection.token.claim'),
        ('1bb5c512-fd61-4bff-83ae-6f25ad39ce9b',	'true',	'userinfo.token.claim'),
        ('1bb5c512-fd61-4bff-83ae-6f25ad39ce9b',	'true',	'id.token.claim'),
        ('1bb5c512-fd61-4bff-83ae-6f25ad39ce9b',	'true',	'access.token.claim'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'true',	'introspection.token.claim'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'true',	'userinfo.token.claim'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'nickname',	'user.attribute'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'true',	'id.token.claim'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'true',	'access.token.claim'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'nickname',	'claim.name'),
        ('33875cca-ef3f-4b8a-934b-ad2276a36fae',	'String',	'jsonType.label'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'true',	'introspection.token.claim'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'true',	'userinfo.token.claim'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'gender',	'user.attribute'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'true',	'id.token.claim'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'true',	'access.token.claim'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'gender',	'claim.name'),
        ('3a36b3f7-83d7-495f-a16b-ada22dbf39cd',	'String',	'jsonType.label'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'true',	'introspection.token.claim'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'true',	'userinfo.token.claim'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'updatedAt',	'user.attribute'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'true',	'id.token.claim'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'true',	'access.token.claim'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'updated_at',	'claim.name'),
        ('3cf6e96c-fdd6-4213-b913-299fc7bfc863',	'long',	'jsonType.label'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'true',	'introspection.token.claim'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'true',	'userinfo.token.claim'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'username',	'user.attribute'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'true',	'id.token.claim'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'true',	'access.token.claim'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'preferred_username',	'claim.name'),
        ('43a189ef-5333-4cbe-b00f-d386da3be455',	'String',	'jsonType.label'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'true',	'introspection.token.claim'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'true',	'userinfo.token.claim'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'website',	'user.attribute'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'true',	'id.token.claim'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'true',	'access.token.claim'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'website',	'claim.name'),
        ('54a2fa36-d43b-4c64-91c5-02144e0a61c2',	'String',	'jsonType.label'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'true',	'introspection.token.claim'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'true',	'userinfo.token.claim'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'profile',	'user.attribute'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'true',	'id.token.claim'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'true',	'access.token.claim'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'profile',	'claim.name'),
        ('b9c8b2df-2928-425a-ace2-34b4f7c985f8',	'String',	'jsonType.label'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'true',	'introspection.token.claim'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'true',	'userinfo.token.claim'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'picture',	'user.attribute'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'true',	'id.token.claim'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'true',	'access.token.claim'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'picture',	'claim.name'),
        ('baada9eb-c69e-451c-ad2d-f52fa27f66de',	'String',	'jsonType.label'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'true',	'introspection.token.claim'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'true',	'userinfo.token.claim'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'zoneinfo',	'user.attribute'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'true',	'id.token.claim'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'true',	'access.token.claim'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'zoneinfo',	'claim.name'),
        ('c25ac13e-e16b-46d5-87ab-1524a2b92367',	'String',	'jsonType.label'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'true',	'introspection.token.claim'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'true',	'userinfo.token.claim'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'birthdate',	'user.attribute'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'true',	'id.token.claim'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'true',	'access.token.claim'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'birthdate',	'claim.name'),
        ('cd6e9bf7-7930-406c-a7bd-c30340f7bbac',	'String',	'jsonType.label'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'true',	'introspection.token.claim'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'true',	'userinfo.token.claim'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'locale',	'user.attribute'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'true',	'id.token.claim'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'true',	'access.token.claim'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'locale',	'claim.name'),
        ('e7f1de94-3a6e-48e6-9670-26aedfd706e6',	'String',	'jsonType.label'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'true',	'introspection.token.claim'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'true',	'userinfo.token.claim'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'middleName',	'user.attribute'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'true',	'id.token.claim'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'true',	'access.token.claim'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'middle_name',	'claim.name'),
        ('f3716b3c-d88a-4bf1-9972-f908f2b347ab',	'String',	'jsonType.label'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'true',	'introspection.token.claim'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'true',	'userinfo.token.claim'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'lastName',	'user.attribute'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'true',	'id.token.claim'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'true',	'access.token.claim'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'family_name',	'claim.name'),
        ('f8566215-f54d-4ddb-8909-48bf34bc3f05',	'String',	'jsonType.label'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'true',	'introspection.token.claim'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'true',	'userinfo.token.claim'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'firstName',	'user.attribute'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'true',	'id.token.claim'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'true',	'access.token.claim'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'given_name',	'claim.name'),
        ('f936c220-e59c-49b2-a492-1b3e45bd528a',	'String',	'jsonType.label'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'true',	'introspection.token.claim'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'true',	'userinfo.token.claim'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'email',	'user.attribute'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'true',	'id.token.claim'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'true',	'access.token.claim'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'email',	'claim.name'),
        ('2ebf2d28-2189-4991-b52d-8429d2392c50',	'String',	'jsonType.label'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'true',	'introspection.token.claim'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'true',	'userinfo.token.claim'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'emailVerified',	'user.attribute'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'true',	'id.token.claim'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'true',	'access.token.claim'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'email_verified',	'claim.name'),
        ('8f6c9ebf-3d72-4743-b3c4-d2a003c914bb',	'boolean',	'jsonType.label'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'formatted',	'user.attribute.formatted'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'country',	'user.attribute.country'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'true',	'introspection.token.claim'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'postal_code',	'user.attribute.postal_code'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'true',	'userinfo.token.claim'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'street',	'user.attribute.street'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'true',	'id.token.claim'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'region',	'user.attribute.region'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'true',	'access.token.claim'),
        ('7a84cb8f-0d5d-4373-b1b9-2f7c16eb6041',	'locality',	'user.attribute.locality'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'true',	'introspection.token.claim'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'true',	'userinfo.token.claim'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'phoneNumberVerified',	'user.attribute'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'true',	'id.token.claim'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'true',	'access.token.claim'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'phone_number_verified',	'claim.name'),
        ('40104475-887d-4eee-8e95-1ddf9d6b56b5',	'boolean',	'jsonType.label'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'true',	'introspection.token.claim'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'true',	'userinfo.token.claim'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'phoneNumber',	'user.attribute'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'true',	'id.token.claim'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'true',	'access.token.claim'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'phone_number',	'claim.name'),
        ('ee45631f-e350-4b30-b9fc-542a55b32550',	'String',	'jsonType.label'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'true',	'introspection.token.claim'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'true',	'multivalued'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'foo',	'user.attribute'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'true',	'access.token.claim'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'realm_access.roles',	'claim.name'),
        ('03a0ed26-4dc0-457f-ac1a-e62f84545085',	'String',	'jsonType.label'),
        ('467add52-362e-44e1-a127-ee5307d35e77',	'true',	'introspection.token.claim'),
        ('467add52-362e-44e1-a127-ee5307d35e77',	'true',	'access.token.claim'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'true',	'introspection.token.claim'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'true',	'multivalued'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'foo',	'user.attribute'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'true',	'access.token.claim'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'resource_access.${client_id}.roles',	'claim.name'),
        ('c235c741-089f-4a8f-aa93-0d4ea1478718',	'String',	'jsonType.label'),
        ('f53b4db8-14c6-47df-a20e-3e00ad74432d',	'true',	'introspection.token.claim'),
        ('f53b4db8-14c6-47df-a20e-3e00ad74432d',	'true',	'access.token.claim'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'true',	'introspection.token.claim'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'true',	'multivalued'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'foo',	'user.attribute'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'true',	'id.token.claim'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'true',	'access.token.claim'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'groups',	'claim.name'),
        ('a5f0871c-1eea-468a-9893-5cfb659fe952',	'String',	'jsonType.label'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'true',	'introspection.token.claim'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'true',	'userinfo.token.claim'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'username',	'user.attribute'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'true',	'id.token.claim'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'true',	'access.token.claim'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'upn',	'claim.name'),
        ('af4fd3e1-2701-4f77-9cc4-0567f9b996d6',	'String',	'jsonType.label'),
        ('3f5cd5f5-fa60-4201-9ba3-b15f6320cbc9',	'true',	'introspection.token.claim'),
        ('3f5cd5f5-fa60-4201-9ba3-b15f6320cbc9',	'true',	'id.token.claim'),
        ('3f5cd5f5-fa60-4201-9ba3-b15f6320cbc9',	'true',	'access.token.claim'),
        ('6e28ebb5-e6df-41e2-a1e3-4b6add4f1e18',	'true',	'introspection.token.claim'),
        ('6e28ebb5-e6df-41e2-a1e3-4b6add4f1e18',	'true',	'access.token.claim'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'AUTH_TIME',	'user.session.note'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'true',	'introspection.token.claim'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'true',	'id.token.claim'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'true',	'access.token.claim'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'auth_time',	'claim.name'),
        ('c4308e5e-2883-42f3-b567-4cca458bc7fb',	'long',	'jsonType.label'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'client_id',	'user.session.note'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'true',	'introspection.token.claim'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'true',	'id.token.claim'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'true',	'access.token.claim'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'client_id',	'claim.name'),
        ('d84d7f7b-ce37-4fdf-98b3-04035d769296',	'String',	'jsonType.label'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'clientHost',	'user.session.note'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'true',	'introspection.token.claim'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'true',	'id.token.claim'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'true',	'access.token.claim'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'clientHost',	'claim.name'),
        ('dda7df94-6aa7-470a-8c08-530b3faf0a73',	'String',	'jsonType.label'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'clientAddress',	'user.session.note'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'true',	'introspection.token.claim'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'true',	'id.token.claim'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'true',	'access.token.claim'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'clientAddress',	'claim.name'),
        ('ed177cc6-ae23-4599-9961-09f4cbef22ff',	'String',	'jsonType.label'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'true',	'introspection.token.claim'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'true',	'multivalued'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'true',	'id.token.claim'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'true',	'access.token.claim'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'organization',	'claim.name'),
        ('bef19de9-0fe2-4272-8dfb-27653c4041dc',	'String',	'jsonType.label'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'true',	'introspection.token.claim'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'true',	'userinfo.token.claim'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'locale',	'user.attribute'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'true',	'id.token.claim'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'true',	'access.token.claim'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'locale',	'claim.name'),
        ('4eaedf38-936d-4823-90e7-ffae2a481bab',	'String',	'jsonType.label'),
        ('96175ba8-6a55-40aa-a7f7-d08e2b6dae3e',	'false',	'single'),
        ('96175ba8-6a55-40aa-a7f7-d08e2b6dae3e',	'Basic',	'attribute.nameformat'),
        ('96175ba8-6a55-40aa-a7f7-d08e2b6dae3e',	'Role',	'attribute.name'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'true',	'introspection.token.claim'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'true',	'userinfo.token.claim'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'middleName',	'user.attribute'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'true',	'id.token.claim'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'true',	'access.token.claim'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'middle_name',	'claim.name'),
        ('09a236e9-144c-4d4f-88d3-b724053b906f',	'String',	'jsonType.label'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'true',	'introspection.token.claim'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'true',	'userinfo.token.claim'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'picture',	'user.attribute'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'true',	'id.token.claim'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'true',	'access.token.claim'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'picture',	'claim.name'),
        ('12ffe159-48f3-4546-9e4a-15105d335450',	'String',	'jsonType.label'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'true',	'introspection.token.claim'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'true',	'userinfo.token.claim'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'zoneinfo',	'user.attribute'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'true',	'id.token.claim'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'true',	'access.token.claim'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'zoneinfo',	'claim.name'),
        ('3f5172ec-2fe6-4422-85be-570d31b03460',	'String',	'jsonType.label'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'true',	'introspection.token.claim'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'true',	'userinfo.token.claim'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'gender',	'user.attribute'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'true',	'id.token.claim'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'true',	'access.token.claim'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'gender',	'claim.name'),
        ('40d88353-ac25-4711-9dfe-9a487ffd4fe3',	'String',	'jsonType.label'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'true',	'introspection.token.claim'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'true',	'userinfo.token.claim'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'website',	'user.attribute'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'true',	'id.token.claim'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'true',	'access.token.claim'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'website',	'claim.name'),
        ('45788536-eecb-480c-904f-729bfbfa796a',	'String',	'jsonType.label'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'true',	'introspection.token.claim'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'true',	'userinfo.token.claim'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'updatedAt',	'user.attribute'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'true',	'id.token.claim'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'true',	'access.token.claim'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'updated_at',	'claim.name'),
        ('58f89016-a245-4ce1-a3e4-798d4eb9058f',	'long',	'jsonType.label'),
        ('6b6a8179-3e23-44a7-b9e9-9b1d17070c2d',	'true',	'introspection.token.claim'),
        ('6b6a8179-3e23-44a7-b9e9-9b1d17070c2d',	'true',	'userinfo.token.claim'),
        ('6b6a8179-3e23-44a7-b9e9-9b1d17070c2d',	'true',	'id.token.claim'),
        ('6b6a8179-3e23-44a7-b9e9-9b1d17070c2d',	'true',	'access.token.claim'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'true',	'introspection.token.claim'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'true',	'userinfo.token.claim'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'username',	'user.attribute'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'true',	'id.token.claim'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'true',	'access.token.claim'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'preferred_username',	'claim.name'),
        ('737d8329-886e-4584-8249-f30feb0e0563',	'String',	'jsonType.label'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'true',	'introspection.token.claim'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'true',	'userinfo.token.claim'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'birthdate',	'user.attribute'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'true',	'id.token.claim'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'true',	'access.token.claim'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'birthdate',	'claim.name'),
        ('9a12048d-b45d-4325-9750-9fe4ddef8172',	'String',	'jsonType.label'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'true',	'introspection.token.claim'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'true',	'userinfo.token.claim'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'lastName',	'user.attribute'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'true',	'id.token.claim'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'true',	'access.token.claim'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'family_name',	'claim.name'),
        ('9b5b525f-da81-4469-b289-46caea29a014',	'String',	'jsonType.label'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'true',	'introspection.token.claim'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'true',	'userinfo.token.claim'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'nickname',	'user.attribute'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'true',	'id.token.claim'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'true',	'access.token.claim'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'nickname',	'claim.name'),
        ('a6e22968-238f-4dae-a30f-36bde2abbefd',	'String',	'jsonType.label'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'true',	'introspection.token.claim'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'true',	'userinfo.token.claim'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'locale',	'user.attribute'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'true',	'id.token.claim'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'true',	'access.token.claim'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'locale',	'claim.name'),
        ('cffd27cc-7598-45cd-8357-bd228ff4e765',	'String',	'jsonType.label'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'true',	'introspection.token.claim'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'true',	'userinfo.token.claim'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'profile',	'user.attribute'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'true',	'id.token.claim'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'true',	'access.token.claim'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'profile',	'claim.name'),
        ('df8b6bed-1c4c-4767-9c7d-e7d2152e0c1a',	'String',	'jsonType.label'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'true',	'introspection.token.claim'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'true',	'userinfo.token.claim'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'firstName',	'user.attribute'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'true',	'id.token.claim'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'true',	'access.token.claim'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'given_name',	'claim.name'),
        ('f3606fd9-2772-435a-93a7-15acdb6955f9',	'String',	'jsonType.label'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'true',	'introspection.token.claim'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'true',	'userinfo.token.claim'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'emailVerified',	'user.attribute'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'true',	'id.token.claim'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'true',	'access.token.claim'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'email_verified',	'claim.name'),
        ('54306a35-4af5-4ae4-b56b-e500d4a26657',	'boolean',	'jsonType.label'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'true',	'introspection.token.claim'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'true',	'userinfo.token.claim'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'email',	'user.attribute'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'true',	'id.token.claim'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'true',	'access.token.claim'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'email',	'claim.name'),
        ('7050aa9f-efa0-4cac-82cd-f802a46fdafa',	'String',	'jsonType.label'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'formatted',	'user.attribute.formatted'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'country',	'user.attribute.country'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'true',	'introspection.token.claim'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'postal_code',	'user.attribute.postal_code'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'true',	'userinfo.token.claim'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'street',	'user.attribute.street'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'true',	'id.token.claim'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'region',	'user.attribute.region'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'true',	'access.token.claim'),
        ('854108f2-c2e8-41f3-8dee-7b2a8284c9d2',	'locality',	'user.attribute.locality'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'true',	'introspection.token.claim'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'true',	'userinfo.token.claim'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'phoneNumberVerified',	'user.attribute'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'true',	'id.token.claim'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'true',	'access.token.claim'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'phone_number_verified',	'claim.name'),
        ('1357b93b-5fb7-4699-b589-783654ea0da1',	'boolean',	'jsonType.label'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'true',	'introspection.token.claim'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'true',	'userinfo.token.claim'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'phoneNumber',	'user.attribute'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'true',	'id.token.claim'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'true',	'access.token.claim'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'phone_number',	'claim.name'),
        ('b9a800fa-01f9-4b5a-a07e-01b3edd43857',	'String',	'jsonType.label'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'true',	'introspection.token.claim'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'true',	'multivalued'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'foo',	'user.attribute'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'true',	'access.token.claim'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'realm_access.roles',	'claim.name'),
        ('8d4e8900-f016-40d4-b378-b745fd1cba0a',	'String',	'jsonType.label'),
        ('95e65d6d-4a4d-48fa-8829-8d9c4268651f',	'true',	'introspection.token.claim'),
        ('95e65d6d-4a4d-48fa-8829-8d9c4268651f',	'true',	'access.token.claim'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'true',	'introspection.token.claim'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'true',	'multivalued'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'foo',	'user.attribute'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'true',	'access.token.claim'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'resource_access.${client_id}.roles',	'claim.name'),
        ('c5c42a41-823c-4c1e-83cf-3e7df0ae2ecc',	'String',	'jsonType.label'),
        ('f4677fb5-3bb6-4b2d-a490-4ef63150ba88',	'true',	'introspection.token.claim'),
        ('f4677fb5-3bb6-4b2d-a490-4ef63150ba88',	'true',	'access.token.claim'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'true',	'introspection.token.claim'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'true',	'userinfo.token.claim'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'username',	'user.attribute'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'true',	'id.token.claim'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'true',	'access.token.claim'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'upn',	'claim.name'),
        ('330694b5-6e55-4dbb-a057-5785852ed087',	'String',	'jsonType.label'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'true',	'introspection.token.claim'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'true',	'multivalued'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'foo',	'user.attribute'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'true',	'id.token.claim'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'true',	'access.token.claim'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'groups',	'claim.name'),
        ('e24b5be5-58f5-4171-bbe1-aeac62089928',	'String',	'jsonType.label'),
        ('42c0423f-4faf-414e-b9f3-b136b0f34650',	'true',	'introspection.token.claim'),
        ('42c0423f-4faf-414e-b9f3-b136b0f34650',	'true',	'id.token.claim'),
        ('42c0423f-4faf-414e-b9f3-b136b0f34650',	'true',	'access.token.claim'),
        ('42dba870-cb0e-4571-90fc-50634e141c71',	'true',	'introspection.token.claim'),
        ('42dba870-cb0e-4571-90fc-50634e141c71',	'true',	'access.token.claim'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'AUTH_TIME',	'user.session.note'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'true',	'introspection.token.claim'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'true',	'id.token.claim'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'true',	'access.token.claim'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'auth_time',	'claim.name'),
        ('b2607bbf-a307-452c-9431-31d66b223e2e',	'long',	'jsonType.label'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'clientAddress',	'user.session.note'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'true',	'introspection.token.claim'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'true',	'id.token.claim'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'true',	'access.token.claim'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'clientAddress',	'claim.name'),
        ('1c764da4-b643-4f6d-8185-2fb1c650874b',	'String',	'jsonType.label'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'clientHost',	'user.session.note'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'true',	'introspection.token.claim'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'true',	'id.token.claim'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'true',	'access.token.claim'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'clientHost',	'claim.name'),
        ('4069cd1f-6293-4d6d-9a6f-c0177a1a1111',	'String',	'jsonType.label'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'client_id',	'user.session.note'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'true',	'introspection.token.claim'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'true',	'id.token.claim'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'true',	'access.token.claim'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'client_id',	'claim.name'),
        ('4b0a190a-4b8f-4c9b-b04e-7176f8e07dca',	'String',	'jsonType.label'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'true',	'introspection.token.claim'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'true',	'multivalued'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'true',	'id.token.claim'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'true',	'access.token.claim'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'organization',	'claim.name'),
        ('59b15773-bd7a-4c41-b379-75beb55d00a2',	'String',	'jsonType.label'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'true',	'introspection.token.claim'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'true',	'userinfo.token.claim'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'locale',	'user.attribute'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'true',	'id.token.claim'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'true',	'access.token.claim'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'locale',	'claim.name'),
        ('06f7d88d-a98c-469d-a538-082027feecdb',	'String',	'jsonType.label');

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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_orvsdmla56612eaefiq6wl5oi ON public.realm USING btree (name);

        CREATE INDEX idx_realm_master_adm_cli ON public.realm USING btree (master_admin_client);

        INSERT INTO "realm" ("id", "access_code_lifespan", "user_action_lifespan", "access_token_lifespan", "account_theme", "admin_theme", "email_theme", "enabled", "events_enabled", "events_expiration", "login_theme", "name", "not_before", "password_policy", "registration_allowed", "remember_me", "reset_password_allowed", "social", "ssl_required", "sso_idle_timeout", "sso_max_lifespan", "update_profile_on_soc_login", "verify_email", "master_admin_client", "login_lifespan", "internationalization_enabled", "default_locale", "reg_email_as_username", "admin_events_enabled", "admin_events_details_enabled", "edit_username_allowed", "otp_policy_counter", "otp_policy_window", "otp_policy_period", "otp_policy_digits", "otp_policy_alg", "otp_policy_type", "browser_flow", "registration_flow", "direct_grant_flow", "reset_credentials_flow", "client_auth_flow", "offline_session_idle_timeout", "revoke_refresh_token", "access_token_life_implicit", "login_with_email_allowed", "duplicate_emails_allowed", "docker_auth_flow", "refresh_token_max_reuse", "allow_user_managed_access", "sso_max_lifespan_remember_me", "sso_idle_timeout_remember_me", "default_role") VALUES
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	60,	300,	300,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'tenant-manager',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'4f28172b-c234-4831-ad02-a50b56fb59ac',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'8fa14c62-098d-4d87-9d56-e27a3525de8b',	'd0fb91e5-3652-4cc2-aeb6-0e0bf1de4662',	'3d475867-13dc-4b82-87f7-49da51efa0eb',	'4510b8e8-ef1f-4e30-8d82-5f42ec0ab318',	'2ddb5ffb-5457-48e7-b8fa-0a53a5ce83b6',	2592000,	'0',	900,	'1',	'0',	'56d82c54-a5e3-4eb5-bfd7-8fb3c9cadeb9',	0,	'0',	0,	0,	'88c38d21-732e-40b0-942d-7392520f0ef4'),
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	60,	300,	60,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'master',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'f0d8c14e-1c08-4aaa-ac61-67c4e4f1d4c5',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'7b3202ae-cb95-4fae-9863-3cc48379d3e1',	'1f822451-7d52-4f8f-98ea-adc6dab77176',	'c665881e-3b45-4b9f-a390-06b82530faab',	'02bc64c5-b9dc-4d3a-879e-72ddfeb45eaf',	'76b4f0c5-9457-441d-ae0c-9a0d945ba5f3',	2592000,	'0',	900,	'1',	'0',	'3cbf3454-3a95-44d3-9ced-3c51e5882669',	0,	'0',	0,	0,	'1a685a27-f87d-48c9-882f-0c81f6a7e412'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	60,	300,	300,	NULL,	NULL,	NULL,	'1',	'0',	0,	NULL,	'openk9',	0,	NULL,	'0',	'0',	'0',	'0',	'EXTERNAL',	1800,	36000,	'0',	'0',	'5c4e5129-99ef-4dab-9ad8-a161dba408db',	1800,	'0',	NULL,	'0',	'0',	'0',	'0',	0,	1,	30,	6,	'HmacSHA1',	'totp',	'd553321a-ffd5-439a-88de-6c0e6b889ed1',	'aae7828a-8732-4d18-a944-e064a390a16d',	'9cd24228-ad2b-41a5-a179-79ff571d3f4e',	'14414cbc-ef1d-481c-b5bf-a9da2366671c',	'6b1855bd-dc9d-4589-acda-04fcf4e33d33',	2592000,	'0',	900,	'1',	'0',	'a56e9328-65ce-4869-9ba2-16807cd25c7b',	0,	'0',	0,	0,	'08181cbd-ccaf-4ff4-98ed-daa5aa59e956');

        CREATE TABLE "public"."realm_attribute" (
            "name" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            "value" text,
            CONSTRAINT "constraint_9" PRIMARY KEY ("name", "realm_id")
        ) WITH (oids = false);

        CREATE INDEX idx_realm_attr_realm ON public.realm_attribute USING btree (realm_id);

        INSERT INTO "realm_attribute" ("name", "realm_id", "value") VALUES
        ('_browser_header.contentSecurityPolicyReportOnly',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	''),
        ('_browser_header.xContentTypeOptions',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'nosniff'),
        ('_browser_header.referrerPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'no-referrer'),
        ('_browser_header.xRobotsTag',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'none'),
        ('_browser_header.xFrameOptions',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'false'),
        ('permanentLockout',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'false'),
        ('maxTemporaryLockouts',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0'),
        ('bruteForceStrategy',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'MULTIPLE'),
        ('maxFailureWaitSeconds',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'900'),
        ('minimumQuickLoginWaitSeconds',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'60'),
        ('waitIncrementSeconds',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'60'),
        ('quickLoginCheckMilliSeconds',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1000'),
        ('maxDeltaTimeSeconds',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'43200'),
        ('failureFactor',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'30'),
        ('realmReusableOtpCode',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'false'),
        ('firstBrokerLoginFlowId',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'cf18ec32-d1bc-4541-ab7f-c3d76a51c8cd'),
        ('displayName',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'Keycloak'),
        ('displayNameHtml',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'<div class="kc-logo-text"><span>Keycloak</span></div>'),
        ('defaultSignatureAlgorithm',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'false'),
        ('offlineSessionMaxLifespan',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'5184000'),
        ('_browser_header.contentSecurityPolicyReportOnly',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	''),
        ('_browser_header.xContentTypeOptions',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'nosniff'),
        ('_browser_header.referrerPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'no-referrer'),
        ('_browser_header.xRobotsTag',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'none'),
        ('_browser_header.xFrameOptions',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'false'),
        ('permanentLockout',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'false'),
        ('maxTemporaryLockouts',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0'),
        ('bruteForceStrategy',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'MULTIPLE'),
        ('maxFailureWaitSeconds',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'900'),
        ('minimumQuickLoginWaitSeconds',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'60'),
        ('waitIncrementSeconds',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'60'),
        ('quickLoginCheckMilliSeconds',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1000'),
        ('maxDeltaTimeSeconds',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'43200'),
        ('failureFactor',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'30'),
        ('realmReusableOtpCode',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'false'),
        ('defaultSignatureAlgorithm',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'false'),
        ('offlineSessionMaxLifespan',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'5184000'),
        ('actionTokenGeneratedByAdminLifespan',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'43200'),
        ('actionTokenGeneratedByUserLifespan',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'300'),
        ('oauth2DeviceCodeLifespan',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'600'),
        ('oauth2DevicePollingInterval',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'5'),
        ('webAuthnPolicyRpEntityName',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithms',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ES256,RS256'),
        ('webAuthnPolicyRpId',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	''),
        ('webAuthnPolicyAttestationConveyancePreference',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachment',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyRequireResidentKey',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirement',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyCreateTimeout',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegister',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'false'),
        ('webAuthnPolicyRpEntityNamePasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithmsPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ES256,RS256'),
        ('webAuthnPolicyRpIdPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	''),
        ('webAuthnPolicyAttestationConveyancePreferencePasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachmentPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyRequireResidentKeyPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirementPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'not specified'),
        ('webAuthnPolicyCreateTimeoutPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'false'),
        ('cibaBackchannelTokenDeliveryMode',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'poll'),
        ('cibaExpiresIn',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'120'),
        ('cibaInterval',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'5'),
        ('cibaAuthRequestedUserHint',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'login_hint'),
        ('parRequestUriLifespan',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'60'),
        ('firstBrokerLoginFlowId',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'ea6ba12a-e11d-4d89-a19c-26961b4abea4'),
        ('_browser_header.contentSecurityPolicyReportOnly',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	''),
        ('_browser_header.xContentTypeOptions',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'nosniff'),
        ('_browser_header.referrerPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'no-referrer'),
        ('_browser_header.xRobotsTag',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'none'),
        ('_browser_header.xFrameOptions',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'SAMEORIGIN'),
        ('_browser_header.contentSecurityPolicy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'frame-src ''self''; frame-ancestors ''self''; object-src ''none'';'),
        ('_browser_header.xXSSProtection',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1; mode=block'),
        ('_browser_header.strictTransportSecurity',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'max-age=31536000; includeSubDomains'),
        ('bruteForceProtected',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'false'),
        ('permanentLockout',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'false'),
        ('maxTemporaryLockouts',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0'),
        ('bruteForceStrategy',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'MULTIPLE'),
        ('maxFailureWaitSeconds',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'900'),
        ('minimumQuickLoginWaitSeconds',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'60'),
        ('waitIncrementSeconds',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'60'),
        ('quickLoginCheckMilliSeconds',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1000'),
        ('maxDeltaTimeSeconds',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'43200'),
        ('failureFactor',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'30'),
        ('realmReusableOtpCode',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'false'),
        ('defaultSignatureAlgorithm',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'RS256'),
        ('offlineSessionMaxLifespanEnabled',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'false'),
        ('offlineSessionMaxLifespan',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'5184000'),
        ('actionTokenGeneratedByAdminLifespan',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'43200'),
        ('actionTokenGeneratedByUserLifespan',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'300'),
        ('oauth2DeviceCodeLifespan',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'600'),
        ('oauth2DevicePollingInterval',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'5'),
        ('webAuthnPolicyRpEntityName',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithms',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'ES256,RS256'),
        ('webAuthnPolicyRpId',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	''),
        ('webAuthnPolicyAttestationConveyancePreference',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachment',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyRequireResidentKey',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirement',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyCreateTimeout',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegister',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'false'),
        ('webAuthnPolicyRpEntityNamePasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'keycloak'),
        ('webAuthnPolicySignatureAlgorithmsPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'ES256,RS256'),
        ('webAuthnPolicyRpIdPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	''),
        ('webAuthnPolicyAttestationConveyancePreferencePasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyAuthenticatorAttachmentPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyRequireResidentKeyPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyUserVerificationRequirementPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'not specified'),
        ('webAuthnPolicyCreateTimeoutPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0'),
        ('webAuthnPolicyAvoidSameAuthenticatorRegisterPasswordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'false'),
        ('cibaBackchannelTokenDeliveryMode',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'poll'),
        ('cibaExpiresIn',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'120'),
        ('cibaInterval',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'5'),
        ('cibaAuthRequestedUserHint',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'login_hint'),
        ('parRequestUriLifespan',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'60'),
        ('firstBrokerLoginFlowId',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'87568ca1-089e-423e-a47c-38de2f7b5e0a');

        CREATE TABLE "public"."realm_default_groups" (
            "realm_id" character varying(36) NOT NULL,
            "group_id" character varying(36) NOT NULL,
            CONSTRAINT "constr_realm_default_groups" PRIMARY KEY ("realm_id", "group_id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX con_group_id_def_groups ON public.realm_default_groups USING btree (group_id);

        CREATE INDEX idx_realm_def_grp_realm ON public.realm_default_groups USING btree (realm_id);


        CREATE TABLE "public"."realm_enabled_event_types" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_enabl_event_types" PRIMARY KEY ("realm_id", "value")
        ) WITH (oids = false);

        CREATE INDEX idx_realm_evt_types_realm ON public.realm_enabled_event_types USING btree (realm_id);


        CREATE TABLE "public"."realm_events_listeners" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_events_listeners" PRIMARY KEY ("realm_id", "value")
        ) WITH (oids = false);

        CREATE INDEX idx_realm_evt_list_realm ON public.realm_events_listeners USING btree (realm_id);

        INSERT INTO "realm_events_listeners" ("realm_id", "value") VALUES
        ('60cc6974-2944-452e-a1b4-9ca8d732ed55',	'jboss-logging'),
        ('797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'jboss-logging'),
        ('46e0cc9f-90b8-49f5-87c0-130927b60639',	'jboss-logging');

        CREATE TABLE "public"."realm_localizations" (
            "realm_id" character varying(255) NOT NULL,
            "locale" character varying(255) NOT NULL,
            "texts" text NOT NULL,
            CONSTRAINT "realm_localizations_pkey" PRIMARY KEY ("realm_id", "locale")
        ) WITH (oids = false);


        CREATE TABLE "public"."realm_required_credential" (
            "type" character varying(255) NOT NULL,
            "form_label" character varying(255),
            "input" boolean DEFAULT false NOT NULL,
            "secret" boolean DEFAULT false NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_92" PRIMARY KEY ("realm_id", "type")
        ) WITH (oids = false);

        INSERT INTO "realm_required_credential" ("type", "form_label", "input", "secret", "realm_id") VALUES
        ('password',	'password',	'1',	'1',	'60cc6974-2944-452e-a1b4-9ca8d732ed55'),
        ('password',	'password',	'1',	'1',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f'),
        ('password',	'password',	'1',	'1',	'46e0cc9f-90b8-49f5-87c0-130927b60639');

        CREATE TABLE "public"."realm_smtp_config" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_e" PRIMARY KEY ("realm_id", "name")
        ) WITH (oids = false);


        CREATE TABLE "public"."realm_supported_locales" (
            "realm_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constr_realm_supported_locales" PRIMARY KEY ("realm_id", "value")
        ) WITH (oids = false);

        CREATE INDEX idx_realm_supp_local_realm ON public.realm_supported_locales USING btree (realm_id);


        CREATE TABLE "public"."redirect_uris" (
            "client_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_redirect_uris" PRIMARY KEY ("client_id", "value")
        ) WITH (oids = false);

        CREATE INDEX idx_redir_uri_client ON public.redirect_uris USING btree (client_id);

        INSERT INTO "redirect_uris" ("client_id", "value") VALUES
        ('d12747b4-3958-40b8-b46d-a81db3d115c2',	'/realms/master/account/*'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'/realms/master/account/*'),
        ('46305075-83e2-4080-91ca-4f953b91868d',	'/admin/master/console/*'),
        ('3ed63266-c37d-4a89-b2b6-226108e1ae4f',	'/realms/tenant-manager/account/*'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'/realms/tenant-manager/account/*'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'/admin/tenant-manager/console/*'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'https://tenant-manager.localhost/*'),
        ('eb44f382-bd55-49ee-8103-4ab4e8881d0b',	'/realms/openk9/account/*'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'/realms/openk9/account/*'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'/admin/openk9/console/*'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'http://demo.openk9.localhost/*');

        CREATE TABLE "public"."required_action_config" (
            "required_action_id" character varying(36) NOT NULL,
            "value" text,
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_req_act_cfg_pk" PRIMARY KEY ("required_action_id", "name")
        ) WITH (oids = false);


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
        ) WITH (oids = false);

        CREATE INDEX idx_req_act_prov_realm ON public.required_action_provider USING btree (realm_id);

        INSERT INTO "required_action_provider" ("id", "alias", "name", "realm_id", "enabled", "default_action", "provider_id", "priority") VALUES
        ('5e5baaab-a8d7-4c88-b887-5a9550695a2f',	'VERIFY_EMAIL',	'Verify Email',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('d6af2d85-ef68-4af1-9282-6ac2a818fe1c',	'UPDATE_PROFILE',	'Update Profile',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('4bc54dea-fd69-46ee-b5d2-429241f14513',	'CONFIGURE_TOTP',	'Configure OTP',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('d6b7f029-2e5a-49b8-90f0-2f1a9eaede2f',	'UPDATE_PASSWORD',	'Update Password',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('26d877ac-8f1b-4ab4-97e5-1c3d5d8efe89',	'TERMS_AND_CONDITIONS',	'Terms and Conditions',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'0',	'TERMS_AND_CONDITIONS',	20),
        ('5762bc5a-8503-4b47-8ff1-130825bed5cf',	'delete_account',	'Delete Account',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'0',	'0',	'delete_account',	60),
        ('d82cec60-5404-4c56-b699-c0791223fbe4',	'delete_credential',	'Delete Credential',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'delete_credential',	100),
        ('0ca8a275-2980-4c7e-b0b4-1597fe4b0cd0',	'update_user_locale',	'Update User Locale',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'update_user_locale',	1000),
        ('37abde6f-000f-4323-a864-241808e6dc32',	'webauthn-register',	'Webauthn Register',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'webauthn-register',	70),
        ('4095f2c4-d601-40ab-8a9a-ffe3396db0ae',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'webauthn-register-passwordless',	80),
        ('80f30d3b-25b4-48b9-9b06-5e6b2a289df6',	'VERIFY_PROFILE',	'Verify Profile',	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'1',	'0',	'VERIFY_PROFILE',	90),
        ('c319acba-b94a-4120-8043-5783956cfc85',	'VERIFY_EMAIL',	'Verify Email',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('f382a037-5987-45ef-97b2-353f71e98bfd',	'UPDATE_PROFILE',	'Update Profile',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('732e4439-0b00-4594-b2c0-c62bfabd6be5',	'CONFIGURE_TOTP',	'Configure OTP',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('e0048c8b-8042-478b-931e-249ea70197fb',	'UPDATE_PASSWORD',	'Update Password',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('63a13ef2-9cc9-4574-bf9a-8f6ccefed7a8',	'TERMS_AND_CONDITIONS',	'Terms and Conditions',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0',	'0',	'TERMS_AND_CONDITIONS',	20),
        ('c3ff2584-e999-4651-8e22-6dc1e552a419',	'delete_account',	'Delete Account',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'0',	'0',	'delete_account',	60),
        ('1350d833-73c3-486b-aabc-38bffabc2561',	'delete_credential',	'Delete Credential',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'delete_credential',	100),
        ('02318825-5255-4611-9995-3354fc443623',	'update_user_locale',	'Update User Locale',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'update_user_locale',	1000),
        ('247eb424-2661-4e97-8be6-8473b6aaf610',	'webauthn-register',	'Webauthn Register',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'webauthn-register',	70),
        ('e7e1c5f8-732a-4200-bdc0-042f2d3bd37a',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'webauthn-register-passwordless',	80),
        ('2be0e7fb-3ce7-439c-bd61-75e7c4eb2498',	'VERIFY_PROFILE',	'Verify Profile',	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'1',	'0',	'VERIFY_PROFILE',	90),
        ('3004f8d3-ca8d-479f-8d88-b64b2fbe4688',	'VERIFY_EMAIL',	'Verify Email',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'VERIFY_EMAIL',	50),
        ('ca98d923-8f8d-4838-a7d9-175d561d9218',	'UPDATE_PROFILE',	'Update Profile',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'UPDATE_PROFILE',	40),
        ('1f2be2ff-c2e6-4106-98e6-71179923664c',	'CONFIGURE_TOTP',	'Configure OTP',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'CONFIGURE_TOTP',	10),
        ('72d3d362-d119-4778-8ede-6c767abbb723',	'UPDATE_PASSWORD',	'Update Password',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'UPDATE_PASSWORD',	30),
        ('586131bd-2fd5-4c29-83a0-1096e6a78153',	'TERMS_AND_CONDITIONS',	'Terms and Conditions',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'0',	'TERMS_AND_CONDITIONS',	20),
        ('96fb4456-e8bd-453e-b321-8211b7e29f37',	'delete_account',	'Delete Account',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'0',	'0',	'delete_account',	60),
        ('d9da40f5-58db-463b-b11d-c3b92852b9a2',	'delete_credential',	'Delete Credential',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'delete_credential',	100),
        ('e98a8372-49b2-498a-a07e-cfd8e42fa1a1',	'update_user_locale',	'Update User Locale',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'update_user_locale',	1000),
        ('6c3b45be-4715-452e-8d42-d89a1646fc74',	'webauthn-register',	'Webauthn Register',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'webauthn-register',	70),
        ('c2d85dcf-cbe1-4282-9a97-d513a8eecea9',	'webauthn-register-passwordless',	'Webauthn Register Passwordless',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'webauthn-register-passwordless',	80),
        ('6cae664f-2489-41b5-963b-6031ab31c4b6',	'VERIFY_PROFILE',	'Verify Profile',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'1',	'0',	'VERIFY_PROFILE',	90);

        CREATE TABLE "public"."resource_attribute" (
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "resource_id" character varying(36) NOT NULL,
            CONSTRAINT "res_attr_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);


        CREATE TABLE "public"."resource_policy" (
            "resource_id" character varying(36) NOT NULL,
            "policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrpp" PRIMARY KEY ("resource_id", "policy_id")
        ) WITH (oids = false);

        CREATE INDEX idx_res_policy_policy ON public.resource_policy USING btree (policy_id);


        CREATE TABLE "public"."resource_scope" (
            "resource_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrsp" PRIMARY KEY ("resource_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX idx_res_scope_scope ON public.resource_scope USING btree (scope_id);


        CREATE TABLE "public"."resource_server" (
            "id" character varying(36) NOT NULL,
            "allow_rs_remote_mgmt" boolean DEFAULT false NOT NULL,
            "policy_enforce_mode" smallint NOT NULL,
            "decision_strategy" smallint DEFAULT '1' NOT NULL,
            CONSTRAINT "pk_resource_server" PRIMARY KEY ("id")
        ) WITH (oids = false);


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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsr6t700s9v50bu18ws5pmt ON public.resource_server_perm_ticket USING btree (owner, requester, resource_server_id, resource_id, scope_id);

        CREATE INDEX idx_perm_ticket_requester ON public.resource_server_perm_ticket USING btree (requester);

        CREATE INDEX idx_perm_ticket_owner ON public.resource_server_perm_ticket USING btree (owner);


        CREATE TABLE "public"."resource_server_policy" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "description" character varying(255),
            "type" character varying(255) NOT NULL,
            "decision_strategy" smallint,
            "logic" smallint,
            "resource_server_id" character varying(36) NOT NULL,
            "owner" character varying(255),
            CONSTRAINT "constraint_farsrp" PRIMARY KEY ("id")
        ) WITH (oids = false);

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
        ) WITH (oids = false);

        CREATE INDEX idx_res_srv_res_res_srv ON public.resource_server_resource USING btree (resource_server_id);

        CREATE UNIQUE INDEX uk_frsr6t700s9v50bu18ws5ha6 ON public.resource_server_resource USING btree (name, owner, resource_server_id);


        CREATE TABLE "public"."resource_server_scope" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "icon_uri" character varying(255),
            "resource_server_id" character varying(36) NOT NULL,
            "display_name" character varying(255),
            CONSTRAINT "constraint_farsrs" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_frsrst700s9v50bu18ws5ha6 ON public.resource_server_scope USING btree (name, resource_server_id);

        CREATE INDEX idx_res_srv_scope_res_srv ON public.resource_server_scope USING btree (resource_server_id);


        CREATE TABLE "public"."resource_uris" (
            "resource_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_resour_uris_pk" PRIMARY KEY ("resource_id", "value")
        ) WITH (oids = false);


        CREATE TABLE "public"."revoked_token" (
            "id" character varying(255) NOT NULL,
            "expire" bigint NOT NULL,
            CONSTRAINT "constraint_rt" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_rev_token_on_expire ON public.revoked_token USING btree (expire);


        CREATE TABLE "public"."role_attribute" (
            "id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            CONSTRAINT "constraint_role_attribute_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_role_attribute ON public.role_attribute USING btree (role_id);


        CREATE TABLE "public"."scope_mapping" (
            "client_id" character varying(36) NOT NULL,
            "role_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_81" PRIMARY KEY ("client_id", "role_id")
        ) WITH (oids = false);

        CREATE INDEX idx_scope_mapping_role ON public.scope_mapping USING btree (role_id);

        INSERT INTO "scope_mapping" ("client_id", "role_id") VALUES
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'78053aa1-ce91-463e-8bc0-0cefef09c7ba'),
        ('6e144c2e-55a1-4978-926d-e1b2979e109a',	'9c8aae56-a38e-4c53-a6aa-37220b4fb170'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'359987c6-da37-4cb7-a1b8-956b217d8ead'),
        ('307570d2-8840-42af-8226-8f49db789dd7',	'45b02373-9d28-4b72-9985-6de40a31e3b9'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'7da37ec1-399e-4ddd-8f7b-99ade12ffaed'),
        ('86f793cb-35f7-4ac6-805e-e194e7dc0dfb',	'e2aacd9e-d560-44e6-8db3-6c80bd391dd5');

        CREATE TABLE "public"."scope_policy" (
            "scope_id" character varying(36) NOT NULL,
            "policy_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_farsrsps" PRIMARY KEY ("scope_id", "policy_id")
        ) WITH (oids = false);

        CREATE INDEX idx_scope_policy_policy ON public.scope_policy USING btree (policy_id);


        CREATE TABLE "public"."user_attribute" (
            "name" character varying(255) NOT NULL,
            "value" character varying(255),
            "user_id" character varying(36) NOT NULL,
            "id" character varying(36) DEFAULT 'sybase-needs-something-here' NOT NULL,
            "long_value_hash" bytea,
            "long_value_hash_lower_case" bytea,
            "long_value" text,
            CONSTRAINT "constraint_user_attribute_pk" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_user_attribute ON public.user_attribute USING btree (user_id);

        CREATE INDEX idx_user_attribute_name ON public.user_attribute USING btree (name, value);

        CREATE INDEX user_attr_long_values ON public.user_attribute USING btree (long_value_hash, name);

        CREATE INDEX user_attr_long_values_lower_case ON public.user_attribute USING btree (long_value_hash_lower_case, name);

        INSERT INTO "user_attribute" ("name", "value", "user_id", "id", "long_value_hash", "long_value_hash_lower_case", "long_value") VALUES
        ('is_temporary_admin',	'true',	'819823be-1c8c-4da1-a247-71524f8c5539',	'9dd24f9b-f38f-455e-9423-b3f6f5402afe',	NULL,	NULL,	NULL);

        CREATE TABLE "public"."user_consent" (
            "id" character varying(36) NOT NULL,
            "client_id" character varying(255),
            "user_id" character varying(36) NOT NULL,
            "created_date" bigint,
            "last_updated_date" bigint,
            "client_storage_provider" character varying(36),
            "external_client_id" character varying(255),
            CONSTRAINT "constraint_grntcsnt_pm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_user_consent ON public.user_consent USING btree (user_id);

        CREATE UNIQUE INDEX uk_local_consent ON public.user_consent USING btree (client_id, user_id);

        CREATE UNIQUE INDEX uk_external_consent ON public.user_consent USING btree (client_storage_provider, external_client_id, user_id);


        CREATE TABLE "public"."user_consent_client_scope" (
            "user_consent_id" character varying(36) NOT NULL,
            "scope_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_grntcsnt_clsc_pm" PRIMARY KEY ("user_consent_id", "scope_id")
        ) WITH (oids = false);

        CREATE INDEX idx_usconsent_clscope ON public.user_consent_client_scope USING btree (user_consent_id);

        CREATE INDEX idx_usconsent_scope_id ON public.user_consent_client_scope USING btree (scope_id);


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
        ) WITH (oids = false);

        CREATE UNIQUE INDEX uk_dykn684sl8up1crfei6eckhd7 ON public.user_entity USING btree (realm_id, email_constraint);

        CREATE INDEX idx_user_email ON public.user_entity USING btree (email);

        CREATE UNIQUE INDEX uk_ru8tt6t700s9v50bu18ws5ha6 ON public.user_entity USING btree (realm_id, username);

        CREATE INDEX idx_user_service_account ON public.user_entity USING btree (realm_id, service_account_client_link);

        INSERT INTO "user_entity" ("id", "email", "email_constraint", "email_verified", "enabled", "federation_link", "first_name", "last_name", "realm_id", "username", "created_timestamp", "service_account_client_link", "not_before") VALUES
        ('819823be-1c8c-4da1-a247-71524f8c5539',	NULL,	'c2d3fc81-1e7f-4dd7-a79b-476ebd8290ad',	'0',	'1',	NULL,	NULL,	NULL,	'60cc6974-2944-452e-a1b4-9ca8d732ed55',	'user',	1747579184324,	NULL,	0),
        ('b27ac118-d0fb-430b-9a82-6bf684d74000',	NULL,	'328eca62-d517-422a-b5e4-6c2124eb5f16',	'0',	'1',	NULL,	NULL,	NULL,	'797bc2bc-232f-47fc-854d-4f5a2148bf5f',	'tenant-manager-admin',	1747579258147,	NULL,	0),
        ('5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73',	'test@email3.com',	'test@email3.com',	'0',	'1',	NULL,	'test',	'test',	'46e0cc9f-90b8-49f5-87c0-130927b60639',	'k9admin',	1747579354887,	NULL,	0);

        CREATE TABLE "public"."user_federation_config" (
            "user_federation_provider_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_f9" PRIMARY KEY ("user_federation_provider_id", "name")
        ) WITH (oids = false);


        CREATE TABLE "public"."user_federation_mapper" (
            "id" character varying(36) NOT NULL,
            "name" character varying(255) NOT NULL,
            "federation_provider_id" character varying(36) NOT NULL,
            "federation_mapper_type" character varying(255) NOT NULL,
            "realm_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_fedmapperpm" PRIMARY KEY ("id")
        ) WITH (oids = false);

        CREATE INDEX idx_usr_fed_map_fed_prv ON public.user_federation_mapper USING btree (federation_provider_id);

        CREATE INDEX idx_usr_fed_map_realm ON public.user_federation_mapper USING btree (realm_id);


        CREATE TABLE "public"."user_federation_mapper_config" (
            "user_federation_mapper_id" character varying(36) NOT NULL,
            "value" character varying(255),
            "name" character varying(255) NOT NULL,
            CONSTRAINT "constraint_fedmapper_cfg_pm" PRIMARY KEY ("user_federation_mapper_id", "name")
        ) WITH (oids = false);


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
        ) WITH (oids = false);

        CREATE INDEX idx_usr_fed_prv_realm ON public.user_federation_provider USING btree (realm_id);


        CREATE TABLE "public"."user_group_membership" (
            "group_id" character varying(36) NOT NULL,
            "user_id" character varying(36) NOT NULL,
            "membership_type" character varying(255) NOT NULL,
            CONSTRAINT "constraint_user_group" PRIMARY KEY ("group_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX idx_user_group_mapping ON public.user_group_membership USING btree (user_id);


        CREATE TABLE "public"."user_required_action" (
            "user_id" character varying(36) NOT NULL,
            "required_action" character varying(255) DEFAULT ' ' NOT NULL,
            CONSTRAINT "constraint_required_action" PRIMARY KEY ("required_action", "user_id")
        ) WITH (oids = false);

        CREATE INDEX idx_user_reqactions ON public.user_required_action USING btree (user_id);


        CREATE TABLE "public"."user_role_mapping" (
            "role_id" character varying(255) NOT NULL,
            "user_id" character varying(36) NOT NULL,
            CONSTRAINT "constraint_c" PRIMARY KEY ("role_id", "user_id")
        ) WITH (oids = false);

        CREATE INDEX idx_user_role_mapping ON public.user_role_mapping USING btree (user_id);

        INSERT INTO "user_role_mapping" ("role_id", "user_id") VALUES
        ('1a685a27-f87d-48c9-882f-0c81f6a7e412',	'819823be-1c8c-4da1-a247-71524f8c5539'),
        ('fce4ac0a-10d4-49c4-9a04-15ffa46da137',	'819823be-1c8c-4da1-a247-71524f8c5539'),
        ('88c38d21-732e-40b0-942d-7392520f0ef4',	'b27ac118-d0fb-430b-9a82-6bf684d74000'),
        ('7c0ebbca-d2a5-42ba-af2a-4207ecd79101',	'b27ac118-d0fb-430b-9a82-6bf684d74000'),
        ('08181cbd-ccaf-4ff4-98ed-daa5aa59e956',	'5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73'),
        ('ffbcebb9-7783-4e9d-ab0a-4d03ea836c1f',	'5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73'),
        ('ffbdb4bb-a64c-4c1b-b7f9-ad85cc013f9a',	'5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73'),
        ('490ea4a2-4d12-47e6-bd54-0a3035b4e869',	'5ce8c22d-1fa3-49dd-b4e6-6d1ccc2efd73');

        CREATE TABLE "public"."web_origins" (
            "client_id" character varying(36) NOT NULL,
            "value" character varying(255) NOT NULL,
            CONSTRAINT "constraint_web_origins" PRIMARY KEY ("client_id", "value")
        ) WITH (oids = false);

        CREATE INDEX idx_web_orig_client ON public.web_origins USING btree (client_id);

        INSERT INTO "web_origins" ("client_id", "value") VALUES
        ('46305075-83e2-4080-91ca-4f953b91868d',	'+'),
        ('c9ee8742-f206-4ee1-a45a-9cb73bec1e4c',	'+'),
        ('6c8bc383-4508-4c51-b8ec-42c2929a6500',	'+'),
        ('a013b77f-af95-4fcd-a627-175070130683',	'+'),
        ('437140a9-297d-4dff-8873-0e5eb6a7e515',	'+');

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

        ALTER TABLE ONLY "public"."web_origins" ADD CONSTRAINT "fk_lojpho213xcx4wnkog82ssrfy" FOREIGN KEY (client_id) REFERENCES client(id) NOT DEFERRABLE;
        END IF;
END $$;