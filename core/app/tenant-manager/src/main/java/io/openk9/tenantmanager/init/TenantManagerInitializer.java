package io.openk9.tenantmanager.init;

import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.openk9.tenantmanager.util.CustomClassLoaderResourceAccessor;
import io.openk9.tenantmanager.util.VertxUtil;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.ScopeManager;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
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

		setThreadLocalLiquibaseScopeManager();

		Uni<Void> tenantManagerLiquibase =
			Uni
				.createFrom()
				.item(Unchecked.supplier(() -> {
					runTenantManagerLiquibase();
					logger.info("Tenant Manager Liquibase Initialized");
					return null;
				}))
				.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
				.replaceWithVoid();

		Uni<Void> tenantUpgrade =
			tenantService
				.findAllSchemaNameAndLiquibaseSchemaName()
				.emitOn(Infrastructure.getDefaultWorkerPool())
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

		CustomClassLoaderResourceAccessor resourceAccessor =
			new CustomClassLoaderResourceAccessor(
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

	private static void setThreadLocalLiquibaseScopeManager() {
		Scope.setScopeManager(new ScopeManager() {

			private final ThreadLocal<Scope> currentScope = new ThreadLocal<>();
			private final Scope rootScope = Scope.getCurrentScope();

			@Override
			public synchronized Scope getCurrentScope() {
				Scope returnedScope = currentScope.get();

				if (returnedScope == null) {
					returnedScope = rootScope;
				}

				return returnedScope;
			}

			@Override
			protected Scope init(Scope scope) throws Exception {
				return scope;
			}

			@Override
			protected synchronized void setCurrentScope(Scope scope) {
				this.currentScope.set(scope);
			}
		});
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
