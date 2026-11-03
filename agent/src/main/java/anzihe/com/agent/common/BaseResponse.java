package anzihe.com.agent.common;

import anzihe.com.agent.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {

    private T data;

    private int code;

    private String message;

    public BaseResponse(int code, String message, T data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public BaseResponse(ErrorCode errorCode, T data) {
        this(errorCode.getCode(), errorCode.getMessage(), data);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode, null);
    }
    public  BaseResponse(String message){
        this(0,message,null);
    }
}
