package anzihe.com.common_template.controller.system;

import anzihe.com.common_template.common.BaseResponse;
import anzihe.com.common_template.common.DeleteRequest;
import anzihe.com.common_template.common.ResultUtils;
import anzihe.com.common_template.model.DTO.usermenu.SysUserMenuDTO;
import anzihe.com.common_template.model.VO.usermenu.SysUserMenuVO;
import anzihe.com.common_template.model.entity.SysUserMenu;
import anzihe.com.common_template.service.SysUserMenuService;
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
    public  BaseResponse<SysUserMenuVO> deteli (@RequestParam Long id){
        SysUserMenuVO userMenuVO = BeanUtil.toBean(userMenuService.getById(id), SysUserMenuVO.class);
        return ResultUtils.success(userMenuVO);
    }

}
