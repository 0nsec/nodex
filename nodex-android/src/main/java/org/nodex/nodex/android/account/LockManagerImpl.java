package org.nodex.android.account;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.Service;
import org.nodex.core.api.settings.Settings;
import org.nodex.core.api.settings.SettingsManager;
import org.nodex.core.api.settings.event.SettingsUpdatedEvent;
import org.nodex.R;
import org.nodex.android.NodexService;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.api.android.LockManager;
import org.nodex.nullsafety.MethodsNotNullByDefault;
import org.nodex.nullsafety.ParametersNotNullByDefault;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getService;
import static android.content.Context.ALARM_SERVICE;
import static android.os.Process.myPid;
import static android.os.SystemClock.elapsedRealtime;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.logging.Level.WARNING;
import static org.nodex.core.util.AndroidUtils.getImmutableFlags;
import static org.nodex.core.util.LogUtils.logException;
import static org.nodex.android.settings.SecurityFragment.PREF_SCREEN_LOCK;
import static org.nodex.android.settings.SecurityFragment.PREF_SCREEN_LOCK_TIMEOUT;
import static org.nodex.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static org.nodex.android.util.UiUtils.hasScreenLock;
@ThreadSafe
@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class LockManagerImpl implements LockManager, Service, EventListener {
	private static final Logger LOG =
			Logger.getLogger(LockManagerImpl.class.getName());
	private final Context appContext;
	private final SettingsManager settingsManager;
	private final AndroidNotificationManager notificationManager;
	@DatabaseExecutor
	private final Executor dbExecutor;
	private final AlarmManager alarmManager;
	private final PendingIntent lockIntent;
	private final int timeoutNever, timeoutDefault;
	private volatile boolean locked = false;
	private volatile boolean lockableSetting = false;
	private volatile int timeoutMinutes;
	private int activitiesRunning = 0;
	private boolean alarmSet = false;
	private long idleTime;
	private final MutableLiveData<Boolean> lockable = new MutableLiveData<>();
	@Inject
	LockManagerImpl(Application app, SettingsManager settingsManager,
			AndroidNotificationManager notificationManager,
			@DatabaseExecutor Executor dbExecutor) {
		appContext = app.getApplicationContext();
		this.settingsManager = settingsManager;
		this.notificationManager = notificationManager;
		this.dbExecutor = dbExecutor;
		alarmManager =
				(AlarmManager) appContext.getSystemService(ALARM_SERVICE);
		Intent i =
				new Intent(ACTION_LOCK, null, appContext, NodexService.class);
		i.putExtra(EXTRA_PID, myPid());
		lockIntent = getService(appContext, 0, i,
				getImmutableFlags(FLAG_UPDATE_CURRENT));
		timeoutNever = Integer.parseInt(
				appContext.getString(R.string.pref_lock_timeout_value_never));
		timeoutDefault = Integer.parseInt(
				appContext.getString(R.string.pref_lock_timeout_value_default));
		timeoutMinutes = timeoutNever;
		lockable.setValue(false);
	}
	@Override
	public void startService() {
		loadSettings();
	}
	@Override
	public void stopService() {
		timeoutMinutes = timeoutNever;
		if (alarmSet) alarmManager.cancel(lockIntent);
	}
	@UiThread
	@Override
	public void onActivityStart() {
		if (!locked && activitiesRunning == 0 && timeoutEnabled() &&
				timedOut()) {
			setLocked(true);
		}
		activitiesRunning++;
		if (alarmSet) {
			alarmManager.cancel(lockIntent);
			alarmSet = false;
		}
	}
	@UiThread
	@Override
	public void onActivityStop() {
		activitiesRunning--;
		if (activitiesRunning == 0) {
			idleTime = elapsedRealtime();
			if (!locked && timeoutEnabled()) {
				if (alarmSet) alarmManager.cancel(lockIntent);
				long triggerAt =
						elapsedRealtime() + MINUTES.toMillis(timeoutMinutes);
				alarmManager.set(ELAPSED_REALTIME, triggerAt, lockIntent);
				alarmSet = true;
			}
		}
	}
	@Override
	public LiveData<Boolean> isLockable() {
		return lockable;
	}
	@UiThread
	@Override
	public void checkIfLockable() {
		boolean oldValue = lockable.getValue();
		boolean newValue = hasScreenLock(appContext) && lockableSetting;
		if (oldValue != newValue) {
			lockable.setValue(newValue);
		}
	}
	@Override
	public boolean isLocked() {
		if (locked && !hasScreenLock(appContext)) {
			lockable.postValue(false);
			locked = false;
		} else if (!locked && activitiesRunning == 0 && timeoutEnabled() &&
				timedOut()) {
			setLocked(true);
		}
		return locked;
	}
	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
		notificationManager.updateForegroundNotification(locked);
	}
	@Override
	public void eventOccurred(Event event) {
		if (event instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) event;
			if (s.getNamespace().equals(SETTINGS_NAMESPACE)) {
				applySettings(s.getSettings());
			}
		}
	}
	private void loadSettings() {
		dbExecutor.execute(() -> {
			try {
				applySettings(settingsManager.getSettings(SETTINGS_NAMESPACE));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
	private void applySettings(Settings settings) {
		lockableSetting = settings.getBoolean(PREF_SCREEN_LOCK, false);
		boolean newValue = hasScreenLock(appContext) && lockableSetting;
		lockable.postValue(newValue);
		timeoutMinutes = settings.getInt(PREF_SCREEN_LOCK_TIMEOUT,
				timeoutDefault);
	}
	private boolean timeoutEnabled() {
		return timeoutMinutes != timeoutNever && lockable.getValue();
	}
	private boolean timedOut() {
		return elapsedRealtime() - idleTime > MINUTES.toMillis(timeoutMinutes);
	}
}