package lg.connected_platform.gst.Mapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Converter(autoApply = true)
public class PathConverter implements AttributeConverter<Path, String> {
    @Override
    public String convertToDatabaseColumn(Path path) {
        if (path != null) {
            return path.toString();
        }
        return null;
    }

    @Override
    public Path convertToEntityAttribute(String s) {
        if (s != null) {
            return Paths.get(s);
        }
        return null;
    }
}
