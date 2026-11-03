package anzihe.com.agent.service.impl;

import anzihe.com.agent.exception.BusinessException;
import anzihe.com.agent.exception.ErrorCode;
import anzihe.com.agent.mapper.SysRoleMenuMapper;
import anzihe.com.agent.model.entity.SysRoleMenu;
import anzihe.com.agent.service.SysRoleMenuService;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对表【sys_role_menu(角色菜单关联表)】的数据库操作Service实现
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu>
        implements SysRoleMenuService {

    @Override
    public List<Long> listMenuIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return this.list(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        if (roleId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色ID不能为空");
        }
        this.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));

        if (CollectionUtil.isEmpty(menuIds)) {
            return true;
        }
        for (Long menuId : new LinkedHashSet<>(menuIds)) {
            if (menuId == null) {
                continue;
            }
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            this.save(roleMenu);
        }
        return true;
    }
}
