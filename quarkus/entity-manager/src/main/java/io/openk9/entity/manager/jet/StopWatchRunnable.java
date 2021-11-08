package io.openk9.entity.manager.jet;

import org.jboss.logging.Logger;

public interface StopWatchRunnable extends Runnable {

	default void run() {

		long ms = System.currentTimeMillis();

		run_();

		logger.info(this.getClass().getName() + ".run execution time: " + (System.currentTimeMillis() - ms) + "ms");

	}

	void run_();

	 static final Logger logger = Logger.getLogger(StopWatchRunnable.class);

}
