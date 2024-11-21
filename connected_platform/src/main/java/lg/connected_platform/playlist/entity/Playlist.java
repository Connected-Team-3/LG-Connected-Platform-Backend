package lg.connected_platform.playlist.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.playlist.dto.request.PlaylistUpdateRequest;
import lg.connected_platform.playlistVideo.entity.PlaylistVideo;
import lg.connected_platform.playlistVideo.repository.PlaylistVideoRepository;
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

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PlaylistVideo> playlistVideos = new ArrayList<>();

    @NotBlank
    private String title;

    @Builder
    public Playlist(
            Long id,
            User user,
            List<PlaylistVideo> playlistVideos,
            String title
    ){
        this.id = id;
        this.user = user;
        this.playlistVideos = playlistVideos;
        this.title = title;
    }

    public Playlist update(PlaylistUpdateRequest request, Video video, PlaylistVideoRepository playlistVideoRepository){
        this.title = request.title();

        if(request.insertFlag()){
            boolean exists = playlistVideoRepository.existsByPlaylistAndVideo(this, video);
            if(!exists){
                //PlaylistVideo 생성 및 저장
                PlaylistVideo playlistVideo = new PlaylistVideo(this, video);
                playlistVideoRepository.save(playlistVideo);

                this.playlistVideos.add(playlistVideo);

            }
        }
        if(request.deleteFlag()){
            PlaylistVideo target = playlistVideoRepository.findByPlaylistAndVideo(this, video);
            if(target != null){
                playlistVideoRepository.delete(target);

                this.playlistVideos.remove(target);
            }
        }

        return this;
    }
}
