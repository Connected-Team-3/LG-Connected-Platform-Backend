package lg.connected_platform.playlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaylistCreateRequest(
        @NotNull
        @Schema(description = "user id", example = "1")
        Long userId,
        @NotNull
        @Schema(description = "영상 목록", example = "[1, 2, 3]")
        List<Long> videoIdList,
        @NotBlank
        @Schema(description = "플레이리스트 제목", example = "제목입니다")
        String title
) {
}
