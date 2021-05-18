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

package io.openk9.ingestion.logic.internal;

import io.openk9.ingestion.logic.api.IngestionLogic;
import io.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;

@Component(
	service = {
		IngestionLogic.class
	}
)
public class IngestionLogicImpl implements IngestionLogic {

	@Activate
	public void activate() {
		_many = Sinks.many().multicast().onBackpressureBuffer();
	}

	@Deactivate
	public void deactivate() {
		_many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		_many = null;
	}

	@Override
	public void send(IngestionPayload ingestionPayload) {
		_many.emitNext(ingestionPayload, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	@Override
	public Publisher<IngestionPayload> genericFlux() {
		return _many.asFlux();
	}

	private Sinks.Many<IngestionPayload> _many;

}
