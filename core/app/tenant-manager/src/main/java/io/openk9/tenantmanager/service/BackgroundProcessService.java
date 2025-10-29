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

package io.openk9.tenantmanager.service;

import java.util.List;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.SingularAttribute;

import io.openk9.common.graphql.util.service.GraphQLRelayService;
import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.model.BackgroundProcess_;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class BackgroundProcessService extends GraphQLRelayService<BackgroundProcess> {

	public Uni<Void> updateBackgroundProcess(
		long id, BackgroundProcess.Status status, String message, String name) {

		return sf
			.withTransaction((session, transaction) ->
				session.find(BackgroundProcess.class, id).chain(bp -> {

					if (status != null) {
						bp.setStatus(status);
					}

					if (message != null) {
						bp.setMessage(message);
					}

					if (name != null) {
						bp.setName(name);
					}

					return session.persist(bp);

				})
			);
	}

	public Uni<BackgroundProcess> createBackgroundProcess(
		BackgroundProcess backgroundProcess) {

		return sf.withSession(
			session -> session.persist(backgroundProcess)
				.chain(session::flush)
				.replaceWith(backgroundProcess)
		);
	}

	public Uni<List<BackgroundProcess>> findBackgroundProcessListByProcessId(UUID processId) {
		return sf.withSession(
			session -> session
				.createQuery(
					"select bp from BackgroundProcess bp where bp.processId = :processId",
					BackgroundProcess.class
				)
				.setParameter("processId", processId)
				.getResultList()
		);
	}

	public Uni<BackgroundProcess> findBackgroundProcessById(long id) {
		return sf.withStatelessSession(s -> s.get(BackgroundProcess.class, id));
	}

	public Uni<Void> deleteBackgroundProcessById(UUID id) {
		return sf.withTransaction(
			session -> session
				.find(BackgroundProcess.class, id)
				.chain(session::remove)
		);
	}

	public Uni<List<BackgroundProcess>> findAllBackgroundProcess() {
		return sf.withStatelessSession(
			s -> s.createQuery("from BackgroundProcess", BackgroundProcess.class).getResultList()
		);
	}

	public Uni<List<BackgroundProcess>> findAllBackgroundProcessByStatus(BackgroundProcess.Status status) {
		return sf.withStatelessSession(
			s -> s.createQuery("from BackgroundProcess where status = :status", BackgroundProcess.class)
				.setParameter("status", status)
				.getResultList()
		);
	}

	public Uni<List<UUID>> findAllBackgroundProcess(BackgroundProcess.Status status) {
		return sf.withStatelessSession(
			s -> s
				.createQuery(
					"select id from BackgroundProcess where status = :status",
					UUID.class
				)
				.setParameter("status", status)
				.getResultList()
		);
	}

	@Override
	protected Class<BackgroundProcess> getEntityClass() {
		return BackgroundProcess.class;
	}

	@Override
	protected String[] getSearchFields() {
		return new String[] {
			BackgroundProcess_.STATUS,
			BackgroundProcess_.MESSAGE,
			BackgroundProcess_.NAME,
			BackgroundProcess_.PROCESS_ID
		};
	}

	@Override
	protected CriteriaBuilder getCriteriaBuilder() {
		return sf.getCriteriaBuilder();
	}

	@Override
	protected Mutiny.SessionFactory getSessionFactory() {
		return sf;
	}

	@Override
	protected SingularAttribute<BackgroundProcess, Long> getIdAttribute() {
		return BackgroundProcess_.id;
	}

	@Inject
	Mutiny.SessionFactory sf;

}
