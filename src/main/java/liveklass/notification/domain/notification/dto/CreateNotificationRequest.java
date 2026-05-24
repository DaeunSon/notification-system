package liveklass.notification.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record CreateNotificationRequest(

        @NotNull
        Long receiverId,

        @NotNull
        NotificationType notificationType,

        @NotBlank
        String referenceId,

        @NotNull
        NotificationChannel channel,

        LocalDateTime scheduledAt
) {
    public CreateNotificationRequest(
            Long receiverId,
            NotificationType notificationType,
            String referenceId,
            NotificationChannel channel
    ) {
        this(receiverId, notificationType, referenceId, channel, null);
    }
}
