package org.nodex.core.crypto;

import org.nodex.api.crypto.PasswordStrengthEstimator;
import org.nodex.api.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.util.regex.Pattern;

/**
 * Implementation of PasswordStrengthEstimator.
 */
@Immutable
@NotNullByDefault
public class PasswordStrengthEstimatorImpl implements PasswordStrengthEstimator {

    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGITS = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

    @Inject
    public PasswordStrengthEstimatorImpl() {
        // Constructor for dependency injection
    }

    @Override
    public float estimateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0.0f;
        }

        float strength = 0.0f;
        int length = password.length();

        // Length contributes to strength
        if (length >= 8) {
            strength += 0.25f;
        }
        if (length >= 12) {
            strength += 0.15f;
        }
        if (length >= 16) {
            strength += 0.1f;
        }

        // Character variety contributes to strength
        if (LOWERCASE.matcher(password).find()) {
            strength += 0.1f;
        }
        if (UPPERCASE.matcher(password).find()) {
            strength += 0.1f;
        }
        if (DIGITS.matcher(password).find()) {
            strength += 0.15f;
        }
        if (SPECIAL.matcher(password).find()) {
            strength += 0.15f;
        }

        // Penalize common patterns
        if (isCommonPattern(password)) {
            strength -= 0.2f;
        }

        // Ensure strength is between 0 and 1
        return Math.max(0.0f, Math.min(1.0f, strength));
    }

    private boolean isCommonPattern(String password) {
        String lower = password.toLowerCase();
        
        // Check for common weak passwords
        return lower.equals("password") ||
               lower.equals("123456") ||
               lower.equals("qwerty") ||
               lower.equals("admin") ||
               lower.equals("welcome") ||
               lower.contains("password") ||
               isSequential(password);
    }

    private boolean isSequential(String password) {
        if (password.length() < 3) {
            return false;
        }
        
        // Check for sequential characters
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true; // Found ascending sequence
            }
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true; // Found descending sequence
            }
        }
        
        return false;
    }
}
