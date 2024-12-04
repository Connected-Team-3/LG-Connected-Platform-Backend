package lg.connected_platform.video.repository;

import lg.connected_platform.video.entity.Category;
import lg.connected_platform.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCategory(Category category);

    @Query("SELECT v FROM Video v " +
            "LEFT JOIN v.food f " +
            "LEFT JOIN f.ingredients i " +
            "WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(i) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Video> searchByTitleOrFoodOrIngredients(@Param("searchTerm") String searchTerm);
}
