package lg.connected_platform.videoHashtag.repository;

import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHashtag.entity.VideoHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoHashtagRepository extends JpaRepository<VideoHashtag, Long> {
    List<VideoHashtag> findByHashtag(Hashtag hashtag);
    void deleteAllByVideo(Video video);
}
