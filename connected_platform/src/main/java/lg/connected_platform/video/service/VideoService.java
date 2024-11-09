package lg.connected_platform.video.service;

import jakarta.transaction.Transactional;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    //영상 업로드 -> 회원 여부 확인 필요
    public SingleResult<Long> save(VideoCreateRequest request, String token){
        //업로더와 업로드 요청 회원이 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        if(!currentUserId.equals(request.uploaderId())){
            throw new CustomException(ErrorCode.VIDEO_UPLOADER_MISMATCH);
        }

        //업로더 조회
        User uploader = userRepository.findById(request.uploaderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        Video newVideo = VideoMapper.from(request, uploader, new HashSet<>());

        //해시태그 처리
        Video finalNewVideo = newVideo;
        Set<Hashtag> hashtags = request.hashtags().stream()
                .map(tagName ->{
                    Hashtag hashtag = findOrCreateHashtag(tagName);
                    hashtag.getVideos().add(finalNewVideo);
                    return hashtag;
                })
                .collect(Collectors.toSet());

        newVideo.setHashtags(hashtags);
        newVideo = videoRepository.save(newVideo);
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


        //요청에 포함된 해시태그들
        Set<Hashtag> newHashTags = request.hashtags().stream()
                .map(this::findOrCreateHashtag)
                .collect(Collectors.toSet());

        //기존 해시태그들
        Set<Hashtag> currentHashTags = video.getHashtags();

        //제거할 해시태그
        Set<Hashtag> removeHashTags = currentHashTags.stream()
                .filter(hashtag -> !newHashTags.contains(hashtag))
                .collect(Collectors.toSet());

        //해시태그에서 해당 비디오 제거
        removeHashTags.forEach(hashtag -> {
            hashtag.getVideos().remove(video);
        });

        //video.setHashtags(newHashTags);
        videoRepository.save(video.update(request, newHashTags));
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

        video.getHashtags().forEach(hashtag -> {
            hashtag.getVideos().remove(video);
        });

        videoRepository.deleteById(id);
        return ResponseService.getSingleResult(video.getId());
    }

    //카테고리별 영상 전체 조회
    public ListResult<VideoResponse> getVideosByCategory(Category category){
        List<VideoResponse> videoList = videoRepository.findByCategory(category).stream()
                .map(VideoResponse::of)
                .toList();

        return ResponseService.getListResult(videoList);
    }
}
