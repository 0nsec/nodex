package org.nodex.android;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import com.bumptech.glide.Glide;
import org.nodex.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import org.nodex.core.api.account.AccountManager;
import org.nodex.core.api.crypto.SecretKey;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.lifecycle.LifecycleManager.StartResult;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.core.api.system.Clock;
import org.nodex.core.util.AndroidUtils;
import org.nodex.R;
import org.nodex.android.logout.HideUiActivity;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.android.LockManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.content.Intent.ACTION_SHUTDOWN;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Process.myPid;
import static androidx.core.app.NotificationCompat.VISIBILITY_SECRET;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.api.lifecycle.LifecycleManager.StartResult.ALREADY_RUNNING;
import static org.nodex.core.api.lifecycle.LifecycleManager.StartResult.SUCCESS;
import static org.nodex.core.util.AndroidUtils.isUiThread;
import static org.nodex.android.NodexApplication.ENTRY_ACTIVITY;
import static org.nodex.api.android.AndroidNotificationManager.ONGOING_CHANNEL_ID;
import static org.nodex.api.android.AndroidNotificationManager.ONGOING_CHANNEL_OLD_ID;
import static org.nodex.api.android.AndroidNotificationManager.ONGOING_NOTIFICATION_ID;
import static org.nodex.api.android.LockManager.ACTION_LOCK;
import static org.nodex.api.android.LockManager.EXTRA_PID;
import static org.nodex.nullsafety.NullSafety.requireNonNull;
public class NodexService extends Service {
	public static String EXTRA_START_RESULT =
			"org.nodex.START_RESULT";
	public static String EXTRA_STARTUP_FAILED =
			"org.nodex.STARTUP_FAILED";
	private static final Logger LOG =
			Logger.getLogger(NodexService.class.getName());
	private static final long MIN_GLIDE_CACHE_CLEAR_INTERVAL_MS = 5000;
	private final AtomicBoolean created = new AtomicBoolean(false);
	private final Binder binder = new NodexBinder();
	@Nullable
	private BroadcastReceiver receiver = null;
	private NodexApplication app;
	@Inject
	AndroidNotificationManager notificationManager;
	@Inject
	AccountManager accountManager;
	@Inject
	LockManager lockManager;
	@Inject
	AndroidWakeLockManager wakeLockManager;
	@Inject
	volatile LifecycleManager lifecycleManager;
	@Inject
	volatile AndroidExecutor androidExecutor;
	@Inject
	volatile Clock clock;
	private volatile boolean started = false;
	private volatile long glideCacheCleared = 0;
	@Override
	public void onCreate() {
		super.onCreate();
		app = (NodexApplication) getApplication();
		app.getApplicationComponent().inject(this);
		LOG.info("Created");
		if (created.getAndSet(true)) {
			LOG.info("Already created");
			stopSelf();
			return;
		}
		SecretKey dbKey = accountManager.getDatabaseKey();
		if (dbKey == null) {
			LOG.info("No database key");
			stopSelf();
			return;
		}
		wakeLockManager.runWakefully(() -> {
			if (SDK_INT >= 26) {
				NotificationManager nm = (NotificationManager)
						requireNonNull(getSystemService(NOTIFICATION_SERVICE));
				nm.deleteNotificationChannel(ONGOING_CHANNEL_OLD_ID);
				NotificationChannel ongoingChannel = new NotificationChannel(
						ONGOING_CHANNEL_ID,
						getString(R.string.ongoing_notification_title),
						IMPORTANCE_LOW);
				ongoingChannel.setLockscreenVisibility(VISIBILITY_SECRET);
				ongoingChannel.setShowBadge(false);
				nm.createNotificationChannel(ongoingChannel);
			}
			Notification foregroundNotification =
					notificationManager.getForegroundNotification();
			startForeground(ONGOING_NOTIFICATION_ID, foregroundNotification);
			wakeLockManager.executeWakefully(() -> {
				StartResult result = lifecycleManager.startServices(dbKey);
				if (result == SUCCESS) {
					started = true;
				} else if (result == ALREADY_RUNNING) {
					LOG.warning("Already running");
					shutdownFromBackground();
				} else {
					if (LOG.isLoggable(WARNING))
						LOG.warning("Startup failed: " + result);
					showStartupFailure(result);
					stopSelf();
				}
			}, "LifecycleStartup");
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					LOG.info("Device is shutting down");
					shutdownFromBackground();
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_SHUTDOWN);
			filter.addAction("android.intent.action.QUICKBOOT_POWEROFF");
			filter.addAction("com.htc.intent.action.QUICKBOOT_POWEROFF");
			AndroidUtils.registerReceiver(getApplicationContext(), receiver,
					filter);
		}, "LifecycleStartup");
	}
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(Localizer.getInstance().setLocale(base));
		Localizer.getInstance().setLocale(this);
	}
	private void showStartupFailure(StartResult result) {
		androidExecutor.runOnUiThread(() -> {
			Intent i = new Intent(NodexService.this, ENTRY_ACTIVITY);
			i.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra(EXTRA_STARTUP_FAILED, true);
			i.putExtra(EXTRA_START_RESULT, result);
			startActivity(i);
		});
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (ACTION_LOCK.equals(intent.getAction())) {
			int pid = intent.getIntExtra(EXTRA_PID, -1);
			if (pid == myPid()) lockManager.setLocked(true);
			else if (LOG.isLoggable(WARNING)) {
				LOG.warning("Tried to lock process " + pid + " but this is " +
						myPid());
			}
		}
		return START_NOT_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		LOG.info("Destroyed");
		shutdown(false);
		stopForeground(true);
		if (receiver != null) {
			getApplicationContext().unregisterReceiver(receiver);
		}
	}
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		LOG.warning("Memory is low");
		maybeClearGlideCache();
		if (app.isRunningInBackground()) hideUi();
	}
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (level == TRIM_MEMORY_UI_HIDDEN) {
			LOG.info("Trim memory: UI hidden");
		} else if (level == TRIM_MEMORY_BACKGROUND) {
			LOG.info("Trim memory: added to LRU list");
		} else if (level == TRIM_MEMORY_MODERATE) {
			LOG.info("Trim memory: near middle of LRU list");
		} else if (level == TRIM_MEMORY_COMPLETE) {
			LOG.info("Trim memory: near end of LRU list");
		} else if (level == TRIM_MEMORY_RUNNING_MODERATE) {
			LOG.info("Trim memory: running moderately low");
			maybeClearGlideCache();
		} else if (level == TRIM_MEMORY_RUNNING_LOW) {
			LOG.info("Trim memory: running low");
			maybeClearGlideCache();
		} else if (level == TRIM_MEMORY_RUNNING_CRITICAL) {
			LOG.warning("Trim memory: running critically low");
			maybeClearGlideCache();
			if (app.isRunningInBackground()) hideUi();
		} else if (LOG.isLoggable(INFO)) {
			LOG.info("Trim memory: unknown level " + level);
		}
	}
	private void maybeClearGlideCache() {
		if (isUiThread()) {
			maybeClearGlideCacheUiThread();
		} else {
			LOG.warning("Low memory callback was not called on main thread");
			androidExecutor.runOnUiThread(this::maybeClearGlideCacheUiThread);
		}
	}
	@UiThread
	private void maybeClearGlideCacheUiThread() {
		long now = clock.currentTimeMillis();
		if (now - glideCacheCleared >= MIN_GLIDE_CACHE_CLEAR_INTERVAL_MS) {
			LOG.info("Clearing Glide cache");
			Glide.get(getApplicationContext()).clearMemory();
			glideCacheCleared = now;
		}
	}
	private void hideUi() {
		Intent i = new Intent(this, HideUiActivity.class);
		i.addFlags(FLAG_ACTIVITY_NEW_TASK
				| FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| FLAG_ACTIVITY_NO_ANIMATION
				| FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}
	private void shutdownFromBackground() {
		wakeLockManager.runWakefully(() -> {
			shutdown(true);
			hideUi();
			wakeLockManager.executeWakefully(() -> {
				try {
					if (started) lifecycleManager.waitForShutdown();
				} catch (InterruptedException e) {
					LOG.info("Interrupted while waiting for shutdown");
				}
				LOG.info("Exiting");
				if (!app.isInstrumentationTest()) {
					System.exit(0);
				}
			}, "BackgroundShutdown");
		}, "BackgroundShutdown");
	}
	public void waitForStartup() throws InterruptedException {
		lifecycleManager.waitForStartup();
	}
	public void waitForShutdown() throws InterruptedException {
		lifecycleManager.waitForShutdown();
	}
	public void shutdown(boolean stopAndroidService) {
		wakeLockManager.runWakefully(() -> {
			wakeLockManager.executeWakefully(() -> {
				if (started) lifecycleManager.stopServices();
				if (stopAndroidService) {
					androidExecutor.runOnUiThread(() -> stopSelf());
				}
			}, "LifecycleShutdown");
		}, "LifecycleShutdown");
	}
	public class NodexBinder extends Binder {
		public NodexService getService() {
			return NodexService.this;
		}
	}
	public static class NodexServiceConnection implements ServiceConnection {
		private final CountDownLatch binderLatch = new CountDownLatch(1);
		private volatile IBinder binder = null;
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			this.binder = binder;
			binderLatch.countDown();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		public IBinder waitForBinder() throws InterruptedException {
			binderLatch.await();
			return binder;
		}
	}
}