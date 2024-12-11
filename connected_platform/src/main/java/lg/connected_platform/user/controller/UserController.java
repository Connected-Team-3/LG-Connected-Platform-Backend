package lg.connected_platform.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lg.connected_platform.global.dto.response.JwtTokenSet;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
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
    public SuccessResponse<SingleResult<UserResponse>> update(
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<UserResponse> result = userService.updateUser(request, token);
        return SuccessResponse.ok(result);
    }

    //로그아웃 엔드포인트 -> 프론트에서 로컬 스토리지/쿠키 삭제 필요
    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public SuccessResponse<SingleResult<Void>> logout() {
        SingleResult<Void> result = userService.logout();
        return SuccessResponse.ok(result);
    }

    //특정 유저 조회
    @GetMapping("/{userId}")
    @Operation(summary = "특정 회원 조회")
    public SuccessResponse<SingleResult<UserResponse>> findById(
            HttpServletRequest httpServletRequest){
        //Http 헤더의 Authorization에서 토큰 추출
        String token = httpServletRequest.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            // 토큰이 없거나 형식이 올바르지 않을 경우 예외 처리
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // "Bearer " 부분 제거
        token = token.substring(7);

        SingleResult<UserResponse> result = userService.findById(token);
        return SuccessResponse.ok(result);
    }
}
