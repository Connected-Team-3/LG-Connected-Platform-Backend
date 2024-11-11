package lg.connected_platform.playlist.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.playlist.dto.request.PlaylistUpdateRequest;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.entity.Video;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Playlist extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "playlist_video",
            joinColumns = @JoinColumn(name="playlist_id"),
            inverseJoinColumns = @JoinColumn(name="video_id")
    )
    private List<Video> videos = new ArrayList<>();

    @NotBlank
    private String title;

    @Builder
    public Playlist(
            Long id,
            User user,
            List<Video> videos,
            String title
    ){
        this.id = id;
        this.user = user;
        this.videos = videos;
        this.title = title;
    }

    public Playlist update(PlaylistUpdateRequest request, Video video){
        this.title = request.title();

        if(request.insertFlag()){
            this.videos.add(video);
        }
        if(request.deleteFlag()){
            this.videos.remove(video);
        }

        return this;
    }
}
