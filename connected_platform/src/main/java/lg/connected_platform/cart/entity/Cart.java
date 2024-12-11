package lg.connected_platform.cart.entity;

import jakarta.persistence.*;
import lg.connected_platform.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "ingredient")
    @Column(name = "quantity")
    private Map<String, Integer> items = new HashMap<>();

    public void addItem(String ingredient, int quantity){
        items.put(ingredient, items.getOrDefault(ingredient, 0) + quantity);
    }

    public void removeItem(String ingredient){
        items.remove(ingredient);
    }


}
