package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.exception.GlobalExceptionHandler;
import cn.chunana.simblog17api.services.MediaService;
import cn.chunana.simblog17api.services.UserAccessService;
import cn.chunana.simblog17api.utils.JwtUtils;
import cn.chunana.simblog17api.utils.TokenSecurityService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAccessCtrl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserAccessCtrlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAccessService userAccessService;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private TokenSecurityService tokenSecurityService;

    @BeforeEach
    void setUp() {
        lenient().when(tokenSecurityService.tryAcquireRateLimit(anyString(), anyLong(), anyLong())).thenReturn(true);
        lenient().when(tokenSecurityService.isTokenRevoked(anyString())).thenReturn(false);
    }

    @Test
    void loginShouldReturnSuccessResponseWhenCredentialIsValid() throws Exception {
        LocalDateTime createTime = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
        User          user       = new User(1L, "alice", null, "secret123", User.UserRole.USER, createTime);
        when(userAccessService.login("alice", "secret123")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(user)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(user)).thenReturn("refresh-token");

        mockMvc.perform(post("/api/user/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "username": "alice",
                                                   "password": "secret123"
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.SUCCESS.getCode()))
               .andExpect(jsonPath("$.data.id").value(1))
               .andExpect(jsonPath("$.data.username").value("alice"))
               .andExpect(jsonPath("$.data.createTime").value("2026-01-02T03:04:05"))
               .andExpect(jsonPath("$.data.accessToken").value("access-token"))
               .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void loginShouldReturnInvalidRequestWhenUsernameIsBlank() throws Exception {
        mockMvc.perform(post("/api/user/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "username": "",
                                                   "password": "secret123"
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.INVALID_REQUEST.getCode()))
               .andExpect(jsonPath("$.statusMessage").value(Status.INVALID_REQUEST.getMessage()))
               .andExpect(jsonPath("$.data").isString());
    }

    @Test
    void registerShouldReturnInvalidRequestWhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/user/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "username": "alice",
                                                   "password": ""
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.INVALID_REQUEST.getCode()))
               .andExpect(jsonPath("$.statusMessage").value(Status.INVALID_REQUEST.getMessage()))
               .andExpect(jsonPath("$.data").isString());
    }

    @Test
    void profileShouldReturnUserNotFoundWhenUidNotExists() throws Exception {
        mockMvc.perform(get("/api/user/auth/profile/999"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.USER_NOT_FOUND.getCode()));
    }

    @Test
    void refreshShouldReturnUnauthorizedWhenRefreshHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/user/auth/refresh"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.UNAUTHORIZED.getCode()));
    }

    @Test
    void refreshShouldReturnSuccessWhenRefreshHeaderIsValid() throws Exception {
        LocalDateTime createTime = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
        User user = new User(1L, "alice", null, "secret123", User.UserRole.USER, createTime);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");

        when(jwtUtils.parseRefreshToken("refresh-token")).thenReturn(claims);
        when(userAccessService.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtUtils.remainingRefreshValidityMillis("refresh-token")).thenReturn(60_000L);

        mockMvc.perform(post("/api/user/auth/refresh")
                                .header("X-Refresh-Token", "refresh-token"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.SUCCESS.getCode()))
               .andExpect(jsonPath("$.data.id").value(1))
               .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
               .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    void updateCurrentUserShouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        mockMvc.perform(put("/api/user/auth/profile/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "username": "newname"
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.UNAUTHORIZED.getCode()));
    }

    @Test
    void changePasswordShouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        mockMvc.perform(put("/api/user/auth/password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "oldPassword": "oldpwd",
                                                   "newPassword": "newpwd"
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.UNAUTHORIZED.getCode()));
    }

    @Test
    void loginShouldReturnTooManyRequestsWhenRateLimited() throws Exception {
        when(tokenSecurityService.tryAcquireRateLimit(anyString(), anyLong(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/api/user/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                 {
                                                   "username": "alice",
                                                   "password": "secret123"
                                                 }
                                                 """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.statusCode").value(Status.TOO_MANY_REQUESTS.getCode()));
    }
}
