package lg.connected_platform.user.mapper;

import lg.connected_platform.user.dto.request.UserCreateRequest;
import lg.connected_platform.user.entity.User;

public class UserMapper {
    //DTO와 엔티티 사이의 변환을 담당
    public static User from(UserCreateRequest request){
        return User.builder()
                .loginId(request.loginId())
                .password(request.password())
                .name(request.name())
                .build();
    }
}
