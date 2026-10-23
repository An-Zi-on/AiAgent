package anzihe.com.agent.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    /**
     * 返回当前工作路径
     * @return
     */
    public static String getCurrentBasePath() {
        return Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize()
                .toString();
    }
}