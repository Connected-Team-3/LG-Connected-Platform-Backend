package lg.connected_platform.videoHistory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHistory.dto.request.VideoHistoryUpdateRequest;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
//개별 영상 시청 기록
public class VideoHistory extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @NotNull
    private Long videoTimeStamp = 0L; //어디까지 봤는지

    @Getter
    @LastModifiedDate
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
        this.videoTimeStamp = request.videoTimeStamp();
        return this;
    }

}
