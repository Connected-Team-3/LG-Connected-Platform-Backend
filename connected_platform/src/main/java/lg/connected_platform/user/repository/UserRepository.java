package lg.connected_platform.user.repository;

import lg.connected_platform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByLoginId(String loginId);
    Optional<User> findByLoginId(String loginId);
}
