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

package io.openk9.datasource.hibernate.interceptor;

import akka.actor.typed.ActorSystem;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.cache.P2PCache;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

import java.io.Serializable;

@PersistenceUnitExtension
@ApplicationScoped
public class InvalidateCacheInterceptor extends EmptyInterceptor {

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	Logger log;

	@Override
	public void onDelete(
		Object entity, Serializable id, Object[] state, String[] propertyNames,
		Type[] types) {

		if (log.isTraceEnabled()) {
			log.trace("intercepted delete");
		}

		invalidateLocalCache();

		super.onDelete(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onSave(
		Object entity, Serializable id, Object[] state, String[] propertyNames,
		Type[] types) {

		if (log.isTraceEnabled()) {
			log.trace("intercepted save");
		}

		invalidateLocalCache();

		return super.onSave(entity, id, state, propertyNames, types);
	}


	@Override
	public boolean onFlushDirty(
		Object entity, Serializable id, Object[] currentState,
		Object[] previousState, String[] propertyNames, Type[] types) {

		if (log.isTraceEnabled()) {
			log.trace("intercepted flushDirty");
		}

		invalidateLocalCache();

		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}

	@Override
	public void onCollectionUpdate(Object collection, Serializable key)
	throws CallbackException {

		if (log.isTraceEnabled()) {
			log.trace("intercepted collectionUpdate");
		}

		invalidateLocalCache();

		super.onCollectionUpdate(collection, key);
	}

	private void invalidateLocalCache() {
		ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();
		P2PCache.askInvalidation(actorSystem);
	}

}
