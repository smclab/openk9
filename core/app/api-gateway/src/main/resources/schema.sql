-- TENANT table
CREATE TABLE TENANT (
    id BIGINT NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    host_name VARCHAR(100) NOT NULL,
    issuer_uri VARCHAR(200),
    CONSTRAINT pk_tenant PRIMARY KEY (id),
    CONSTRAINT uq_tenant_id UNIQUE (tenant_id)
);

-- API_KEY table
CREATE TABLE API_KEY (
    id BIGINT NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    api_key_hash VARCHAR(64) NOT NULL,
    checksum VARCHAR(32) NOT NULL,
    CONSTRAINT pk_api_key PRIMARY KEY (id),
    CONSTRAINT fk_api_key_tenant FOREIGN KEY (tenant_id) REFERENCES TENANT(tenant_id)
);

-- ROUTE_SECURITY table
CREATE TABLE ROUTE_SECURITY (
    id BIGINT NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    route VARCHAR(100) NOT NULL,
    authorization_scheme VARCHAR(100) NOT NULL,
    CONSTRAINT pk_route_security PRIMARY KEY (id),
    CONSTRAINT fk_route_security_tenant FOREIGN KEY (tenant_id) REFERENCES TENANT(tenant_id)
);

-- Sequences
CREATE SEQUENCE tenant_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE api_key_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE route_security_seq START WITH 1 INCREMENT BY 1;

-- Indexes for performance
CREATE INDEX idx_api_key_tenant_id ON API_KEY (tenant_id);
CREATE INDEX idx_route_security_tenant_id ON ROUTE_SECURITY (tenant_id);
