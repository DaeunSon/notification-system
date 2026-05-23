package liveklass.notification.domain.notification.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationStuckRecoveryWorker 테스트")
class NotificationStuckRecoveryWorkerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationStuckRecoveryWorker notificationStuckRecoveryWorker;

    private Notification notification;

    @BeforeEach
    void setUp() {
        User receiver = mock(User.class);
        notification = Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.EMAIL
        );
        ReflectionTestUtils.setField(notification, "id", 1L);
        notification.startProcessing();
    }

    @Test
    @DisplayName("PROCESSING 알림을 recoverFromStuckProcessing 후 저장한다")
    void recoverOne() {
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationStuckRecoveryWorker.recoverOne(1L);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getStuckRecoveryCount()).isEqualTo(1);
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("PROCESSING이 아니면 복구하지 않는다")
    void skipWhenNotProcessing() {
        notification.markSuccess();
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        notificationStuckRecoveryWorker.recoverOne(1L);

        verify(notificationRepository, never()).save(any());
    }
}
