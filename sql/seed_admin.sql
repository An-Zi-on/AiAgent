-- ----------------------------
-- 种子管理员（开发 / 演示环境）
-- 账号：admin
-- 密码：12345678（与 UserConstant.DEFAULT_PASSWORD 一致）
-- 说明：
--   1. 密码哈希 = MD5(密码 + UserConstant.USER_PASSWORD_SALT)
--   2. 首次登录后请立即修改密码；生产环境禁止使用默认密码
--   3. 脚本可重复执行，目标库为 an_ai
-- ----------------------------

-- 1. 角色：菜单权限依赖 role_id，先保证 admin / user 两个角色存在
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `remark`, `deleted`)
VALUES (1, '超级管理员', 'admin', '拥有全部菜单', 0)
ON DUPLICATE KEY UPDATE
    `role_name` = '超级管理员',
    `role_key` = 'admin',
    `remark` = '拥有全部菜单',
    `deleted` = 0;

INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `remark`, `deleted`)
VALUES (2, '普通用户', 'user', '只读，仅首页', 0)
ON DUPLICATE KEY UPDATE
    `role_name` = '普通用户',
    `role_key` = 'user',
    `remark` = '只读，仅首页',
    `deleted` = 0;

-- 2. 管理员账号：user_role 与 role_id 统一为 admin，清掉个人账号与头像
INSERT INTO `user` (`id`, `user_account`, `user_password`, `user_name`, `user_avatar`,
                    `user_profile`, `user_role`, `role_id`, `is_delete`)
VALUES (1, 'admin', MD5(CONCAT('12345678', 'common_template')), '管理员', NULL,
        NULL, 'admin',
        (SELECT `id` FROM `sys_role` WHERE `role_key` = 'admin' AND `deleted` = 0 LIMIT 1), 0)
ON DUPLICATE KEY UPDATE
    `user_account` = 'admin',
    `user_password` = MD5(CONCAT('12345678', 'common_template')),
    `user_name` = '管理员',
    `user_avatar` = NULL,
    `user_profile` = NULL,
    `user_role` = 'admin',
    `role_id` = (SELECT `id` FROM `sys_role` WHERE `role_key` = 'admin' AND `deleted` = 0 LIMIT 1),
    `is_delete` = 0;

-- 3. 管理员角色绑定现有全部菜单，走角色授权
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT `r`.`id`, `m`.`id`
FROM `sys_role` `r`
JOIN `sys_menu` `m`
WHERE `r`.`role_key` = 'admin';

-- 4. 删除管理员的历史直授权，统一走 sys_role_menu，避免双写
DELETE FROM `sys_user_menu`
WHERE `user_id` = (SELECT `id` FROM `user` WHERE `user_account` = 'admin' LIMIT 1);
