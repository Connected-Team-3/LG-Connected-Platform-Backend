package lg.connected_platform.playlist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.ListResult;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.playlist.dto.request.PlaylistCreateRequest;
import lg.connected_platform.playlist.dto.request.PlaylistUpdateRequest;
import lg.connected_platform.playlist.dto.response.PlaylistResponse;
import lg.connected_platform.playlist.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "플레이리스트(Playlist)")
@RequestMapping("/api/playlist")
public class PlaylistController {
    private final PlaylistService playlistService;

    //플레이리스트 생성
    @PostMapping("/create")
    @Operation(summary = "플레이리스트 생성")
    public SuccessResponse<SingleResult<Long>> create(
            @Valid @RequestBody PlaylistCreateRequest request,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<Long> result = playlistService.create(request, token);
        return SuccessResponse.ok(result);
    }

    //플레이리스트 조회
    @GetMapping("/{playlistId}")
    @Operation(summary = "플레이리스트 조회")
    public SuccessResponse<SingleResult<PlaylistResponse>> getPlaylist(
            @PathVariable("playlistId") Long id){
        SingleResult<PlaylistResponse> result = playlistService.findById(id);
        return SuccessResponse.ok(result);
    }

    //플레이리스트 업데이트 : 영상 하나 삭제 or 삽입
    @PutMapping("/update")
    @Operation(summary = "플레이리스트 수정")
    public SuccessResponse<SingleResult<PlaylistResponse>> update(
            @Valid @RequestBody PlaylistUpdateRequest request,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<PlaylistResponse> result = playlistService.updatePlaylist(request, token);
        return SuccessResponse.ok(result);
    }

    //플레이리스트 삭제 : 리스트 전체 삭제
    @DeleteMapping("/delete/{playlistId}")
    @Operation(summary = "플레이리스트 삭제")
    public SuccessResponse<SingleResult<Long>> delete(
            @PathVariable("playlistId") Long id,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<Long> result = playlistService.deleteById(id, token);
        return SuccessResponse.ok(result);
    }

    //특정 유저의 전체 플레이리스트 조회
    @GetMapping("/getPlaylist")
    @Operation(summary = "특정 유저의 전체 플레이리스트 조회")
    public SuccessResponse<ListResult<PlaylistResponse>> getPlaylistByUserId(
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        ListResult<PlaylistResponse> result = playlistService.getUserPlaylist(token);
        return SuccessResponse.ok(result);
    }

}
