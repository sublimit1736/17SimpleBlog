package cn.chunana.simblog17api.services.impl;

import cn.chunana.simblog17api.entities.SiteSetting;
import cn.chunana.simblog17api.repository.SiteSettingRepository;
import cn.chunana.simblog17api.services.MetaConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaConfigServiceImpl implements MetaConfigService {

    static final String OWNER_TOKEN_KEY = "owner.token.hash";

    private final SiteSettingRepository siteSettingRepository;
    private final PasswordEncoder       passwordEncoder;

    @Override
    public boolean verifyOwnerToken(String plainToken) {
        if (plainToken == null || plainToken.isBlank()) return false;
        return siteSettingRepository.findById(OWNER_TOKEN_KEY)
                .map(s -> passwordEncoder.matches(plainToken, s.getValue()))
                .orElse(false);
    }

    @Override
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
}
