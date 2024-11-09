package lg.connected_platform.video.repository;

import lg.connected_platform.video.entity.Category;
import lg.connected_platform.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCategory(Category category);
}
