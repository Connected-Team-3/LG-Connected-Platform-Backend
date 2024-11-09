package lg.connected_platform.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.video.dto.request.VideoCreateRequest;
import lg.connected_platform.video.dto.request.VideoUpdateRequest;
import lg.connected_platform.video.dto.response.VideoResponse;
import lg.connected_platform.video.entity.Category;
import lg.connected_platform.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "영상(Video)")
@RequestMapping("/api/video")
public class VideoController {
    private final VideoService videoService;

    //영상 업로드
    @PostMapping("/upload")
    @Operation(summary = "영상 업로드")
    public SuccessResponse<SingleResult<Long>> upload(
            @Valid @RequestBody VideoCreateRequest request,
            HttpServletRequest httpServletRequest) {

        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<Long> result = videoService.save(request, token);
        return SuccessResponse.ok(result);
    }

    //영상 조회
    @GetMapping("/play/{videoId}")
    @Operation(summary = "영상 조회")
    public SuccessResponse<SingleResult<VideoResponse>> play(
            @PathVariable("videoId") Long id) {
        SingleResult<VideoResponse> result = videoService.findById(id);
        return SuccessResponse.ok(result);
    }

    //영상 수정
    @PutMapping("/update")
    @Operation(summary = "영상 수정")
    public SuccessResponse<SingleResult<VideoResponse>> update(
            @Valid @RequestBody VideoUpdateRequest request,
            HttpServletRequest httpServletRequest){

        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<VideoResponse> result = videoService.updateVideo(request, token);
        return SuccessResponse.ok(result);
    }

    //영상 삭제
    @DeleteMapping("/delete/{videoId}")
    @Operation(summary = "영상 삭제")
    public SuccessResponse<SingleResult<Long>> delete(
            @PathVariable("videoId") Long id,
            HttpServletRequest httpServletRequest){

        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<Long> result = videoService.deleteById(id, token);
        return SuccessResponse.ok(result);
    }

    //카테고리별 조회
    @GetMapping("/{category}")
    @Operation(summary = "카테고리별 조회")
    public SuccessResponse<ListResult<VideoResponse>> getVideosByCategory(
            @PathVariable("category")Category category){
        ListResult<VideoResponse> result = videoService.getVideosByCategory(category);
        return SuccessResponse.ok(result);
    }
}
