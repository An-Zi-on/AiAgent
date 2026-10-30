package anzihe.com.agent.common;

import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class PageRequest {
    private int current = 1;

    @Max(value = 200, message = "每页条数不能超过 200")
    private int pageSize = 10;

    private String sortField;

    private String sortOrder = "descend";
}
