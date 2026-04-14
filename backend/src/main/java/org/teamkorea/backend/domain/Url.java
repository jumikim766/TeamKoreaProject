package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "urls",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_urls_url_hash", columnNames = "url_hash")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "url_id")
    private Long urlId;

    @Column(name = "normalized_url", nullable = false, columnDefinition = "TEXT")
    private String normalizedUrl;

    @Column(name = "url_hash", nullable = false, length = 64)
    private String urlHash;

    @Column(name = "domain", length = 255)
    private String domain;

    @Column(name = "scheme", length = 10)
    private String scheme;

    @Column(name = "domain_created_at")
    private LocalDateTime domainCreatedAt;

    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "seen_count", nullable = false)
    private Integer seenCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmailUrl> emailUrls = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.firstSeenAt == null) {
            this.firstSeenAt = now;
        }
        if (this.lastSeenAt == null) {
            this.lastSeenAt = now;
        }
        if (this.seenCount == null) {
            this.seenCount = 0;
        }
    }

    public void addEmailUrl(EmailUrl emailUrl) {
        this.emailUrls.add(emailUrl);
        emailUrl.setUrl(this);
    }

    public void removeEmailUrl(EmailUrl emailUrl) {
        this.emailUrls.remove(emailUrl);
        emailUrl.setUrl(null);
    }
}