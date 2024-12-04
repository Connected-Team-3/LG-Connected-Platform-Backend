package lg.connected_platform.food.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lg.connected_platform.common.entity.TimeStamp;
import lg.connected_platform.video.entity.Video;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Food extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //요리명
    @NotBlank
    private String name;

    /*
    Food 테이블
    +----+---------------+
    | id | name          |
    +----+---------------+
    | 1  | 연어 스테이크 |
    +----+---------------+

    food_ingredients 테이블
    +----------+-------------+
    | food_id  | ingredient  |
    +----------+-------------+
    | 1        | 연어         |
    | 1        | 소금         |
    | 1        | 후추         |
    +----------+-------------+
     */
    @ElementCollection
    @CollectionTable(name = "food_ingredients", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "ingredient")
    private Set<String> ingredients = new HashSet<>();

    @OneToMany(mappedBy = "food")
    private List<Video> videos = new ArrayList<>();

    @Builder
    public Food(String name, Set<String> ingredients){
        this.name = name;
        this.ingredients = ingredients;
    }
}
