package anzihe.com.agent.model.DTO.role;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 给角色分配菜单权限的请求
 */
@Data
public class SysRoleMenuAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID集合（全量覆盖；传空集合表示清空该角色权限）
     *
     * 注意：调用方需要把半选的父节点 id 也一并传进来，
     * 否则父级菜单不会被授权，侧边栏里子菜单会因为找不到父节点而整棵消失。
     */
    private List<Long> menuIds;
}
