package org.nodex.android.removabledrive;
import android.app.Application;
import android.net.Uri;
import org.nodex.core.api.Consumer;
import org.nodex.core.api.account.AccountManager;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.plugin.file.RemovableDriveManager;
import org.nodex.core.api.plugin.file.RemovableDriveTask;
import org.nodex.core.api.plugin.file.RemovableDriveTask.State;
import org.nodex.core.api.properties.TransportProperties;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveEvent;
import org.nodex.android.viewmodel.MutableLiveEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import javax.inject.Inject;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static org.nodex.core.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static org.nodex.core.api.plugin.file.RemovableDriveConstants.PROP_URI;
@UiThread
@NotNullByDefault
class RemovableDriveViewModel extends DbViewModel {
	enum Action {SEND, RECEIVE}
	private final AccountManager accountManager;
	private final RemovableDriveManager manager;
	private final MutableLiveEvent<Action> action = new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> oldTaskResumed =
			new MutableLiveEvent<>();
	private final MutableLiveData<TransferDataState> state =
			new MutableLiveData<>();
	@Nullable
	private ContactId contactId = null;
	@Nullable
	private RemovableDriveTask task = null;
	@Nullable
	private Consumer<State> taskObserver = null;
	@Inject
	RemovableDriveViewModel(
			Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			AccountManager accountManager,
			RemovableDriveManager removableDriveManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.accountManager = accountManager;
		this.manager = removableDriveManager;
	}
	@Override
	protected void onCleared() {
		if (task != null) {
			Consumer<State> observer = requireNonNull(taskObserver);
			task.removeObserver(observer);
		}
	}
	@UiThread
	boolean hasNoState() {
		return action.getLastValue() == null && state.getValue() == null &&
				task == null;
	}
	void setContactId(ContactId contactId) {
		this.contactId = contactId;
	}
	@UiThread
	void startSendData() {
		action.setEvent(Action.SEND);
		task = manager.getCurrentWriterTask();
		if (task == null) {
			ContactId c = requireNonNull(contactId);
			runOnDbThread(() -> {
				try {
					if (!manager.isTransportSupportedByContact(c)) {
						state.postValue(new TransferDataState.NotSupported());
					} else if (manager.isWriterTaskNeeded(c)) {
						state.postValue(new TransferDataState.Ready());
					} else {
						state.postValue(new TransferDataState.NoDataToSend());
					}
				} catch (DbException e) {
					handleException(e);
				}
			});
		} else {
			taskObserver =
					s -> state.setValue(new TransferDataState.TaskAvailable(s));
			task.addObserver(taskObserver);
			oldTaskResumed.setEvent(true);
		}
	}
	@UiThread
	void startReceiveData() {
		action.setEvent(Action.RECEIVE);
		task = manager.getCurrentReaderTask();
		if (task == null) {
			state.setValue(new TransferDataState.Ready());
		} else {
			taskObserver =
					s -> state.setValue(new TransferDataState.TaskAvailable(s));
			task.addObserver(taskObserver);
			oldTaskResumed.setEvent(true);
		}
	}
	String getFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", US);
		return sdf.format(new Date());
	}
	@UiThread
	void exportData(Uri uri) {
		if (task != null) throw new IllegalStateException();
		taskObserver =
				s -> state.setValue(new TransferDataState.TaskAvailable(s));
		TransportProperties p = new TransportProperties();
		p.put(PROP_URI, uri.toString());
		ContactId c = requireNonNull(contactId);
		task = manager.startWriterTask(c, p);
		task.addObserver(taskObserver);
	}
	@UiThread
	void importData(Uri uri) {
		if (task != null) throw new IllegalStateException();
		taskObserver =
				s -> state.setValue(new TransferDataState.TaskAvailable(s));
		TransportProperties p = new TransportProperties();
		p.put(PROP_URI, uri.toString());
		task = manager.startReaderTask(p);
		task.addObserver(taskObserver);
	}
	LiveEvent<Action> getActionEvent() {
		return action;
	}
	LiveEvent<Boolean> getOldTaskResumedEvent() {
		return oldTaskResumed;
	}
	LiveData<TransferDataState> getState() {
		return state;
	}
	boolean isAccountSignedIn() {
		return accountManager.hasDatabaseKey() &&
				lifecycleManager.getLifecycleState().isAfter(STARTING_SERVICES);
	}
}