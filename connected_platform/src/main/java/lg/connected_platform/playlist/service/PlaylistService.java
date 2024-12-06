package lg.connected_platform.playlist.service;

import jakarta.transaction.Transactional;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.playlist.dto.request.PlaylistCreateRequest;
import lg.connected_platform.playlist.dto.request.PlaylistUpdateRequest;
import lg.connected_platform.playlist.dto.response.PlaylistResponse;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.playlist.mapper.PlaylistMapper;
import lg.connected_platform.playlist.repository.PlaylistRepository;
import lg.connected_platform.playlistVideo.entity.PlaylistVideo;
import lg.connected_platform.playlistVideo.repository.PlaylistVideoRepository;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.repository.UserRepository;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final AuthService authService;
    private final PlaylistVideoRepository playlistVideoRepository;

    //플레이리스트 생성
    public SingleResult<Long> create(PlaylistCreateRequest request, String token){
        //요청에 담긴 유저와 인증된 유저가 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);
/*
        if(!currentUserId.equals(request.userId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }*/

        //유저 조회
        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        Playlist newPlaylist = PlaylistMapper.from(request, user);
        playlistRepository.save(newPlaylist);

        //PlaylistVideo 생성
        request.videoIdList().forEach(videoId ->{
            Video video = videoRepository.findById(videoId)
                    .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

            PlaylistVideo playlistVideo = new PlaylistVideo(newPlaylist, video);
            playlistVideoRepository.save(playlistVideo);
        });

        user.getPlaylists().add(newPlaylist);

        return ResponseService.getSingleResult(newPlaylist.getId());
    }

    //플레이리스트 조회
    public SingleResult<PlaylistResponse> findById(Long id){
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.PLAYLIST_NOT_EXIST));

        return ResponseService.getSingleResult(PlaylistResponse.of(playlist));
    }

    //플레이리스트 업데이트 : 영상 하나 삭제 or 삽입
    @Transactional
    public SingleResult<PlaylistResponse> updatePlaylist(PlaylistUpdateRequest request, String token){
        //삽입 또는 삭제 둘 중 하나만 하고 있는지 flag 체크
        if(!request.deleteFlag()^request.insertFlag()){
            throw new CustomException(ErrorCode.PLAYLIST_FLAG_ERROR);
        }

        //요청에 담긴 유저와 인증된 유저가 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);
/*
        if(!currentUserId.equals(request.userId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }*/

        Playlist playlist = playlistRepository.findById(request.id())
                .orElseThrow(()-> new CustomException(ErrorCode.PLAYLIST_NOT_EXIST));

        Video video = videoRepository.findById(request.videoId())
                .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        //유저에서 기존의 플레이리스트 삭제
        user.getPlaylists().remove(playlist);

        playlistRepository.save(playlist.update(request, video, playlistVideoRepository));
        //업데이트 된 플레이리스트 다시 삽입
        user.getPlaylists().add(playlist);

        return ResponseService.getSingleResult(PlaylistResponse.of(playlist));
    }

    //플레이리스트 삭제 : 리스트 전체 삭제
    @Transactional
    public SingleResult<Long> deleteById(Long id, String token){
        //인증된 유저와 플레이리스트를 만든 유저가 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        //삭제하고자 하는 플레이리스트
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.PLAYLIST_NOT_EXIST));

        if(!currentUserId.equals(playlist.getUser().getId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }

        playlist.getUser().getPlaylists().remove(playlist);
        playlistVideoRepository.deleteAllByPlaylist(playlist);
        playlistRepository.deleteById(id);
        return ResponseService.getSingleResult(playlist.getId());
    }

    //특정 유저의 전체 플레이리스트 조회
    public ListResult<PlaylistResponse> getUserPlaylist(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        List<PlaylistResponse> playlist = user.getPlaylists().stream()
                .map(PlaylistResponse::of)
                .toList();

        return ResponseService.getListResult(playlist);
    }
}
