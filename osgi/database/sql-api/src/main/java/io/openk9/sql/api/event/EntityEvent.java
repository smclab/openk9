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

package io.openk9.sql.api.event;

public abstract class EntityEvent<T> {

	public enum Type {
		INSERT,UPDATE,DELETE
	}

	private EntityEvent(Class<?> entityClass) {
		_entityClass = entityClass;
	}

	public Class<?> getEntityClass() {
		return _entityClass;
	}

	public abstract T getValue();

	public abstract Type getType();

	public boolean isInsert() {
		return false;
	}

	public boolean isUpdate() {
		return false;
	}

	public boolean isDelete() {
		return false;
	}

	public static class InsertEvent<T> extends EntityEvent<T> {

		private InsertEvent(Class<?> entityClass, T entity) {
			super(entityClass);
			_newEntity = entity;
		}

		public T getNewEntity() {
			return _newEntity;
		}

		@Override
		public T getValue() {
			return getNewEntity();
		}

		@Override
		public Type getType() {
			return Type.INSERT;
		}

		@Override
		public boolean isInsert() {
			return true;
		}

		private final T _newEntity;

	}

	public static class DeleteEvent<T> extends EntityEvent<T> {

		private DeleteEvent(Class<?> entityClass, T entity) {
			super(entityClass);
			_deletedEntity = entity;
		}

		public T getDeletedEntity() {
			return _deletedEntity;
		}

		@Override
		public T getValue() {
			return getDeletedEntity();
		}

		@Override
		public Type getType() {
			return Type.DELETE;
		}

		@Override
		public boolean isDelete() {
			return true;
		}

		private final T _deletedEntity;

	}

	public static class UpdateEvent<T> extends EntityEvent<T> {

		private UpdateEvent(
			Class<?> entityClass, T updatedEntity) {
			super(entityClass);
			_updatedEntity = updatedEntity;

		}

		public T getUpdatedEntity() {
			return _updatedEntity;
		}

		@Override
		public T getValue() {
			return getUpdatedEntity();
		}

		@Override
		public Type getType() {
			return Type.UPDATE;
		}

		@Override
		public boolean isUpdate() {
			return true;
		}

		private final T _updatedEntity;

	}

	public static <T> InsertEvent<T> insert(Class<?> entityType, T newEntity) {
		return new InsertEvent<>(entityType, newEntity);
	}

	public static <T> DeleteEvent<T> delete(
		Class<?> entityType, T deletedEntity) {

		return new DeleteEvent<>(entityType, deletedEntity);
	}

	public static <T> UpdateEvent<T> update(
		Class<?> entityType, T updatedEntity) {

		return new UpdateEvent<>(entityType, updatedEntity);
	}

	private final Class<?> _entityClass;

}
