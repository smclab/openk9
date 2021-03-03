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

package com.openk9.ingestion.queue.logic;


import com.openk9.datasource.repository.DatasourceRepository;
import com.openk9.ingestion.logic.api.IngestionLogic;
import com.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component(immediate = true, service = UpdateLastIngestionDate.class)
public class UpdateLastIngestionDate {

	@interface Config {
		long timespan() default 30_000;
	}

	@Activate
	public void activate(Config config) {
		_disposable = _ingestionLogicReceiver
			.flux()
			.groupBy(IngestionPayload::getDatasourceId)
			.flatMap(group -> group
				.sample(Duration.ofMillis(config.timespan())))
			.flatMap(ip -> _datasourceRepository
					.updateLastIngestionDate(
						ip.getDatasourceId(),
						Instant.ofEpochMilli(ip.getParsingDate()))
					.doOnNext(unused -> {
						if (_log.isDebugEnabled()) {
							_log.debug(
								"update lastIngestionDate " +
								new Date(ip.getParsingDate())
									.toInstant().toString() +
								"of the datasourceId: " +
								ip.getDatasourceId());
						}
					})
			)
			.subscribe();
	}

	@Deactivate
	public void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private IngestionLogic _ingestionLogicReceiver;

	private static final Logger _log =
		LoggerFactory.getLogger(UpdateLastIngestionDate.class);

}
