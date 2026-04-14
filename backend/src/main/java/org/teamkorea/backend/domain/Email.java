package org.teamkorea.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "emails",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_emails_message_uid", columnNames = "message_uid")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private Long emailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private EmailAccount emailAccount;

    @Column(name = "message_uid", nullable = false, length = 255)
    private String messageUid;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "sender_email", length = 100)
    private String senderEmail;

    @Column(name = "receiver_email", length = 100)
    private String receiverEmail;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body_html", columnDefinition = "LONGTEXT")
    private String bodyHtml;

    @Column(name = "body_text", columnDefinition = "LONGTEXT")
    private String bodyText;

    @Column(name = "spf_result", length = 20)
    private String spfResult;

    @Column(name = "dkim_result", length = 20)
    private String dkimResult;

    @Column(name = "dmarc_result", length = 20)
    private String dmarcResult;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "email", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmailUrl> emailUrls = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void addEmailUrl(EmailUrl emailUrl) {
        this.emailUrls.add(emailUrl);
        emailUrl.setEmail(this);
    }

    public void removeEmailUrl(EmailUrl emailUrl) {
        this.emailUrls.remove(emailUrl);
        emailUrl.setEmail(null);
    }
}