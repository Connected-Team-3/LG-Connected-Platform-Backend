package lg.connected_platform.video.entity;

import jakarta.persistence.Entity;
import lg.connected_platform.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    private Long id;
    private String title;
    private String description;
    private User uploader;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
