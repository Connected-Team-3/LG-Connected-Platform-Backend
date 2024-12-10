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
import lg.connected_platform.video.dto.request.VideoUploadRequest;
import lg.connected_platform.video.dto.response.VideoResponse;
import lg.connected_platform.video.entity.Category;
import lg.connected_platform.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Tag(name = "영상(Video)")
@RequestMapping("/api/video")
public class VideoController {
    private final VideoService videoService;
    private final S3Client s3Client;

    //비디오, 썸네일을 S3에 업로드
    private String uploadVideoToS3(MultipartFile file, String fileType) throws IOException {
        try{

            // S3 버킷 이름과 업로드할 파일 경로 (key)
            String bucketName = "connectedplatform";
            String s3Key = fileType + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // MultipartFile에서 InputStream을 가져옵니다.
            InputStream inputStream = file.getInputStream();

            // PutObjectRequest 생성: 이 객체에서 파일과 ACL을 처리
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)   // S3 버킷 이름
                    .key(s3Key)           // S3에 저장할 파일 경로 (key)
                    .acl("public-read")   // 퍼블릭 읽기 권한 설정
                    .build();

            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, file.getSize()));

            //업로드 된 파일의 s3 url 반환
            return "https://connectedplatform.s3.ap-northeast-2.amazonaws.com/" + s3Key;
        }catch (Exception e){
            throw new RuntimeException("Failed to upload file to s3");
        }
    }

    //영상 업로드
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "영상 업로드")
    public SuccessResponse<SingleResult<Long>> upload(
            @Valid @ModelAttribute VideoCreateRequest request,
            @RequestParam("videoFile") MultipartFile videoFile,
            @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
            HttpServletRequest httpServletRequest) throws IOException {

        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);



        String videoUrl = uploadVideoToS3(videoFile, "videos");
        String thumbnailUrl = uploadVideoToS3(thumbnailFile, "thumbnails");

        VideoCreateRequest new_request = new VideoCreateRequest(
                request.title(),
                request.description(),
                request.uploaderId(),
                videoUrl,
                thumbnailUrl,
                request.hashtags(),
                request.category(),
                request.foodName(),
                request.ingredients());
        SingleResult<Long> result = videoService.save(new_request, token);
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
            HttpServletRequest httpServletRequest) {

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
            HttpServletRequest httpServletRequest) {

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
            @PathVariable("category") Category category,
            HttpServletRequest httpServletRequest) {
        //현재 시간 가져오기
        LocalDateTime now  = LocalDateTime.now();

        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        ListResult<VideoResponse> result = videoService.getVideosByCategory(category, token, now);
        return SuccessResponse.ok(result);
    }

    //특정 유저가 업로드한 영상 전체 조회
    @GetMapping("/getVideos/{userId}")
    @Operation(summary = "특정 유저가 업로드한 영상 전체 조회")
    public SuccessResponse<ListResult<VideoResponse>> getVideosByUserId(
            @PathVariable("userId") Long id) {
        ListResult<VideoResponse> result = videoService.getVideosByUserId(id);
        return SuccessResponse.ok(result);
    }

    //검색
    @GetMapping("/search")
    @Operation(summary = "제목, 요리명, 재료명으로 검색")
    public SuccessResponse<ListResult<VideoResponse>> search(@RequestParam("query") String query){
        ListResult<VideoResponse> result = videoService.searchVideos(query);
        return SuccessResponse.ok(result);
    }
}
