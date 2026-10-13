package anzihe.com.common_template.service.impl;

import anzihe.com.common_template.common.DeleteRequest;
import anzihe.com.common_template.exception.BusinessException;
import anzihe.com.common_template.exception.ErrorCode;
import anzihe.com.common_template.model.DTO.menu.SysMenuDTO;
import anzihe.com.common_template.model.VO.menu.SysMenuTreeVO;
import anzihe.com.common_template.model.VO.menu.SysMenuVO;
import anzihe.com.common_template.model.entity.SysMenu;
import anzihe.com.common_template.model.entity.SysUserMenu;
import anzihe.com.common_template.model.entity.User;
import anzihe.com.common_template.service.SysMenuService;
import anzihe.com.common_template.mapper.SysMenuMapper;
import anzihe.com.common_template.mapper.UserMapper;
import anzihe.com.common_template.service.SysRoleMenuService;
import anzihe.com.common_template.service.SysUserMenuService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author qq529
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service实现
* @createDate 2026-09-22 11:06:39
*/
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
    implements SysMenuService{

    @Resource
    private SysUserMenuService userMenuService;

    @Resource
    private SysRoleMenuService roleMenuService;

    @Resource
    private UserMapper userMapper;

    @Override
    public void save(SysMenuDTO dto) {
        SysMenu bean = BeanUtil.toBean(dto, SysMenu.class);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        boolean save = this.save(bean);
        if (!save){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public void update(SysMenuDTO dto) {
        SysMenu exist = this.getById(dto.getId());
        if (BeanUtil.isEmpty(exist)){
            throw  new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        SysMenu bean = BeanUtil.toBean(dto, SysMenu.class);
        bean.setUpdateTime(new Date());
        boolean b = this.updateById(bean);
        if (!b){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public void delete(DeleteRequest id) {
        SysMenu exist = this.getById(id.getId());
        if (BeanUtil.isEmpty(exist)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        boolean b = this.removeById(exist);
        if (!b){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    @Override
    public SysMenuVO getDetailById(Long id) {
        if (id == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SysMenu exist = this.getById(id);
        if (BeanUtil.isEmpty(exist)){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return BeanUtil.toBean(exist, SysMenuVO.class);
    }

    @Override
    public List<SysMenuTreeVO> tree() {
        // 1. 查询所有菜单
        List<SysMenu> list = this.list(
                new LambdaQueryWrapper<SysMenu>()
                    .orderByAsc(SysMenu::getRank));
        return getSysMenuTreeVOS(list);
    }

    private List<SysMenuTreeVO> findSubMenuTree(SysMenuTreeVO menuTreeVO, List<SysMenu> list) {
        List<SysMenuTreeVO> menuTreeVOList = new ArrayList<>();
        for (SysMenu item : list){
            if (item.getParentId().equals(menuTreeVO.getId())){
                SysMenuTreeVO itemChildTreeVO = BeanUtil.toBean(item, SysMenuTreeVO.class);
                menuTreeVOList.add(itemChildTreeVO);
                itemChildTreeVO.setChildren(findSubMenuTree(itemChildTreeVO, list));
            }
        }
        return menuTreeVOList;
    }


    @Override
    public List<SysMenuTreeVO> getUserMenuTree(Long userId) {
        Set<Long> menuIdSet = new LinkedHashSet<>();
        User user = userMapper.selectById(userId);
        if (user != null && user.getRoleId() != null) {
            menuIdSet.addAll(roleMenuService.listMenuIdsByRoleId(user.getRoleId().longValue()));
        }
        userMenuService.list(new LambdaQueryWrapper<SysUserMenu>()
                        .eq(SysUserMenu::getUserId, userId))
                .forEach(item -> menuIdSet.add(item.getMenuId()));

        if (menuIdSet.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> menuIds = new ArrayList<>(menuIdSet);
        List<SysMenu> list = this.listByIds(menuIds);
        return getSysMenuTreeVOS(list);
    }

    @NotNull
    private List<SysMenuTreeVO> getSysMenuTreeVOS(List<SysMenu> list) {
        List<SysMenuTreeVO> menuTreeVOList = new ArrayList<>();
        list.forEach(item  ->{
            //查找顶级菜单
            if (item.getParentId() == 0){
                SysMenuTreeVO menuTreeVO = BeanUtil.toBean(item, SysMenuTreeVO.class);
                //寻找对应的子菜单
                List<SysMenuTreeVO> subMenuTree = findSubMenuTree(menuTreeVO, list);
                menuTreeVO.setChildren(subMenuTree);
                menuTreeVOList.add(menuTreeVO);
            }
        });
        return menuTreeVOList;
    }
}




