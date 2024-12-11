package lg.connected_platform.playlistVideo.repository;

import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.playlistVideo.entity.PlaylistVideo;
import lg.connected_platform.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistVideoRepository extends JpaRepository<PlaylistVideo, Long> {
    boolean existsByPlaylistAndVideo(Playlist playlist, Video video);
    PlaylistVideo findByPlaylistAndVideo(Playlist playlist, Video video);
    void deleteAllByPlaylist(Playlist playlist);
}
