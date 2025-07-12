package org.briarproject.briar.api.android;
import android.app.Activity;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
public interface LockManager {
	String ACTION_LOCK = "lock";
	String EXTRA_PID = "PID";
	@UiThread
	void onActivityStart();
	@UiThread
	void onActivityStop();
	LiveData<Boolean> isLockable();
	@UiThread
	void checkIfLockable();
	boolean isLocked();
	void setLocked(boolean locked);
}