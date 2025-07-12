package org.nodex.api.privategroup;

import org.nodex.api.contact.ContactId;
import org.nodex.api.db.DbException;
import org.nodex.api.db.Transaction;
import org.nodex.api.identity.LocalAuthor;
import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.sync.ClientId;
import org.nodex.api.sync.GroupId;
import org.nodex.api.sync.MessageId;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Constants for private group functionality.
 */
@NotNullByDefault
class PrivateGroupConstants {

    /**
     * Maximum length for group invitation text.
     */
    static final int MAX_GROUP_INVITATION_TEXT_LENGTH = 500;

    /**
     * Maximum length for group name.
     */
    static final int MAX_GROUP_NAME_LENGTH = 100;

    /**
     * Maximum length for group description.
     */
    static final int MAX_GROUP_DESCRIPTION_LENGTH = 500;

    /**
     * Maximum number of members in a private group.
     */
    static final int MAX_GROUP_MEMBERS = 100;

    private PrivateGroupConstants() {
        // Prevent instantiation
    }
}

/**
 * Manager for private groups.
 */
@NotNullByDefault
public interface PrivateGroupManager {

    ClientId CLIENT_ID = new ClientId("org.nodex.privategroup");

    /**
     * Creates a private group.
     */
    PrivateGroup createPrivateGroup(String name, LocalAuthor creator) throws DbException;

    /**
     * Creates a private group.
     */
    PrivateGroup createPrivateGroup(Transaction txn, String name, 
                                   LocalAuthor creator) throws DbException;

    /**
     * Adds a private group.
     */
    void addPrivateGroup(PrivateGroup privateGroup) throws DbException;

    /**
     * Adds a private group.
     */
    void addPrivateGroup(Transaction txn, PrivateGroup privateGroup) throws DbException;

    /**
     * Removes a private group.
     */
    void removePrivateGroup(GroupId groupId) throws DbException;

    /**
     * Removes a private group.
     */
    void removePrivateGroup(Transaction txn, GroupId groupId) throws DbException;

    /**
     * Returns all private groups.
     */
    Collection<PrivateGroup> getPrivateGroups() throws DbException;

    /**
     * Returns all private groups.
     */
    Collection<PrivateGroup> getPrivateGroups(Transaction txn) throws DbException;

    /**
     * Sends a message to a private group.
     */
    void sendGroupMessage(GroupId groupId, String text, 
                         @Nullable MessageId parentId) throws DbException;

    /**
     * Sends a message to a private group.
     */
    void sendGroupMessage(Transaction txn, GroupId groupId, String text, 
                         @Nullable MessageId parentId) throws DbException;

    /**
     * Returns all messages in a private group.
     */
    Collection<GroupMessageHeader> getGroupMessages(GroupId groupId) throws DbException;

    /**
     * Returns all messages in a private group.
     */
    Collection<GroupMessageHeader> getGroupMessages(Transaction txn, 
                                                   GroupId groupId) throws DbException;

    /**
     * Returns all members of a private group.
     */
    Collection<GroupMember> getGroupMembers(GroupId groupId) throws DbException;

    /**
     * Returns all members of a private group.
     */
    Collection<GroupMember> getGroupMembers(Transaction txn, GroupId groupId) throws DbException;
    
    /**
     * Major version of the private group client.
     */
    int MAJOR_VERSION = 1;
    
    /**
     * Hook interface for private group events.
     */
    interface PrivateGroupHook {
        /**
         * Called when a private group is added.
         */
        void onPrivateGroupAdded(PrivateGroup privateGroup);
        
        /**
         * Called when a private group is removed.
         */
        void onPrivateGroupRemoved(PrivateGroup privateGroup);
        
        /**
         * Called when a member is added to a private group.
         */
        void onMemberAdded(GroupId groupId, GroupMember member);
        
        /**
         * Called when a member is removed from a private group.
         */
        void onMemberRemoved(GroupId groupId, GroupMember member);
    }
}
