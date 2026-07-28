package top.huanyu666.backend.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.huanyu666.backend.modules.system.entity.SysRole;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 一次 JOIN 查询用户的所有角色编码，替代 2 次单独查询 */
    @Select("""
        SELECT DISTINCT r.code
        FROM t_sys_role r
        INNER JOIN t_sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId}
          AND r.status = 1
        """)
    List<String> selectRoleCodesByUserId(Long userId);
}
