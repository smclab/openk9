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

package io.openk9.ingestion.queue.logic;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.ingestion.logic.api.IngestionLogic;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import reactor.core.Disposable;

@Component(immediate = true, service = SendToQueue.class)
public class SendToQueue {

	@Activate
	public void activate() {
		_disposable = _ingestionLogicReceiver
			.flux()
			.map(_cborFactory::toCBOR)
			.transform(_bundleSender::send)
			.subscribe();
	}

	@Deactivate
	public void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference(
		target = "(routingKey=io.openk9.ingestion)"
	)
	private BundleSender _bundleSender;

	@Reference
	private CBORFactory _cborFactory;

	@Reference
	private IngestionLogic _ingestionLogicReceiver;

}
