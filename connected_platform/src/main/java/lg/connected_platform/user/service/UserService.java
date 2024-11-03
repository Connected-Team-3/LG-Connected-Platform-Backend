package lg.connected_platform.user.service;

import lg.connected_platform.global.dto.response.JwtTokenSet;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.user.dto.request.UserCreateRequest;
import lg.connected_platform.user.dto.request.UserLoginRequest;
import lg.connected_platform.user.entity.User;
import lg.connected_platform.user.mapper.UserMapper;
import lg.connected_platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthService authService;

    //회원가입
    public SingleResult<JwtTokenSet> register(UserCreateRequest request){
        //로그인 아이디 중복 체크
        if(userRepository.existsByLoginId(request.loginId())){
            throw new CustomException(ErrorCode.USER_ALREADY_EXIST);
        }

        User newUser = userRepository.save(UserMapper.from(request));

        JwtTokenSet jwtTokenSet = authService.generateToken(newUser.getId());
        return ResponseService.getSingleResult(jwtTokenSet);
    }

    //로그인
    public SingleResult<JwtTokenSet> login(UserLoginRequest request){
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        //비밀번호 검증
        if(!user.getPassword().equals(request.password())){
            throw new CustomException(ErrorCode.USER_WRONG_PASSWORD);
        }

        JwtTokenSet jwtTokenSet = authService.generateToken(user.getId());
        return ResponseService.getSingleResult(jwtTokenSet);
    }
}
