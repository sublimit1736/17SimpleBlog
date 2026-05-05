package cn.chunana.simblog17api.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoderShouldEncodeAndMatchRawPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        String rawPassword = "secret123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encoded);
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    void passwordEncoderShouldNotMatchIncorrectPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        String encoded = passwordEncoder.encode("secret123");

        assertFalse(passwordEncoder.matches("wrong-password", encoded));
    }
}
