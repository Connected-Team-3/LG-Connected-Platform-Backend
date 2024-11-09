package lg.connected_platform.videoHistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.entity.Video;

import java.time.LocalDateTime;

public record VideoHistoryUpdateRequest(
        @NotNull
        @Schema(description = "user id", example = "1")
        Long userId,
        @NotNull
        @Schema(description = "video id", example = "1")
        Long videoId,
        @NotNull
        @Schema(description = "어디까지 봤는지", example = "0")
        Long videoTimeStamp
) {
}
