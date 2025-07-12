package org.nodex.api.identity;
import org.nodex.api.contact.Contact;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.identity.LocalAuthor;
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