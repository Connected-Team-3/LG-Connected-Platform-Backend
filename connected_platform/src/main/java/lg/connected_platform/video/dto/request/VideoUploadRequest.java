package lg.connected_platform.video.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.video.entity.Category;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record VideoUploadRequest(
        @NotBlank
        @Schema(description = "video 제목", example = "제목입니다")
        String title,
        @NotBlank
        @Schema(description = "video 설명", example = "설명입니다")
        String description,
        @NotNull
        @Schema(description = "업로더 회원 id", example = "1")
        Long uploaderId,
        @Schema(description = "해시태그", example = "[\"#abcd\", \"#efgh\", \"#ijkl\"]")
        Set<String> hashtags,
        @NotNull
        @Schema(description = "음식 카테고리", example = "KOREAN_FOOD")
        Category category,
        @NotBlank
        @Schema(description = "요리명", example = "연어 스테이크")
        String foodName,
        @NotNull
        @Schema(description = "재료명", example = "[\"연어\", \"후추\"]")
        Set<String> ingredients,
        @NotNull
        @Schema(description = "썸네일 이미지 파일")
        MultipartFile thumbnailFile,  // 썸네일 파일 추가

        @NotNull
        @Schema(description = "영상 파일")
        MultipartFile videoFile   // 영상 파일 추가
) {
}
