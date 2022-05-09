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

package io.openk9.ingestion.rabbitmq.service;

import com.rabbitmq.client.AMQP;
import io.openk9.ingestion.api.QueueService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import java.util.function.Supplier;

@Component(
	immediate = true,
	service = QueueService.class
)
public class QueueServiceImpl implements QueueService {

	@Override
	public void deleteQueueBlocking(String queue) {

		Mono<Void> voidMono = deleteQueue(queue);

		if (Schedulers.isInNonBlockingThread()) {
			voidMono.subscribe();
			return;
		}
		voidMono.block();

	}

	@Override
	public Publisher<Void> genericDeleteQueue(String queue) {
		return deleteQueue(queue);
	}

	@Override
	public Mono<Void> deleteQueue(String queue) {

		Sender sender = _senderProvider.get();

		Mono<AMQP.Queue.DeleteOk> deleteOkMono = sender.deleteQueue(
			QueueSpecification.queue(queue), true, false);

		return deleteOkMono.then();
	}

	@Reference(target = "(rabbit=sender)", policyOption = ReferencePolicyOption.GREEDY)
	private Supplier<Sender> _senderProvider;

}
