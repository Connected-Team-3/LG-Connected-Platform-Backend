package lg.connected_platform.videoHashtag.entity;

import jakarta.persistence.*;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.video.entity.Video;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

//@Data
@Entity
@Getter
@NoArgsConstructor
public class VideoHashtag extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    public VideoHashtag(Video video, Hashtag hashtag){
        this.video = video;
        this.hashtag = hashtag;
    }
}
