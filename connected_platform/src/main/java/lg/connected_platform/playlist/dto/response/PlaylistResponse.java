package lg.connected_platform.playlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.playlist.entity.Playlist;
import lg.connected_platform.video.entity.Video;
import lombok.Builder;

import java.util.List;

@Builder
public record PlaylistResponse(
        @NotNull
        @Schema(description = "플레이리스트 id", example = "1")
        Long id,
        @NotNull
        @Schema(description = "user id", example = "1")
        Long userId,
        @NotNull
        @Schema(description = "비디오 id list", example = "[1, 2, 3]")
        List<Long> videoIdList,
        @NotBlank
        @Schema(description = "플레이리스트 제목", example = "제목입니다")
        String title
) {
        public static PlaylistResponse of(Playlist playlist){
                List<Long> videoIdList = playlist.getVideos().stream()
                        .map(Video::getId)
                        .toList();

                return PlaylistResponse.builder()
                        .id(playlist.getId())
                        .userId(playlist.getUser().getId())
                        .videoIdList(videoIdList)
                        .title(playlist.getTitle())
                        .build();
        }
}
