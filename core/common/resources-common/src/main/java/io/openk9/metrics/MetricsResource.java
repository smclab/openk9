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

package io.openk9.metrics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

@ApplicationScoped
@Path("/metrics")
public class MetricsResource {

	@Path("/threads")
	@GET
	public String threads() {

		ThreadMXBean threadsBean = ManagementFactory.getThreadMXBean();

		ThreadInfo[] threadInfoList =
			threadsBean.dumpAllThreads(true, true);

		StringBuilder sb = new StringBuilder();

		for (ThreadInfo threadInfo : threadInfoList) {
			sb.append(_toString(threadInfo));
		}

		return sb.toString();

	}

	private String _toString(ThreadInfo threadInfo) {
		StringBuilder sb =
			new StringBuilder(
				"\"" + threadInfo.getThreadName() + "\"" +
				(threadInfo.isDaemon() ? " daemon" : "") +
				" prio=" + threadInfo.getPriority() +
				" Id=" + threadInfo.getThreadId() + " " +
				threadInfo.getThreadState()
			);

		if (threadInfo.getLockName() != null) {
			sb.append(" on ").append(threadInfo.getLockName());
		}
		if (threadInfo.getLockOwnerName() != null) {
			sb.append(" owned by \"").append(
				threadInfo.getLockOwnerName()).append("\" Id=").append(
				threadInfo.getLockOwnerId());
		}
		if (threadInfo.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (threadInfo.isInNative()) {
			sb.append(" (in native)");
		}
		sb.append('\n');

		StackTraceElement[] stackTrace = threadInfo.getStackTrace();

		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			sb
				.append("\tat ")
				.append(ste.toString())
				.append('\n');
			if (i == 0 && threadInfo.getLockInfo() != null) {
				Thread.State ts = threadInfo.getThreadState();
				switch (ts) {
					case BLOCKED:
						sb
							.append("\t-  blocked on ")
							.append(threadInfo.getLockInfo())
							.append('\n');
						break;
					case WAITING:
					case TIMED_WAITING:
						sb
							.append("\t-  waiting on ")
							.append(threadInfo.getLockInfo())
							.append('\n');
						break;
				}
			}

			MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();

			for (MonitorInfo mi : lockedMonitors) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked ").append(mi);
					sb.append('\n');
				}
			}
		}

		LockInfo[] locks = threadInfo.getLockedSynchronizers();
		if (locks.length > 0) {
			sb
				.append("\n\tNumber of locked synchronizers = ")
				.append(locks.length)
				.append('\n');

			for (LockInfo li : locks) {
				sb
					.append("\t- ")
					.append(li)
					.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

}
