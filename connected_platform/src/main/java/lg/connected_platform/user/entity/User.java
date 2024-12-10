package lg.connected_platform.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.user.dto.request.UserUpdateRequest;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHistory.entity.VideoHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
public class User extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @OneToMany(mappedBy = "user")
    private List<VideoHistory> videoHistories = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Playlist> playlists = new ArrayList<>();

    @OneToMany(mappedBy = "uploader")
    private List<Video> videos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_food_preference", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preferences")
    private Map<String, List<String>> foodPreferences = new HashMap<>(); //key : "morning", "afternoon" 등

    @Builder
    public User(
            Long id,
            String loginId,
            String password,
            String name,
            List<VideoHistory> videoHistories){
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.videoHistories = videoHistories;
    }

    public User update(UserUpdateRequest request){
        this.loginId = request.loginId();
        this.password = request.password();
        this.name = request.name();
        return this;
    }
}
