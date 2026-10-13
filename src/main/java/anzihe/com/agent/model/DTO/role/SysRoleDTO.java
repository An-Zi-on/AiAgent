package anzihe.com.agent.model.DTO.role;

import anzihe.com.agent.common.PageRequest;
import anzihe.com.agent.common.validation.SaveGroup;
import anzihe.com.agent.common.validation.UpdateGroup;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 角色新增/修改 DTO
 */
@Data
public class SysRoleDTO extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（更新时必填）
     */
    @NotNull(groups = UpdateGroup.class, message = "角色ID不能为空")
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class}, message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过64")
    private String roleName;

    /**
     * 鉴权值
     */
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class}, message = "鉴权值不能为空")
    @Size(max = 100, message = "鉴权值长度不能超过100")
    @Pattern(regexp = "^[A-Za-z0-9_:]+$", message = "鉴权值只能包含字母、数字、下划线和冒号")
    private String roleKey;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}