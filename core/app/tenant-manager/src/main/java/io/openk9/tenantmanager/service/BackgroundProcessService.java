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

	public Uni<UUID> addBackgroundProcess() {
		return sf.withTransaction(
			session -> {

				BackgroundProcess backgroundProcess = new BackgroundProcess();
				backgroundProcess.setStatus(BackgroundProcess.Status.IN_PROGRESS);

				return session
					.persist(backgroundProcess)
					.map(__ -> backgroundProcess.getId());

			}
		);
	}

	public Uni<BackgroundProcess> findBackgroundProcessById(UUID id) {
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

	public Uni<Void> updateBackgroundProcessStatus(UUID id, BackgroundProcess.Status status) {
		return updateBackgroundProcessStatus(id, status, null);
	}

	public Uni<Void> updateBackgroundProcessStatus(UUID id, BackgroundProcess.Status status, String message) {
		return sf.withTransaction(
			session -> session
				.find(BackgroundProcess.class, id)
				.onItem()
				.ifNotNull()
				.transformToUni(backgroundProcess -> {
					backgroundProcess.setStatus(status);
					backgroundProcess.setMessage(message);
					return session.persist(backgroundProcess);
				})
		);
	}

	@Inject
	Mutiny.SessionFactory sf;

}
