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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

/**
 * Integration test of {@link StagedBinaryService} on a deployment whose
 * {@code quarkus.minio.host} holds only whitespace: the MinIO extension treats
 * it as unset, so the object storage counts as not configured.
 */
@QuarkusTest
@TestProfile(StagedBinaryServiceBlankHostTest.BlankHost.class)
class StagedBinaryServiceBlankHostTest {

	@Test
	void should_be_a_noop_when_the_object_storage_host_is_blank()
		throws Exception {

		// dropping a datasource working copy completes without reaching MinIO
		StagedBinaryService
			.deleteByDatasource("nostoragetenant", 1L)
			.toCompletableFuture()
			.get(10, TimeUnit.SECONDS);
	}

	public static class BlankHost implements QuarkusTestProfile {

		@Override
		public Map<String, String> getConfigOverrides() {
			return Map.of(
				"quarkus.minio.devservices.enabled", "false",
				"quarkus.minio.host", "  ");
		}

	}

}
