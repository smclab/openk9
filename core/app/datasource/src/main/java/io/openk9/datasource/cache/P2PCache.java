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

package io.openk9.datasource.cache;

import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.quarkus.cache.Cache;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.actor.typed.receptionist.Receptionist;
import org.apache.pekko.actor.typed.receptionist.ServiceKey;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

public class P2PCache extends AbstractBehavior<P2PCache.Command> {

	public static final ServiceKey<Command> SERVICE_KEY =
		ServiceKey.create(Command.class, "quarkus-cache-handler");

	public static Behavior<Command> create(Set<Cache> cacheSet) {
		return Behaviors.setup(ctx -> new P2PCache(ctx, cacheSet));
	}

	public static void askInvalidation(ActorSystem<?> actorSystem) {
		Receptionist receptionist = Receptionist.get(actorSystem);

		AskPattern.ask(
			receptionist.ref(),
			(ActorRef<Receptionist.Listing> replyTo) ->
				Receptionist.find(P2PCache.SERVICE_KEY, replyTo),
			Duration.ofSeconds(10),
			actorSystem.scheduler()
		).whenComplete(
			(listing, throwable) -> {
				if (throwable == null) {
					listing
						.getServiceInstances(P2PCache.SERVICE_KEY)
						.stream()
						.filter(ref -> ref.path().address().port().isEmpty())
						.forEach(ref -> ref.tell(P2PCache.InvalidateAllLocal.INSTANCE));
				}
				else {
					log.warn("Cannot trigger cache invalidation", throwable);
				}
			}
		);
	}

	public sealed interface Command {}
	private enum Start implements Command {INSTANCE}
	public enum InvalidateAllLocal implements Command {INSTANCE}
	public enum InvalidateAllRemotes implements Command, CborSerializable {INSTANCE}
	private record ListingAdapter(Receptionist.Listing response) implements Command {}

	private final ActorSystem<Void> system;
	private final Set<Cache> cacheSet;
	private Set<ActorRef<Command>> instances;

	public P2PCache(ActorContext<Command> context, Set<Cache> cacheSet) {
		super(context);
		this.system = context.getSystem();
		this.cacheSet = cacheSet;
		getContext().getSelf().tell(Start.INSTANCE);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessage(ListingAdapter.class, this::onListing)
			.onMessageEquals(InvalidateAllLocal.INSTANCE, this::onInvalidateAllLocal)
			.onMessageEquals(InvalidateAllRemotes.INSTANCE, this::onInvalidateAllOthers)
			.build();
	}

	private Behavior<Command> onStart() {
		receptionist().tell(Receptionist.register(SERVICE_KEY, getContext().getSelf()));

		ActorRef<Receptionist.Listing> messagedAdapter =
			getContext().messageAdapter(Receptionist.Listing.class, ListingAdapter::new);

		receptionist().tell(Receptionist.subscribe(SERVICE_KEY, messagedAdapter));

		return Behaviors.same();
	}

	private Behavior<Command> onListing(ListingAdapter listingAdapter) {
		Receptionist.Listing listing = listingAdapter.response();

		this.instances = listing
			.getServiceInstances(SERVICE_KEY)
			.stream()
			.filter(ref -> !ref.equals(this.getContext().getSelf()))
			.collect(Collectors.toSet());

		return Behaviors.same();
	}

	private Behavior<Command> onInvalidateAllLocal() {

		invalidateLocalCache();

		for (ActorRef<Command> instance : this.instances) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("Triggering cache invalidation on address: %s",
					instance.path().address()));
			}

			instance.tell(InvalidateAllRemotes.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onInvalidateAllOthers() {
		invalidateLocalCache();
		return Behaviors.same();
	}

	private void invalidateLocalCache() {
		if (log.isDebugEnabled()) {
			log.debug("Invalidating local cache set...");
		}

		for (Cache cache : this.cacheSet) {
			QuarkusCacheUtil.invalidateAllAsync(cache);
		}
	}

	private ActorRef<Receptionist.Command> receptionist() {
		return this.system.receptionist();
	}

	private final static Logger log = Logger.getLogger(P2PCache.class);
}
