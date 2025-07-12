package org.nodex.introduction;
import org.nodex.core.api.crypto.KeyPair;
import org.nodex.core.api.crypto.PrivateKey;
import org.nodex.core.api.crypto.SecretKey;
import org.nodex.core.api.identity.Author;
import org.nodex.core.api.identity.AuthorId;
import org.nodex.core.api.identity.LocalAuthor;
import org.nodex.api.client.SessionId;
import java.security.GeneralSecurityException;
interface IntroductionCrypto {
	SessionId getSessionId(Author introducer, Author local, Author remote);
	boolean isAlice(AuthorId local, AuthorId remote);
	KeyPair generateAgreementKeyPair();
	SecretKey deriveMasterKey(IntroduceeSession s)
			throws GeneralSecurityException;
	SecretKey deriveMacKey(SecretKey masterKey, boolean alice);
	byte[] authMac(SecretKey macKey, IntroduceeSession s,
			AuthorId localAuthorId);
	void verifyAuthMac(byte[] mac, IntroduceeSession s, AuthorId localAuthorId)
			throws GeneralSecurityException;
	byte[] sign(SecretKey macKey, PrivateKey privateKey)
			throws GeneralSecurityException;
	void verifySignature(byte[] signature, IntroduceeSession s)
			throws GeneralSecurityException;
	byte[] activateMac(IntroduceeSession s);
	void verifyActivateMac(byte[] mac, IntroduceeSession s)
			throws GeneralSecurityException;
}