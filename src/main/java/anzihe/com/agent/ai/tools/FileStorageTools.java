package anzihe.com.agent.ai.tools;

import anzihe.com.agent.model.constant.FileConstant;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static anzihe.com.agent.model.constant.FileConstant.FILE_MAX_SIZE;

/**
 * 文件读取 写入工具
 */
@Slf4j
@Component
public class FileStorageTools {
    private static final long MAX_READ_BYTES = 1024 * 1024;
    @Tool(name = "read-file",
            description = "Read the text content of a file stored in the workspace. " +
                    "Use this when the user asks to read, summarize, or answer questions about a file. " +
                    "Returns the UTF-8 text content of the file.")
    public String readFile(
            @ToolParam(required = true, description = "The file name or relative path inside the workspace, e.g. 'notes.txt' or 'docs/a.md'")
            String content) {

        String filePath = buildPath(content);
        File file = new File(filePath);
        if (!FileUtil.exist(file) || !FileUtil.isFile(file)) {
            return "File not found: " + content;
        }
        if (file.length()> MAX_READ_BYTES){
            return  "The file size exceeds the 1MB limit ";
        }
        String text = FileUtil.readUtf8String(filePath);
        //可在做一层上下文本大小限制
        return text;
    }

    @Tool(name = "create-file",
            description = "Create a new text file in the workspace with the given content. " +
                    "Use this when the user asks to save or write text into a file. " +
                    "If the file already exists it will be overwritten.")
    public String createFile(
            @ToolParam(required = true, description = "The file name or relative path inside the workspace, e.g. 'notes.txt'")
            String fileName,
            @ToolParam(required = true, description = "The text content to write into the file")
            String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        if (bytes.length>FILE_MAX_SIZE){
            return "File content too large: " + bytes.length + " bytes, max allowed is " + FILE_MAX_SIZE;
        }
        if (fileName.length()>255){
            return "File name too long: " + bytes.length + " bytes, max allowed is 255";
        }
        String filePath = buildPath(fileName);
        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists()) {
            FileUtil.mkdir(parent);
        }
        FileUtil.writeUtf8String(content, filePath);
        return "File created: " + fileName;
    }


    /** 统一拼路径 + 防止路径穿越 */
    private String buildPath(String relativeName) {
        File base = new File(FileConstant.FILE_PATH);
        File target = new File(base, relativeName);
        try {
            String basePath = base.getCanonicalPath();
            String targetPath = target.getCanonicalPath();
            if (!targetPath.startsWith(basePath + File.separator) && !targetPath.equals(basePath)) {
                throw new IllegalArgumentException("Illegal file path: " + relativeName);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to resolve file path", e);
        }
        log.info(target.getAbsolutePath());
        return target.getPath();
    }
}