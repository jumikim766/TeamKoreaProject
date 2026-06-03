package org.teamkorea.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DomainAgeResult {

    private boolean checked;
    private boolean newDomain;
    private int ageDays;
    private LocalDate createdDate;
    private String rootDomain;

    public static DomainAgeResult unchecked(String rootDomain) {
        return new DomainAgeResult(false, false, -1, null, rootDomain);
    }
}