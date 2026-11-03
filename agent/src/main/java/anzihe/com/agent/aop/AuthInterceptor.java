package anzihe.com.agent.aop;

import anzihe.com.agent.annotation.AuthCheck;
import anzihe.com.agent.exception.BusinessException;
import anzihe.com.agent.exception.ErrorCode;
import anzihe.com.agent.model.VO.user.UserVO;
import anzihe.com.agent.model.enums.UserRoleEnum;
import anzihe.com.agent.service.UserService;
import anzihe.com.agent.utils.TokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class AuthInterceptor {


    @Around("@annotation(authCheck)")
    public  Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String token = request.getHeader("token");
        UserVO currentUser = TokenUtil.getCurrentUser(token);
        UserRoleEnum currentRole = UserRoleEnum.getEnumByValue(currentUser.getUserRole());
        UserRoleEnum mustRole  = UserRoleEnum.getEnumByValue(authCheck.mustRole());
        if (mustRole == null){
            log.error("检查该权限是否存在{}",authCheck.mustRole());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (!UserRoleEnum.ADMIN.equals(currentRole) && !mustRole.equals(currentRole)){
            log.error("需要该权限");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return  joinPoint.proceed();
    }
}
