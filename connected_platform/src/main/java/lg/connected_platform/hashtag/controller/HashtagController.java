package lg.connected_platform.hashtag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.hashtag.dto.service.HashtagService;
import lg.connected_platform.video.dto.response.VideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "해시태그(Hashtag)")
@RequestMapping("/api/hashtag")
public class HashtagController {
    private final HashtagService hashtagService;

    //해당 해시태그를 포함하는 영상 전체 조회
    @GetMapping("/getVideos/{hashtagId}")
    @Operation(summary = "해당 해시태그를 포함하는 영상 전체 조회")
    public SuccessResponse<ListResult<VideoResponse>> getVideos(@PathVariable("hashtagId") Long id){
        ListResult<VideoResponse> result = hashtagService.getVideosByHashtag(id);
        return SuccessResponse.ok(result);
    }
}
