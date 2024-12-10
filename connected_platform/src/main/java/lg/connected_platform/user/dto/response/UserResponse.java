package lg.connected_platform.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.videoHistory.dto.response.VideoHistoryResponse;
import lg.connected_platform.videoHistory.entity.VideoHistory;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record UserResponse(
        @NotNull
        @Schema(description = "회원 id", example = "1")
        Long id,
        @NotBlank
        @Schema(description = "로그인 id", example = "abcd")
        String loginId,
        @NotBlank
        @Schema(description = "비밀번호", example = "pwd")
        String password,
        @NotBlank
        @Schema(description = "이름", example = "정현정")
        String name,
        @NotNull
        @Schema(description = "시청 기록")
        List<VideoHistoryResponse> videoHistories,
        @NotNull
        @Schema(description = "선호 음식")
        Map<String, List<String>> foodPreferences
) {
    public static UserResponse of(User user){
        return UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .password(user.getPassword())
                .name(user.getName())
                .videoHistories(user.getVideoHistories().stream()
                        .map(VideoHistoryResponse::of).toList())
                .foodPreferences(user.getFoodPreferences())
                .build();
    }
}
