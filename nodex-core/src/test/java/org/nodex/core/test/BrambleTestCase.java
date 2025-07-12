package org.nodex.core.test;

import org.junit.After;
import org.junit.Before;

/**
 * Base test case for NodeX tests.
 */
public abstract class BrambleTestCase {
    
    @Before
    public void setUp() throws Exception {
        // Override in subclasses for setup
    }
    
    @After
    public void tearDown() throws Exception {
        // Override in subclasses for cleanup
    }
}
