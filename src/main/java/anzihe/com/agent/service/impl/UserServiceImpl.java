package anzihe.com.agent.service.impl;

import anzihe.com.agent.common.UserConstant;
import anzihe.com.agent.exception.ErrorCode;
import anzihe.com.agent.utils.ThrowUtils;
import anzihe.com.agent.mapper.UserMapper;
import anzihe.com.agent.model.DTO.user.UserQueryRequest;
import anzihe.com.agent.model.VO.user.LoginUserVO;
import anzihe.com.agent.model.VO.user.UserVO;
import anzihe.com.agent.model.entity.User;
import anzihe.com.agent.model.enums.UserRoleEnum;
import anzihe.com.agent.service.UserService;
import anzihe.com.agent.utils.TokenUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 允许前端排序的字段白名单：key 为前端字段名，value 为实体字段。
     * 不在白名单内的字段一律使用默认排序，避免直接拼接前端字符串造成非法列名或注入风险。
     */
    private static final Map<String, SFunction<User, ?>> SORT_COLUMNS = Map.of(
            "id", User::getId,
            "userAccount", User::getUserAccount,
            "userName", User::getUserName,
            "userRole", User::getUserRole,
            "createTime", User::getCreateTime,
            "updateTime", User::getUpdateTime
    );

    @Override
    public void register(String userAccount, String password, String checkPassword) {
        ThrowUtils.throwException(userAccount.length() < 8, ErrorCode.PARAMS_ERROR, "账号长度大于8位");
        ThrowUtils.throwException(password.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码长度大于8位");
        ThrowUtils.throwException(!password.equals(checkPassword), ErrorCode.PARAMS_ERROR, "输入密码不一致");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount,userAccount);
        long count = this.count(queryWrapper);
        ThrowUtils.throwException(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");
        String encryptPassword = getEncryptPassword(password);
        User updateUser = new User();
        updateUser.setUserAccount(userAccount);
        updateUser.setUserPassword(encryptPassword);
        updateUser.setUserName("无名");
        updateUser.setUserRole(UserRoleEnum.USER.getValue());
        long result = userMapper.insert(updateUser);
        ThrowUtils.throwException(result <= 0, ErrorCode.SYSTEM_ERROR);
    }

    @Override
    public String userLogin(String userAccount, String password, HttpServletRequest request) {
        ThrowUtils.throwException(userAccount.length() < 8 || StrUtil.isEmptyIfStr(userAccount), ErrorCode.PARAMS_ERROR);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User selectUser = userMapper.selectOne(queryWrapper);
        ThrowUtils.throwException(selectUser == null, ErrorCode.PARAMS_ERROR, "用户不存在");
        String userPassword = selectUser.getUserPassword();
        ThrowUtils.throwException(!userPassword.equals(getEncryptPassword(password)), ErrorCode.PARAMS_ERROR, "密码错误");
        UserVO loginUserVO = toUserVO(selectUser);
        return TokenUtil.sign(loginUserVO);
    }

    @Override
    public UserVO currentUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        ThrowUtils.throwException(!TokenUtil.verify(token), ErrorCode.NOT_LOGIN_ERROR);
        UserVO currentUser = TokenUtil.getCurrentUser(token);
        ThrowUtils.throwException(currentUser == null, ErrorCode.NOT_LOGIN_ERROR);
        User selectUser = userMapper.selectById(currentUser.getId());
        UserVO loginUserVO = toUserVO(selectUser);
        // 刷新 token，并通过响应头回传给前端
        String newToken = TokenUtil.refreshToken(token);
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getResponse() != null) {
            attributes.getResponse().setHeader("token", newToken);
        }
        return loginUserVO;
    }

    @Override
    public boolean loginOutUser(HttpServletRequest request) {
        return true;
    }

    @Override
    public String getEncryptPassword(String text) {
        return SecureUtil.md5(text + UserConstant.USER_PASSWORD_SALT);
    }

    @Override
    public LoginUserVO toLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO bean = BeanUtil.toBean(user, LoginUserVO.class);
        bean.setCreateTime(LocalDateTimeUtil.of(user.getCreateTime()));
        bean.setUpdateTime(LocalDateTimeUtil.of(user.getUpdateTime()));
        return bean;
    }

    @Override
    public UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = BeanUtil.toBean(user, UserVO.class);
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(java.util.Date.from(user.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        if (user.getUpdateTime() != null) {
            userVO.setUpdateTime(java.util.Date.from(user.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return userVO;
    }

    @Override
    public List<UserVO> toListUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::toUserVO).collect(Collectors.toList());
    }

    @Override
    public  LambdaQueryWrapper<User> getUserQueryWrapper(UserQueryRequest queryRequest) {
        ThrowUtils.throwException(queryRequest == null, ErrorCode.OPERATION_ERROR, "查询条件为空");
        Long id = queryRequest.getId();
        String userName = queryRequest.getUserName();
        String userAccount = queryRequest.getUserAccount();
        String userProfile = queryRequest.getUserProfile();
        String userRole = queryRequest.getUserRole();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(id), User::getId, id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), User::getUserRole, userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), User::getUserName, userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), User::getUserProfile, userProfile);
        SFunction<User, ?> sortColumn = StrUtil.isBlank(sortField) ? null : SORT_COLUMNS.get(sortField);
        if (sortColumn == null) {
            queryWrapper.orderByDesc(User::getCreateTime);
        } else {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortColumn);
        }
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(LoginUserVO userVO) {
        return userVO != null && UserRoleEnum.ADMIN.equals(UserRoleEnum.getEnumByValue(userVO.getUserRole()));
    }
}
