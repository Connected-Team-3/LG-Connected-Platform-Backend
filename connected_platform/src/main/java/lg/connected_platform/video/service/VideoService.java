package lg.connected_platform.video.service;

import jakarta.transaction.Transactional;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.repository.UserRepository;
import lg.connected_platform.video.dto.request.VideoCreateRequest;
import lg.connected_platform.video.dto.request.VideoUpdateRequest;
import lg.connected_platform.video.dto.response.VideoResponse;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.mapper.VideoMapper;
import lg.connected_platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    //영상 업로드 -> 회원 여부 확인 필요
    public SingleResult<Long> save(VideoCreateRequest request, String token){
        //업로더와 업로드 요청 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        if(!currentUserId.equals(request.uploaderId())){
            throw new CustomException(ErrorCode.VIDEO_UPLOADER_MISMATCH);
        }

        User uploader = userRepository.findById(request.uploaderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
        Video newVideo = videoRepository.save(VideoMapper.from(request, uploader));
        return ResponseService.getSingleResult(newVideo.getId());
    }

    //영상 조회 -> 회원 여부 확인 필요
    public SingleResult<VideoResponse> findById(Long id){
        Video video = videoRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

        return ResponseService.getSingleResult(VideoResponse.of(video));
    }

    //영상 수정 -> 회원 여부 + 업로더인지 확인 필요
    @Transactional
    public SingleResult<VideoResponse> updateVideo(VideoUpdateRequest request, String token){
        //업로더와 수정 요청 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);
        
        if(!currentUserId.equals(request.uploaderId())){
            throw new CustomException(ErrorCode.VIDEO_UPLOADER_MISMATCH);
        }

        Video video = videoRepository.findById(request.id())
                .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

        videoRepository.save(video.update(request));
        return ResponseService.getSingleResult(VideoResponse.of(video));
    }

    //영상 삭제 -> 회원 여부 + 업로더인지 확인 필요
    @Transactional
    public SingleResult<Long> deleteById(Long id, String token){
        //업로더와 삭제 요청 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        //삭제하고자 하는 비디오
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

        if(!currentUserId.equals(video.getId())){
            throw new CustomException(ErrorCode.VIDEO_UPLOADER_MISMATCH);
        }

        videoRepository.deleteById(id);
        return ResponseService.getSingleResult(video.getId());
    }
}
