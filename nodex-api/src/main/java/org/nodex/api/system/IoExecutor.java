package org.nodex.api.system;

import org.nodex.api.nullsafety.NotNullByDefault;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import javax.inject.Qualifier;

/**
 * Annotation for I/O executor dependency injection.
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NotNullByDefault
public @interface IoExecutor {
}
