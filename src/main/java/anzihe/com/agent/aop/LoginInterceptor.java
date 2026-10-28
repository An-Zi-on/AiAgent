package anzihe.com.agent.aop;

import anzihe.com.agent.utils.TokenUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 全局登入拦截
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 执行执行方法前
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            log.info(request.getMethod());
            return true;
        }
        String token = request.getHeader("token");
        if(StrUtil.isBlank(token)|| !TokenUtil.verify(token)){
            log.error("用户未登入,访问当前路径为{}",request.getPathInfo());
            //未登入重定向到登入页面
            response.sendRedirect(request.getContextPath()+"/login.html");
            return false;
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 方法执行后,视图渲染前,可修改 ModelAndView
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     * (can also be {@code null})
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 请求完全结束后（视图渲染完）：可清理资源、记日志
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param ex any exception thrown on handler execution, if any; this does not
     * include exceptions that have been handled through an exception resolver
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}
