package lg.connected_platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank
        @Schema(description = "로그인 아이디", example = "abcd")
        String loginId,
        @NotBlank
        @Schema(description = "회원 비밀번호", example = "pwd")
        String password,
        @NotBlank
        @Schema(description = "회원 이름", example = "정현정")
        String name
) {
}
