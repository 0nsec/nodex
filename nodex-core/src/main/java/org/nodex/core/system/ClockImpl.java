package org.nodex.core.system;

import org.nodex.api.system.Clock;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

/**
 * Default implementation of Clock that uses system time.
 */
@Immutable
@NotNullByDefault
public class ClockImpl implements Clock {

    @Inject
    public ClockImpl() {
        // Constructor for dependency injection
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void sleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
