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

package io.openk9.datasource;

import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that the Pekko cluster configuration routes sharding traffic
 * through the large-message channel, preventing serialization failures
 * for large payloads on multi-node clusters.
 *
 */
class PekkoClusterConfigTest {

	private static final String LARGE_MESSAGE_DESTINATIONS_PATH =
		"pekko.remote.artery.advanced.large-message-destinations";

	private static List<String> largeMessageDestinations;

	@BeforeAll
	static void loadConfig() {
		Config config = ConfigFactory.parseResources("cluster.conf");
		largeMessageDestinations = config.getStringList(
			LARGE_MESSAGE_DESTINATIONS_PATH);
	}

	@Test
	void schedulingShardingUsesLargeMessageChannel() {
		assertTrue(
			largeMessageDestinations.contains(
				"/system/sharding/scheduling-key/*"),
			"scheduling-key sharding must route through large-message channel");
	}

	@Test
	void tokenShardingUsesLargeMessageChannel() {
		assertTrue(
			largeMessageDestinations.contains(
				"/system/sharding/tokenKey/*"),
			"tokenKey sharding must route through large-message channel");
	}

	@Test
	void enrichPipelineShardingUsesLargeMessageChannel() {
		assertTrue(
			largeMessageDestinations.contains(
				"/system/sharding/enrich-pipeline/*"),
			"enrich-pipeline sharding must route through large-message channel");
	}

	@Test
	void embeddingProcessorShardingUsesLargeMessageChannel() {
		assertTrue(
			largeMessageDestinations.contains(
				"/system/sharding/embedding-processor/*"),
			"embedding-processor sharding must route through large-message channel");
	}

}
