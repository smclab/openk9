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

package io.openk9.tika.web;

import io.openk9.tika.Processor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/process")
public class ProcessEndpoint {

	@ConfigProperty(name = "tika.pool.size", defaultValue = "4")
	int tikaPoolSize;

	@PostConstruct
	public void init() {
		AtomicInteger atomicInteger = new AtomicInteger(0);

		_executorService = Executors.newFixedThreadPool(tikaPoolSize, r -> {
			Thread t = new Thread(r);
			t.setName("tika-thread-" + atomicInteger.getAndIncrement());
			return t;
		});
	}

	@PreDestroy
	public void destroy(){
		_executorService.shutdown();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)

	public void process(JsonObject payload) {

		_executorService.execute(() -> {
			_processor.process(payload);
		});

	}

	@Inject
	Processor _processor;

	private ExecutorService _executorService;


}