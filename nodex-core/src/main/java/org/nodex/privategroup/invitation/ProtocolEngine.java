package org.nodex.privategroup.invitation;
import org.nodex.api.FormatException;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.Nullable;
@NotNullByDefault
interface ProtocolEngine<S extends Session<?>> {
	S onInviteAction(Transaction txn, S session, @Nullable String text,
			long timestamp, byte[] signature, long autoDeleteTimer)
			throws DbException;
	S onJoinAction(Transaction txn, S session) throws DbException;
	S onLeaveAction(Transaction txn, S session, boolean isAutoDecline)
			throws DbException;
	S onMemberAddedAction(Transaction txn, S session) throws DbException;
	S onInviteMessage(Transaction txn, S session, InviteMessage m)
			throws DbException, FormatException;
	S onJoinMessage(Transaction txn, S session, JoinMessage m)
			throws DbException, FormatException;
	S onLeaveMessage(Transaction txn, S session, LeaveMessage m)
			throws DbException, FormatException;
	S onAbortMessage(Transaction txn, S session, AbortMessage m)
			throws DbException, FormatException;
}
