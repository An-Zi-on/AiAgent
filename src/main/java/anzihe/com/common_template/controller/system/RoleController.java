package anzihe.com.common_template.controller.system;

import anzihe.com.common_template.common.BaseResponse;
import anzihe.com.common_template.common.DeleteRequest;
import anzihe.com.common_template.common.ResultUtils;
import anzihe.com.common_template.model.DTO.role.SysRoleDTO;
import anzihe.com.common_template.model.DTO.role.SysRoleMenuAssignDTO;
import anzihe.com.common_template.model.VO.role.SysRoleVO;
import anzihe.com.common_template.model.entity.SysRole;
import anzihe.com.common_template.service.SysRoleMenuService;
import anzihe.com.common_template.service.SysRoleService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysRoleMenuService sysRoleMenuService;

    @PostMapping("/save")
    public BaseResponse save( @RequestBody SysRoleDTO roleDTO){

        return ResultUtils.success(sysRoleService.save(BeanUtil.toBean(roleDTO, SysRole.class)));
    }

    @PostMapping("/delete")
    public BaseResponse save( @RequestBody DeleteRequest deleteRequest){
        return ResultUtils.success(sysRoleService.removeById(deleteRequest.getId()));
    }

    @PostMapping("/update")
    public BaseResponse update( @RequestBody SysRoleDTO roleDTO){
        return ResultUtils.success(sysRoleService.updateById(BeanUtil.toBean(roleDTO, SysRole.class)));
    }

    @GetMapping("/get/{id}")
    public BaseResponse<SysRoleVO> detail( @PathVariable Long id ){
        return ResultUtils.success(BeanUtil.toBean(sysRoleService.getById(id), SysRoleVO.class));
    }

    @PostMapping("/page")
    public BaseResponse<Page<SysRoleDTO>> getList(SysRoleDTO roleDTO) {
        return ResultUtils.success(sysRoleService.getlist(roleDTO));
    }

    /**
     * 查询某角色已授权的菜单 id 列表（分配权限弹窗回显用）
     */
    @GetMapping("/menu/list")
    public BaseResponse<List<Long>> roleMenuIds(@RequestParam Long roleId) {
        return ResultUtils.success(sysRoleMenuService.listMenuIdsByRoleId(roleId));
    }

    /**
     * 覆盖式给角色分配菜单权限（menuIds 传空集合表示清空该角色权限）
     */
    @PostMapping("/menu/assign")
    public BaseResponse<Boolean> assignRoleMenus(@RequestBody SysRoleMenuAssignDTO assignDTO) {
        return ResultUtils.success(
                sysRoleMenuService.assignMenus(assignDTO.getRoleId(), assignDTO.getMenuIds()));
    }

}
