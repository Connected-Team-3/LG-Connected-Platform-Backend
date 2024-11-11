package lg.connected_platform.user.service;

import jakarta.transaction.Transactional;
import lg.connected_platform.global.dto.response.JwtTokenSet;
import lg.connected_platform.global.dto.response.result.SingleResult;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import lg.connected_platform.global.service.AuthService;
import lg.connected_platform.global.service.ResponseService;
import lg.connected_platform.user.dto.request.UserCreateRequest;
import lg.connected_platform.user.dto.request.UserLoginRequest;
import lg.connected_platform.user.dto.request.UserUpdateRequest;
import lg.connected_platform.user.dto.response.UserResponse;
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
    @Transactional
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

    //회원 정보 수정 -> 회원 여부 확인 필요
    @Transactional
    public SingleResult<UserResponse> updateUser(UserUpdateRequest request, String token){
        //회원 정보 수정 요청을 하는 유저와 수정 대상인 유저가 같아야 함
        Long currentUserId = authService.getUserIdFromToken(token);

        if(!currentUserId.equals(request.id())){
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        userRepository.save(user.update(request)); //dirty checking으로 반환값 안 받아도 업데이트 반영
        return ResponseService.getSingleResult(UserResponse.of(user));
    }

    //로그아웃 -> 회원 여부 확인 필요
    public SingleResult<Void> logout(){
        //토큰 무효화 클라이언트에서 처리
        Void tmp = null;
        return ResponseService.getSingleResult(tmp);
    }

    //특정 회원 조회
    public SingleResult<UserResponse> findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_EXIST));

        return ResponseService.getSingleResult(UserResponse.of(user));
    }

}
