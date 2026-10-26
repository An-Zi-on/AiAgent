package anzihe.com.agent.service;

import anzihe.com.agent.model.DTO.user.UserQueryRequest;
import anzihe.com.agent.model.VO.user.LoginUserVO;
import anzihe.com.agent.model.VO.user.UserVO;
import anzihe.com.agent.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.spring.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService extends IService<User> {

    void register(String userAccount, String password, String checkPassword);

    String userLogin(String userAccount, String password, HttpServletRequest request);

    UserVO currentUser(HttpServletRequest request);

    boolean loginOutUser(HttpServletRequest request);

    String getEncryptPassword(String text);

    LoginUserVO toLoginUserVO(User user);

    UserVO toUserVO(User user);

    List<UserVO> toListUserVO(List<User> userList);

    LambdaQueryWrapper<User> getUserQueryWrapper(UserQueryRequest queryRequest);

    boolean isAdmin(LoginUserVO userVO);
}
