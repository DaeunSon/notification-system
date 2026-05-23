package liveklass.notification.domain.notification.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.entity.NotificationStatus;
import org.hibernate.LockMode;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Repository
public class NotificationDispatchClaimRepositoryImpl implements NotificationDispatchClaimRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Notification> findAndLockNextPending() {
        return findAndLockOne(
                """
                        SELECT n FROM Notification n
                        WHERE n.status = :status
                        ORDER BY n.id ASC
                        """,
                query -> query.setParameter("status", NotificationStatus.PENDING)
        );
    }

    @Override
    public Optional<Notification> findAndLockNextFailedRetryDue(LocalDateTime now, int maxRetryCount) {
        return findAndLockOne(
                """
                        SELECT n FROM Notification n
                        WHERE n.status = :status
                          AND n.nextRetryAt <= :now
                          AND n.retryCount < :maxRetryCount
                        ORDER BY n.id ASC
                        """,
                query -> {
                    query.setParameter("status", NotificationStatus.FAILED);
                    query.setParameter("now", now);
                    query.setParameter("maxRetryCount", maxRetryCount);
                }
        );
    }

    private Optional<Notification> findAndLockOne(String jpql, Consumer<TypedQuery<Notification>> parameterizer) {
        TypedQuery<Notification> query = entityManager.createQuery(jpql, Notification.class);
        parameterizer.accept(query);
        query.setMaxResults(1);
        query.unwrap(org.hibernate.query.Query.class)
                .setHibernateLockMode(LockMode.UPGRADE_SKIPLOCKED);

        List<Notification> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
