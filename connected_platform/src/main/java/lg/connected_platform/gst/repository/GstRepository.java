package lg.connected_platform.gst.repository;

import lg.connected_platform.gst.entity.Gst;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GstRepository extends JpaRepository<Gst, Long> {
    Optional<Gst> findByVideoId(Long videoId);
}
