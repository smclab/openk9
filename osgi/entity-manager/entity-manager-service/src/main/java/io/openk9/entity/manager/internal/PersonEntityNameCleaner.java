package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class PersonEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "person";
	}

	@Override
	protected Map<String, Object> createQueryBuilder(
		String entityName) {

		return Map.of(
			Constants.ENTITY_NAME_FIELD, entityName,
			Constants.ENTITY_TYPE_FIELD, getEntityType());
	}
}
