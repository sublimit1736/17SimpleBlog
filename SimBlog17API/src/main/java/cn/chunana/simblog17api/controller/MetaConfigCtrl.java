package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.request.BlacklistModifyRequest;
import cn.chunana.simblog17api.dto.request.OwnerTokenRequest;
import cn.chunana.simblog17api.dto.request.SetUploadPermissionsRequest;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import cn.chunana.simblog17api.dto.response.UploadPermissionsResponse;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.services.MetaConfigService;
import cn.chunana.simblog17api.utils.TokenSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meta")
@Tag(name = "站点配置", description = "MetaConfig 站长口令校验、投稿权限、用户组管理接口")
public class MetaConfigCtrl {

    private final MetaConfigService    metaConfigService;
    private final TokenSecurityService tokenSecurityService;

    @Value("${app.security.auth.rate-limit-window-seconds:60}")
    private long rateLimitWindowSeconds;

    @Value("${app.meta.token-max-attempts:5}")
    private long tokenMaxAttempts;

    // ── Owner token verify ────────────────────────────────────────────────────

    @PostMapping("/verify-token")
    @Operation(summary = "校验站长口令")
    public ApiStatusResponse<Boolean> verifyToken(
            @Valid @RequestBody OwnerTokenRequest request,
            HttpServletRequest httpRequest) {

        if (!acquireRateLimit(httpRequest)) {
            return ApiStatusResponse.fail(Status.TOO_MANY_REQUESTS);
        }

        boolean valid = metaConfigService.verifyOwnerToken(request.token());
        if (!valid) {
            log.warn("meta.token.invalid ip={}", extractIp(httpRequest));
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        log.info("meta.token.verified ip={}", extractIp(httpRequest));
        return ApiStatusResponse.ok(true);
    }

    // ── Upload permissions ────────────────────────────────────────────────────

    @GetMapping("/upload-permissions")
    @Operation(summary = "获取投稿权限配置", description = "返回用户组开关及黑名单列表（公开接口）")
    public ApiStatusResponse<UploadPermissionsResponse> getUploadPermissions() {
        return ApiStatusResponse.ok(metaConfigService.getUploadPermissions());
    }

    @PostMapping("/upload-permissions")
    @Operation(summary = "更新投稿权限配置", description = "需要站长口令")
    public ApiStatusResponse<UploadPermissionsResponse> setUploadPermissions(
            @Valid @RequestBody SetUploadPermissionsRequest request,
            HttpServletRequest httpRequest) {

        if (!verifyOwnerTokenWithRateLimit(request.token(), httpRequest)) {
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        metaConfigService.setUploadPermissions(request.userAllowed(), request.adminAllowed());
        log.info("meta.upload_permissions.update userAllowed={} adminAllowed={} ip={}",
                 request.userAllowed(), request.adminAllowed(), extractIp(httpRequest));
        return ApiStatusResponse.ok(metaConfigService.getUploadPermissions());
    }

    @PostMapping("/upload-blacklist/add")
    @Operation(summary = "将用户加入上传黑名单", description = "需要站长口令")
    public ApiStatusResponse<UploadPermissionsResponse> addToBlacklist(
            @Valid @RequestBody BlacklistModifyRequest request,
            HttpServletRequest httpRequest) {

        if (!verifyOwnerTokenWithRateLimit(request.token(), httpRequest)) {
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        metaConfigService.addToUploadBlacklist(request.userId());
        log.info("meta.blacklist.add userId={} ip={}", request.userId(), extractIp(httpRequest));
        return ApiStatusResponse.ok(metaConfigService.getUploadPermissions());
    }

    @PostMapping("/upload-blacklist/remove")
    @Operation(summary = "将用户移出上传黑名单", description = "需要站长口令")
    public ApiStatusResponse<UploadPermissionsResponse> removeFromBlacklist(
            @Valid @RequestBody BlacklistModifyRequest request,
            HttpServletRequest httpRequest) {

        if (!verifyOwnerTokenWithRateLimit(request.token(), httpRequest)) {
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        metaConfigService.removeFromUploadBlacklist(request.userId());
        log.info("meta.blacklist.remove userId={} ip={}", request.userId(), extractIp(httpRequest));
        return ApiStatusResponse.ok(metaConfigService.getUploadPermissions());
    }

    // ── User role management (owner-token protected) ──────────────────────────

    @PutMapping("/users/{uid}/promote")
    @Operation(summary = "将用户提升为管理员（站长权限）", description = "需要站长口令")
    public ApiStatusResponse<String> promoteUser(
            @PathVariable Long uid,
            @Valid @RequestBody OwnerTokenRequest request,
            HttpServletRequest httpRequest) {

        if (!verifyOwnerTokenWithRateLimit(request.token(), httpRequest)) {
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        try {
            metaConfigService.setUserRole(uid, User.UserRole.ADMIN);
            log.info("meta.user.promote uid={} ip={}", uid, extractIp(httpRequest));
            return ApiStatusResponse.ok("用户 " + uid + " 已设为管理员");
        } catch (NoSuchElementException e) {
            return ApiStatusResponse.fail(Status.USER_NOT_FOUND);
        }
    }

    @PutMapping("/users/{uid}/demote")
    @Operation(summary = "将用户降级为普通用户（站长权限）", description = "需要站长口令")
    public ApiStatusResponse<String> demoteUser(
            @PathVariable Long uid,
            @Valid @RequestBody OwnerTokenRequest request,
            HttpServletRequest httpRequest) {

        if (!verifyOwnerTokenWithRateLimit(request.token(), httpRequest)) {
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        try {
            metaConfigService.setUserRole(uid, User.UserRole.USER);
            log.info("meta.user.demote uid={} ip={}", uid, extractIp(httpRequest));
            return ApiStatusResponse.ok("用户 " + uid + " 已降级为普通用户");
        } catch (NoSuchElementException e) {
            return ApiStatusResponse.fail(Status.USER_NOT_FOUND);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean verifyOwnerTokenWithRateLimit(String token, HttpServletRequest httpRequest) {
        if (!acquireRateLimit(httpRequest)) {
            return false;
        }
        return metaConfigService.verifyOwnerToken(token);
    }

    private boolean acquireRateLimit(HttpServletRequest httpRequest) {
        String ip  = extractIp(httpRequest);
        String key = "meta:verify:" + ip;
        return tokenSecurityService.tryAcquireRateLimit(key, tokenMaxAttempts, rateLimitWindowSeconds);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String addr = request.getRemoteAddr();
        return addr == null || addr.isBlank() ? "unknown" : addr;
    }
}
