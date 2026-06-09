package org.teamkorea.backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.teamkorea.backend.ai.dto.DomainAgeResult;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class DomainAgeService {

    private final RestClient restClient = RestClient.create();

    public DomainAgeResult checkDomainAge(String domain) {
        String rootDomain = extractRootDomain(domain);

        if (rootDomain == null || rootDomain.isBlank()) {
            return DomainAgeResult.unchecked(domain);
        }

        try {
            Map response = restClient.get()
                    .uri("https://rdap.org/domain/" + rootDomain)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return DomainAgeResult.unchecked(rootDomain);
            }

            Object eventsObj = response.get("events");

            if (!(eventsObj instanceof List events)) {
                return DomainAgeResult.unchecked(rootDomain);
            }

            LocalDate createdDate = null;

            for (Object eventObj : events) {
                if (!(eventObj instanceof Map event)) {
                    continue;
                }

                Object action = event.get("eventAction");
                Object date = event.get("eventDate");

                if (action != null
                        && date != null
                        && action.toString().equalsIgnoreCase("registration")) {

                    createdDate = OffsetDateTime.parse(date.toString())
                            .toLocalDate();
                    break;
                }
            }

            if (createdDate == null) {
                return DomainAgeResult.unchecked(rootDomain);
            }

            int ageDays = (int) ChronoUnit.DAYS.between(
                    createdDate,
                    LocalDate.now()
            );

            boolean newDomain = ageDays <= 90;

            return new DomainAgeResult(
                    true,
                    newDomain,
                    ageDays,
                    createdDate,
                    rootDomain
            );

        } catch (Exception e) {
            System.out.println("도메인 생성일 조회 실패: " + e.getMessage());
            return DomainAgeResult.unchecked(rootDomain);
        }
    }

    private String extractRootDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return null;
        }

        String lower = domain.toLowerCase().trim();

        if (lower.startsWith("www.")) {
            lower = lower.substring(4);
        }

        String[] parts = lower.split("\\.");

        if (parts.length < 2) {
            return lower;
        }

        if (lower.endsWith(".co.kr") && parts.length >= 3) {
            return parts[parts.length - 3] + ".co.kr";
        }

        return parts[parts.length - 2] + "." + parts[parts.length - 1];
    }
}