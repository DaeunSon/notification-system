package liveklass.notification.domain.notification.entity;

import jakarta.persistence.*;
import liveklass.notification.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id",  nullable = false)
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

    /**
     *
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
        return notification;
    }

}


