package org.nodex.api.system;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@NotNullByDefault
public @interface Wakeful {
}
