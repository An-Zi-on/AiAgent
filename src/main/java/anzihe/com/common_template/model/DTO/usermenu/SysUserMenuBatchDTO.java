package anzihe.com.common_template.model.DTO.usermenu;

import anzihe.com.common_template.common.validation.SaveGroup;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 批量分配菜单 DTO
 */
@Data
public class SysUserMenuBatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(groups = SaveGroup.class, message = "用户ID不能为空")
    private Integer userId;

    /**
     * 角色ID
     */
    @NotNull(groups = SaveGroup.class, message = "角色ID不能为空")
    private Integer roleId;

    /**
     * 菜单ID列表（全量覆盖）
     */
    @NotEmpty(groups = SaveGroup.class, message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}