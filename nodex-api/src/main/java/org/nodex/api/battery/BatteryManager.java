package org.nodex.api.battery;

import org.nodex.api.nullsafety.NotNullByDefault;
import org.nodex.api.lifecycle.Service;

/**
 * Manages battery state and optimization.
 */
@NotNullByDefault  
public interface BatteryManager extends Service {
    
    /**
     * Get current battery level (0-100).
     */
    int getBatteryLevel();
    
    /**
     * Check if device is charging.
     */
    boolean isCharging();
    
    /**
     * Check if battery optimization is enabled.
     */
    boolean isBatteryOptimizationEnabled();
}
