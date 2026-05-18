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

package io.openk9.datasource.grpc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.openk9.ml.grpc.Embedding;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EmbeddingGrpcTest {

	@GrpcClient("embedding")
	Embedding embedding;

	@Test
	void should_inject_embedding_grpc_client_from_application_property() {
		assertNotNull(
			embedding,
			"The @GrpcClient(\"embedding\") service must be resolvable as a CDI bean; " +
				"verify that quarkus.grpc.clients.embedding.host/port are configured."
		);
	}
}
