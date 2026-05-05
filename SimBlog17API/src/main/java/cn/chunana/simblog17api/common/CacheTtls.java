package cn.chunana.simblog17api.common;

import java.time.Duration;

/**
 * Centralized cache TTL definitions.
 */
public enum CacheTtls {
    ;

    public static final Duration DEFAULT = Duration.ofMinutes(10);

    public static final Duration HOME_LATEST          = Duration.ofSeconds(60);
    public static final Duration HOME_HOT             = Duration.ofMinutes(5);
    public static final Duration HOME_STATS           = Duration.ofMinutes(5);
    public static final Duration HOME_HOT_TAGS        = Duration.ofMinutes(10);
    public static final Duration HOME_RECENT_COMMENTS = Duration.ofSeconds(60);
}

