package lg.connected_platform.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "LG Connected Platform Backend API 명세서",
                description = "API 명세서",
                version = "v1",
                contact = @Contact(
                        name = "정현정",
                        email = "cathy2750@naver.com"
                )
        )
)
@Configuration
public class OpenApiConfig {
}
