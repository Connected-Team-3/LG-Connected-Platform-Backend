package lg.connected_platform.gst.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.gst.Mapper.PathConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Gst {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Convert(converter = PathConverter.class)
    private Path playlistRoot;

    @NotBlank
    private Long videoId;

    public Gst(Path playlistRoot, Long videoId) {
        this.playlistRoot = playlistRoot;
        this.videoId = videoId;
    }
}
