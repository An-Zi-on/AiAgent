package anzihe.com.agent.controller.system;

import anzihe.com.agent.common.BaseResponse;
import anzihe.com.agent.common.DeleteRequest;
import anzihe.com.agent.common.ResultUtils;
import anzihe.com.agent.model.DTO.usermenu.SysUserMenuDTO;
import anzihe.com.agent.model.VO.usermenu.SysUserMenuVO;
import anzihe.com.agent.model.entity.SysUserMenu;
import anzihe.com.agent.service.SysUserMenuService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/user_menu")
public class UserMenuController {

    @Resource
    private SysUserMenuService userMenuService;

    @PostMapping("/save")
    public BaseResponse<Boolean> save (@RequestBody SysUserMenuDTO userMenuDTO){
        boolean save = userMenuService.save(BeanUtil.toBean(userMenuDTO, SysUserMenu.class));
        return ResultUtils.success(save);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest){
        Long id = deleteRequest.getId();
        boolean remove = userMenuService.removeById(id);
        return ResultUtils.success(remove);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> update(@RequestBody SysUserMenuDTO userMenuDTO){
        boolean update = userMenuService.updateById(BeanUtil.toBean(userMenuDTO,SysUserMenu.class));
        return ResultUtils.success(update);
    }

    @GetMapping("/get")
    public  BaseResponse<SysUserMenuVO> deteli (@RequestParam("id") Long id){
        SysUserMenuVO userMenuVO = BeanUtil.toBean(userMenuService.getById(id), SysUserMenuVO.class);
        return ResultUtils.success(userMenuVO);
    }

}
