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
import java.util.Objects;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a SeaweedFS S3 gateway for the tests, with the same image and S3
 * identity used by the Docker Compose stack, and points the MinIO client at
 * it. It replaces the MinIO dev service, whose image is no longer pullable.
 */
public class SeaweedFsTestResource
	implements QuarkusTestResourceLifecycleManager {

	private static final String IMAGE = "chrislusf/seaweedfs:4.47";
	private static final int S3_PORT = 8333;
	private static final String ACCESS_KEY = "openk9";
	private static final String SECRET_KEY = "openk9-secret";
	private static final String S3_CONFIG_PATH = "/etc/seaweedfs/s3.json";
	private static final String S3_CONFIG = """
		{
		  "identities": [
		    {
		      "name": "openk9",
		      "credentials": [
		        {"accessKey": "%s", "secretKey": "%s"}
		      ],
		      "actions": ["Admin", "Read", "Write", "List"]
		    }
		  ]
		}
		""".formatted(ACCESS_KEY, SECRET_KEY);

	private GenericContainer<?> container;

	/**
	 * Starts the SeaweedFS container and waits for its S3 gateway.
	 *
	 * @return the {@code quarkus.minio.*} properties pointing at the container
	 */
	@Override
	public Map<String, String> start() {
		container = new GenericContainer<>(DockerImageName.parse(IMAGE))
			.withExposedPorts(S3_PORT)
			.withCopyToContainer(Transferable.of(S3_CONFIG), S3_CONFIG_PATH)
			.withCommand(
				"server", "-dir=/data", "-s3", "-s3.config=" + S3_CONFIG_PATH)
			.waitingFor(Wait.forHttp("/healthz").forPort(S3_PORT));

		container.start();

		return Map.of(
			"quarkus.minio.host",
			"http://" + container.getHost() + ":" +
				container.getMappedPort(S3_PORT),
			"quarkus.minio.secure", "false",
			"quarkus.minio.access-key", ACCESS_KEY,
			"quarkus.minio.secret-key", SECRET_KEY
		);
	}

	/**
	 * Stops the SeaweedFS container, if it was started.
	 */
	@Override
	public void stop() {
		if (Objects.nonNull(container)) {
			container.stop();
		}
	}

}
