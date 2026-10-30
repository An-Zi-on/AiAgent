package anzihe.com.agent.config;

import anzihe.com.agent.aop.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 资源访问权限
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    /**
     * 跨域配置
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Token");
    }

    /**
     * 拦截器注册
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册拦截器
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                //那些地址不需要拦截
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**"
                );
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
