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

package io.openk9.core.api.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class Async {

	public static void run(Runnable runnable) {
		run(runnable, false);
	}

	public static void await(Runnable runnable) {
		run(runnable, true);
	}

	public static void run(Runnable runnable, boolean await) {

		Future<Void> future =
			CompletableFuture.runAsync(runnable, COMMON_EXECUTOR);

		if (await) {
			try {
				future.get();
			}
			catch (Exception e) {
				if (_log.isErrorEnabled()) {
					_log.error(e.getMessage(), e);
				}
			}
		}

	}

	public static <T> CompletableFuture<T> future(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, COMMON_EXECUTOR);
	}

	public static <T> T await(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, COMMON_EXECUTOR).join();
	}

	private static final String COMMON_NAME = "OPENK9_SINGLE_THREAD";

	private static final ExecutorService COMMON_EXECUTOR =
		Executors.newSingleThreadExecutor(r -> new Thread(r, COMMON_NAME));

	private static final Logger _log = LoggerFactory.getLogger(
		Async.class.getName());

}
