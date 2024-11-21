package lg.connected_platform.playlist.mapper;

import lg.connected_platform.playlist.dto.request.PlaylistCreateRequest;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.entity.Video;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PlaylistMapper {
    //DTO와 엔티티 사이의 변환을 담당
    public static Playlist from(PlaylistCreateRequest request, User user){
        return Playlist.builder()
                .user(user)
                .title(request.title())
                .build();
    }
}
