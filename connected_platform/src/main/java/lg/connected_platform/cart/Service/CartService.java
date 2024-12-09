package lg.connected_platform.cart.Service;

import jakarta.transaction.Transactional;
import lg.connected_platform.cart.dto.response.CartResponse;
import lg.connected_platform.cart.entity.Cart;
import lg.connected_platform.cart.repository.CartRepository;
import lg.connected_platform.food.entity.Food;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.repository.UserRepository;
import lg.connected_platform.video.entity.Video;
import lg.connected_platform.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final AuthService authService;

    //userId로 장바구니 가져오기
    public SingleResult<CartResponse> getCartByUserId(String token){
        Long userId = authService.getUserIdFromToken(token);
        Cart cart =  cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
        return ResponseService.getSingleResult(CartResponse.of(cart));
    }

    private Cart createCartForUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    //장바구니에 재료 추가
    @Transactional
    public SingleResult<CartResponse> addIngredientsToCart(Long videoId, String token){
        Long currentUserId = authService.getUserIdFromToken(token);
        Video video = videoRepository.findById(videoId)
                .orElseThrow(()-> new CustomException(ErrorCode.VIDEO_NOT_EXIST));
        Food food = video.getFood();

        if(food == null){
            throw new CustomException(ErrorCode.FOOD_NOT_EXIST);
        }

        Cart cart =  cartRepository.findByUserId(currentUserId)
                .orElseGet(() -> createCartForUser(currentUserId));

        //Food 재료를 장바구니에 추가
        for(String ingredient : food.getIngredients()){
            cart.addItem(ingredient, 1);
        }

        cartRepository.save(cart);

        return ResponseService.getSingleResult(CartResponse.of(cart));
    }

    public SingleResult<CartResponse> clearCart(String token){
        Long userId = authService.getUserIdFromToken(token);
        Cart cart =  cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));

        cart.getItems().clear();
        cartRepository.save(cart);

        return ResponseService.getSingleResult(CartResponse.of(cart));
    }
}
