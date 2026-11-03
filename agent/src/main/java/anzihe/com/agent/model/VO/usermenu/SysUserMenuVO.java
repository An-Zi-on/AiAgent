package anzihe.com.agent.model.VO.usermenu;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户菜单权限关联 VO
 */
@Data
public class SysUserMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 角色ID
     */
    private Integer roleId;

    /**
     * 菜单ID
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