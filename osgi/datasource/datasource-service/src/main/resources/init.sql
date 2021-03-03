CREATE TABLE IF NOT EXISTS Datasource(
	datasourceId SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(255) NOT NULL,
	tenantId BIGINT NOT NULL,
	active bool NOT NULL,
	description TEXT NOT NULL,
	driverName VARCHAR(255) NOT NULL,
	jsonConfig TEXT NOT NULL,
	lastIngestionDate TIMESTAMP NOT NULL,
	scheduling VARCHAR(255) NOT NULL,
	driverServiceName VARCHAR(255) NOT NULL);
CREATE INDEX IF NOT EXISTS index_tenantId ON Datasource (tenantId);
CREATE UNIQUE INDEX IF NOT EXISTS index_name ON Datasource (name);
CREATE TABLE IF NOT EXISTS Tenant(
	tenantId BIGINT PRIMARY KEY NOT NULL,
	name VARCHAR(255) NOT NULL,
	virtualHost VARCHAR(255) NOT NULL);
CREATE UNIQUE INDEX IF NOT EXISTS index_virtualHost ON Tenant (virtualHost);
CREATE TABLE IF NOT EXISTS EnrichItem(
	enrichItemId SERIAL PRIMARY KEY NOT NULL,
	enrichPipelineId BIGINT NOT NULL,
	name VARCHAR(255) NOT NULL,
	active bool NOT NULL,
	_position INT NOT NULL,
	jsonConfig TEXT NOT NULL,
	serviceName VARCHAR(255) NOT NULL);
CREATE INDEX IF NOT EXISTS index_enrichPipelineId_and__position ON EnrichItem (enrichPipelineId, _position);
CREATE INDEX IF NOT EXISTS index_enrichPipelineId_and_active ON EnrichItem (enrichPipelineId, active);
CREATE TABLE IF NOT EXISTS EnrichPipeline(
	enrichPipelineId SERIAL PRIMARY KEY NOT NULL,
	datasourceId BIGINT NOT NULL,
	name VARCHAR(255) NOT NULL,
	active bool NOT NULL);
CREATE UNIQUE INDEX IF NOT EXISTS index_enrichPipelineId_and_datasourceId ON EnrichPipeline (enrichPipelineId, datasourceId);
CREATE UNIQUE INDEX IF NOT EXISTS index_datasourceId ON EnrichPipeline (datasourceId);
