package lg.connected_platform.videoHistory.repository;

import lg.connected_platform.videoHistory.entity.VideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {
    Optional<VideoHistory> findByUserIdAndVideoId(Long userId, Long videoId);
}
