package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.request.ChangePasswordRequest;
import cn.chunana.simblog17api.dto.request.UpdateUsernameRequest;
import cn.chunana.simblog17api.dto.request.UserAccessRequest;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.PageResponse;
import cn.chunana.simblog17api.dto.response.UserAccessResponse;
import cn.chunana.simblog17api.mapper.UserAccessMapper;
import cn.chunana.simblog17api.services.MediaService;
import cn.chunana.simblog17api.services.UserAccessService;
import cn.chunana.simblog17api.utils.JwtUtils;
import cn.chunana.simblog17api.utils.SecurityUtils;
import cn.chunana.simblog17api.utils.TokenSecurityService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/auth")
@Tag(name = "用户认证", description = "用户登录与注册接口")
@Slf4j
public class UserAccessCtrl {
    private final UserAccessService userAccessService;
    private final MediaService mediaService;
    private final JwtUtils jwtUtils;
    private final TokenSecurityService tokenSecurityService;

    @Value("${app.security.auth.rate-limit-window-seconds:60}")
    private long authRateLimitWindowSeconds;

    @Value("${app.security.auth.login.max-attempts-per-window:10}")
    private long loginMaxAttempts;

    @Value("${app.security.auth.refresh.max-attempts-per-window:20}")
    private long refreshMaxAttempts;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "请求参数不合法", content = @Content(schema = @Schema()))
    })
    public ApiStatusResponse<UserAccessResponse> login(@Valid @RequestBody UserAccessRequest request,
                                                       HttpServletRequest httpRequest) {
        if (isAuthRateLimited(httpRequest, "login", request.username(), loginMaxAttempts)) {
            log.warn("auth.rate_limited action=login username={} ip={}", request.username(), extractClientIp(httpRequest));
            return ApiStatusResponse.fail(Status.TOO_MANY_REQUESTS);
        }

        return userAccessService.login(request.username(), request.password())
                                .map(user -> {
                                    String accessToken = jwtUtils.generateAccessToken(user);
                                    String refreshToken = jwtUtils.generateRefreshToken(user);
                                    log.info("auth.login.success uid={}", user.getId());
                                    return ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user, accessToken, refreshToken));
                                })
                                .orElseGet(() -> {
                                    log.warn("auth.login.failed username={}", request.username());
                                    return ApiStatusResponse.fail(Status.LOGIN_FAILED);
                                });
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "使用用户名和密码创建新用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册请求已处理"),
            @ApiResponse(responseCode = "400", description = "请求参数不合法", content = @Content(schema = @Schema()))
    })
    public ApiStatusResponse<UserAccessResponse> register(@Valid @RequestBody UserAccessRequest request) {
        return userAccessService.register(request.username(), request.password())
                                .map(user -> ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user)))
                                .orElseGet(() -> ApiStatusResponse.fail(Status.USER_ALREADY_EXISTS));
    }

    @GetMapping("/profile/{uid}")
    @Operation(summary = "用户信息", description = "根据用户 ID 返回用户基本信息")
    public ApiStatusResponse<UserAccessResponse> getUserProfile(@PathVariable Long uid) {
        return userAccessService.findById(uid)
                                .map(user -> ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user)))
                                .orElseGet(() -> ApiStatusResponse.fail(Status.USER_NOT_FOUND));
    }

    @PutMapping("/profile/{uid}")
    @Operation(summary = "修改用户名", description = "按用户 ID 修改用户名，仅本人或管理员允许")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<UserAccessResponse> updateUserProfile(
            @PathVariable Long uid,
            @Valid @RequestBody UpdateUsernameRequest request,
            Authentication authentication) {

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        if (!isAdmin && !currentUserId.equals(uid)) {
            return ApiStatusResponse.fail(Status.ACCESS_DENIED);
        }

        return userAccessService.updateUsername(uid, request.username())
                                .map(user -> {
                                    log.info("auth.profile.updated targetUid={} operatorUid={}", uid, currentUserId);
                                    return ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user));
                                })
                                .orElseGet(() -> ApiStatusResponse.fail(Status.USER_ALREADY_EXISTS));
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "校验旧密码后更新为新密码")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<Boolean> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        Long userId = SecurityUtils.getCurrentUserId(authentication);
        if (userId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        if (userAccessService.findById(userId).isEmpty()) {
            return ApiStatusResponse.fail(Status.USER_NOT_FOUND);
        }

        boolean changed = userAccessService.changePassword(userId, request.oldPassword(), request.newPassword());
        if (!changed) {
            log.warn("auth.password.change_failed uid={}", userId);
            return ApiStatusResponse.fail(Status.OLD_PASSWORD_INCORRECT);
        }

        log.info("auth.password.changed uid={}", userId);
        return ApiStatusResponse.ok(true);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用请求头 X-Refresh-Token 刷新 access/refresh 双令牌")
    public ApiStatusResponse<UserAccessResponse> refreshToken(
            @Parameter(description = "刷新令牌", required = true, example = "<refresh-token>")
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken,
            HttpServletRequest httpRequest) {

        if (isAuthRateLimited(httpRequest, "refresh", null, refreshMaxAttempts)) {
            log.warn("auth.rate_limited action=refresh ip={}", extractClientIp(httpRequest));
            return ApiStatusResponse.fail(Status.TOO_MANY_REQUESTS);
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        try {
            String token = refreshToken.trim();
            if (token.isEmpty() || tokenSecurityService.isTokenRevoked(token)) {
                return ApiStatusResponse.fail(Status.UNAUTHORIZED);
            }

            Claims claims = jwtUtils.parseRefreshToken(token);
            Long userId = Long.valueOf(claims.getSubject());

            return userAccessService.findById(userId)
                                    .map(user -> {
                                        String refreshedAccessToken = jwtUtils.generateAccessToken(user);
                                        String refreshedRefreshToken = jwtUtils.generateRefreshToken(user);
                                        tokenSecurityService.revokeToken(token, jwtUtils.remainingRefreshValidityMillis(token));
                                        log.info("auth.refresh.success uid={}", userId);
                                        return ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user, refreshedAccessToken, refreshedRefreshToken));
                                    })
                                    .orElseGet(() -> ApiStatusResponse.fail(Status.USER_NOT_FOUND));
        } catch (Exception e) {
            log.warn("auth.refresh.failed reason={}", e.getMessage());
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "将 access/refresh token 标记为失效，客户端同时删除本地 token")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<Boolean> logout(
            @Parameter(description = "访问令牌（Bearer）", required = false, example = "Bearer <access-token>")
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "刷新令牌", required = false, example = "<refresh-token>")
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader,
            Authentication authentication) {

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    tokenSecurityService.revokeToken(token, jwtUtils.remainingAccessValidityMillis(token));
                } catch (Exception ignored) {
                    // Ignore malformed/expired tokens and keep logout idempotent.
                }
            }
        }

        if (refreshTokenHeader != null && !refreshTokenHeader.isBlank()) {
            try {
                tokenSecurityService.revokeToken(refreshTokenHeader.trim(), jwtUtils.remainingRefreshValidityMillis(refreshTokenHeader.trim()));
            } catch (Exception ignored) {
                // Ignore malformed/expired refresh token.
            }
        }

        log.info("auth.logout uid={}", SecurityUtils.getCurrentUserId(authentication));

        return ApiStatusResponse.ok(true);
    }

    @GetMapping("/search/by_username")
    @Operation(summary = "用户名搜索（纯文本）", description = "按用户名关键字做部分匹配搜索")
    public ApiStatusResponse<PageResponse<UserAccessResponse>> searchUsersByUsername(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {

        return ApiStatusResponse.ok(userAccessService.searchUsersByUsername(keyword, pageable));
    }

    @GetMapping("/search/regex/by_username")
    @Operation(summary = "用户名搜索（正则）", description = "按用户名正则表达式进行搜索")
    public ApiStatusResponse<PageResponse<UserAccessResponse>> searchUsersByUsernameRegex(
            @RequestParam String pattern,
            @PageableDefault(size = 10) Pageable pageable) {

        return ApiStatusResponse.ok(userAccessService.searchUsersByUsernameRegex(pattern, pageable));
    }

    @PutMapping(value = "/profile/{uid}/avatar", consumes = "multipart/form-data")
    @Operation(summary = "上传头像", description = "上传头像图片并更新指定用户头像，仅本人或管理员允许")
    @PreAuthorize("isAuthenticated()")
    public ApiStatusResponse<UserAccessResponse> uploadAvatar(
            @PathVariable Long uid,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Long currentUserId = SecurityUtils.getCurrentUserId(authentication);
        if (currentUserId == null) {
            return ApiStatusResponse.fail(Status.UNAUTHORIZED);
        }

        boolean isAdmin = SecurityUtils.isAdmin(authentication);
        if (!isAdmin && !uid.equals(currentUserId)) {
            return ApiStatusResponse.fail(Status.ACCESS_DENIED);
        }

        mediaService.uploadAvatar(file, uid, currentUserId, isAdmin);
        return userAccessService.findById(uid)
                                .map(user -> ApiStatusResponse.ok(UserAccessMapper.toUserAccessResponse(user)))
                                .orElseGet(() -> ApiStatusResponse.fail(Status.USER_NOT_FOUND));
    }

    private boolean isAuthRateLimited(HttpServletRequest request,
                                      String action,
                                      String username,
                                      long maxAttempts) {
        String ip = extractClientIp(request);
        if (!tokenSecurityService.tryAcquireRateLimit(action + ":ip:" + ip, maxAttempts, authRateLimitWindowSeconds)) {
            return true;
        }

        if (username == null || username.isBlank()) {
            return false;
        }

        String normalizedUsername = username.trim().toLowerCase();
        return !tokenSecurityService.tryAcquireRateLimit(
                action + ":username:" + normalizedUsername,
                maxAttempts,
                authRateLimitWindowSeconds
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }
}
