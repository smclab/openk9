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

package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.context.api.NamedInstance;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.keycloak.admin.client.Keycloak;

@ApplicationScoped
public class DeleteTenantActorSystem {

	@PostConstruct
	public void init() {
		system = new TypedActor.System(sharedConfiguredExecutor);
		deleteGroupActor = system.actorOf(self ->
			new DeleteGroupBehavior(
				self, system, datasourceLiquibaseService, tenantService,
				keycloak
			)
		);
	}

	public void runDelete(String virtualHost, String token) {
		deleteGroupActor.tell(new DeleteGroupMessage.TellDelete(virtualHost, token));
	}

	public void startDeleteTenant(String virtualHost) {

		deleteGroupActor.tell(
			new DeleteGroupMessage.addDeleteRequest(virtualHost));

	}

	@Inject
	@ManagedExecutorConfig
	@NamedInstance("delete-tenant-actor-executor")
	ManagedExecutor sharedConfiguredExecutor;

	@Inject
	DatasourceLiquibaseService datasourceLiquibaseService;
	@Inject
	TenantService tenantService;
	@Inject
	Keycloak keycloak;

	private TypedActor.System system;
	private TypedActor.Address<DeleteGroupMessage> deleteGroupActor;

}
