package lg.connected_platform.video.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.video.entity.Category;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record VideoCreateRequest(
    @NotBlank
    @Schema(description = "video 제목", example = "제목입니다")
    String title,
    @NotBlank
    @Schema(description = "video 설명", example = "설명입니다")
    String description,
    @NotBlank
    @Schema(description = "영상 url", example = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    String sourceUrl,
    @NotBlank
    @Schema(description = "썸네일 url", example = "https://storage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg")
    String thumbUrl,
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
    Set<String> ingredients
) {
}
