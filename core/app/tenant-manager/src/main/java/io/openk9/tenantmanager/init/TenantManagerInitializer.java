package io.openk9.tenantmanager.init;

import io.openk9.tenantmanager.dto.SchemaTuple;
import io.openk9.tenantmanager.pipe.liquibase.validate.LiquibaseValidatorActorSystem;
import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.LinkedList;

@ApplicationScoped
@Startup
public class TenantManagerInitializer {

	@PostConstruct
	public void onStart() {

		LinkedList<Params> paramsList = new LinkedList<>();

		paramsList.add(
			new Params(null, null, changeLogLocation, null, null,
				liquibaseService.toJdbcUrl(openk9DatasourceUrl),
				datasourceUsername, datasourcePassword)
		);

		liquibaseValidatorActorSystem
			.validateSchemas(paramsList)
			.call(() -> tenantService
				.findAllSchemaNameAndLiquibaseSchemaName()
				.flatMap((schemas) -> Uni
					.createFrom()
					.deferred(() -> {

						LinkedList<Params> schemaParamList = new LinkedList<>();

						for (SchemaTuple schema : schemas) {
							schemaParamList.add(
								new Params(
									schema.schemaName(), schema.liquibaseSchemaName(),
									liquibaseService.getChangeLogLocation(), liquibaseService.getChangeLogLockTableName(),
									liquibaseService.getChangeLogTableName(), liquibaseService.getDatasourceJdbcUrl(),
									datasourceUsername, datasourcePassword)
							);
						}

						return liquibaseValidatorActorSystem.validateSchemas(schemaParamList);

					})))
			.subscribe().with(
				nothing -> logger.info("Tenant Upgrade Finished"),
				throwable -> logger.error("Tenant Upgrade Failed", throwable));

	}

	@ConfigProperty(name = "quarkus.datasource.reactive.url")
	String openk9DatasourceUrl;

	@ConfigProperty(name = "quarkus.datasource.username")
	String datasourceUsername;

	@ConfigProperty(name = "quarkus.datasource.password")
	String datasourcePassword;

	@ConfigProperty(name = "openk9.tenant-manager.liquibase.change-log")
	String changeLogLocation;

	@Inject
	DatasourceLiquibaseService liquibaseService;

	@Inject
	TenantService tenantService;

	@Inject
	Logger logger;
	
	@Inject
	LiquibaseValidatorActorSystem liquibaseValidatorActorSystem;


}
