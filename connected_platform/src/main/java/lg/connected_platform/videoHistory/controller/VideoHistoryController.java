package lg.connected_platform.videoHistory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.videoHistory.dto.request.VideoHistoryUpdateRequest;
import lg.connected_platform.videoHistory.dto.response.VideoHistoryResponse;
import lg.connected_platform.videoHistory.service.VideoHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "영상 시청 기록(VideoHistory)")
@RequestMapping("/api/videoHistory")
public class VideoHistoryController {
    private final VideoHistoryService videoHistoryService;

    //영상 시청 기록 생성 또는 업데이트
    @PostMapping("/create")
    @Operation(summary = "영상 시청 기록 생성 또는 업데이트")
    public SuccessResponse<SingleResult<Long>> create(
            @Valid @RequestBody VideoHistoryUpdateRequest request,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<Long> result = videoHistoryService.createOrUpdate(request, token);
        return SuccessResponse.ok(result);
    }

    //영상 시청 기록 단건 조회
    @GetMapping("/getHistory/{videoHistoryId}")
    @Operation(summary = "영상 시청 기록 단건 조회")
    public SuccessResponse<SingleResult<VideoHistoryResponse>> getHistory(
            @PathVariable("videoHistoryId") Long id,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<VideoHistoryResponse> result = videoHistoryService.findById(id, token);
        return SuccessResponse.ok(result);
    }

    //유저의 전체 시청 기록 조회
    @GetMapping("/getHistories/{userId}")
    @Operation(summary = "유저의 영상 시청 기록 전체 조회")
    public SuccessResponse<ListResult<VideoHistoryResponse>> getHistories(
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        ListResult<VideoHistoryResponse> result = videoHistoryService.getUserVideoHistories(token);
        return SuccessResponse.ok(result);
    }

    //시청 기록 삭제
    @DeleteMapping("/delete/{videoHistoryId}")
    @Operation(summary = "영상 시청 기록 단건 삭제")
    public SuccessResponse<SingleResult<Long>> delete(
            @PathVariable("videoHistoryId") Long id,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<Long> result = videoHistoryService.deleteById(id, token);
        return SuccessResponse.ok(result);
    }

    //videoId와 userId로 videoHistory 찾기
    @GetMapping("/getHistoryByVideo/{videoId}")
    @Operation(summary = "videoId와 userId로 시청 기록 조회")
    public SuccessResponse<SingleResult<VideoHistoryResponse>> getHistoryByVideoAndUser(
            @PathVariable("videoId") Long videoId,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<VideoHistoryResponse> result = videoHistoryService.getHistoryByVideoAndUser(videoId, token);
        return SuccessResponse.ok(result);
    }
}
