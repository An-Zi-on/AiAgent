package anzihe.com.agent.model.DTO.menu;

import lombok.Data;

import java.io.Serializable;

/**
 * 菜单权限查询 DTO
 */
@Data
public class SysMenuQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单名称（模糊查询）
     */
    private String title;

    /**
     * 启用状态：0=禁用 1=启用
     */
    private Integer status;

    /**
     * 菜单类型：0=菜单 1=iframe 2=外链 3=按钮
     */
    private Integer menuType;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 页码，默认 1
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认 10
     */
    private Integer pageSize = 10;
}