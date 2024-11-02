package lg.connected_platform.videoHistory.entity;

import jakarta.persistence.*;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHistory.dto.request.VideoHistoryUpdateRequest;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class VideoHistory extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "VIDEO_ID")
    private Video video;

    private Long videoTimeStamp; //어디까지 봤는지

    private LocalDateTime lastWatchedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public VideoHistory(
            Long id,
            Video video,
            Long videoTimeStamp,
            LocalDateTime lastWatchedAt,
            User user){
        this.id = id;
        this.video = video;
        this.videoTimeStamp = videoTimeStamp;
        this.lastWatchedAt = lastWatchedAt;
        this.user = user;
    }

    public VideoHistory update(VideoHistoryUpdateRequest request){
        this.id = request.id();
        this.video = request.video();
        this.videoTimeStamp = request.videoTimeStamp();
        this.lastWatchedAt = request.lastWatchedAt();
        this.user = request.user();
        return this;
    }
}
