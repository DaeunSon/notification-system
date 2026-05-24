package liveklass.notification.domain.notification.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationStuckRecoveryService 테스트")
class NotificationStuckRecoveryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationStuckRecoveryWorker notificationStuckRecoveryWorker;

    @InjectMocks
    private NotificationStuckRecoveryService notificationStuckRecoveryService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        User receiver = mock(User.class);
        notification = NotificationTestFixtures.pending(receiver);
        ReflectionTestUtils.setField(notification, "id", 1L);
    }

    @Test
    @DisplayName("오래된 PROCESSING을 조회해 Worker에 건별 위임한다")
    void recoverStuckProcessingNotifications() {
        given(notificationRepository.findByStatusAndProcessingStartedAtBefore(
                eq(NotificationStatus.PROCESSING),
                any(LocalDateTime.class)
        )).willReturn(List.of(notification));

        notificationStuckRecoveryService.recoverStuckProcessingNotifications();

        verify(notificationStuckRecoveryWorker).recoverOne(1L);
    }

    @Test
    @DisplayName("조회 threshold는 now - STUCK_PROCESSING_THRESHOLD_MINUTES")
    void usesStaticThreshold() {
        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        given(notificationRepository.findByStatusAndProcessingStartedAtBefore(
                eq(NotificationStatus.PROCESSING),
                thresholdCaptor.capture()
        )).willReturn(List.of());

        int minutes = NotificationDispatchConstants.STUCK_PROCESSING_THRESHOLD_MINUTES;
        LocalDateTime before = LocalDateTime.now().minusMinutes(minutes).minusSeconds(2);
        notificationStuckRecoveryService.recoverStuckProcessingNotifications();
        LocalDateTime after = LocalDateTime.now().minusMinutes(minutes).plusSeconds(2);

        LocalDateTime threshold = thresholdCaptor.getValue();
        assertThat(threshold).isAfterOrEqualTo(before);
        assertThat(threshold).isBeforeOrEqualTo(after);
    }
}
