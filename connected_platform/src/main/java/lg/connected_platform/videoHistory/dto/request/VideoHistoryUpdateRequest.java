package lg.connected_platform.videoHistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.entity.Video;

import java.time.LocalDateTime;

public record VideoHistoryUpdateRequest(
        Long id,
        Video video,
        Long videoTimeStamp,
        LocalDateTime lastWatchedAt,
        User user
) {
}
