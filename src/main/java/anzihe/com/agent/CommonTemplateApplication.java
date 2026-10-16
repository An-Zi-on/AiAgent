package anzihe.com.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class CommonTemplateApplication {

    public static void main(String[] args) {
        printBuddha();
        SpringApplication.run(CommonTemplateApplication.class, args);
    }

    private static void printBuddha() {
        System.out.println("""
                  _ooOoo_
                 o8888888o
                 88" . "88
                 (| -_- |)
                 O\\  =  /O
              ____/`---'\\____
            .'  \\\\|     |//  `.
           /  \\\\|||  :  |||//  \\
          /  _||||| -:- |||||-  \\
          |   | \\\\\\  -  /// |   |
          | \\_|  ''\\---/''  |   |
          \\  .-\\__  `-`  ___/-. /
        ___`. .'  /--.--\\  `. . __
     ."" '<  `.___\\_<|>_/___.'  >'"". 
    | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |
    \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /
======`-.____`-.___\\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
              佛祖保佑    永无BUG
""");
    }

}
