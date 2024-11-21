package lg.connected_platform.videoHashtag.entity;

import jakarta.persistence.*;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.video.entity.Video;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class VideoHashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    public VideoHashtag(Video video, Hashtag hashtag){
        this.video = video;
        this.hashtag = hashtag;
    }
}
