package anzihe.com.agent.common;

public interface UserConstant {

    String USER_PASSWORD_SALT = "common_template";

    String USER_LOGIN = "loginUser";

    String USER = "user";

    String USER_ADMIN = "admin";

    String DEFAULT_PASSWORD = "12345678";

    /** 账号最小长度：种子管理员 admin 为 5 位，不能再用 8 */
    int ACCOUNT_MIN_LENGTH = 4;

    /** 账号最大长度 */
    int ACCOUNT_MAX_LENGTH = 32;

    /** 密码最小长度 */
    int PASSWORD_MIN_LENGTH = 8;

    /** 密码最大长度 */
    int PASSWORD_MAX_LENGTH = 64;
}
