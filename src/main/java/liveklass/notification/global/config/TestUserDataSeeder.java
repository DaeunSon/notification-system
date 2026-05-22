package liveklass.notification.global.config;

import liveklass.notification.domain.user.entity.User;
import liveklass.notification.domain.user.entity.UserRole;
import liveklass.notification.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestUserDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsById(1L)) {
            log.debug("테스트 사용자(id=1)가 이미 존재합니다.");
            return;
        }

        if (userRepository.count() > 0) {
            log.warn("users 테이블에 데이터가 있지만 id=1 사용자가 없습니다. Postman receiverId를 확인하세요.");
            return;
        }

        User user = User.create("testuser", "password", "test@example.com", UserRole.STUDENT);
        User saved = userRepository.save(user);
        log.info("테스트 사용자 생성 완료 — receiverId={} 사용", saved.getId());
    }
}
