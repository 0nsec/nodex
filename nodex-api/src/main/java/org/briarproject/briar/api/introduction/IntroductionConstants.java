package org.nodex.api.introduction;
import static org.nodex.core.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
public interface IntroductionConstants {
	int MAX_INTRODUCTION_TEXT_LENGTH = MAX_MESSAGE_BODY_LENGTH - 1024;
	String LABEL_SESSION_ID = "org.nodex.introduction/SESSION_ID";
	String LABEL_MASTER_KEY = "org.nodex.introduction/MASTER_KEY";
	String LABEL_ALICE_MAC_KEY =
			"org.nodex.introduction/ALICE_MAC_KEY";
	String LABEL_BOB_MAC_KEY =
			"org.nodex.introduction/BOB_MAC_KEY";
	String LABEL_AUTH_MAC = "org.nodex.introduction/AUTH_MAC";
	String LABEL_AUTH_SIGN = "org.nodex.introduction/AUTH_SIGN";
	String LABEL_AUTH_NONCE = "org.nodex.introduction/AUTH_NONCE";
	String LABEL_ACTIVATE_MAC =
			"org.nodex.introduction/ACTIVATE_MAC";
}