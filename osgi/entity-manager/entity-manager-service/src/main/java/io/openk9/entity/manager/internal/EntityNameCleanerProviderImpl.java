package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.EntityNameCleaner;
import io.openk9.entity.manager.api.EntityNameCleanerProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component(
	immediate = true,
	service = EntityNameCleanerProvider.class
)
public class EntityNameCleanerProviderImpl
	implements EntityNameCleanerProvider {

	@Override
	public EntityNameCleaner getEntityNameCleaner(String entityType) {
		return _entityNameCleanerMap.getOrDefault(
			entityType, _defaultEntityNameCleaner);
	}

	@Override
	public Collection<EntityNameCleaner> getEntityNameCleaner() {
		return _entityNameCleanerMap.values();
	}

	@Reference(
		service = EntityNameCleaner.class,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		cardinality = ReferenceCardinality.MULTIPLE,
		target = "(!(component.name=io.openk9.entity.manager.internal.DefaultEntityNameCleaner))",
		bind = "addEntityNameCleaner",
		unbind = "removeEntityNameCleaner"
	)
	protected void addEntityNameCleaner(EntityNameCleaner entityNameCleaner) {
		_entityNameCleanerMap.put(
			entityNameCleaner.getEntityType(), entityNameCleaner);
	}

	protected void removeEntityNameCleaner(EntityNameCleaner entityNameCleaner) {
		_entityNameCleanerMap.remove(
			entityNameCleaner.getEntityType(), entityNameCleaner);
	}

	@Reference(
		target = "(component.name=io.openk9.entity.manager.internal.DefaultEntityNameCleaner)"
	)
	private EntityNameCleaner _defaultEntityNameCleaner;

	private final Map<String, EntityNameCleaner> _entityNameCleanerMap =
		new HashMap<>();

}
