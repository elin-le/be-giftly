package elin.le.giftly.auth.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    @Mock
    private UserDetails userDetails;

    private JwtUtil createJwtUtil() throws Exception {
        JwtUtil jwtUtil = new JwtUtil();
        setField(jwtUtil, "secret", "TestSecretKeyForUnitTesting12345678901234567890123456789012345678901234567890");
        setField(jwtUtil, "accessTokenExpiration", 3600000L);
        setField(jwtUtil, "refreshTokenExpiration", 604800000L);
        return jwtUtil;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtUtil.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private UserDetails mockUser() {
        UserDetails user = User.withUsername("testuser@example.com")
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
        return user;
    }

    @Test
    @DisplayName("Should generate access token")
    void shouldGenerateAccessToken() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails user = mockUser();
        String token = jwtUtil.generateAccessToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should generate refresh token")
    void shouldGenerateRefreshToken() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails user = mockUser();
        String token = jwtUtil.generateRefreshToken(user);
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void shouldExtractUsername() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails user = mockUser();
        String token = jwtUtil.generateAccessToken(user);
        assertEquals("testuser@example.com", jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("Should extract expiration from valid token")
    void shouldExtractExpiration() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails user = mockUser();
        String token = jwtUtil.generateAccessToken(user);
        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should throw for invalid token")
    void shouldThrowForInvalidToken() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        assertThrows(Exception.class, () -> jwtUtil.extractUsername("invalid.token.here"));
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails user = mockUser();
        String token = jwtUtil.generateAccessToken(user);
        assertTrue(jwtUtil.validateToken(token, user));
    }

    @Test
    @DisplayName("Should reject token for different user")
    void shouldRejectTokenForDifferentUser() throws Exception {
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails user = mockUser();
        String token = jwtUtil.generateAccessToken(user);
        UserDetails other = User.withUsername("other@example.com").password("p").authorities((org.springframework.security.core.GrantedAuthority[])new org.springframework.security.core.GrantedAuthority[0]).build();
        assertFalse(jwtUtil.validateToken(token, other));
    }
}
