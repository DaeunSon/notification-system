package liveklass.notification.domain.user.entity;

import jakarta.persistence.*;
import liveklass.notification.global.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public static User create(String username, String password, String email, UserRole role) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.email = email;
        user.role = role;
        return user;
    }
}
