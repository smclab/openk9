package io.openk9.datasource.hibernate.interceptor;

import akka.actor.typed.ActorSystem;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.cache.P2PCache;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;

@PersistenceUnitExtension
@ApplicationScoped
public class InvalidateCacheInterceptor extends EmptyInterceptor {

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		invalidateLocalCache();
		return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		invalidateLocalCache();
		super.onDelete(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		invalidateLocalCache();
		return super.onSave(entity, id, state, propertyNames, types);
	}


	@Override
	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
		invalidateLocalCache();
		super.onCollectionUpdate(collection, key);
	}

	private void invalidateLocalCache() {
		ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();
		P2PCache.askInvalidation(actorSystem);
	}

}
