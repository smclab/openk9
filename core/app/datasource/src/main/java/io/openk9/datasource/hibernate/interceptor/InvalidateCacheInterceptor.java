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

import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.cache.P2PCache;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pekko.actor.typed.ActorSystem;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

@PersistenceUnitExtension
@ApplicationScoped
public class InvalidateCacheInterceptor implements Interceptor {

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	Logger log;

	public void onRemove(
		Object entity, Object id, Object[] state, String[] propertyNames,
		Type[] types) {

		if (log.isTraceEnabled()) {
			log.trace("intercepted delete");
		}

		invalidateLocalCache();
	}

	public boolean onPersist(
		Object entity, Object id, Object[] state, String[] propertyNames,
		Type[] types) {

		if (log.isTraceEnabled()) {
			log.trace("intercepted save");
		}

		invalidateLocalCache();
		return false;
	}


	public boolean onFlushDirty(
		Object entity, Object id, Object[] currentState,
		Object[] previousState, String[] propertyNames, Type[] types) {

		if (log.isTraceEnabled()) {
			log.trace("intercepted flushDirty");
		}

		invalidateLocalCache();
		return false;
	}

	public void onCollectionUpdate(Object collection, Object key)
	throws CallbackException {

		if (log.isTraceEnabled()) {
			log.trace("intercepted collectionUpdate");
		}

		invalidateLocalCache();
	}

	private void invalidateLocalCache() {
		ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();
		P2PCache.askInvalidation(actorSystem);
	}

}
