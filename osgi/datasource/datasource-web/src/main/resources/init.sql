CREATE TABLE IF NOT EXISTS Datasource(
	datasourceId SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(255) NOT NULL,
	tenantId SERIAL NOT NULL,
	active bool NOT NULL,
	description TEXT NOT NULL,
	driverName VARCHAR(255) NOT NULL,
	jsonConfig JSON NOT NULL);
CREATE TABLE IF NOT EXISTS Tenant(
	tenantId BIGINT PRIMARY KEY NOT NULL,
	name VARCHAR(255) NOT NULL);
CREATE TABLE IF NOT EXISTS EnrichItem(
	enrichItemId SERIAL PRIMARY KEY NOT NULL,
	enrichPipelineId BIGINT NOT NULL,
	name VARCHAR(255) NOT NULL,
	active bool NOT NULL,
	orderValue INT NOT NULL,
	data JSON NOT NULL,
	serviceName VARCHAR(255) NOT NULL);
CREATE TABLE IF NOT EXISTS EnrichPipeline(
	enrichPipelineId SERIAL PRIMARY KEY NOT NULL,
	datasourceId BIGINT NOT NULL,
	name VARCHAR(255) NOT NULL,
	active bool NOT NULL);
CREATE UNIQUE INDEX IF NOT EXISTS index_enrichPipelineId_and_datasourceId ON EnrichPipeline (enrichPipelineId, datasourceId);
