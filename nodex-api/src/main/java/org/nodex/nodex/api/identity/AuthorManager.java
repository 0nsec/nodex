package org.nodex.api.identity;
import org.nodex.core.api.contact.Contact;
import org.nodex.core.api.db.DbException;
import org.nodex.core.api.db.Transaction;
import org.nodex.core.api.identity.AuthorId;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.nullsafety.NotNullByDefault;
@NotNullByDefault
public interface AuthorManager {
	AuthorInfo getAuthorInfo(AuthorId a) throws DbException;
	AuthorInfo getAuthorInfo(Transaction txn, AuthorId a) throws DbException;
	AuthorInfo getAuthorInfo(Contact c) throws DbException;
	AuthorInfo getAuthorInfo(Transaction txn, Contact c)
			throws DbException;
	AuthorInfo getMyAuthorInfo() throws DbException;
	AuthorInfo getMyAuthorInfo(Transaction txn) throws DbException;
}