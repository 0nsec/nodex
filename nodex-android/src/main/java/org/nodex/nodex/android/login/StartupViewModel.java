package org.nodex.android.login;
import android.app.Application;
import org.nodex.core.api.account.AccountManager;
import org.nodex.core.api.crypto.DecryptionException;
import org.nodex.core.api.crypto.DecryptionResult;
import org.nodex.core.api.event.Event;
import org.nodex.core.api.event.EventBus;
import org.nodex.core.api.event.EventListener;
import org.nodex.core.api.lifecycle.IoExecutor;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState;
import org.nodex.core.api.lifecycle.event.LifecycleEvent;
import org.nodex.android.viewmodel.LiveEvent;
import org.nodex.android.viewmodel.MutableLiveEvent;
import org.nodex.api.android.AndroidNotificationManager;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static org.nodex.core.api.crypto.DecryptionResult.SUCCESS;
import static org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState.COMPACTING_DATABASE;
import static org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState.MIGRATING_DATABASE;
import static org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static org.nodex.android.login.StartupViewModel.State.COMPACTING;
import static org.nodex.android.login.StartupViewModel.State.MIGRATING;
import static org.nodex.android.login.StartupViewModel.State.SIGNED_IN;
import static org.nodex.android.login.StartupViewModel.State.SIGNED_OUT;
import static org.nodex.android.login.StartupViewModel.State.STARTED;
import static org.nodex.android.login.StartupViewModel.State.STARTING;
@NotNullByDefault
public class StartupViewModel extends AndroidViewModel
		implements EventListener {
	enum State {SIGNED_OUT, SIGNED_IN, STARTING, MIGRATING, COMPACTING, STARTED}
	private final AccountManager accountManager;
	private final AndroidNotificationManager notificationManager;
	private final EventBus eventBus;
	@IoExecutor
	private final Executor ioExecutor;
	private final MutableLiveEvent<DecryptionResult> passwordValidated =
			new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> accountDeleted =
			new MutableLiveEvent<>();
	private final MutableLiveData<State> state = new MutableLiveData<>();
	@Inject
	StartupViewModel(Application app,
			AccountManager accountManager,
			LifecycleManager lifecycleManager,
			AndroidNotificationManager notificationManager,
			EventBus eventBus,
			@IoExecutor Executor ioExecutor) {
		super(app);
		this.accountManager = accountManager;
		this.notificationManager = notificationManager;
		this.eventBus = eventBus;
		this.ioExecutor = ioExecutor;
		updateState(lifecycleManager.getLifecycleState());
		eventBus.addListener(this);
	}
	@Override
	protected void onCleared() {
		eventBus.removeListener(this);
	}
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof LifecycleEvent) {
			LifecycleState s = ((LifecycleEvent) e).getLifecycleState();
			updateState(s);
		}
	}
	@UiThread
	private void updateState(LifecycleState s) {
		if (accountManager.hasDatabaseKey()) {
			if (s.isAfter(STARTING_SERVICES)) state.setValue(STARTED);
			else if (s == MIGRATING_DATABASE) state.setValue(MIGRATING);
			else if (s == COMPACTING_DATABASE) state.setValue(COMPACTING);
			else state.setValue(STARTING);
		} else {
			state.setValue(SIGNED_OUT);
		}
	}
	boolean accountExists() {
		return accountManager.accountExists();
	}
	void clearSignInNotification() {
		notificationManager.blockSignInNotification();
		notificationManager.clearSignInNotification();
	}
	void validatePassword(String password) {
		ioExecutor.execute(() -> {
			try {
				accountManager.signIn(password);
				passwordValidated.postEvent(SUCCESS);
				state.postValue(SIGNED_IN);
			} catch (DecryptionException e) {
				passwordValidated.postEvent(e.getDecryptionResult());
			}
		});
	}
	LiveEvent<DecryptionResult> getPasswordValidated() {
		return passwordValidated;
	}
	LiveEvent<Boolean> getAccountDeleted() {
		return accountDeleted;
	}
	LiveData<State> getState() {
		return state;
	}
	@UiThread
	void deleteAccount() {
		accountManager.deleteAccount();
		accountDeleted.setEvent(true);
	}
}