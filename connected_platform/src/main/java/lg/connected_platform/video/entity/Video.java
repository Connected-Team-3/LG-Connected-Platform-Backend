package lg.connected_platform.video.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.food.entity.Food;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.dto.request.VideoUpdateRequest;
import lg.connected_platform.videoHashtag.entity.VideoHashtag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Video extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 외래 키 컬럼 정의
    private User uploader;

    @NotBlank
    private String sourceUrl;

    @NotBlank
    private String thumbUrl;

    @Enumerated(EnumType.STRING)
    private Category category;

    @OneToMany(mappedBy = "video", fetch = FetchType.LAZY)
    private Set<VideoHashtag> videoHashtags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "food_id")
    private Food food;

    @Builder
    public Video(
            Long id,
            String title,
            String description,
            User uploader,
            String sourceUrl,
            String thumbUrl,
            Set<VideoHashtag> videoHashtags,
            Category category,
            Food food
    ){
        this.id = id;
        this.title = title;
        this.description = description;
        this.uploader = uploader;
        this.sourceUrl = sourceUrl;
        this.thumbUrl = thumbUrl;
        this.videoHashtags = videoHashtags;
        this.category = category;
        this.food = food;
    }

    public Video update(VideoUpdateRequest request, Set<VideoHashtag> videoHashtags){
        this.title = request.title();
        this.description = request.description();
        this.sourceUrl = request.sourceUrl();
        this.thumbUrl = request.thumbUrl();
        this.videoHashtags = videoHashtags;
        return this;
    }

}
