package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.dto.response.UploadPermissionsResponse;
import cn.chunana.simblog17api.entities.SiteSetting;
import cn.chunana.simblog17api.entities.User;
import cn.chunana.simblog17api.repository.SiteSettingRepository;
import cn.chunana.simblog17api.repository.UserRepository;
import cn.chunana.simblog17api.services.MetaConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MetaConfigServiceImpl implements MetaConfigService {

    // ── Setting keys ──────────────────────────────────────────────────────────

    static final String OWNER_TOKEN_KEY       = "owner.token.hash";
    static final String UPLOAD_ALLOW_USER_KEY = "upload.allow.user";
    static final String UPLOAD_ALLOW_ADMIN_KEY = "upload.allow.admin";
    static final String UPLOAD_BLACKLIST_KEY   = "upload.blacklist";

    private final SiteSettingRepository siteSettingRepository;
    private final PasswordEncoder       passwordEncoder;
    private final UserRepository        userRepository;

    // ── Owner token ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean verifyOwnerToken(String plainToken) {
        if (plainToken == null || plainToken.isBlank()) return false;
        return siteSettingRepository.findById(OWNER_TOKEN_KEY)
                .map(s -> passwordEncoder.matches(plainToken, s.getValue()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwnerTokenConfigured() {
        return siteSettingRepository.existsById(OWNER_TOKEN_KEY);
    }

    @Override
    public void saveOwnerToken(String plainToken) {
        String hash = passwordEncoder.encode(plainToken);
        SiteSetting setting = siteSettingRepository.findById(OWNER_TOKEN_KEY)
                .orElse(SiteSetting.builder().key(OWNER_TOKEN_KEY).build());
        setting.setValue(hash);
        siteSettingRepository.save(setting);
        log.info("meta.owner_token.saved");
    }

    // ── Upload permissions ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UploadPermissionsResponse getUploadPermissions() {
        boolean userAllowed  = getBoolSetting(UPLOAD_ALLOW_USER_KEY,  true);
        boolean adminAllowed = getBoolSetting(UPLOAD_ALLOW_ADMIN_KEY, true);
        List<Long> blacklist = getBlacklist();
        return new UploadPermissionsResponse(userAllowed, adminAllowed, blacklist);
    }

    @Override
    public void setUploadPermissions(boolean userAllowed, boolean adminAllowed) {
        setSetting(UPLOAD_ALLOW_USER_KEY,  String.valueOf(userAllowed));
        setSetting(UPLOAD_ALLOW_ADMIN_KEY, String.valueOf(adminAllowed));
        log.info("meta.upload_permissions.set userAllowed={} adminAllowed={}", userAllowed, adminAllowed);
    }

    @Override
    public void addToUploadBlacklist(Long userId) {
        List<Long> list = new ArrayList<>(getBlacklist());
        if (!list.contains(userId)) {
            list.add(userId);
            saveBlacklist(list);
            log.info("meta.upload_blacklist.add userId={}", userId);
        }
    }

    @Override
    public void removeFromUploadBlacklist(Long userId) {
        List<Long> list = new ArrayList<>(getBlacklist());
        if (list.remove(userId)) {
            saveBlacklist(list);
            log.info("meta.upload_blacklist.remove userId={}", userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserUpload(Long userId, User.UserRole role) {
        // 1. Check role-level switch
        String key = (role == User.UserRole.ADMIN) ? UPLOAD_ALLOW_ADMIN_KEY : UPLOAD_ALLOW_USER_KEY;
        if (!getBoolSetting(key, true)) {
            return false;
        }
        // 2. Check per-user blacklist
        return !getBlacklist().contains(userId);
    }

    // ── User role management ───────────────────────────────────────────────────

    @Override
    public void setUserRole(Long userId, User.UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        user.setRole(role);
        userRepository.save(user);
        log.info("meta.user.role_set userId={} role={}", userId, role);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean getBoolSetting(String key, boolean defaultValue) {
        return siteSettingRepository.findById(key)
                .map(s -> Boolean.parseBoolean(s.getValue()))
                .orElse(defaultValue);
    }

    private void setSetting(String key, String value) {
        SiteSetting setting = siteSettingRepository.findById(key)
                .orElse(SiteSetting.builder().key(key).build());
        setting.setValue(value);
        siteSettingRepository.save(setting);
    }

    private List<Long> getBlacklist() {
        return siteSettingRepository.findById(UPLOAD_BLACKLIST_KEY)
                .map(s -> {
                    String raw = s.getValue();
                    if (raw == null || raw.isBlank()) return List.<Long>of();
                    return Arrays.stream(raw.split(","))
                                 .map(String::strip)
                                 .filter(t -> !t.isEmpty())
                                 .map(Long::parseLong)
                                 .collect(Collectors.toList());
                })
                .orElse(List.of());
    }

    private void saveBlacklist(List<Long> list) {
        String value = list.stream().map(String::valueOf).collect(Collectors.joining(","));
        setSetting(UPLOAD_BLACKLIST_KEY, value);
    }
}
