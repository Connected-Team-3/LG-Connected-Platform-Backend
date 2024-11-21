package lg.connected_platform.hashtag.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHashtag.entity.VideoHashtag;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class Hashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VideoHashtag> videoHashtags = new HashSet<>();

    public Hashtag(String name) {
        this.name = name;
    }
}
