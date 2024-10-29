/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.tenantmanager.init;

import io.openk9.tenantmanager.dto.SchemaTuple;
import io.openk9.tenantmanager.pipe.liquibase.validate.LiquibaseValidatorActorSystem;
import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.LinkedList;

@Singleton
public class TenantManagerInitializer {

	public static final String INITIALIZED = "TenantManagerInitializer#INITIALIZED";
	public static final String ERROR = "TenantManagerInitializer#ERROR";

	@ConfigProperty(name = "quarkus.datasource.username")
	String datasourceUsername;
	@ConfigProperty(name = "quarkus.datasource.password")
	String datasourcePassword;
	@Inject
	DatasourceLiquibaseService liquibaseService;
	@Inject
	TenantService tenantService;
	@Inject
	Logger logger;
	@Inject
	LiquibaseValidatorActorSystem liquibaseValidatorActorSystem;
	@Inject
	EventBus eventBus;

	public void onStart(@Observes Startup startup) {

		tenantService.findAllSchemaNameAndLiquibaseSchemaName()
			.flatMap((schemas) -> {
					LinkedList<Params> schemaParamList = new LinkedList<>();

					for (SchemaTuple schema : schemas) {
						schemaParamList.add(
							new Params(
								schema.schemaName(),
								schema.liquibaseSchemaName(),
								liquibaseService.getChangeLogLocation(),
								liquibaseService.getChangeLogLockTableName(),
								liquibaseService.getChangeLogTableName(),
								liquibaseService.getDatasourceJdbcUrl(),
								datasourceUsername,
								datasourcePassword
							)
						);
					}

				return liquibaseValidatorActorSystem.validateSchemas(schemaParamList)
					.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
				}
			)
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
			.subscribe()
			.with(
				nothing -> {
					logger.info("Tenant Upgrade Finished");
					eventBus.send(INITIALIZED, INITIALIZED);
				},
				throwable -> {
					logger.error("Tenant Upgrade Failed", throwable);
					eventBus.send(ERROR, ERROR);
				}
			);

	}

}
