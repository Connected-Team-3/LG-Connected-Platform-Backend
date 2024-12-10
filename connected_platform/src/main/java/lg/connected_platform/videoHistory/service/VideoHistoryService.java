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
import lg.connected_platform.user.service.UserService;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.repository.VideoRepository;
import lg.connected_platform.videoHistory.dto.request.VideoHistoryUpdateRequest;
import lg.connected_platform.videoHistory.dto.response.VideoHistoryResponse;
import lg.connected_platform.videoHistory.entity.VideoHistory;
import lg.connected_platform.videoHistory.repository.VideoHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        /*if(!currentUserId.equals(request.userId())){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }*/


        //videoHistory가 존재하는지 확인하고 없으면 생성
        VideoHistory videoHistory = videoHistoryRepository.findByUserIdAndVideoId(currentUserId, request.videoId())
                .orElseGet(()->{
                    //새로운 VideoHistory 생성
                    User user = userRepository.findById(currentUserId)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
                    Video video = videoRepository.findById(request.videoId())
                            .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));

                    return VideoHistory.builder()
                            .user(user)
                            .video(video)
                            .build();
                });

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));
        user.getVideoHistories().add(videoHistory);
        //userRepository.save(user);

        videoHistoryRepository.save(videoHistory.update(request));

        //선호도 업데이트
        updateFoodPreferences(user);
        userRepository.save(user);

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
    public ListResult<VideoHistoryResponse> getUserVideoHistories(String token){
        //시청 기록 조회 요청 회원과 토큰에 저장된 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        /*if(!currentUserId.equals(userId)){
            throw new CustomException(ErrorCode.USER_MISMATCH);
        }*/

        User user = userRepository.findById(currentUserId)
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


    //유저의 시간대별 음식 선호도 조사
    public void updateFoodPreferences(User user){
        Map<String, List<String>> preferences = new HashMap<>();
        List<VideoHistory> histories = user.getVideoHistories();

        //시간대를 기준으로 음식 조회 기록 분류
        Map<String, List<Video>> categorizedVideos = histories.stream()
                .collect(Collectors.groupingBy(
                        (VideoHistory vh) ->{
                            LocalDateTime time = vh.getLastWatchedAt();
                            int hour = time.getHour();
                            if(hour >= 6 && hour < 12) return "morning";
                            else if(hour >= 12 && hour < 18) return "afternoon";
                            else if(hour >= 18 && hour < 24) return "evening";
                            else return "night";
                        },
                        Collectors.mapping(VideoHistory::getVideo, Collectors.toList())
                ));

        //시간대별로 음식 종류 집계
        for(Map.Entry<String, List<Video>> entry : categorizedVideos.entrySet()){
            String timeCategory = entry.getKey();
            List<Video> videos = entry.getValue();

            Map<String, Long> foodCount = videos.stream()
                    .map(video -> video.getFood().getName()) //음식 종류 추출
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            //음식 종류를 조회 수에 따라 정렬해서 상위 3개 선택
            List<String> topFoods = foodCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            preferences.put(timeCategory, topFoods);
        }

        user.setFoodPreferences(preferences);
    }
}
