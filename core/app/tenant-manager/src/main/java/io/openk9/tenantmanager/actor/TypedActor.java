package io.openk9.tenantmanager.actor;

import org.jboss.logging.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface TypedActor {
    interface Effect<T> extends Function<Behavior<T>, Behavior<T>> {}
    interface Behavior<T> extends Function<T, Effect<T>> { }
    interface Address<T> { void tell(T msg); }
    static <T> Effect<T> Become(Behavior<T> next) { return current -> next; }
    static <T> Effect<T> Stay() { return current -> current; }
    static <T> Effect<T> Die() { return Become(msg -> { logger.info("Dropping msg [" + msg + "] due to severe case of death."); return Stay(); }); }
    record System(Executor executor) {
        public <T> Address<T> actorOf(Function<Address<T>, Behavior<T>> initial) {
            abstract class AtomicRunnableAddress<T> implements Address<T>, Runnable {
                AtomicInteger on = new AtomicInteger(0);
            }

            return new AtomicRunnableAddress<T>() {
                // Our awesome little mailbox, free of blocking and evil
                final ConcurrentLinkedQueue<T> mbox = new ConcurrentLinkedQueue<>();
                Behavior<T> behavior = initial.apply(this);
                public void tell(T msg) {
                    mbox.offer(msg);
                    async();
                }  // Enqueue the message onto the mailbox and try to schedule for execution
                // Switch ourselves off, and then see if we should be rescheduled for execution
                public void run() {
                    try {
                        if (on.get() == 1) {
                            T m = (T) mbox.poll();
                            if (m != null) {
                                behavior = behavior.apply(m).apply(behavior);
                            }
                        }
                    }
                    finally {
                        on.set(0);
                        async();
                    }
                }
                // If there's something to process, and we're not already scheduled
                void async() {
                    if (!mbox.isEmpty() && on.compareAndSet(0, 1)) {
                        // Schedule to run on the Executor and back out on failure
                        try {
                            executor.execute(this);
                        }
                        catch (Throwable t) {
                            on.set(0);
                            throw t;
                        }
                    }
                }
            };
        }
    }

    Logger logger = Logger.getLogger(TypedActor.class);

}