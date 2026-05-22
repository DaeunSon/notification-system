package liveklass.notification.domain.notification.controller;

import jakarta.validation.Valid;
import liveklass.notification.domain.notification.dto.CreateNotificationRequest;
import liveklass.notification.domain.notification.dto.response.NotificationAcceptedResponse;
import liveklass.notification.domain.notification.dto.response.NotificationDetailResponse;
import liveklass.notification.domain.notification.dto.response.NotificationStatusResponse;
import liveklass.notification.domain.notification.dto.response.NotificationSummaryResponse;
import liveklass.notification.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationAcceptedResponse> createNotificationRequest(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(notificationService.createRequest(request));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDetailResponse> getNotification(
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getNotification(notificationId));
    }

    @GetMapping("/{notificationId}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getStatus(notificationId));
    }

    @GetMapping("/users/{receiverId}")
    public ResponseEntity<List<NotificationSummaryResponse>> getNotificationList(
            @PathVariable Long receiverId,
            @RequestParam(required = false) Boolean read) {
        return ResponseEntity.ok(notificationService.getNotificationList(receiverId, read));
    }
}
