package anzihe.com.agent.service;

import anzihe.com.agent.common.DeleteRequest;
import anzihe.com.agent.model.DTO.menu.SysMenuDTO;
import anzihe.com.agent.model.VO.menu.SysMenuTreeVO;
import anzihe.com.agent.model.VO.menu.SysMenuVO;
import anzihe.com.agent.model.entity.SysMenu;
import com.baomidou.mybatisplus.spring.service.IService;

import java.util.List;

/**
* @author qq529
* @description 针对表【sys_menu(菜单权限表)】的数据库操作Service
* @createDate 2026-09-22 11:06:39
*/
public interface SysMenuService extends IService<SysMenu> {

    void save(SysMenuDTO dto);

    void update(SysMenuDTO dto);

    void delete(DeleteRequest id);

    SysMenuVO getDetailById(Long id);

    List<SysMenuTreeVO> tree();

    List<SysMenuTreeVO> getUserMenuTree(Long userId);
}
