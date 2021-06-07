package io.openk9.entity.manager.logic;


import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import io.openk9.entity.manager.publisher.api.EntityManagerResponsePublisher;
import io.openk9.entity.manager.subscriber.api.EntityManagerRequestConsumer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import reactor.core.Disposable;

@Component(
	immediate = true,
	service = GetOrAddEntitiesConsumer.class
)
public class GetOrAddEntitiesConsumer {

	@Activate
	void activate() {

		_disposable =
			_entityManagerRequestConsumer
				.stream()
				.concatMap(_getOrAddEntities::handleMessage)
				.flatMap(list ->
					_entityManagerResponsePublisher.publish(
						MessageResponse
							.builder()
							.response(list)
							.build()
					)
				)
				.subscribe();
	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference
	private EntityManagerRequestConsumer _entityManagerRequestConsumer;

	@Reference
	private EntityManagerResponsePublisher _entityManagerResponsePublisher;

	@Reference
	private GetOrAddEntities _getOrAddEntities;

}
