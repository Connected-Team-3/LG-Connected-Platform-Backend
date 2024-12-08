package lg.connected_platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.videoHistory.entity.VideoHistory;

import java.time.LocalDateTime;
import java.util.List;

public record UserUpdateRequest(
        @NotBlank
        @Schema(description = "수정할 로그인 아이디", example = "abcd")
        String loginId,
        @NotBlank
        @Schema(description = "수정할 회원 비밀번호", example = "pwd")
        String password,
        @NotBlank
        @Schema(description = "수정할 회원 이름", example = "정현정")
        String name
) {
}
