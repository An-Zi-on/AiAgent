package anzihe.com.agent.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

@Getter
public enum UserRoleEnum {

    USER("user", "user"),
    ADMIN("admin", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static UserRoleEnum getEnumByText(String text) {
        if (StrUtil.isEmptyIfStr(text)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.text.equals(text)) {
                return userRoleEnum;
            }
        }
        return null;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
