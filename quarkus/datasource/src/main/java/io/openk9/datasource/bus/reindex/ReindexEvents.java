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

package io.openk9.datasource.bus.reindex;

import io.openk9.datasource.index.DatasourceIndexService;
import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.model.Datasource;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class ReindexEvents {

	@ConsumeEvent(REINDEX_STEP_1)
	public Uni<Void> step1(ReindexMessage reindexMessage) {

		String replayTo = reindexMessage.getReplayTo();

		Datasource datasource = reindexMessage.getDatasource();

		Uni<Object> uniReindex = Uni.createFrom().publisher(
			datasourceIndexService.reindex(datasource));

		if (replayTo != null) {
			return uniReindex.call(() ->
				eventBus.request(
					replayTo, ReindexMessage.of(datasource, null)))
				.replaceWithVoid();
		}

		return uniReindex.replaceWithVoid();
	}

	@ConsumeEvent(REINDEX_STEP_2)
	@ReactiveTransactional
	public Uni<Void> step2(ReindexMessage reindexMessage) {

		String replayTo = reindexMessage.getReplayTo();

		Datasource datasource = reindexMessage.getDatasource();

		Uni<Void> response = schedulerInitializer
			.get().triggerJob(
				datasource.getId(), datasource.getName());

		if (replayTo != null) {
			return response
				.call(() ->
					eventBus.request(
						replayTo, ReindexMessage.of(datasource, null)));
		}

		return response;

	}

	@Inject
	EventBus eventBus;

	@Inject
	DatasourceIndexService datasourceIndexService;

	@Inject
	Instance<SchedulerInitializer> schedulerInitializer;

	public static final String REINDEX_STEP_1 = "reindex-step1";

	public static final String REINDEX_STEP_2 = "reindex-step2";

}
