package anzihe.com.agent.controller.system;

import anzihe.com.agent.annotation.AuthCheck;
import anzihe.com.agent.common.BaseResponse;
import anzihe.com.agent.common.DeleteRequest;
import anzihe.com.agent.common.ResultUtils;
import anzihe.com.agent.common.UserConstant;
import anzihe.com.agent.model.DTO.menu.SysMenuDTO;
import anzihe.com.agent.model.VO.menu.SysMenuTreeVO;
import anzihe.com.agent.model.VO.menu.SysMenuVO;
import anzihe.com.agent.service.SysMenuService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 菜单权限接口
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private SysMenuService menuService;

    /**
     * 新增菜单
     */
    @AuthCheck(mustRole = UserConstant.USER_ADMIN)
    @PostMapping("/save")
    public BaseResponse<Void> save(@RequestBody SysMenuDTO dto) {
        menuService.save(dto);
        return ResultUtils.success(null);
    }

    /**
     * 修改菜单
     */
    @AuthCheck(mustRole = UserConstant.USER_ADMIN)
    @PutMapping("/update")
    public BaseResponse<Void> update(@RequestBody SysMenuDTO dto) {
        menuService.update(dto);
        return ResultUtils.success(null);
    }

    /**
     * 删除菜单
     */
    @AuthCheck(mustRole = UserConstant.USER_ADMIN)
    @DeleteMapping("/delete")
    public BaseResponse<Void> delete(DeleteRequest deleteRequest) {
        menuService.delete(deleteRequest);
        return ResultUtils.success(null);
    }

    /**
     * 根据ID查询菜单详情
     */
    @GetMapping("/get/{id}")
    public BaseResponse<SysMenuVO> getById(@PathVariable("id") Long id) {
        return ResultUtils.success(menuService.getDetailById(id));
    }
    /**
     * 查询菜单树（如果前端需要树形结构）
     */
    @GetMapping("/tree")
    public BaseResponse<List<SysMenuTreeVO>> tree() {
        return ResultUtils.success(menuService.tree());
    }

    @GetMapping("/user_tree")
    public BaseResponse<List<SysMenuTreeVO>> getTreeByUser(@RequestParam("userId") Long  userId){
        return ResultUtils.success(menuService.getUserMenuTree(userId));
    }
}