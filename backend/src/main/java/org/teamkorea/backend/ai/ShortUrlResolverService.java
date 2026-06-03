package org.teamkorea.backend.ai;

import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ShortUrlResolverService {

    private static final int MAX_REDIRECTS = 5;
    private static final int TIMEOUT_MS = 3000;

    public String resolve(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank()) {
            return originalUrl;
        }

        String currentUrl = originalUrl;

        try {
            for (int i = 0; i < MAX_REDIRECTS; i++) {
                HttpURLConnection connection =
                        (HttpURLConnection) new URL(currentUrl).openConnection();

                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int statusCode = connection.getResponseCode();

                if (statusCode == 301
                        || statusCode == 302
                        || statusCode == 303
                        || statusCode == 307
                        || statusCode == 308) {

                    String location = connection.getHeaderField("Location");

                    if (location == null || location.isBlank()) {
                        return currentUrl;
                    }

                    URL baseUrl = new URL(currentUrl);
                    URL nextUrl = new URL(baseUrl, location);

                    currentUrl = nextUrl.toString();
                } else {
                    return currentUrl;
                }
            }

            return currentUrl;

        } catch (Exception e) {
            System.out.println("리다이렉트 추적 실패: " + e.getMessage());
            return originalUrl;
        }
    }
}