package lg.connected_platform.videoHistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record VideoHistoryCreateRequest(
    @NotNull
    @Schema(description = "비디오 id", example = "1")
    Long videoId,
    @NotNull
    @Schema(description = "어디까지 봤는지", example = "0")
    Long videoTimeStamp,
    @NotNull
    @Schema(description = "회원 id", example = "1")
    Long userId
) {
}
