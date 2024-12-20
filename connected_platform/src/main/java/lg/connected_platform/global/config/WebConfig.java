package lg.connected_platform.global.config;

import lg.connected_platform.global.jwt.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }
/*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://") // “*“같은 와일드카드를 사용
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP method
                .allowCredentials(true); // 쿠키 인증 요청 허용
    }*/

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/user/update");
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/user/logout");
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/video/upload");
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/video/play/*");
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/video/update");
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/api/video/delete/*");
    }
}
