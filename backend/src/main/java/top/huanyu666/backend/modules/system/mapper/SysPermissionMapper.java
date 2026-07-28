package top.huanyu666.backend.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.huanyu666.backend.modules.system.entity.SysPermission;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 一次 JOIN 查询用户的所有权限码，替代 3 次单独查询 */
    @Select("""
        SELECT DISTINCT p.code
        FROM t_sys_permission p
        INNER JOIN t_sys_role_permission rp ON p.id = rp.permission_id
        INNER JOIN t_sys_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId}
          AND p.status = 1
          AND p.code IS NOT NULL
          AND p.code != ''
        """)
    List<String> selectPermissionCodesByUserId(Long userId);
}
