package anzihe.com.agent.model.VO.role;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色 VO
 */
@Data
public class SysRoleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 鉴权值
     */
    private String roleKey;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}