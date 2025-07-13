package org.nodex.api.crypto;

import org.nodex.api.nullsafety.NotNullByDefault;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public interface SignaturePrivateKey {
    byte[] getEncoded();
}
