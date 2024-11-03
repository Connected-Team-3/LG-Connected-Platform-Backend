package lg.connected_platform.video.entity;

import jakarta.persistence.*;
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id") // 외래 키 컬럼 정의
    private User uploader;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
