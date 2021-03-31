package io.openk9.entity.manager.api;

import java.util.Collection;

public interface EntityNameCleanerProvider {

	EntityNameCleaner getEntityNameCleaner(String entityType);

	Collection<EntityNameCleaner> getEntityNameCleaner();

}
