package anzihe.com.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName(value = "user")
@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userAccount;

    private String userPassword;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    /** 角色ID（关联 sys_role.id）：用户绑角色，间接获得该角色的菜单权限 */
    private Integer roleId;

    private LocalDateTime editTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;
}
