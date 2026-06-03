package org.teamkorea.backend.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class OpenPhishService {

    private volatile Set<String> cachedPhishingUrls = Collections.emptySet();

    @PostConstruct
    public void init() {
        refreshFeed();
    }

    @Scheduled(fixedDelay = 600000)
    public void refreshFeed() {
        Set<String> newCache = new HashSet<>();

        try {
            URL url = new URL("https://openphish.com/feed.txt");

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String phishingUrl = normalize(line);

                    if (!phishingUrl.isBlank()) {
                        newCache.add(phishingUrl);
                    }
                }
            }

            cachedPhishingUrls = newCache;

            System.out.println(
                    "OpenPhish feed 갱신 완료: " + cachedPhishingUrls.size() + "개"
            );

        } catch (Exception e) {
            System.out.println("OpenPhish feed 갱신 실패: " + e.getMessage());
        }
    }

    public boolean isPhishingUrl(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return false;
        }

        String normalizedTarget = normalize(targetUrl);

        return cachedPhishingUrls.contains(normalizedTarget);
    }

    private String normalize(String url) {
        if (url == null) {
            return "";
        }

        return url.trim()
                .toLowerCase()
                .replaceAll("/+$", "");
    }
}