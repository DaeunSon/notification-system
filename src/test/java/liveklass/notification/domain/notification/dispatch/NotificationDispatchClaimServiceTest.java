package liveklass.notification.domain.notification.dispatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.repository.NotificationDispatchClaimRepository;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationDispatchClaimService 테스트")
class NotificationDispatchClaimServiceTest {

    @Mock
    private NotificationDispatchClaimRepository notificationDispatchClaimRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationDispatchClaimService notificationDispatchClaimService;

    private Notification pendingNotification;

    @BeforeEach
    void setUp() {
        User receiver = org.mockito.Mockito.mock(User.class);
        pendingNotification = Notification.createPending(
                receiver,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.EMAIL
        );
        ReflectionTestUtils.setField(pendingNotification, "id", 1L);
    }

    @Test
    @DisplayName("PENDING 1건 선점 후 PROCESSING으로 저장한다")
    void claimNextPending() {
        given(notificationDispatchClaimRepository.findAndLockNextPending())
                .willReturn(Optional.of(pendingNotification));
        when(notificationRepository.save(ArgumentMatchers.any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Notification> claimed = notificationDispatchClaimService.claimNextPending();

        assertThat(claimed).isPresent();
        assertThat(claimed.get().getStatus()).isEqualTo(NotificationStatus.PROCESSING);
        assertThat(claimed.get().getProcessingStartedAt()).isNotNull();
        verify(notificationRepository).save(pendingNotification);
    }

    @Test
    @DisplayName("FAILED 재시도 1건 선점 후 PROCESSING으로 저장한다")
    void claimNextFailedRetryDue() {
        pendingNotification.startProcessing();
        pendingNotification.markFailed("err");
        ReflectionTestUtils.setField(pendingNotification, "nextRetryAt", LocalDateTime.now().minusMinutes(1));

        given(notificationDispatchClaimRepository.findAndLockNextFailedRetryDue(
                ArgumentMatchers.any(LocalDateTime.class),
                eq(Notification.MAX_RETRY_COUNT)
        )).willReturn(Optional.of(pendingNotification));
        when(notificationRepository.save(ArgumentMatchers.any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Notification> claimed = notificationDispatchClaimService.claimNextFailedRetryDue();

        assertThat(claimed).isPresent();
        assertThat(claimed.get().getStatus()).isEqualTo(NotificationStatus.PROCESSING);
    }
}
