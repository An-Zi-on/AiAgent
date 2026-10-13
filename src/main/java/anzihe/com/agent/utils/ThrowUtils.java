package anzihe.com.agent.utils;

import anzihe.com.agent.exception.BusinessException;
import anzihe.com.agent.exception.ErrorCode;

public class ThrowUtils {

    public static void throwException(boolean condition, BusinessException businessException) {
        if (condition) {
            throw businessException;
        }
    }

    public static void throwException(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }

    public static void throwException(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BusinessException(errorCode, message);
        }
    }
}
