package cn.chunana.simblog17api.common;

/**
 * Centralized SpEL cache-key expressions.
 */
public enum CacheKeys {
    ;

    public static final String HOME_PAGEABLE      = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()";
    public static final String HOME_DAYS_PAGEABLE = "#days + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()";
}

