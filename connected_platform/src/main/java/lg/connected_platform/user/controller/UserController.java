package lg.connected_platform.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lg.connected_platform.global.dto.response.JwtTokenSet;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.user.dto.request.UserCreateRequest;
import lg.connected_platform.user.dto.request.UserLoginRequest;
import lg.connected_platform.user.dto.request.UserUpdateRequest;
import lg.connected_platform.user.dto.response.UserResponse;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "회원(User)")
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    //회원가입
    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public SuccessResponse<SingleResult<JwtTokenSet>> register(@Valid @RequestBody UserCreateRequest request){
        SingleResult<JwtTokenSet> result = userService.register(request);
        return SuccessResponse.ok(result);
    }

    //로그인
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public SuccessResponse<SingleResult<JwtTokenSet>> login(@Valid @RequestBody UserLoginRequest request){
        SingleResult<JwtTokenSet> result = userService.login(request);
        return SuccessResponse.ok(result);
    }

    //회원 정보 업데이트
    @PutMapping("/update")
    @Operation(summary = "회원 정보 업데이트")
    public SuccessResponse<SingleResult<UserResponse>> update(@Valid @RequestBody UserUpdateRequest request){
        SingleResult<UserResponse> result = userService.updateUser(request);
        return SuccessResponse.ok(result);
    }

    //로그아웃 엔드포인트 -> 프론트에서 로컬 스토리지/쿠키 삭제 필요
    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public SuccessResponse<SingleResult<Void>> logout() {
        SingleResult<Void> result = userService.logout();
        return SuccessResponse.ok(result);
    }
}
