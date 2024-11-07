package lg.connected_platform.video.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.video.entity.Video;
import lombok.Builder;

@Builder
public record VideoResponse(
        @NotNull
        @Schema(description = "비디오 id", example = "1")
        Long id,
        @NotBlank
        @Schema(description = "제목", example = "제목입니다")
        String title,
        @NotBlank
        @Schema(description = "설명", example = "설명입니다")
        String description,
        @NotNull
        @Schema(description = "업로더 id", example = "1")
        Long uploaderId,
        @NotBlank
        @Schema(description = "영상 url", example = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        String sourceUrl,
        @NotBlank
        @Schema(description = "썸네일 url", example = "https://storage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg")
        String thumbUrl
) {
    public static VideoResponse of(Video video){
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .uploaderId(video.getUploader().getId())
                .sourceUrl(video.getSourceUrl())
                .thumbUrl(video.getThumbUrl())
                .build();
    }
}
