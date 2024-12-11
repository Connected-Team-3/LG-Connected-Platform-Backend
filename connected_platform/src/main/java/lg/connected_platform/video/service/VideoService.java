package lg.connected_platform.video.service;

import jakarta.transaction.Transactional;
import lg.connected_platform.food.entity.Food;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.hashtag.repository.HashtagRepository;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.repository.UserRepository;
import lg.connected_platform.video.dto.request.VideoCreateRequest;
import lg.connected_platform.video.dto.request.VideoUpdateRequest;
import lg.connected_platform.video.dto.response.VideoResponse;
import lg.connected_platform.video.entity.Category;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.mapper.VideoMapper;
import lg.connected_platform.video.repository.VideoRepository;
import lg.connected_platform.videoHashtag.entity.VideoHashtag;
import lg.connected_platform.videoHashtag.repository.VideoHashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final HashtagRepository hashtagRepository;
    private final VideoHashtagRepository videoHashtagRepository;

    //영상 업로드 -> 회원 여부 확인 필요
    @Transactional
    public SingleResult<Long> save(VideoCreateRequest request, String token){
        //업로더와 업로드 요청 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        /*if(!currentUserId.equals(request.uploaderId())){
            throw new CustomException(ErrorCode.VIDEO_UPLOADER_MISMATCH);
        }*/

        //업로더 조회
        User uploader = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        //Food 생성 및 설정
        Food food = new Food();
        food.setName(request.foodName());
        food.setIngredients(request.ingredients());

        Video newVideo = VideoMapper.from(request, uploader, new HashSet<>(), food);
        newVideo = videoRepository.save(newVideo);

        Set<VideoHashtag> tmp = new HashSet<>();
        //해시태그 처리
        Video finalNewVideo = newVideo;
        request.hashtags().forEach(tagName ->{
            Hashtag hashtag = findOrCreateHashtag(tagName);
            VideoHashtag videoHashtag = new VideoHashtag(finalNewVideo, hashtag);
            videoHashtagRepository.save(videoHashtag);
            tmp.add(videoHashtag);
        });
        //System.out.println(tmp);

        newVideo.setVideoHashtags(tmp);

        newVideo = videoRepository.save(newVideo);
        uploader.getVideos().add(newVideo);
        return ResponseService.getSingleResult(newVideo.getId());
    }

    //해시태그가 이미 존재하면 재사용하고, 없으면 생성하여 저장
    private Hashtag findOrCreateHashtag(String tagName){
        return hashtagRepository.findByName(tagName)
                .orElseGet(() -> hashtagRepository.save(new Hashtag(tagName)));
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

        User uploader = userRepository.findById(request.uploaderId())
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        // Food 업데이트
        Food food = video.getFood();
        food.setName(request.foodName());
        food.setIngredients(request.ingredients());

        uploader.getVideos().remove(video);


        //요청에 포함된 해시태그들
        Set<Hashtag> newHashTags = request.hashtags().stream()
                .map(this::findOrCreateHashtag)
                .collect(Collectors.toSet());

        //VideoHashtag를 통해 기존 해시태그 정리
        videoHashtagRepository.deleteAllByVideo(video);

        Set<VideoHashtag> tmp = new HashSet<>();

        //새로운 VideoHashtag 생성
        newHashTags.forEach(tag->{
            VideoHashtag videoHashtag = new VideoHashtag(video, tag);
            videoHashtagRepository.save(videoHashtag);
            tmp.add(videoHashtag);
        });

        //비디오 내용 업데이트
        video.update(request, tmp);

        //user의 videos 리스트 업데이트
        uploader.getVideos().add(video);

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

        if(!currentUserId.equals(video.getUploader().getId())){
            throw new CustomException(ErrorCode.VIDEO_UPLOADER_MISMATCH);
        }

        User uploader = video.getUploader();
        uploader.getVideos().remove(video);

        //VideoHashtag 관계 삭제
        videoHashtagRepository.deleteAllByVideo(video);

        //Video 삭제
        videoRepository.deleteById(id);

        return ResponseService.getSingleResult(video.getId());
    }

    //요청을 보낸 시간대 확인
    private String determineTimeZone(LocalDateTime now){
        int hour = now.getHour();
        if(hour >= 6 && hour < 12) return "morning";
        else if(hour >= 12 && hour < 18) return "afternoon";
        else if(hour >= 18 && hour < 24) return "evening";
        else return "night";
    }

    //foodPreferences에서 특정 시간대의 선호도 가져오기
    public List<String> getTopFoodsByCurrentTime(User user, LocalDateTime requestTime){
        String timeZone = determineTimeZone(requestTime);
        return user.getFoodPreferences().getOrDefault(timeZone, List.of());
    }

    //카테고리별 영상 전체 조회
    public ListResult<VideoResponse> getVideosByCategory(Category category, String token, LocalDateTime requestTime){
        Long currentUserId = authService.getUserIdFromToken(token);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        //요청을 보낸 시간대의 선호도 가져오기
        List<String> topFoods = getTopFoodsByCurrentTime(user, requestTime);

        List<Video> videoList;
        //카테고리별 비디오 리스트 조회
        if(category == Category.ALL){
            videoList = videoRepository.findAll();
        }
        else videoList = videoRepository.findByCategory(category);

        //요청을 보낸 시간대의 선호 음식이 앞에 오도록 비디오 리스트 정렬
        videoList.sort(Comparator.comparingInt((Video video) ->{
            String foodName = video.getFood().getName();
            int index = topFoods.indexOf(foodName);
            return index == -1 ? Integer.MAX_VALUE : index;
        }));

        List<VideoResponse> videoResponseList = videoList.stream()
                .map(VideoResponse::of)
                .toList();

        return ResponseService.getListResult(videoResponseList);
    }

    //특정 유저가 업로드한 영상 전체 조회
    public ListResult<VideoResponse> getVideosByUserId(Long userId){
        User uploader = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        List<VideoResponse> videos = uploader.getVideos().stream()
                .map(VideoResponse::of)
                .toList();

        return ResponseService.getListResult(videos);
    }

    //검색 : 영상 제목, 요리명, 재료명
    public ListResult<VideoResponse> searchVideos(String searchTerm){
        List<Video> videos = videoRepository.searchByTitleOrFoodOrIngredients(searchTerm);
        List<VideoResponse> responses = videos.stream()
                .map(VideoResponse::of)
                .toList();
        return ResponseService.getListResult(responses);
    }
}
