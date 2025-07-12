package org.nodex.api.android;
import org.nodex.core.api.lifecycle.Service;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface NetworkUsageMetrics extends Service {
	Metrics getMetrics();
	class Metrics {
		private final long sessionDurationMs, rxBytes, txBytes;
		public Metrics(long sessionDurationMs, long rxBytes,
				long txBytes) {
			this.sessionDurationMs = sessionDurationMs;
			this.rxBytes = rxBytes;
			this.txBytes = txBytes;
		}
		public long getSessionDurationMs() {
			return sessionDurationMs;
		}
		public long getRxBytes() {
			return rxBytes;
		}
		public long getTxBytes() {
			return txBytes;
		}
	}
}