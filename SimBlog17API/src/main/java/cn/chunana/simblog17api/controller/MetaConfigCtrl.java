package cn.chunana.simblog17api.controller;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.request.OwnerTokenRequest;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meta")
@Tag(name = "站点配置", description = "MetaConfig 站长口令校验接口")
public class MetaConfigCtrl {

    private final MetaConfigService   metaConfigService;
    private final TokenSecurityService tokenSecurityService;

    @Value("${app.security.auth.rate-limit-window-seconds:60}")
    private long rateLimitWindowSeconds;

    @Value("${app.meta.token-max-attempts:5}")
    private long tokenMaxAttempts;

    /**
     * 校验站长口令。
     * <p>
     * 此接口不需要 JWT 认证（在 SecurityConfig 中开放），因为只有知道口令的人才能访问 MetaConfig 页面。
     * 接口本身有 IP 级速率限制（防暴力破解）。
     */
    @PostMapping("/verify-token")
    @Operation(
            summary = "校验站长口令",
            description = "传入明文口令，与数据库中的 BCrypt 哈希比对。成功返回 statusCode=0，失败返回 statusCode=4。"
    )
    public ApiStatusResponse<Boolean> verifyToken(
            @Valid @RequestBody OwnerTokenRequest request,
            HttpServletRequest httpRequest) {

        String ip = extractIp(httpRequest);
        String rateLimitKey = "meta:verify:" + ip;

        if (!tokenSecurityService.tryAcquireRateLimit(rateLimitKey, tokenMaxAttempts, rateLimitWindowSeconds)) {
            log.warn("meta.token.rate_limited ip={}", ip);
            return ApiStatusResponse.fail(Status.TOO_MANY_REQUESTS);
        }

        boolean valid = metaConfigService.verifyOwnerToken(request.token());
        if (!valid) {
            log.warn("meta.token.invalid ip={}", ip);
            return ApiStatusResponse.fail(Status.OWNER_TOKEN_INVALID);
        }

        log.info("meta.token.verified ip={}", ip);
        return ApiStatusResponse.ok(true);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String addr = request.getRemoteAddr();
        return addr == null || addr.isBlank() ? "unknown" : addr;
    }
}
