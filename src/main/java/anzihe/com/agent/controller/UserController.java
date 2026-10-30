package anzihe.com.agent.controller;

import anzihe.com.agent.annotation.AuthCheck;
import anzihe.com.agent.common.BaseResponse;
import anzihe.com.agent.common.DeleteRequest;
import anzihe.com.agent.common.ResultUtils;
import anzihe.com.agent.common.UserConstant;
import anzihe.com.agent.exception.ErrorCode;
import anzihe.com.agent.utils.ThrowUtils;
import anzihe.com.agent.model.DTO.user.UserAddRequest;
import anzihe.com.agent.model.DTO.user.UserLoginRequest;
import anzihe.com.agent.model.DTO.user.UserQueryRequest;
import anzihe.com.agent.model.DTO.user.UserRegisterRequest;
import anzihe.com.agent.model.DTO.user.UserRoleAssignDTO;
import anzihe.com.agent.model.DTO.user.UserUpdateRequest;
import anzihe.com.agent.model.VO.user.UserVO;
import anzihe.com.agent.model.entity.User;
import anzihe.com.agent.service.UserService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb:2}")
    private int maxSizeMb;

    private static final Set<String> ALLOWED_AVATAR_EXT = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp", "gif"));

    @PostMapping("/register")
    public BaseResponse<?> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        userService.register(userAccount, password, checkPassword);
        return ResultUtils.success("");
    }

    @PostMapping("/login")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        String token = userService.userLogin(userAccount, password, request);
        return ResultUtils.success(token);
    }

    @GetMapping("/current")
    public BaseResponse<UserVO> currentUser(HttpServletRequest request) {
        UserVO loginUserVO = userService.currentUser(request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/loginOut")
    public BaseResponse<?> loginOut(HttpServletRequest request) {
        boolean result = userService.loginOutUser(request);
        return ResultUtils.success(result);
    }

    /**
     * 当前用户更新个人资料（昵称 / 头像 / 简介）
     */
    @PostMapping("/update/my")
    public BaseResponse<?> updateMyProfile(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwException(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        UserVO current = userService.currentUser(request);
        User user = new User();
        user.setId(current.getId());
        if (StrUtil.isNotBlank(userUpdateRequest.getUserName())) {
            user.setUserName(userUpdateRequest.getUserName().trim());
        }
        if (userUpdateRequest.getUserAvatar() != null) {
            user.setUserAvatar(userUpdateRequest.getUserAvatar().trim());
        }
        if (userUpdateRequest.getUserProfile() != null) {
            user.setUserProfile(userUpdateRequest.getUserProfile().trim());
        }
        boolean result = userService.updateById(user);
        ThrowUtils.throwException(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 给用户分配角色（只改 role_id，不碰其他字段）
     *
     * 这里用 LambdaUpdateWrapper 显式 set：updateById 会忽略 null 字段，
     * 那样 roleId 传 null 时就没法解绑角色了。
     */
    @PostMapping("/role/assign")
    public BaseResponse<Boolean> assignRole(@RequestBody UserRoleAssignDTO userRoleAssignDTO) {
        ThrowUtils.throwException(userRoleAssignDTO == null || userRoleAssignDTO.getUserId() == null,
                ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        User existUser = userService.getById(userRoleAssignDTO.getUserId());
        ThrowUtils.throwException(existUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        boolean result = userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, userRoleAssignDTO.getUserId())
                .set(User::getRoleId, userRoleAssignDTO.getRoleId()));
        ThrowUtils.throwException(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 上传头像，返回可访问 URL 路径（相对 /api）
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        userService.currentUser(request);
        ThrowUtils.throwException(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "请选择头像文件");
        long maxBytes = maxSizeMb * 1024L * 1024L;
        ThrowUtils.throwException(file.getSize() > maxBytes, ErrorCode.PARAMS_ERROR, "头像不能超过 " + maxSizeMb + "MB");

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        ThrowUtils.throwException(StrUtil.isBlank(ext) || !ALLOWED_AVATAR_EXT.contains(ext.toLowerCase()),
                ErrorCode.PARAMS_ERROR, "仅支持 jpg/png/webp/gif");

        File dir = new File(uploadDir).getAbsoluteFile();
        if (!dir.exists() && !dir.mkdirs()) {
            ThrowUtils.throwException(true, ErrorCode.SYSTEM_ERROR, "创建上传目录失败");
        }
        String filename = IdUtil.simpleUUID() + "." + ext.toLowerCase();
        File dest = new File(dir, filename);
        file.transferTo(dest);
        String url = "/api/uploads/" + filename;
        return ResultUtils.success(url);
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.USER_ADMIN)
    public BaseResponse<?> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwException(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        user.setUserPassword(userService.getEncryptPassword(UserConstant.DEFAULT_PASSWORD));
        user.setUserRole(UserConstant.USER);
        boolean save = userService.save(user);
        ThrowUtils.throwException(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(save);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.USER_ADMIN)
    public BaseResponse<?> delete(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwException(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        boolean result = userService.removeById(id);
        ThrowUtils.throwException(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<?> update(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwException(userUpdateRequest == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwException(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.USER_ADMIN)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwException(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getUserQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> listUserVO = userService.toListUserVO(userPage.getRecords());
        userVOPage.setRecords(listUserVO);
        return ResultUtils.success(userVOPage);
    }
}
