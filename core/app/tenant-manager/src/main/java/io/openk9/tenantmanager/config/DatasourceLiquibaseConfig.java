package io.openk9.tenantmanager.config;

import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Getter
public class DatasourceLiquibaseConfig {

	@ConfigProperty(name = "openk9.datasource.url")
	String openk9DatasourceUrl;

	@ConfigProperty(name = "quarkus.datasource.username")
	String datasourceUsername;

	@ConfigProperty(name = "quarkus.datasource.password")
	String datasourcePassword;

	@ConfigProperty(name = "openk9.datasource.liquibase.change-log")
	String changeLogLocation;

	@ConfigProperty(name = "openk9.datasource.liquibase.database-change-log-lock-table-name")
	String changeLogLockTableName;

	@ConfigProperty(name = "openk9.datasource.liquibase.database-change-log-table-name")
	String changeLogTableName;

}
