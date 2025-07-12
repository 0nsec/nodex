package org.nodex.android.contact.add.remote;
import android.app.Application;
import org.nodex.core.api.FormatException;
import org.nodex.core.api.UnsupportedVersionException;
import org.nodex.core.api.contact.ContactManager;
import org.nodex.core.api.contact.PendingContact;
import org.nodex.core.api.db.DatabaseExecutor;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.NoSuchPendingContactException;
import org.nodex.core.api.db.TransactionManager;
import org.nodex.core.api.lifecycle.LifecycleManager;
import org.nodex.core.api.system.AndroidExecutor;
import org.nodex.android.viewmodel.DbViewModel;
import org.nodex.android.viewmodel.LiveEvent;
import org.nodex.android.viewmodel.LiveResult;
import org.nodex.android.viewmodel.MutableLiveEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javax.inject.Inject;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static org.nodex.core.api.contact.HandshakeLinkConstants.LINK_REGEX;
import static org.nodex.core.util.LogUtils.logException;
@NotNullByDefault
public class AddContactViewModel extends DbViewModel {
	private final static Logger LOG =
			getLogger(AddContactViewModel.class.getName());
	private final ContactManager contactManager;
	private final MutableLiveData<String> handshakeLink =
			new MutableLiveData<>();
	private final MutableLiveEvent<Boolean> remoteLinkEntered =
			new MutableLiveEvent<>();
	private final MutableLiveData<LiveResult<Boolean>> addContactResult =
			new MutableLiveData<>();
	@Nullable
	private String remoteHandshakeLink;
	@Inject
	AddContactViewModel(Application application,
			ContactManager contactManager,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
	}
	void onCreate() {
		if (handshakeLink.getValue() == null) loadHandshakeLink();
	}
	private void loadHandshakeLink() {
		runOnDbThread(() -> {
			try {
				handshakeLink.postValue(contactManager.getHandshakeLink());
			} catch (DbException e) {
				handleException(e);
			}
		});
	}
	LiveData<String> getHandshakeLink() {
		return handshakeLink;
	}
	@Nullable
	String getRemoteHandshakeLink() {
		return remoteHandshakeLink;
	}
	void setRemoteHandshakeLink(String link) {
		remoteHandshakeLink = link;
	}
	boolean isValidRemoteContactLink(@Nullable CharSequence link) {
		return link != null && LINK_REGEX.matcher(link).find();
	}
	LiveEvent<Boolean> getRemoteLinkEntered() {
		return remoteLinkEntered;
	}
	void onRemoteLinkEntered() {
		if (remoteHandshakeLink == null) throw new IllegalStateException();
		remoteLinkEntered.setEvent(true);
	}
	void addContact(String nickname) {
		if (remoteHandshakeLink == null) throw new IllegalStateException();
		runOnDbThread(() -> {
			try {
				contactManager.addPendingContact(remoteHandshakeLink, nickname);
				addContactResult.postValue(new LiveResult<>(true));
			} catch (UnsupportedVersionException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			} catch (DbException | FormatException
					| GeneralSecurityException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			}
		});
	}
	LiveData<LiveResult<Boolean>> getAddContactResult() {
		return addContactResult;
	}
	void updatePendingContact(String name, PendingContact p) {
		runOnDbThread(() -> {
			try {
				contactManager.removePendingContact(p.getId());
				addContact(name);
			} catch (NoSuchPendingContactException e) {
				logException(LOG, WARNING, e);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			}
		});
	}
}