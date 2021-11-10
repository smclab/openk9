package io.openk9.entity.manager.cleaner;

import io.quarkus.arc.Unremovable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
@Unremovable
public class EntityNameCleanerProvider {

	public EntityNameCleaner get(String type) {
		return handlers
			.stream()
			.filter(e -> e.getEntityType().equals(type))
			.findFirst()
			.orElseGet(
				() -> EntityNameCleaner.DefaultEntityNameCleaner.of(type));
	}

	@Inject
	Instance<EntityNameCleaner> handlers;

}
