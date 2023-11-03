package io.openk9.datasource.cache;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.util.QuarkusCacheUtil;
import io.quarkus.cache.Cache;
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
