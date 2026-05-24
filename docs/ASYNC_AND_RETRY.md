# 비동기 처리 구조 및 재시도 정책 설명

이 문서는 **알림 발송 시스템의 비동기 처리 구조**와 **재시도/DEAD/스턱 복구 정책**을 설명합니다.

---

## 비동기 처리 구조

### 전체 흐름 (요약)

- **API는 알림을 즉시 발송하지 않고** DB에 **PENDING** 상태로 “접수”만 합니다.
- 실제 발송은 **스케줄러 + Worker**가 DB를 폴링/선점(claim)하여 수행합니다.
- 비즈니스 트랜잭션(예: 수강신청)과 알림 생성/발송은 분리되어, 알림 실패가 본업무 커밋에 영향을 주지 않습니다.

### 구성 요소와 역할

- **비즈니스 도메인 Service (예: `EnrollmentService`)**
  - 비즈니스 처리를 수행한 뒤 `NotificationRequestedEvent`를 발행합니다.
- **`@TransactionalEventListener(AFTER_COMMIT)`**
  - 비즈니스 트랜잭션이 **커밋된 이후**에만 동작합니다.
  - 커밋 이후 알림을 “생성”하므로, 알림 저장/발송 문제가 본업무 롤백을 유발하지 않습니다.
- **알림 생성 Service (`REQUIRES_NEW`)**
  - 별도 트랜잭션으로 알림을 **PENDING** 상태로 저장합니다.
- **스케줄러 (`@Scheduled`, 기본 5초 폴링)**
  - PENDING/FAILED 중 발송 조건이 맞는 건을 대상으로 claim을 시도하고 Worker를 실행합니다.
- **claim 전략: `FOR UPDATE SKIP LOCKED`**
  - 다중 인스턴스 환경에서 동일 알림을 동시에 잡지 않도록 **row lock + skip locked**로 선점합니다.
  - 선점 성공 시 상태를 **PROCESSING**으로 전환하고 발송을 수행합니다.
- **채널별 트리거**
  - `IN_APP`: 생성 직후 한 번 `@Async`로 dispatch를 트리거하고, 스케줄러가 백업 역할을 합니다.
  - `EMAIL`: 스케줄러가 전담합니다.
- **Sender**
  - 실제 외부 발송(이메일/푸시) 대신 `LoggingNotificationSender`로 **로그 Mock** 처리합니다.

### 예약 발송 (`scheduledAt`)

- `scheduledAt == null`: 즉시 발송 대상
- `scheduledAt`이 미래: claim 대상에서 제외 (해당 시간이 지난 뒤 claim 가능)

---

## 재시도 정책

### 상태 전이

```
PENDING ──claim──► PROCESSING ──OK──► SUCCESS
                      ├── fail ──► FAILED ──► (재시도) ──► DEAD
                      └── stuck ──► recover ──► PENDING/FAILED or DEAD
```

### 실패/재시도 규칙

- **FAILED**
  - 발송 실패 시 상태는 FAILED가 됩니다.
  - `failureReason`에 마지막 실패 사유를 기록합니다.
  - `nextRetryAt`(재시도 예정 시각)을 설정합니다.
- **자동 재시도**
  - 최대 **3회**까지 재시도합니다.
  - 재시도 간격은 **1분**입니다.
- **DEAD**
  - `retryCount >= 3`인 경우 DEAD로 전환합니다.

### 스턱(stuck) 복구 정책

- **스턱 판별**
  - PROCESSING 진입 시각(`processingStartedAt`) 기준으로 **10분 이상** 처리 중이면 스턱으로 판단합니다.
- **복구**
  - 스턱 복구 시 PENDING/FAILED로 되돌려 재처리 대상이 되게 합니다.
  - 스턱 복구가 **3회 초과**되면 DEAD로 전환합니다.

### 수동 재시도 (DEAD)

- DEAD 상태 알림은 `POST /api/notifications/{id}/retry`로 수동 재시도할 수 있습니다.
- 정책: DEAD → PENDING, `retryCount = 0`으로 리셋

---

## 중복 방지 (재시도/비동기와 연계)

- 유니크 키(UK): `(receiver_id, notification_type, reference_id, channel)`
- 동일 이벤트/채널의 중복 요청은 1건만 생성되도록 방지하며, 동시 요청은 UK 위반 시 **409**로 응답합니다.

