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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.openk9.ml.grpc.MutinyEmbeddingGrpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EmbeddingStubRegistry {

	private static final ConcurrentMap<String, MutinyEmbeddingGrpc.MutinyEmbeddingStub> stubs =
		new ConcurrentHashMap<>();

	private EmbeddingStubRegistry() {}

	public static MutinyEmbeddingGrpc.MutinyEmbeddingStub getStub(String target) {

		return stubs.computeIfAbsent(target, key -> {
			var channel = ManagedChannelBuilder
				.forTarget(key)
				.usePlaintext()
				.build();

			return MutinyEmbeddingGrpc.newMutinyStub(channel);
		});
	}

	public static void remove(String target) {

		var stub = stubs.remove(target);

		if (stub != null) {
			var channel = (ManagedChannel) stub.getChannel();
			channel.shutdown();
		}
	}

	public static void clear() {

		stubs.forEach((target, stub) -> {
			var channel = (ManagedChannel) stub.getChannel();
			channel.shutdown();
		});

		stubs.clear();
	}

}
