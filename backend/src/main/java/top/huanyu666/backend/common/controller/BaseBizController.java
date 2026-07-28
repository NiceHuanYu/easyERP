package top.huanyu666.backend.common.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.huanyu666.backend.common.model.ApiResponse;
import top.huanyu666.backend.common.model.BaseEntity;

/**
 * 基础 CRUD Controller 抽象基类 —— 提供 getById / create / update / delete 的 helper 方法。
 * <p>
 * 子类在自已的 {@code @GetMapping/@PostMapping} 方法中调用这些 helper，
 * 以便各自声明 {@code @SaCheckPermission} 注解。
 * </p>
 *
 * @param <E> 实体类型
 * @param <M> Mapper 类型
 */
public abstract class BaseBizController<E extends BaseEntity, M extends BaseMapper<E>> {

    protected abstract M getMapper();

    protected ApiResponse<E> doGetById(Long id) {
        return ApiResponse.ok(getMapper().selectById(id));
    }

    protected ApiResponse<Void> doCreate(E entity) {
        getMapper().insert(entity);
        return ApiResponse.ok();
    }

    protected ApiResponse<Void> doUpdate(Long id, E entity) {
        entity.setId(id);
        getMapper().updateById(entity);
        return ApiResponse.ok();
    }

    protected ApiResponse<Void> doDelete(Long id) {
        getMapper().deleteById(id);
        return ApiResponse.ok();
    }
}
