package cn.chunana.simblog17api.repository;

import cn.chunana.simblog17api.entities.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}
