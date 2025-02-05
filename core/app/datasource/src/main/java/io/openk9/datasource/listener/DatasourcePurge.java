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

package io.openk9.datasource.listener;

import com.typesafe.config.Config;
import io.openk9.datasource.model.DataIndex;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

public class DatasourcePurge extends AbstractBehavior<DatasourcePurge.Command> {

	public sealed interface Command {}
	private enum Start implements Command {INSTANCE}
	private enum Stop implements Command {INSTANCE}
	private enum FetchDataIndexOrphans implements Command {INSTANCE}
	private record PrepareChunks(List<DataIndex> dataIndices) implements Command {}
	private enum WorkNextChunk implements Command {INSTANCE}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessageEquals(FetchDataIndexOrphans.INSTANCE, this::onFetchDataIndexOrphans)
			.onMessage(PrepareChunks.class, this::onPrepareChunks)
			.onMessageEquals(WorkNextChunk.INSTANCE, this::onWorkNextChunk)
			.onMessageEquals(DeleteIndices.INSTANCE, this::onDeleteEsIndices)
			.onMessageEquals(DeleteDataIndices.INSTANCE, this::onDeleteDataIndices)
			.onMessage(DeleteError.class, this::onEsDeleteError)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.build();
	}

	public DatasourcePurge(
		ActorContext<Command> context, String tenantName, long datasourceId, String purgeMaxAge) {
		super(context);
		this.tenantName = tenantName;
		this.datasourceId = datasourceId;
		//TODO use parsing of maxAge parameter
		this.maxAge = getMaxAge(context);
		getContext().getSelf().tell(Start.INSTANCE);
	}

	private enum DeleteDataIndices implements Command {INSTANCE}

	private final String tenantName;
	private final long datasourceId;
	private final Duration maxAge;
	private final Deque<List<DataIndex>> chunks = new ArrayDeque<>();
	private List<DataIndex> currentChunk;

	public static Behavior<Command> create(
		String tenantName, long datasourceId, String purgeMaxAge) {

		return Behaviors.setup(ctx ->
			new DatasourcePurge(ctx, tenantName, datasourceId, purgeMaxAge));
	}

	private Behavior<Command> onDeleteEsIndices() {

		getContext().pipeToSelf(
			DatasourcePurgeService.deleteIndices(currentChunk),
			(res, err) -> {
				if (err != null) {
					return new DeleteError(new DatasourcePurgeException(err));
				}
				else {
					return DeleteDataIndices.INSTANCE;
				}
			}
		);

		return Behaviors.same();
	}

	private Behavior<Command> onWorkNextChunk() {
		try {
			this.currentChunk = chunks.pop();
			getContext().getLog().info(
				"Working on a chunk for datasource {}-{}", tenantName, datasourceId);
			getContext().getSelf().tell(DeleteIndices.INSTANCE);
		}
		catch (NoSuchElementException e) {
			getContext().getLog().info(
				"No more chunks to work for datasource {}-{}", tenantName, datasourceId);
			getContext().getSelf().tell(Stop.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onStart() {
		getContext().getLog().info(
			"Job DatasourcePurge started for datasource {}-{}", tenantName, datasourceId);

		getContext().getSelf().tell(FetchDataIndexOrphans.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onFetchDataIndexOrphans() {

		getContext().pipeToSelf(
			DatasourcePurgeService.fetchOrphans(
				tenantName, datasourceId, maxAge),
			(dataIndices, throwable) -> new PrepareChunks(dataIndices)
		);

		return Behaviors.same();
	}

	private Behavior<Command> onPrepareChunks(PrepareChunks prepareChunks) {
		List<DataIndex> dataIndices = prepareChunks.dataIndices;

		if (dataIndices != null && !dataIndices.isEmpty()) {
			int chunkSize = 10;

			int lastIndex = dataIndices.size();

			boolean lastChunk = false;

			int i = 0;

			while (!lastChunk) {
				int fromIndex = chunkSize * i;
				int toIndex = chunkSize * ++i;
				lastChunk = toIndex >= lastIndex;

				chunks.add(new ArrayList<>(
					dataIndices.subList(fromIndex, lastChunk ? lastIndex : toIndex))
				);
			}

			getContext().getLog().info(
				"DataIndex orphans found for datasource {}-{}: {}",
				tenantName, datasourceId, lastIndex);

			getContext().getLog().info("Chunks to work for datasource {}-{}: {}",
				tenantName, datasourceId, i);

			getContext().getSelf().tell(WorkNextChunk.INSTANCE);
		}
		else {
			getContext().getLog().info(
				"No DataIndex orphans found for datasource {}-{}", tenantName, datasourceId);
			getContext().getSelf().tell(Stop.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onEsDeleteError(DeleteError ede) {
		getContext().getLog().error("Opensearch DeleteIndexRequest went wrong.", ede.error);

		getContext().getSelf().tell(WorkNextChunk.INSTANCE);

		return Behaviors.same();
	}

	private enum DeleteIndices implements Command {
		INSTANCE
	}

	private Behavior<Command> onDeleteDataIndices() {

		getContext().pipeToSelf(
			DatasourcePurgeService.deleteDataIndices(
				tenantName, datasourceId, currentChunk),
			(ignore, throwable) -> WorkNextChunk.INSTANCE
		);

		return Behaviors.same();
	}

	private record DeleteError(DatasourcePurgeException error) implements Command {}


	private Behavior<Command> onStop() {
		getContext().getLog().info(
			"Job DatasourcePurge finished for datasource {}-{}", tenantName, datasourceId);
		return Behaviors.stopped();
	}

	//TODO change method to parse maxAge String or remove it.
	private static Duration getMaxAge(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.scheduling.purge.max-age";

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return Duration.ofDays(2);
			} else {
				return config.getDuration(configPath);
			}
		} else {
			return Duration.ofDays(2);
		}

	}

}
