package anzihe.com.agent.model.DTO.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 给用户分配角色的请求
 */
@Data
public class UserRoleAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID（传 null 表示解除角色绑定）
     */
    private Integer roleId;
}
