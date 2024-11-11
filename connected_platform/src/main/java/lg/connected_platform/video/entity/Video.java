package lg.connected_platform.video.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.dto.request.VideoUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "video_hashtag",
            joinColumns = @JoinColumn(name="video_id"),
            inverseJoinColumns = @JoinColumn(name="hashtag_id")
    )
    private Set<Hashtag> hashtags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Category category;

    @Builder
    public Video(
            Long id,
            String title,
            String description,
            User uploader,
            String sourceUrl,
            String thumbUrl,
            Set<Hashtag> hashtags,
            Category category
    ){
        this.id = id;
        this.title = title;
        this.description = description;
        this.uploader = uploader;
        this.sourceUrl = sourceUrl;
        this.thumbUrl = thumbUrl;
        this.hashtags = hashtags;
        this.category = category;
    }

    public Video update(VideoUpdateRequest request, Set<Hashtag> hashtags){
        this.title = request.title();
        this.description = request.description();
        this.sourceUrl = request.sourceUrl();
        this.thumbUrl = request.thumbUrl();
        this.hashtags = hashtags;
        return this;
    }

}
