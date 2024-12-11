package lg.connected_platform.playlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaylistUpdateRequest(
        @NotNull
        @Schema(description = "playlist id", example = "1")
        Long id,
        @NotNull
        @Schema(description = "video id", example = "1")
        Long videoId,
        @NotBlank
        @Schema(description = "플레이리스트 제목", example = "제목입니다")
        String title,
        @NotNull
        @Schema(description = "삽입", example = "true")
        Boolean insertFlag, //삽입이면 true
        @NotNull
        @Schema(description = "삭제", example = "false")
        Boolean deleteFlag //삭제면 true
) {
}
