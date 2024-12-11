package lg.connected_platform.video.mapper;

import lg.connected_platform.food.entity.Food;
import lg.connected_platform.hashtag.entity.Hashtag;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.video.dto.request.VideoCreateRequest;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.videoHashtag.entity.VideoHashtag;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class VideoMapper {
    //DTO와 엔티티 사이의 변환을 담당
    public static Video from(VideoCreateRequest request, User uploader, Set<VideoHashtag> hashtags, Food food){
        return Video.builder()
                .title(request.title())
                .description(request.description())
                .uploader(uploader)
                .sourceUrl(request.sourceUrl())
                .thumbUrl(request.thumbUrl())
                .videoHashtags(hashtags)
                .category(request.category())
                .food(food)
                .build();
    }
}
