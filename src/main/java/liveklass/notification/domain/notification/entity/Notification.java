package liveklass.notification.domain.notification.entity;

import jakarta.persistence.*;
import liveklass.notification.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_recipient_type_reference_channel",
                columnNames = {"receiver_id", "notification_type", "reference_id", "channel"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MINUTES = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * PENDING 상태의 알림 생성
     */
    public static Notification createPending(
            User receiver,
            NotificationType notificationType,
            String referenceId,
            NotificationChannel channel
    ) {
        Notification notification = new Notification();
        notification.receiver = receiver;
        notification.notificationType = notificationType;
        notification.referenceId = referenceId;
        notification.channel = channel;
        notification.title = notificationType.getTitle();
        notification.content = notificationType.formatContent(referenceId);
        notification.status = NotificationStatus.PENDING;
        notification.read = false;
        notification.retryCount = 0;
        return notification;
    }

    /** 최초 발송: PENDING → PROCESSING */
    public void startProcessing() {
        assertStatus(NotificationStatus.PENDING);
        this.status = NotificationStatus.PROCESSING;
    }

    /** 재시도 발송: FAILED → PROCESSING (nextRetryAt 경과 후) */
    public void startProcessingForRetry() {
        assertStatus(NotificationStatus.FAILED);
        if (!isRetryDue()) {
            throw new IllegalStateException(
                    "재시도 시각이 아직 되지 않았습니다. nextRetryAt=" + this.nextRetryAt
            );
        }
        this.status = NotificationStatus.PROCESSING;
    }

    public void markSuccess() {
        assertStatus(NotificationStatus.PROCESSING);
        this.status = NotificationStatus.SUCCESS;
        this.failureReason = null;
        this.nextRetryAt = null;
    }

    /**
     * 발송 실패 처리. retryCount 증가 후
     * - retryCount &lt; 3 → FAILED + nextRetryAt 예약
     * - retryCount &gt;= 3 → DEAD (자동 재시도 종료)
     */
    public void markFailed(String reason) {
        assertStatus(NotificationStatus.PROCESSING);
        this.failureReason = reason;
        this.retryCount++;

        if (this.retryCount >= MAX_RETRY_COUNT) {
            markDead();
        } else {
            this.status = NotificationStatus.FAILED;
            this.nextRetryAt = LocalDateTime.now().plusMinutes(RETRY_DELAY_MINUTES);
        }
    }

    public void markDead() {
        this.status = NotificationStatus.DEAD;
        this.nextRetryAt = null;
    }

    /** FAILED이고 재시도 시각이 지났으며 아직 DEAD가 아닌 경우 */
    public boolean isRetryDue() {
        return this.status == NotificationStatus.FAILED
                && this.retryCount < MAX_RETRY_COUNT
                && this.nextRetryAt != null
                && !this.nextRetryAt.isAfter(LocalDateTime.now());
    }

    private void assertStatus(NotificationStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "알림 상태 전이 불가: 현재=" + this.status + ", 기대=" + expected
            );
        }
    }
}
