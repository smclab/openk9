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
