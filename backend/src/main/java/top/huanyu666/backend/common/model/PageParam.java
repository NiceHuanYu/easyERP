package top.huanyu666.backend.common.model;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageParam {

    private long page = 1;
    private long size = 20;
}
