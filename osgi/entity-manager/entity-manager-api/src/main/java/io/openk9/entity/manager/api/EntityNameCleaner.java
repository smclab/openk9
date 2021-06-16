package io.openk9.entity.manager.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public interface EntityNameCleaner {

	String AND_OPERATOR = "_AND";

	String EXACT = "_EXACT";

	String getEntityType();

	Map<String, Object> cleanEntityName(long tenantId, String entityName);

	default String cleanEntityName(String entityName) {
		return entityName.trim();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	class DefaultEntityNameCleaner implements EntityNameCleaner {

		private String entityType;

		@Override
		public Map<String, Object> cleanEntityName(long tenantId, String entityName) {
			return createQueryBuilder(cleanEntityName(entityName));
		}

		protected Map<String, Object> createQueryBuilder(String entityName) {
			return Map.of(
				Constants.ENTITY_NAME_FIELD, entityName,
				Constants.ENTITY_TYPE_FIELD, getEntityType()
			);
		}

	}

}
