package liveklass.notification.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import liveklass.notification.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Notification 읽음 처리")
class NotificationReadTest {

    @Test
    @DisplayName("markAsRead()는 is_read를 true로 설정한다")
    void markAsRead_setsTrue() {
        User receiver = Mockito.mock(User.class);
        Notification notification = Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.EMAIL
        );

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("이미 읽음인 알림에 markAsRead()를 다시 호출해도 true를 유지한다")
    void markAsRead_isIdempotent() {
        User receiver = Mockito.mock(User.class);
        Notification notification = Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.IN_APP
        );

        notification.markAsRead();
        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }
}
