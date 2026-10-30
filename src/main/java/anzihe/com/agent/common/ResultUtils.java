package anzihe.com.agent.common;

import anzihe.com.agent.exception.ErrorCode;

public class ResultUtils {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "ok", data);
    }

    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode, null);
    }

    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    public  static  BaseResponse<?> error(String message){
        return new BaseResponse<>(message);
    }

    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message, null);
    }
}
