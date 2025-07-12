package org.nodex.api.identity;

import org.nodex.api.contact.Contact;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.AuthorId;
import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * Manager for author information and authentication.
 */
@NotNullByDefault
public interface AuthorManager {

    /**
     * Returns the {@link AuthorInfo} for the given author.
     */
    AuthorInfo getAuthorInfo(AuthorId authorId) throws DbException;

    /**
     * Returns the {@link AuthorInfo} for the given author.
     */
    AuthorInfo getAuthorInfo(Transaction txn, AuthorId authorId) throws DbException;

    /**
     * Returns the {@link AuthorInfo} for the given contact.
     */
    AuthorInfo getAuthorInfo(Contact contact) throws DbException;

    /**
     * Returns the {@link AuthorInfo} for the given contact.
     */
    AuthorInfo getAuthorInfo(Transaction txn, Contact contact) throws DbException;

    /**
     * Returns the {@link AuthorInfo} for the local author.
     */
    AuthorInfo getMyAuthorInfo() throws DbException;

    /**
     * Returns the {@link AuthorInfo} for the local author.
     */
    AuthorInfo getMyAuthorInfo(Transaction txn) throws DbException;
}
