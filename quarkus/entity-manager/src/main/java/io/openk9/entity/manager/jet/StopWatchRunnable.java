package io.openk9.entity.manager.jet;

import org.jboss.logging.Logger;

public interface StopWatchRunnable extends Runnable {

	default void run() {

		if (logger.isDebugEnabled()) {

			long ms = System.currentTimeMillis();

			run_();

			logger.debug(this.getClass().getName() + ".run execution time: " +
						(System.currentTimeMillis() - ms) + "ms");
		}
		else {
			run_();
		}

	}

	void run_();

	 Logger logger = Logger.getLogger(StopWatchRunnable.class);

}
