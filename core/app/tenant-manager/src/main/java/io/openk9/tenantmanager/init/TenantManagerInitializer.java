package io.openk9.tenantmanager.init;

import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.openk9.tenantmanager.util.VertxUtil;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Startup
public class TenantManagerInitializer {

	@PostConstruct
	public void onStart() {

		Uni<Void> tenantManagerLiquibase =
			Uni
				.createFrom()
				.item(Unchecked.supplier(() -> {
					runTenantManagerLiquibase();
					logger.info("Tenant Manager Liquibase Initialized");
					return null;
				}));

		Uni<Void> tenantUpgrade =
			tenantService
				.findAllSchemaName()
				.flatMap((schemas) -> Uni
					.createFrom()
					.item(Unchecked.supplier(() -> {
						liquibaseService.runUpdate(schemas);
						return null;
					})));

		VertxUtil
			.runOnContext(() ->
				Uni
					.join()
					.all(tenantManagerLiquibase, tenantUpgrade)
					.andFailFast()
			);
	}

	public void runTenantManagerLiquibase() throws Exception {

		ClassLoaderResourceAccessor resourceAccessor =
			new ClassLoaderResourceAccessor(
				Thread.currentThread().getContextClassLoader());

		DatabaseConnection connection = DatabaseFactory.getInstance().openConnection(
			liquibaseService.toJdbcUrl(openk9DatasourceUrl), datasourceUsername, datasourcePassword,
			null, resourceAccessor);

		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

		try (Liquibase liquibase = new Liquibase(changeLogLocation, resourceAccessor, database)) {
			liquibase.validate();
			liquibase.update(new Contexts(), new LabelExpression());
		}

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


}
