package liveklass.notification.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.notification.dto.CreateNotificationRequest;
import liveklass.notification.domain.notification.dto.response.NotificationAcceptedResponse;
import liveklass.notification.domain.notification.dto.response.NotificationDetailResponse;
import liveklass.notification.domain.notification.dto.response.NotificationReadResponse;
import liveklass.notification.domain.notification.dto.response.NotificationStatusResponse;
import liveklass.notification.domain.notification.dto.response.NotificationSummaryResponse;
import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.notification.repository.NotificationRepository;
import liveklass.notification.domain.user.entity.User;
import liveklass.notification.domain.user.repository.UserRepository;
import liveklass.notification.global.exception.BusinessException;
import liveklass.notification.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 테스트")
class NotificationServiceTest {

    private static final Long RECEIVER_ID = 1L;
    private static final Long NOTIFICATION_ID = 10L;
    private static final Long UNKNOWN_ID = 99L;

    @Mock
    private NotificationCreationService notificationCreationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User receiver;
    private CreateNotificationRequest createRequest;

    @BeforeEach
    void setUp() {
        receiver = mock(User.class);
        lenient().when(receiver.getId()).thenReturn(RECEIVER_ID);
        lenient().when(userRepository.findById(RECEIVER_ID)).thenReturn(Optional.of(receiver));

        createRequest = new CreateNotificationRequest(
                RECEIVER_ID,
                NotificationType.ENROLLMENT_CONFIRMED,
                "course-1",
                NotificationChannel.EMAIL
        );
    }

    private Notification pendingNotification() {
        Notification notification = NotificationTestFixtures.pending(receiver);
        ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);
        return notification;
    }

    @Nested
    @DisplayName("getStatus()")
    class GetStatus {

        @Test
        @DisplayName("알림 상태 조회 성공")
        void success() {
            Notification notification = pendingNotification();
            given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

            NotificationStatusResponse response = notificationService.getStatus(NOTIFICATION_ID);

            assertThat(response.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(response.status()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 알림 ID로 요청 시 예외 발생")
        void fail_notFound() {
            given(notificationRepository.findById(UNKNOWN_ID)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> notificationService.getStatus(UNKNOWN_ID)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getNotification()")
    class GetNotification {

        @Test
        @DisplayName("존재하는 알림 단건 조회 성공")
        void success() {
            Notification notification = pendingNotification();
            given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

            NotificationDetailResponse response = notificationService.getNotification(NOTIFICATION_ID);

            assertThat(response.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(response.receiverId()).isEqualTo(RECEIVER_ID);
            assertThat(response.notificationType()).isEqualTo(NotificationType.ENROLLMENT_CONFIRMED);
            assertThat(response.referenceId()).isEqualTo("course-1");
            assertThat(response.channel()).isEqualTo(NotificationChannel.EMAIL);
            assertThat(response.status()).isEqualTo(NotificationStatus.PENDING);
            assertThat(response.read()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 알림 조회 시 예외 발생")
        void fail_notFound() {
            given(notificationRepository.findById(UNKNOWN_ID)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> notificationService.getNotification(UNKNOWN_ID)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getNotificationList()")
    class GetNotificationList {

        @Test
        @DisplayName("수신자 기준 목록 조회 성공")
        void success() {
            Notification notification = pendingNotification();
            given(notificationRepository.findByReceiver_IdOrderByIdDesc(RECEIVER_ID))
                    .willReturn(List.of(notification));

            List<NotificationSummaryResponse> responses =
                    notificationService.getNotificationList(RECEIVER_ID, null);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(responses.get(0).status()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("알림이 없는 수신자 조회 시 빈 목록 반환")
        void success_emptyList() {
            given(notificationRepository.findByReceiver_IdOrderByIdDesc(RECEIVER_ID))
                    .willReturn(Collections.emptyList());

            List<NotificationSummaryResponse> responses =
                    notificationService.getNotificationList(RECEIVER_ID, null);

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 수신자 ID로 요청 시 예외 발생")
        void fail_userNotFound() {
            given(userRepository.findById(UNKNOWN_ID)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> notificationService.getNotificationList(UNKNOWN_ID, null)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(notificationRepository, never()).findByReceiver_IdOrderByIdDesc(any());
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("존재하지 않는 알림 ID면 NOTIFICATION_NOT_FOUND")
        void fail_notFound() {
            given(notificationRepository.findById(UNKNOWN_ID)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> notificationService.markAsRead(UNKNOWN_ID, null)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        @Test
        @DisplayName("receiverId가 수신자와 다르면 NOTIFICATION_ACCESS_DENIED")
        void fail_accessDenied() {
            Notification notification = pendingNotification();
            given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> notificationService.markAsRead(NOTIFICATION_ID, UNKNOWN_ID)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_ACCESS_DENIED);
            assertThat(notification.isRead()).isFalse();
        }

        @Test
        @DisplayName("receiverId가 수신자와 일치하면 읽음 처리한다")
        void success_withMatchingReceiverId() {
            Notification notification = pendingNotification();
            given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

            NotificationReadResponse response =
                    notificationService.markAsRead(NOTIFICATION_ID, RECEIVER_ID);

            assertThat(response.read()).isTrue();
        }
    }

    @Nested
    @DisplayName("retryDeadNotification()")
    class RetryDeadNotification {

        @Test
        @DisplayName("DEAD가 아니면 NOTIFICATION_NOT_RETRYABLE")
        void fail_notRetryable() {
            Notification notification = pendingNotification();
            given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> notificationService.retryDeadNotification(NOTIFICATION_ID)
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_RETRYABLE);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }
    }
}
