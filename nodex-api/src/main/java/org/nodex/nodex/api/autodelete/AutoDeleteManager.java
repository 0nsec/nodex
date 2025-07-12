package org.nodex.api.autodelete;
import org.nodex.core.api.contact.ContactId;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.sync.ClientId;
import org.nodex.nullsafety.NotNullByDefault;
import static java.util.concurrent.TimeUnit.DAYS;
@NotNullByDefault
public interface AutoDeleteManager {
	ClientId CLIENT_ID = new ClientId("org.nodex.autodelete");
	int MAJOR_VERSION = 0;
	int MINOR_VERSION = 0;
	long DEFAULT_TIMER_DURATION = DAYS.toMillis(7);
	long getAutoDeleteTimer(Transaction txn, ContactId c) throws DbException;
	long getAutoDeleteTimer(Transaction txn, ContactId c, long timestamp)
			throws DbException;
	void setAutoDeleteTimer(Transaction txn, ContactId c, long timer)
			throws DbException;
	void receiveAutoDeleteTimer(Transaction txn, ContactId c, long timer,
			long timestamp) throws DbException;
}