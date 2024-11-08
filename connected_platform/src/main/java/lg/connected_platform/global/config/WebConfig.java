package lg.connected_platform.global.config;

import lg.connected_platform.global.jwt.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

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
