package anzihe.com.agent.model.VO.menu;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 菜单权限 VO
 */
@Data
public class SysMenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 重定向路径
     */
    private String redirect;

    /**
     * 父菜单ID（0表示根节点）
     */
    private Long parentId;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件路径（动态导入）
     */
    private String component;

    /**
     * 菜单名称（支持国际化）
     */
    private String title;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单排序
     */
    private Integer rank;

    /**
     * 菜单类型：0=菜单 1=iframe 2=外链 3=按钮
     */
    private Integer menuType;

    /**
     * 启用状态：0=禁用 1=启用
     */
    private Integer status;

    /**
     * 右侧额外图标
     */
    private String extraIcon;

    /**
     * 菜单激活路径（指定激活菜单的path）
     */
    private String activePath;

    /**
     * 权限标识（按钮级别）
     */
    private String auths;

    /**
     * iframe/外链链接地址
     */
    private String frameSrc;

    /**
     * iframe页面首次加载动画：0=否 1=是
     */
    private Integer frameLoading;

    /**
     * 缓存页面（keep-alive）：0=否 1=是
     */
    private Integer keepAlive;

    /**
     * 是否禁止添加到标签页：0=否 1=是
     */
    private Integer hiddenTag;

    /**
     * 是否固定显示且不可关闭：0=否 1=是
     */
    private Integer fixedTag;

    /**
     * 是否显示该菜单：0=否 1=是
     */
    private Integer showLink;

    /**
     * 是否显示父级菜单：0=否 1=是
     */
    private Integer showParent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;


}