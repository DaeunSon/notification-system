package liveklass.notification.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("Notification 발송 예약")
class NotificationScheduledDispatchTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 24, 12, 0);

    @Test
    @DisplayName("scheduledAt이 null이면 즉시 발송 대상이다")
    void dispatchDue_whenScheduledAtIsNull() {
        User receiver = Mockito.mock(User.class);
        Notification notification = NotificationTestFixtures.pending(receiver);

        assertThat(notification.isDispatchDue(NOW)).isTrue();
    }

    @Test
    @DisplayName("scheduledAt이 현재 시각 이전이면 발송 대상이다")
    void dispatchDue_whenScheduledAtIsPast() {
        User receiver = Mockito.mock(User.class);
        Notification notification = NotificationTestFixtures.pending(receiver, NOW.minusMinutes(1));

        assertThat(notification.isDispatchDue(NOW)).isTrue();
    }

    @Test
    @DisplayName("scheduledAt이 현재 시각이면 발송 대상이다")
    void dispatchDue_whenScheduledAtIsNow() {
        User receiver = Mockito.mock(User.class);
        Notification notification = NotificationTestFixtures.pending(receiver, NOW);

        assertThat(notification.isDispatchDue(NOW)).isTrue();
    }

    @Test
    @DisplayName("scheduledAt이 미래이면 발송 대상이 아니다")
    void dispatchNotDue_whenScheduledAtIsFuture() {
        User receiver = Mockito.mock(User.class);
        Notification notification = NotificationTestFixtures.pending(receiver, NOW.plusMinutes(1));

        assertThat(notification.isDispatchDue(NOW)).isFalse();
    }
}
