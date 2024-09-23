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
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.LinkedList;

@ApplicationScoped
@Startup
public class TenantManagerInitializer {

	public static final String INITIALIZED = "TenantManagerInitializer#INITIALIZED";
	public static final String ERROR = "TenantManagerInitializer#ERROR";

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
	@Inject
	EventBus eventBus;

	@PostConstruct
	public void onStart() {

		LinkedList<Params> paramsList = new LinkedList<>();

		paramsList.add(
			new Params(null, null, changeLogLocation, null, null,
				liquibaseService.toJdbcUrl(openk9DatasourceUrl),
				datasourceUsername, datasourcePassword
			)
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

						return liquibaseValidatorActorSystem.validateSchemas(schemaParamList);

					})))
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
