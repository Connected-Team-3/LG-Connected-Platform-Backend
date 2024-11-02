package lg.connected_platform.user.dto.request;

import lg.connected_platform.videoHistory.entity.VideoHistory;

import java.time.LocalDateTime;
import java.util.List;

public record UserUpdateRequest(
        Long id,
        String loginId,
        String password,
        String name,
        List<VideoHistory> videoHistories
) {
}
