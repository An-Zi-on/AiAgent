package anzihe.com.agent.ai.controller;

import anzihe.com.agent.ai.service.RagIndexService;
import anzihe.com.agent.common.BaseResponse;
import anzihe.com.agent.common.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识库索引维护接口（context-path 为 /api，实际地址为 /api/rag/**）。
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    @Resource
    private RagIndexService ragIndexService;

    /**
     * 手动触发一次后台建索引：POST /api/rag/reindex
     */
    @PostMapping("/reindex")
    public BaseResponse<Map<String, Object>> reindex() {
        if (ragIndexService.isRunning()) {
            return new BaseResponse<>(409, "索引任务正在执行中，请稍后用 /api/rag/status 查询进度", null);
        }
        ragIndexService.reindexAsync();
        return ResultUtils.success(Map.of(
                "accepted", true,
                "tip", "索引任务已提交到后台，轮询 GET /api/rag/status 查看结果"
        ));
    }

    /**
     * 查询索引状态：GET /api/rag/status
     */
    @GetMapping("/status")
    public BaseResponse<Map<String, Object>> status() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ready", ragIndexService.isReady());
        data.put("running", ragIndexService.isRunning());
        data.put("docCount", ragIndexService.getDocCount());
        data.put("costMs", ragIndexService.getCostMs());
        data.put("lastError", ragIndexService.getLastError());
        data.put("lastFinishedAt", ragIndexService.getLastFinishedAt());
        data.put("vectorFile", ragIndexService.getVectorFile().getAbsolutePath());
        return ResultUtils.success(data);
    }
}
