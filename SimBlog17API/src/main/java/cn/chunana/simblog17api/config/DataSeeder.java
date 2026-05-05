package cn.chunana.simblog17api.config;

import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 应用启动时自动写入管理员账号（若尚不存在）。
 * <p>
 * Prod 环境要求 APP_ADMIN_USERNAME 与 APP_ADMIN_PASSWORD 均已配置，否则启动失败。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment     environment;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        boolean isProd = Arrays.stream(environment.getActiveProfiles())
                               .anyMatch("prod"::equalsIgnoreCase);

        if (isProd) {
            if (adminUsername == null || adminUsername.isBlank()) {
                throw new IllegalStateException("app.admin.username must be configured in prod");
            }
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException("app.admin.password must be configured in prod");
            }
        }

        if (!adminUsername.isBlank() && !adminPassword.isBlank()) {
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = User.builder()
                                 .username(adminUsername)
                                 .password(passwordEncoder.encode(adminPassword))
                                 .role(User.UserRole.ADMIN)
                                 .createTime(LocalDateTime.now())
                                 .build();
                userRepository.save(admin);
                log.info("Default admin user '{}' created.", adminUsername);
            } else {
                log.debug("Admin user '{}' already exists, skipping seed.", adminUsername);
            }
        }
    }
}

