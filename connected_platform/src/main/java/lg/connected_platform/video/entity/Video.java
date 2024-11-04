package lg.connected_platform.video.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.dto.request.VideoUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class Video extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id") // 외래 키 컬럼 정의
    private User uploader;

    @NotBlank
    private String sourceUrl;

    @NotBlank
    private String thumbUrl;

    @Builder
    public Video(
            Long id,
            String title,
            String description,
            User uploader,
            String sourceUrl,
            String thumbUrl
    ){
        this.id = id;
        this.title = title;
        this.description = description;
        this.uploader = uploader;
        this.sourceUrl = sourceUrl;
        this.thumbUrl = thumbUrl;
    }

    public Video update(VideoUpdateRequest request){
        this.title = request.title();
        this.description = request.description();;
        this.sourceUrl = request.sourceUrl();;
        this.thumbUrl = request.thumbUrl();
        return this;
    }

}
