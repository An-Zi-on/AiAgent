package anzihe.com.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 角色菜单权限关联表
 * @TableName sys_role_menu
 */
@TableName(value = "sys_role_menu")
@Data
public class SysRoleMenu {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID（关联sys_role.id）
     */
    private Long roleId;

    /**
     * 菜单ID（关联sys_menu.id）
     */
    private Long menuId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
