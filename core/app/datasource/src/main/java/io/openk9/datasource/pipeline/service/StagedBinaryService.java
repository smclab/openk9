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

package io.openk9.datasource.pipeline.service;

import java.util.concurrent.CompletionStage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.storage.PresignedUrlService;
import io.openk9.quarkus.common.EventBusInstanceHolder;

import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.jboss.logging.Logger;

/**
 * Single entry point the indexing pipeline uses to reach the binaries staged on
 * the object storage: it mints the pre-signed GET URLs the enrichers read them
 * with, and drops the whole working copy of a datasource when a scheduling
 * closes.
 *
 * <p>The pipeline actors are not CDI beans, so both operations are exposed as
 * static methods backed by this eagerly-started bean. URL signing is served
 * in-process (a local SigV4 computation, no I/O); the blocking deletion is
 * dispatched over the Vert.x event bus and run on a worker thread, the pattern
 * used by the other pipeline stages.
 */
@Startup
@ApplicationScoped
public class StagedBinaryService {

	private static final Logger log =
		Logger.getLogger(StagedBinaryService.class);

	private static final String DELETE_BY_DATASOURCE =
		"StagedBinaryService#deleteByDatasource";

	private static volatile StagedBinaryService instance;

	@Inject
	PresignedUrlService presignedUrlService;

	@PostConstruct
	void init() {
		instance = this;
	}

	/**
	 * Mints a pre-signed GET URL for a single staged binary.
	 *
	 * @param tenantId the tenant owning the bucket
	 * @param datasourceId the datasource owning the content
	 * @param contentId the content the binary belongs to
	 * @param fileId the per-binary discriminator
	 * @return a short-lived pre-signed GET URL (bearer credential)
	 */
	public static String presignGet(
		String tenantId, long datasourceId, String contentId, String fileId) {

		return instance.presignedUrlService.presignGet(
			tenantId, datasourceId, contentId, fileId);
	}

	/**
	 * Requests, over the event bus, the deletion of every binary staged under a
	 * datasource.
	 *
	 * @param tenantId the tenant owning the bucket
	 * @param datasourceId the datasource whose staged binaries are removed
	 * @return a stage completing when the deletion has been carried out, failing
	 * if the storage could not be cleaned
	 */
	public static CompletionStage<Void> deleteByDatasource(
		String tenantId, long datasourceId) {

		return EventBusInstanceHolder
			.request(
				DELETE_BY_DATASOURCE,
				new DeleteByDatasourceRequest(tenantId, datasourceId))
			.replaceWithVoid()
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(DELETE_BY_DATASOURCE)
	Uni<Void> deleteByDatasource(DeleteByDatasourceRequest request) {

		log.debugf(
			"Deleting staged binaries for datasource %s",
			request.datasourceId());

		return Uni.createFrom()
			.<Void>item(() -> {
				presignedUrlService.deleteByDatasource(
					request.tenantId(), request.datasourceId());

				return null;
			})
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
	}

	private record DeleteByDatasourceRequest(
		String tenantId,
		long datasourceId
	) {}

}
