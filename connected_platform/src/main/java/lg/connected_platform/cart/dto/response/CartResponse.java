package lg.connected_platform.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lg.connected_platform.cart.entity.Cart;
import lombok.Builder;

import java.util.Map;

@Builder
public record CartResponse(
        @NotNull
        @Schema(description = "장바구니 id", example = "1")
        Long id,
        @NotBlank
        @Schema(description = "유저 id", example = "1")
        Long userId,
        @NotNull
        @Schema(description = "재료 및 수량", example = "{\"연어\": 2, \"소금\": 1}")
        Map<String, Integer> items
) {
    public static CartResponse of(Cart cart){
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(cart.getItems())
                .build();
    }
}
