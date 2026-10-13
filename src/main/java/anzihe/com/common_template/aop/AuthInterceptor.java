package anzihe.com.common_template.aop;

import anzihe.com.common_template.annotation.AuthCheck;
import anzihe.com.common_template.exception.BusinessException;
import anzihe.com.common_template.exception.ErrorCode;
import anzihe.com.common_template.model.VO.user.UserVO;
import anzihe.com.common_template.model.enums.UserRoleEnum;
import anzihe.com.common_template.service.UserService;
import anzihe.com.common_template.utils.TokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public  Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token = request.getHeader("token");
        UserVO currentUser = TokenUtil.getCurrentUser(token);
        UserRoleEnum currentRole = UserRoleEnum.getEnumByValue(currentUser.getUserRole());
        UserRoleEnum mustRole  = UserRoleEnum.getEnumByValue(authCheck.mustRole());
        if (mustRole == null){
           return joinPoint.proceed();
        }
        if (!UserRoleEnum.ADMIN.equals(currentRole) && !mustRole.equals(currentRole)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return  joinPoint.proceed();
    }
}
