package anzihe.com.agent.model.constant;

import java.io.File;

public interface FileConstant {
    String FILE_PATH = System.getProperty("user.dir") +File.separatorChar +"tmp";

    Integer FILE_MAX_SIZE = 5 * 1024 * 1024;
}
