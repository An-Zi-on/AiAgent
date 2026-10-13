package anzihe.com.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class CommonTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonTemplateApplication.class, args);
    }

}
