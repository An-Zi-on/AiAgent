package anzihe.com.agent.service;

import anzihe.com.agent.model.entity.SysRoleMenu;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
 * 针对表【sys_role_menu(角色菜单关联表)】的数据库操作Service
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 查询某角色已授权的菜单 id 列表
     */
    List<Long> listMenuIdsByRoleId(Long roleId);

    /**
     * 覆盖式分配：先清空该角色的全部授权，再按 menuIds 重新写入
     *
     * @param menuIds 允许为空集合（表示清空该角色权限）
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
}
