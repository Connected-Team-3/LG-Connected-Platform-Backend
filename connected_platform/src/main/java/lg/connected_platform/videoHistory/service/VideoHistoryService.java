package lg.connected_platform.videoHistory.service;

import jakarta.transaction.Transactional;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.repository.UserRepository;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.repository.VideoRepository;
import lg.connected_platform.videoHistory.dto.request.VideoHistoryUpdateRequest;
import lg.connected_platform.videoHistory.dto.response.VideoHistoryResponse;
import lg.connected_platform.videoHistory.entity.VideoHistory;
import lg.connected_platform.videoHistory.repository.VideoHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoHistoryService {
    private final VideoHistoryRepository videoHistoryRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final AuthService authService;

    //개별 영상 시청 기록 생성 및 업데이트
    @Transactional
    public SingleResult<Long> createOrUpdate(VideoHistoryUpdateRequest request, String token){
        //시청 기록 생성 요청 회원과 시청한 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        if(!currentUserId.equals(request.userId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }


        //videoHistory가 존재하는지 확인하고 없으면 생성
        VideoHistory videoHistory = videoHistoryRepository.findByUserIdAndVideoId(request.userId(), request.videoId())
                .orElseGet(()->{
                    //새로운 VideoHistory 생성
                    User user = userRepository.findById(request.userId())
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
                    Video video = videoRepository.findById(request.videoId())
                            .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

                    return VideoHistory.builder()
                            .user(user)
                            .video(video)
                            .build();
                });

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
        user.getVideoHistories().add(videoHistory);

        videoHistoryRepository.save(videoHistory.update(request));
        return ResponseService.getSingleResult(videoHistory.getId());
    }

    //특정 영상 시청 기록 조회
    public SingleResult<VideoHistoryResponse> findById(Long id, String token){
        //시청 기록 조회 요청 회원과 시청한 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        VideoHistory videoHistory = videoHistoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.HISTORY_NOT_EXIST));

        if(!currentUserId.equals(videoHistory.getUser().getId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }

        return ResponseService.getSingleResult(VideoHistoryResponse.of(videoHistory));
    }

    //유저의 전체 시청 기록 조회
    public ListResult<VideoHistoryResponse> getUserVideoHistories(Long userId, String token){
        //시청 기록 조회 요청 회원과 토큰에 저장된 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        if(!currentUserId.equals(userId)){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        List<VideoHistoryResponse> histories = user.getVideoHistories().stream()
                .map(VideoHistoryResponse::of)
                .toList();

        return ResponseService.getListResult(histories);
    }

    //시청 기록 삭제
    @Transactional
    public SingleResult<Long> deleteById(Long id, String token){
        //시청 기록 삭제 요청 회원과 시청한 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        VideoHistory videoHistory = videoHistoryRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.HISTORY_NOT_EXIST));

        if(!currentUserId.equals(videoHistory.getUser().getId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
        user.getVideoHistories().remove(videoHistory);

        videoHistoryRepository.deleteById(id);
        return ResponseService.getSingleResult(videoHistory.getId());
    }

    //videoId와 userId로 videoHistory 찾기
    public SingleResult<VideoHistoryResponse> getHistoryByVideoAndUser(Long videoId, String token){
        //토큰에서 현자 userId 추출
        Long currentUserId = authService.getUserIdFromToken(token);

        VideoHistory videoHistory = videoHistoryRepository.findByUserIdAndVideoId(currentUserId, videoId)
                .orElseThrow(() -> new CustomException(ErrorCode.HISTORY_NOT_EXIST));

        return ResponseService.getSingleResult(VideoHistoryResponse.of(videoHistory));
    }
}
