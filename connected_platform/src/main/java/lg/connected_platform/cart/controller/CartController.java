package lg.connected_platform.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lg.connected_platform.cart.Service.CartService;
import lg.connected_platform.cart.dto.response.CartResponse;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "장바구니(Cart)")
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @PostMapping("/add/{videoId}")
    @Operation(summary = "video의 재료를 장바구니에 추가")
    public SuccessResponse<SingleResult<CartResponse>> addFoodIngredientsToCart(
            @PathVariable("videoId") Long videoId,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<CartResponse> result = cartService.addIngredientsToCart(videoId, token);
        return SuccessResponse.ok(result);
    }

    @GetMapping
    @Operation(summary = "장바구니 조회")
    public SuccessResponse<SingleResult<CartResponse>> getCart(HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<CartResponse> result = cartService.getCartByUserId(token);
        return SuccessResponse.ok(result);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "장바구니 비우기")
    public SuccessResponse<SingleResult<CartResponse>> clearCart(HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<CartResponse> result = cartService.clearCart(token);
        return SuccessResponse.ok(result);
    }
}
