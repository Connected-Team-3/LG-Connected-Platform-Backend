package lg.connected_platform.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private Exception originalException;
    private final ErrorCode errorCode;

    /**
     * CustomException을 만들 때, 원래 발생한 예외가 없는 상황
     * 비즈니스 로직 위반, 요청 유효성 검증 실패 등과 같이 개발자가 명시적으로 CustomException을 발생시키는 상황
     * 자체적으로 정의한 예외 사황을 표현함 -> 어떤 오류가 발생했는지에 대한 정보만 필요 / 구체적인 원본 예외 필요 X
     */
    public CustomException(ErrorCode errorCode){
        this.errorCode = errorCode;
    }

    public CustomException(Exception originException, ErrorCode errorCode){
        this.originalException = originException;
        this.errorCode = errorCode;
    }
}
