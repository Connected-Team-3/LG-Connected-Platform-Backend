package lg.connected_platform.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lg.connected_platform.global.dto.response.JwtTokenSet;
import lg.connected_platform.global.dto.response.SuccessResponse;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.user.dto.request.UserCreateRequest;
import lg.connected_platform.user.dto.request.UserLoginRequest;
import lg.connected_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(description = "로그인")
    public SuccessResponse<SingleResult<JwtTokenSet>> login(@Valid @RequestBody UserLoginRequest request){
        SingleResult<JwtTokenSet> result = userService.login(request);
        return SuccessResponse.ok(result);
    }
}
