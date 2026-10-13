package anzihe.com.common_template.model.DTO.usermenu;

import anzihe.com.common_template.common.validation.SaveGroup;
import anzihe.com.common_template.common.validation.UpdateGroup;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 用户菜单权限关联新增/修改 DTO
 */
@Data
public class SysUserMenuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（更新时必填）
     */
    @NotNull(groups = UpdateGroup.class, message = "主键ID不能为空")
    private Long id;

    /**
     * 用户ID
     */
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class}, message = "用户ID不能为空")
    private Integer userId;

    /**
     * 角色ID
     */
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class}, message = "角色ID不能为空")
    private Integer roleId;

    /**
     * 菜单ID
     */
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class}, message = "菜单ID不能为空")
    private Long menuId;
}