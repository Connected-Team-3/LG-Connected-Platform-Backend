package lg.connected_platform.video.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VideoCreateRequest(
    @NotBlank
    @Schema(description = "video 제목", example = "제목입니다")
    String title,
    @NotBlank
    @Schema(description = "video 설명", example = "설명입니다")
    String description,
    @NotNull
    @Schema(description = "업로더 회원 id", example = "1")
    Long uploaderId,
    @NotBlank
    @Schema(description = "영상 url", example = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    String sourceUrl,
    @NotBlank
    @Schema(description = "썸네일 url", example = "https://storage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg")
    String thumbUrl
) {
}
