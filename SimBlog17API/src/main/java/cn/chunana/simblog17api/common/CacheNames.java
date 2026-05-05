package cn.chunana.simblog17api.common;

/**
 * Centralized cache names used by Spring Cache annotations and cache manager TTL config.
 */
public enum CacheNames {
    ;

    public static final String ARTICLE = "article";

    public static final String HOME_LATEST = "home:latest";
    public static final String HOME_HOT = "home:hot";
    public static final String HOME_STATS = "home:stats";
    public static final String HOME_HOT_TAGS = "home:hot-tags";
    public static final String HOME_RECENT_COMMENTS = "home:recent-comments";
}

