package anzihe.com.agent.service.impl;

import anzihe.com.agent.exception.BusinessException;
import anzihe.com.agent.exception.ErrorCode;
import anzihe.com.agent.model.DTO.role.SysRoleDTO;
import anzihe.com.agent.model.entity.SysRole;
import anzihe.com.agent.service.SysRoleService;
import anzihe.com.agent.mapper.SysRoleMapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author qq529
* @description 针对表【sys_role(角色表)】的数据库操作Service实现
* @createDate 2026-09-22 11:06:39
*/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
    implements SysRoleService{

    @Override
    public  Page<SysRoleDTO> getlist(SysRoleDTO roleDTO) {
        SysRole role = BeanUtil.toBean(roleDTO, SysRole.class);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Page<SysRole> page = new Page<>(roleDTO.getCurrent(), roleDTO.getPageSize());
        Page<SysRole> rolePage = this.page(page, new LambdaQueryWrapper<SysRole>()
                .like(StrUtil.isNotBlank(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
                .like(StrUtil.isNotBlank(role.getRoleKey()), SysRole::getRoleKey, role.getRoleKey())
                .like(StrUtil.isNotBlank(role.getRemark()), SysRole::getRemark, role.getRemark())
                // sys_role 的软删列叫 deleted，而全局逻辑删除配置是 isDelete，不会自动过滤
                .eq(SysRole::getDeleted, 0)
        );
        long current = rolePage.getCurrent();
        long size = rolePage.getSize();
        long total = rolePage.getTotal();
        Page<SysRoleDTO> pageResult = new Page<>();
        pageResult.setCurrent(current);
        pageResult.setSize(size);
        pageResult.setTotal(total);
        List<SysRoleDTO> roleDTOList = new ArrayList<>();
        rolePage.getRecords().forEach(item->
                        roleDTOList.add(BeanUtil.toBean(item,SysRoleDTO.class)));
        pageResult.setRecords(roleDTOList);
        return  pageResult;
    }
}




