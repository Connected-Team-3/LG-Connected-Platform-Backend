package lg.connected_platform.hashtag.service;

import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.hashtag.repository.HashtagRepository;
import lg.connected_platform.video.dto.response.VideoResponse;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHashtag.repository.VideoHashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;
    private final VideoHashtagRepository videoHashtagRepository;

    //해당 해시태그를 포함한 영상 전체 조회
    public ListResult<VideoResponse> getVideosByHashtag(Long id){
        Hashtag hashtag = hashtagRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.HASHTAG_NOT_EXIST));

        List<VideoResponse> videos = videoHashtagRepository.findByHashtag(hashtag).stream()
                .map(videoHashtag -> VideoResponse.of(videoHashtag.getVideo()))
                .toList();
        return ResponseService.getListResult(videos);
    }

}
