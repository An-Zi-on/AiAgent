package anzihe.com.agent.model.DTO.menu;

import anzihe.com.agent.common.validation.SaveGroup;
import anzihe.com.agent.common.validation.UpdateGroup;
import lombok.Data;


import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * 菜单权限新增/修改 DTO
 */
@Data
public class SysMenuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID（更新时必填）
     */
    @NotNull(groups = UpdateGroup.class, message = "菜单ID不能为空")
    private Long id;

    /**
     * 重定向路径
     */
    @Size(max = 255, message = "重定向路径长度不能超过255")
    private String redirect;

    /**
     * 父菜单ID（0表示根节点）
     */
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class}, message = "父菜单ID不能为空")
    @Min(value = 0, message = "父菜单ID不能小于0")
    private Long parentId;

    /**
     * 路由路径
     */
    @Size(max = 255, message = "路由路径长度不能超过255")
    private String path;

    /**
     * 组件名称
     */
    @Size(max = 64, message = "组件名称长度不能超过64")
    private String name;

    /**
     * 组件路径（动态导入）
     */
    @Size(max = 255, message = "组件路径长度不能超过255")
    private String component;

    /**
     * 菜单名称（支持国际化）
     */
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class}, message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过64")
    private String title;

    /**
     * 菜单图标
     */
    @Size(max = 255, message = "菜单图标长度不能超过255")
    private String icon;

    /**
     * 菜单排序
     */
    @Min(value = 0, message = "菜单排序不能小于0")
    @Max(value = 9999, message = "菜单排序不能大于9999")
    private Integer rank;

    /**
     * 菜单类型：0=菜单 1=iframe 2=外链 3=按钮
     */
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class}, message = "菜单类型不能为空")
    @Min(value = 0, message = "菜单类型不合法")
    @Max(value = 3, message = "菜单类型不合法")
    private Integer menuType;

    /**
     * 启用状态：0=禁用 1=启用
     */
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class}, message = "启用状态不能为空")
    @Min(value = 0, message = "启用状态只能为0或1")
    @Max(value = 1, message = "启用状态只能为0或1")
    private Integer status;

    /**
     * 右侧额外图标
     */
    @Size(max = 255, message = "右侧额外图标长度不能超过255")
    private String extraIcon;

    /**
     * 菜单激活路径
     */
    @Size(max = 255, message = "菜单激活路径长度不能超过255")
    private String activePath;

    /**
     * 权限标识（按钮级别）
     */
    @Size(max = 255, message = "权限标识长度不能超过255")
    private String auths;

    /**
     * iframe/外链链接地址
     */
    @Size(max = 512, message = "链接地址长度不能超过512")
    private String frameSrc;

    /**
     * iframe页面首次加载动画：0=否 1=是
     */
    @Min(value = 0, message = "iframe加载动画只能为0或1")
    @Max(value = 1, message = "iframe加载动画只能为0或1")
    private Integer frameLoading;

    /**
     * 缓存页面（keep-alive）：0=否 1=是
     */
    @Min(value = 0, message = "缓存页面只能为0或1")
    @Max(value = 1, message = "缓存页面只能为0或1")
    private Integer keepAlive;

    /**
     * 是否禁止添加到标签页：0=否 1=是
     */
    @Min(value = 0, message = "hiddenTag只能为0或1")
    @Max(value = 1, message = "hiddenTag只能为0或1")
    private Integer hiddenTag;

    /**
     * 是否固定显示且不可关闭：0=否 1=是
     */
    @Min(value = 0, message = "fixedTag只能为0或1")
    @Max(value = 1, message = "fixedTag只能为0或1")
    private Integer fixedTag;

    /**
     * 是否显示该菜单：0=否 1=是
     */
    @Min(value = 0, message = "showLink只能为0或1")
    @Max(value = 1, message = "showLink只能为0或1")
    private Integer showLink;

    /**
     * 是否显示父级菜单：0=否 1=是
     */
    @Min(value = 0, message = "showParent只能为0或1")
    @Max(value = 1, message = "showParent只能为0或1")
    private Integer showParent;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
}