package io.openk9.entity.manager.model;

import io.openk9.entity.manager.model.payload.EntityRequest;

public abstract class BaseEntity {

	protected BaseEntity(EntityRequest entityRequest, long tenantId) {
		_entityRequest = entityRequest;
		_tenantId = tenantId;
	}

	public abstract boolean isExistEntity();

	public abstract boolean isNotExistEntity();

	public long getTenantId() {
		return _tenantId;
	}

	public EntityRequest getEntityRequest() {
		return _entityRequest;
	}

	public static class ExistEntity extends BaseEntity {

		private ExistEntity(
			EntityRequest entityRequest,
			long tenantId,
			Entity entity,
			DocumentEntity DocumentEntity) {
			super(entityRequest, tenantId);
			_entity = entity;
			_documentEntity = DocumentEntity;
		}

		@Override
		public boolean isExistEntity() {
			return true;
		}

		@Override
		public boolean isNotExistEntity() {
			return false;
		}

		public Entity getEntity() {
			return _entity;
		}

		public DocumentEntity getDocumentEntity() {
			return _documentEntity;
		}

		private final Entity _entity;

		private final DocumentEntity _documentEntity;

	}

	public static class NotExistEntity extends BaseEntity {

		private NotExistEntity(EntityRequest entityRequest, long tenantId) {
			super(entityRequest, tenantId);
		}

		@Override
		public boolean isExistEntity() {
			return false;
		}

		@Override
		public boolean isNotExistEntity() {
			return true;
		}

	}

	public static BaseEntity exist(
		EntityRequest entityRequest, long tenantId, Entity entity,
		DocumentEntity DocumentEntity) {
		return new ExistEntity(entityRequest, tenantId, entity, DocumentEntity);
	}

	public static BaseEntity notExist(EntityRequest entityRequest, long tenantId) {
		return new NotExistEntity(entityRequest, tenantId);
	}

	private final EntityRequest _entityRequest;
	private final long _tenantId;

}
