package anzihe.com.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户菜单权限关联表
 * @TableName sys_user_menu
 */
@TableName(value ="sys_user_menu")
@Data
public class SysUserMenu {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联account_role.id）
     */
    private Integer userId;

    /**
     * 角色id
     */
    private Integer roleId;

    /**
     * 菜单ID（关联sys_menu.id）
     */
    private Long menuId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新者
     */
    private String updateBy;
}