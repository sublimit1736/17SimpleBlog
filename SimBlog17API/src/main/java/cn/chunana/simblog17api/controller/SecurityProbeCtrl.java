package cn.chunana.simblog17api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "安全探针", description = "用于验证公开与受保护访问规则的测试接口")
public class SecurityProbeCtrl {

    @GetMapping("/api/public/ping")
    @Operation(summary = "公开接口探活", description = "用于验证无需认证即可访问")
    String publicEndpoint() {
        return "Public port request successfully";
    }

    @GetMapping("/api/private/ping")
    @Operation(summary = "受保护接口探活", description = "用于验证需要认证后才可访问")
    String privateEndpoint() {
        return "Private  port request successfully";
    }
}

