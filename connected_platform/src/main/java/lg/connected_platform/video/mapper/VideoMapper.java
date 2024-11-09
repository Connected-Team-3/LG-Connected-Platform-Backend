package lg.connected_platform.video.mapper;

import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.dto.request.VideoCreateRequest;
import lg.connected_platform.video.entity.Video;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class VideoMapper {
    //DTO와 엔티티 사이의 변환을 담당
    public static Video from(VideoCreateRequest request, User uploader, Set<Hashtag> hashtags){
        return Video.builder()
                .title(request.title())
                .description(request.description())
                .uploader(uploader)
                .sourceUrl(request.sourceUrl())
                .thumbUrl(request.thumbUrl())
                .hashtags(hashtags)
                .category(request.category())
                .build();
    }
}
