package lg.connected_platform.videoHistory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.videoHistory.entity.VideoHistory;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VideoHistoryResponse(
    @NotNull
    @Schema(description = "개별 영상 시청 기록 id", example = "1")
    Long id,
    @NotNull
    @Schema(description = "해당 영상 id", example = "1")
    Long videoId,
    @NotNull
    @Schema(description = "시청한 user id", example = "1")
    Long userId,
    @NotNull
    @Schema(description = "어디까지 봤는지", example = "1")
    Long videoTimeStamp,
    @NotNull
    @Schema(description = "최근 시청 시각", example = "2024-11-09T14:35:10.123")
    LocalDateTime lastWatchedAt
) {
    public static VideoHistoryResponse of(VideoHistory videoHistory){
        return VideoHistoryResponse.builder()
                .id(videoHistory.getId())
                .videoId(videoHistory.getVideo().getId())
                .userId(videoHistory.getUser().getId())
                .videoTimeStamp(videoHistory.getVideoTimeStamp())
                .lastWatchedAt(videoHistory.getLastWatchedAt())
                .build();
    }
}
