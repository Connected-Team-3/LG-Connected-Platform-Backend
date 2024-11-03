package lg.connected_platform.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.user.dto.request.UserUpdateRequest;
import lg.connected_platform.videoHistory.entity.VideoHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class User extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String loginId;

    @NotNull
    private String password;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "user")
    private List<VideoHistory> videoHistories = new ArrayList<>();

    @Builder
    public User(
            Long id,
            String loginId,
            String password,
            String name,
            List<VideoHistory> videoHistories){
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.videoHistories = videoHistories;
    }

    public User update(UserUpdateRequest request){
        this.id = request.id();
        this.loginId = request.loginId();
        this.password = request.password();
        this.name = request.name();
        this.videoHistories = request.videoHistories();
        return this;
    }
}
