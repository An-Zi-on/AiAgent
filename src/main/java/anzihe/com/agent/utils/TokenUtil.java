package anzihe.com.agent.utils;

import anzihe.com.agent.exception.BusinessException;
import anzihe.com.agent.exception.ErrorCode;
import anzihe.com.agent.model.VO.user.UserVO;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Date;

@Slf4j
@Component
public class TokenUtil {

    // ========== 配置信息（从配置文件读取） ==========
    @Value("${jwt.secret:template}")
    private String tokenSecret;

    @Value("${jwt.expire-minutes:30}")
    private Integer expireMinutes;

    @Value("${jwt.issuer:template}")
    private String issuer;

    // ========== 静态常量 ==========
    public static final String USER_INFO = "currentUser";

    // ========== 静态工具方法（使用配置） ==========
    private static String staticTokenSecret;
    private static Integer staticExpireMinutes;
    private static String staticIssuer;

    @PostConstruct
    public void init() {
        staticTokenSecret = this.tokenSecret;
        staticExpireMinutes = this.expireMinutes;
        staticIssuer = this.issuer;
    }

    /**
     * 生成 Token
     * @param userVO 用户信息
     * @return JWT Token
     * @throws BusinessException 生成失败时抛出
     */
    public static String sign(UserVO userVO) {
        if (userVO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }

        try {
            String json = JSONUtil.toJsonStr(userVO);
            Date expiresAt = new Date(System.currentTimeMillis() + staticExpireMinutes * 60 * 1000L);

            return JWT.create()
                    .withIssuer(staticIssuer)
                    .withClaim(USER_INFO, json)
                    .withExpiresAt(expiresAt)
                    .sign(Algorithm.HMAC256(staticTokenSecret));

        } catch (Exception e) {
            log.error("生成Token失败，用户ID: {}", userVO.getId(), e);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "生成Token失败");
        }
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return true=有效, false=无效
     */
    public static boolean verify(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(staticTokenSecret))
                    .withIssuer(staticIssuer)
                    .build();
            verifier.verify(token);
            return true;

        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return false;
        }
    }

    /**
     * 从 Token 中获取当前用户信息
     * @param token JWT Token
     * @return 用户信息
     * @throws BusinessException Token 无效时抛出
     */
    public static UserVO getCurrentUser(String token) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Token不能为空");
        }

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(staticTokenSecret))
                    .withIssuer(staticIssuer)
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            String userJson = jwt.getClaim(USER_INFO).asString();

            if (StrUtil.isBlank(userJson)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Token中未包含用户信息");
            }

            return JSONUtil.toBean(userJson, UserVO.class);
        } catch (JWTVerificationException e) {
            log.warn("获取用户信息失败，Token无效: {}", e.getMessage());
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Token无效或已过期");
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解析用户信息失败");
        }
    }

    /**
     * 刷新 Token（延长过期时间）
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    public static String refreshToken(String oldToken) {
        UserVO userVO = getCurrentUser(oldToken);
        return sign(userVO);
    }

    /**
     * 获取 Token 的过期时间
     * @param token JWT Token
     * @return 过期时间
     */
    public static Date getExpiresAt(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt();
        } catch (Exception e) {
            log.warn("获取过期时间失败: {}", e.getMessage());
            return null;
        }
    }

}