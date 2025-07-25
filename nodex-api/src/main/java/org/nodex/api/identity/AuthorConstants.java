package org.nodex.api.identity;

/**
 * Constants for author identification
 */
public interface AuthorConstants {

    /**
     * The maximum length of an author's name in UTF-8 bytes.
     */
    int MAX_AUTHOR_NAME_LENGTH = 50;

    /**
     * The maximum length of a public key in bytes.
     */
    int MAX_PUBLIC_KEY_LENGTH = 1024;

    /**
     * The maximum length of a signature in bytes.
     */
    int MAX_SIGNATURE_LENGTH = 1024;
}
