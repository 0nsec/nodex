package org.nodex.api.contact;

import org.nodex.api.nullsafety.NotNullByDefault;

/**
 * A contact in the system
 */
@NotNullByDefault
public class Contact {
    private final ContactId id;
    private final String name;
    private final String alias;
    private final boolean verified;

    public Contact(ContactId id, String name, String alias, boolean verified) {
        if (id == null) throw new IllegalArgumentException("Contact ID cannot be null");
        if (name == null || name.trim().isEmpty()) 
            throw new IllegalArgumentException("Name cannot be null or empty");
        this.id = id;
        this.name = name.trim();
        this.alias = alias != null ? alias.trim() : null;
        this.verified = verified;
    }

    public ContactId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return id.equals(contact.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Contact{id=" + id + ", name='" + name + "', verified=" + verified + '}';
    }
}
