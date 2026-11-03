package anzihe.com.agent.mapper;

import anzihe.com.agent.model.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【sys_role_menu(角色菜单关联表)】的数据库操作Mapper
 *
 * @Entity anzihe.com.common_template.model.entity.SysRoleMenu
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

}
