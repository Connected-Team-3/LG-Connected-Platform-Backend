package lg.connected_platform.global.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lg.connected_platform.global.exception.CustomException;
import lg.connected_platform.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    //컨트롤러에 도달하기 전에 호출
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if(token != null && token.startsWith("Bearer ")){
            String jwtToken = token.substring(7);
            jwtUtil.verify(jwtToken);

            Long id = jwtUtil.getId(jwtToken);
            request.setAttribute("id", id);
            return true;
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}
