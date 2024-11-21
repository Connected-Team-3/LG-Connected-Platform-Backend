package lg.connected_platform.playlistVideo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.video.entity.Video;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class PlaylistVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "playlist_id")
    @JsonBackReference // 순환 방지
    private Playlist playlist;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    public PlaylistVideo(Playlist playlist, Video video){
        this.playlist = playlist;
        this.video = video;
    }
}
