package io.openk9.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
