package lg.connected_platform.global.service;

import lg.connected_platform.global.dto.response.JwtTokenSet;
import lg.connected_platform.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;

    public JwtTokenSet generateToken(Long userId){
        String token = jwtUtil.createToken(userId);

        JwtTokenSet jwtTokenSet = JwtTokenSet.builder()
                .token(token)
                .build();

        return jwtTokenSet;
    }

    public Long getUserIdFromToken(String token){
        return jwtUtil.getId(token);
    }
}
