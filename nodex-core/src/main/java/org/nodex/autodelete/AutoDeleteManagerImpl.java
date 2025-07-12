package org.nodex.autodelete;
import org.nodex.api.FormatException;
import org.nodex.api.client.ClientHelper;
import org.nodex.api.client.ContactGroupFactory;
import org.nodex.api.contact.Contact;
import org.nodex.api.contact.ContactId;
import org.nodex.api.contact.ContactManager.ContactHook;
import org.nodex.api.data.BdfDictionary;
import org.nodex.api.data.BdfEntry;
import org.nodex.api.db.DatabaseComponent;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import org.nodex.api.sync.Group;
import org.nodex.api.sync.GroupFactory;
import org.nodex.api.autodelete.AutoDeleteManager;
import org.nodex.api.autodelete.event.AutoDeleteTimerMirroredEvent;
import org.nodex.nullsafety.NotNullByDefault;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.nodex.api.autodelete.AutoDeleteConstants.MAX_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.MIN_AUTO_DELETE_TIMER_MS;
import static org.nodex.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static org.nodex.autodelete.AutoDeleteConstants.GROUP_KEY_PREVIOUS_TIMER;
import static org.nodex.autodelete.AutoDeleteConstants.GROUP_KEY_TIMER;
import static org.nodex.autodelete.AutoDeleteConstants.GROUP_KEY_TIMESTAMP;
import static org.nodex.autodelete.AutoDeleteConstants.NO_PREVIOUS_TIMER;
@Immutable
@NotNullByDefault
class AutoDeleteManagerImpl
		implements AutoDeleteManager, OpenDatabaseHook, ContactHook {
	private static final Logger LOG =
			getLogger(AutoDeleteManagerImpl.class.getName());
	private final DatabaseComponent db;
	private final ClientHelper clientHelper;
	private final GroupFactory groupFactory;
	private final Group localGroup;
	@Inject
	AutoDeleteManagerImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			GroupFactory groupFactory,
			ContactGroupFactory contactGroupFactory) {
		this.db = db;
		this.clientHelper = clientHelper;
		this.groupFactory = groupFactory;
		localGroup = contactGroupFactory.createLocalGroup(CLIENT_ID,
				MAJOR_VERSION);
	}
	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		if (db.containsGroup(txn, localGroup.getId())) return;
		db.addGroup(txn, localGroup);
		for (Contact c : db.getContacts(txn)) addingContact(txn, c);
	}
	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		Group g = getGroup(c);
		db.addGroup(txn, g);
		clientHelper.setContactId(txn, g.getId(), c.getId());
	}
	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		db.removeGroup(txn, getGroup(c));
	}
	@Override
	public long getAutoDeleteTimer(Transaction txn, ContactId c)
			throws DbException {
		try {
			Group g = getGroup(db.getContact(txn, c));
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g.getId());
			return meta.getLong(GROUP_KEY_TIMER, NO_AUTO_DELETE_TIMER);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public long getAutoDeleteTimer(Transaction txn, ContactId c, long timestamp)
			throws DbException {
		try {
			Group g = getGroup(db.getContact(txn, c));
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g.getId());
			long timer = meta.getLong(GROUP_KEY_TIMER, NO_AUTO_DELETE_TIMER);
			if (LOG.isLoggable(INFO)) {
				LOG.info("Sending message with auto-delete timer " + timer);
			}
			meta = BdfDictionary.of(
					new BdfEntry(GROUP_KEY_TIMESTAMP, timestamp),
					new BdfEntry(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER));
			clientHelper.mergeGroupMetadata(txn, g.getId(), meta);
			return timer;
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public void setAutoDeleteTimer(Transaction txn, ContactId c, long timer)
			throws DbException {
		validateTimer(timer);
		try {
			Group g = getGroup(db.getContact(txn, c));
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g.getId());
			long oldTimer = meta.getLong(GROUP_KEY_TIMER, NO_AUTO_DELETE_TIMER);
			if (timer == oldTimer) return;
			if (LOG.isLoggable(INFO)) {
				LOG.info("Setting auto-delete timer to " + timer);
			}
			meta = BdfDictionary.of(
					new BdfEntry(GROUP_KEY_TIMER, timer),
					new BdfEntry(GROUP_KEY_PREVIOUS_TIMER, oldTimer));
			clientHelper.mergeGroupMetadata(txn, g.getId(), meta);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	@Override
	public void receiveAutoDeleteTimer(Transaction txn, ContactId c,
			long timer, long timestamp) throws DbException {
		validateTimer(timer);
		try {
			Group g = getGroup(db.getContact(txn, c));
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g.getId());
			long oldTimestamp = meta.getLong(GROUP_KEY_TIMESTAMP, 0L);
			if (timestamp <= oldTimestamp) return;
			long oldTimer =
					meta.getLong(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER);
			meta = new BdfDictionary();
			if (oldTimer == NO_PREVIOUS_TIMER) {
				if (LOG.isLoggable(INFO)) {
					LOG.info("Mirroring auto-delete timer " + timer);
				}
				meta.put(GROUP_KEY_TIMER, timer);
				txn.attach(new AutoDeleteTimerMirroredEvent(c, timer));
			} else if (timer != oldTimer) {
				if (LOG.isLoggable(INFO)) {
					LOG.info("Mirroring auto-delete timer " + timer
							+ " and forgetting unsent change");
				}
				meta.put(GROUP_KEY_TIMER, timer);
				meta.put(GROUP_KEY_PREVIOUS_TIMER, NO_PREVIOUS_TIMER);
				txn.attach(new AutoDeleteTimerMirroredEvent(c, timer));
			}
			meta.put(GROUP_KEY_TIMESTAMP, timestamp);
			clientHelper.mergeGroupMetadata(txn, g.getId(), meta);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
	private Group getGroup(Contact c) {
		byte[] descriptor = c.getAuthor().getId().getBytes();
		return groupFactory.createGroup(CLIENT_ID, MAJOR_VERSION, descriptor);
	}
	private void validateTimer(long timer) {
		if (timer != NO_AUTO_DELETE_TIMER &&
				(timer < MIN_AUTO_DELETE_TIMER_MS ||
						timer > MAX_AUTO_DELETE_TIMER_MS)) {
			throw new IllegalArgumentException();
		}
	}
}