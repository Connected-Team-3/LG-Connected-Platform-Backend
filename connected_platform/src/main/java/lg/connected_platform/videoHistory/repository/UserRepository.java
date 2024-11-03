package lg.connected_platform.videoHistory.repository;

import lg.connected_platform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
