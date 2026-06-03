package org.teamkorea.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UrlFeatureResult {

    private int domainLength;
    private int hyphenCount;
    private int dotCount;
    private int digitCount;

    private boolean hasIpAddress;
    private boolean hasPunycode;
    private boolean hasSuspiciousKeyword;
    private boolean hasSuspiciousTld;
    private boolean hasBrandImpersonation;
    private boolean hasSubdomainBrandImpersonation;
    private boolean hasShortUrlService;

    private double suspiciousScore;
}