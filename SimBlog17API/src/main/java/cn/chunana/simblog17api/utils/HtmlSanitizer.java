package cn.chunana.simblog17api.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.regex.Pattern;

/**
 * Minimal backend HTML sanitizer for rich-text article content.
 */
public final class HtmlSanitizer {

    private static final Safelist ALLOWLIST = Safelist.relaxed()
            .removeProtocols("a", "href", "ftp", "mailto")
            .removeProtocols("img", "src", "data")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https");

    private static final Pattern FORBIDDEN_JS_PATTERN = Pattern.compile(
            "(?i)(<\\s*script\\b|javascript\\s*:|on[a-z]+\\s*=)"
    );

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return "";
        }

        if (FORBIDDEN_JS_PATTERN.matcher(html).find()) {
            throw new IllegalArgumentException("HTML content contains forbidden javascript");
        }

        return Jsoup.clean(html, ALLOWLIST);
    }
}

