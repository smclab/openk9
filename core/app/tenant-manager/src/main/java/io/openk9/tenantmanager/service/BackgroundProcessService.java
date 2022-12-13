package io.openk9.tenantmanager.service;

import io.openk9.tenantmanager.model.BackgroundProcess;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BackgroundProcessService {

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

	@Inject
	Mutiny.SessionFactory sf;

}
