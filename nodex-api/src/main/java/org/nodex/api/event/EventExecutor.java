package org.nodex.api.event;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import javax.inject.Qualifier;

@Qualifier
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotNullByDefault
public @interface EventExecutor {
}
